package com.moment.app.utils

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapFactory.Options
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import com.alibaba.sdk.android.oss.model.PutObjectRequest
import com.blankj.utilcode.util.LogUtils
import com.moment.app.MomentApp
import com.moment.app.utils.compress.SuperCompressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.ceil

class ImageUploader(val ossDelegate: MomentOSSDelegate) {
    private var delayHandler = Handler(Looper.getMainLooper())
    private var ossFailCount: Int = 0
    private fun useOss() : Boolean {
        return true
    }

    suspend fun uploadImage(path: String, onlyRunmomentServer: Boolean): Rzt{
        ossDelegate.prepare()
       return withContext(Dispatchers.IO) {
            val file = compressImage(path)
            if (file == null || !file.exists()) {
                "file not exist".toast()
                return@withContext Rzt(null, -1, "")
            }
            val option = fetchOption(file.readBytes())
            val rzt = doUploadImage(path, onlyRunmomentServer, file.readBytes())
            rzt.apply {
                this.option = option
            }
            return@withContext rzt
        }
    }

    data class Rzt(
        var fileId: String? = "",
        var code: Int,
        var msg: String? = "",
        var option: Options? = null
    ): BaseBean()

    private suspend fun doUploadImage(path: String, momentOnly: Boolean, byteArray: ByteArray): Rzt {
        val startTime = System.currentTimeMillis()
        if (useOss() && !momentOnly) {
            val key = ossDelegate.getFilePrefix() + CommonUtil.md5(path) + "_" + System.currentTimeMillis()
            kotlin.runCatching {
                doOSSUpload(key, byteArray)
                LogUtils.d(TAG, "success upload image: $path")
                return Rzt(key, 0, "")
            }.onFailure {
                ossFailCount ++
                LogUtils.d(TAG, "fail upload image: $path, error: ${it.message}")
               // track("image", if (it is ServiceException) (it.errorCode ?: "-1") else "-1", byteArray.size, System.currentTimeMillis() - startTime, "oss")
                doUploadImage(path, true, byteArray)
            }
        } else {
//            kotlin.runCatching {
//                val requestFile: RequestBody = byteArray.toRequestBody("multipart/form-data".toMediaType())
//                val body = MultipartBody.Part.createFormData("image", path, requestFile)
//                val fileId = ApiService.getService(UploadApi::class.java).uploadPic(body).compose().fileid
//                LogUtils.d(TAG, "success upload image: $path")
//                track("image", "0", byteArray.size, System.currentTimeMillis() - startTime, "lit")
//                return Rzt(fileId, 0, "")
//            }.onFailure {
//                LogUtils.d(TAG, "fail upload image: $path, error: ${it.message}")
//                track("image", "${it.format().first}", byteArray.size, System.currentTimeMillis() - startTime, "lit")
//                return Rzt("", it.format().first, it.message)
//            }
            return Rzt("", 0, "")
        }
        return Rzt("", -1, "")
    }

    /**
     * 获取bitmap option
     */
    private fun fetchOption(bytes: ByteArray): BitmapFactory.Options {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        return options
    }

     val TAG = "FilesUploader"

    fun compressImage(path: String): File? {
        LogUtils.d(TAG, "start compress image: $path")
        if (path.startsWith("content://")) {
            val pathMd5 = CommonUtil.md5(path) + System.currentTimeMillis()
            var folder = MomentApp.appContext.getExternalFilesDir("compress")?.absolutePath
            if (folder == null) {
                folder = MomentApp.appContext.filesDir.absolutePath + File.separator + "compress"
            }
            val compressFolder = File(folder)
            if (!compressFolder.exists()) {
                compressFolder.mkdirs()
            }
            val compressFile = File(compressFolder, pathMd5)
            val uri = Uri.parse(path)
            val cr = MomentApp.appContext.contentResolver
            var source = MediaStore.Images.Media.getBitmap(cr, uri)
            if (!source.isOk()) {
                "Bitmap is null".toast()
                return null
            } else {
                val inSampleSize: Int = computeSize(source!!.width, source.height)
                val degree = fetchDegree(cr, uri)
                if (degree > 0) {
                    LogUtils.d(TAG, "start rotate source: $degree")
                    source = rotate(source, degree, source.width / 2f, source.height / 2f)!!
                }
                val bytes = compressByInSampleSize(source, inSampleSize, 60)
                compressFile.writeBytes(bytes)
                return compressFile
            }
        } else {
            val uri = if (path.startsWith("file")) Uri.parse(path) else Uri.fromFile(File(path))
            val files = SuperCompressor.with(MomentApp.appContext).load(uri).setFocusAlpha(true).get()
            return if (files.isNullOrEmpty()) {
                File(path)
            } else {
                files[0]
            }
        }
    }

    private fun compressByInSampleSize(
        source: Bitmap,
        sampleSize: Int,
        imageQuality: Int,
    ): ByteArray {
        val options = BitmapFactory.Options()
        options.inSampleSize = sampleSize
        val baos = ByteArrayOutputStream()
        source.compress(Bitmap.CompressFormat.JPEG, imageQuality, baos)
        val bytes = baos.toByteArray()
        if (!source.isRecycled) source.recycle()
        return bytes
    }

    private fun doOSSUpload(key: String, byteArray: ByteArray) {
        val put = PutObjectRequest("moment-test", key, byteArray)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            delayHandler.postDelayed({
                LogUtils.d(TAG, "ali upload timeOut")
            }, key, 60 * 1000)
        }
        val result = ossDelegate.getOSS()?.putObject(put)
        delayHandler.removeCallbacksAndMessages(key)
    }


    private fun rotate(
        src: Bitmap,
        degrees: Int,
        px: Float,
        py: Float,
    ): Bitmap? {
        if (degrees == 0) return src
        val matrix = Matrix()
        matrix.setRotate(degrees.toFloat(), px, py)
        val ret = Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
        if (!src.isRecycled && ret != src) src.recycle()
        return ret
    }

    private fun fetchDegree(cr: ContentResolver, uri: Uri): Int {
        val orientation = cr.openFileDescriptor(uri, "r")?.use { fd ->
            ExifInterface(fd.fileDescriptor).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL)
        }

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }

    private fun computeSize(width: Int, height: Int): Int {
        val srcWidth = if (width % 2 == 1) width + 1 else width
        val srcHeight = if (height % 2 == 1) height + 1 else height
        val longSide: Int = srcWidth.coerceAtLeast(srcHeight) //Math.max(srcWidth,srcHeight)
        val shortSide: Int = srcWidth.coerceAtMost(srcHeight) //Math.min(srcWidth,srcHeight)
        val scale = shortSide.toFloat() / longSide
        return if (scale <= 1 && scale > 0.5625) {
            if (longSide < 1664) {
                1
            } else if (longSide < 4990) {
                2
            } else if (longSide in 4991..10239) {
                4
            } else {
                if (longSide / 1280 == 0) 1 else longSide / 1280
            }
        } else if (scale <= 0.5625 && scale > 0.5) {
            if (longSide / 1280 == 0) 1 else longSide / 1280
        } else {
            ceil(longSide / (1280.0 / scale)).toInt() //Math.ceil(longSide / (1280.0 / scale)).toInt()
        }
    }
}