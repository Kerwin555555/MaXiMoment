package com.moment.app.localimages.logic

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.provider.BaseColumns
import android.provider.MediaStore
import android.util.Log
import androidx.core.database.getStringOrNull
import com.moment.app.localimages.AlbumSearcher
import com.moment.app.localimages.datamodel.Album
import com.moment.app.utils.MOMENT_APP
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


object CursorParseHelper {

    private const val INDEX_ALL_PHOTOS = 0

    @SuppressLint("Range")
    fun parseImagesFromCursor(context:Context, data: Cursor): MutableList<Album> {
        val directories: MutableList<Album> = mutableListOf()

        val map: LinkedHashMap<String, Album> = linkedMapOf()

        val all = Album()
        all.name = "All Media"
        all.id = "ALL"
        try{
            while (data.moveToNext()) {

                val imageId = data.getLong(data.getColumnIndex(BaseColumns._ID))
                val bucketId =
                    data.getStringOrNull(data.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_ID))?:"Unknown"
                val name =
                    data.getString(data.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME))


                val lowPath = data.getString(data.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
                val path = MediaStore.Images.Media
                    .EXTERNAL_CONTENT_URI
                    .buildUpon()
                    .appendPath(imageId.toString()).build().toString()

                //media store查询数据size/width/height都可能为0
                var size = data.getLong(data.getColumnIndex(MediaStore.Files.FileColumns.SIZE))
                var width = data.getInt(data.getColumnIndex(MediaStore.MediaColumns.WIDTH))
                var height = data.getInt(data.getColumnIndex(MediaStore.MediaColumns.HEIGHT))

                val date =
                    data.getLong(data.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED))

                val isExits =  if (Build.VERSION.SDK_INT >= 29){ //android q有saf机制执行效率慢，直接返回true
                   true
                }else{
                    File(lowPath).exists()
                }

                if (Build.VERSION.SDK_INT < 29 && size == 0L){
                    size = File(lowPath).length()
                }

                //mimeType在手机被系统或第三方清理软件清理后，media store会丢失字段
                val mimeType =  data.getStringOrNull(data.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE))?:AlbumSearcher.IMAGE_JPG

                val filter = AlbumSearcher.getRequest()?.getFileFilter()?.filter(path, mimeType, size, width, height) ?: false

                if (!isExits || filter){
                    Log.d("@@@Cursor","filter ==> path: $path , mimeType: $mimeType , size: $size , width: $width , height: $height")
                }else{
                    all.addPhoto(imageId, path, lowPath, mimeType, size, width, height, date)

                    if (map[bucketId] == null) {
                        val imageDirectory = Album()
                        imageDirectory.id = bucketId
                        imageDirectory.name = name
                        imageDirectory.coverPath = path
                        imageDirectory.addPhoto(imageId, path, lowPath, mimeType, size, width, height, date)
                        imageDirectory.dateAdded =
                            data.getLong(data.getColumnIndex(MediaStore.MediaColumns.DATE_ADDED))
                        map[bucketId] = imageDirectory
                    } else {
                        map[bucketId]?.addPhoto(imageId, path, lowPath, mimeType, size, width, height, date)
                    }
                }


            }

            if (all.photoPaths.isNotEmpty()) {
                all.coverPath = all.photoPaths[0]
            }
            directories.add(INDEX_ALL_PHOTOS, all)
            val iterator = map.iterator()
            while (iterator.hasNext()) {
                val entries = iterator.next()
                if (entries.value.files.size > 0) {
                    directories.add(entries.value)
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
            AlbumSearcher.getDataTrack()?.onError("parseImagesFromCursor",e)
        }
        AlbumSearcher.getDataTrack()?.onData("parseImagesFromCursor", directories)
        return directories
    }

    fun parseVideosFromCursor(context: Context, data: Cursor): Album {
        val videoDir = Album()
        videoDir.name = "All Video"
        videoDir.id = "ALL VIDEO"

        try {
            while (data.moveToNext()) {
                val filedId = data.getLong(data.getColumnIndexOrThrow(BaseColumns._ID))
                val album = data.getString(data.getColumnIndexOrThrow(MediaStore.Video.Media.ALBUM))
                val duration = data.getLong(data.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION))

                val mimeType =
                    data.getString(data.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE))

                val date =
                    data.getLong(data.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED))

                val path = MediaStore.Video.Media
                    .EXTERNAL_CONTENT_URI
                    .buildUpon()
                    .appendPath(filedId.toString()).build().toString()

                val thumbnail = MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI.buildUpon()
                    .appendPath(filedId.toString()).build().toString()

                val size = data.getLong(data.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE))
                val width = data.getInt(data.getColumnIndexOrThrow(MediaStore.MediaColumns.WIDTH))
                val height = data.getInt(data.getColumnIndexOrThrow(MediaStore.MediaColumns.HEIGHT))
                if (size == 0L || width == 0||  height == 0) continue
                videoDir.addVideo(filedId, path, thumbnail, mimeType, size, width, height, date, duration)

                Log.e("FileData", "path: $path , mimeType: $mimeType , thumbnail: $thumbnail , duration: $duration , size: $size")
            }
        }catch (e: java.lang.Exception){
            e.printStackTrace()
            AlbumSearcher.getDataTrack()?.onError("parseVideosFromCursor",e)
        }

        AlbumSearcher.getDataTrack()?.onData("parseVideosFromCursor", mutableListOf(videoDir))
        return videoDir
    }


    private fun formatPhotoDate(timeMillis: Long): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm").format(Date(timeMillis))
    }


//    private fun isAndroidQFileExists(context: Context?, path: String): Boolean {
//        if (context == null) {
//            return false
//        }
//        var afd: AssetFileDescriptor? = null
//        val cr: ContentResolver = context.contentResolver
//        try {
//            val uri: Uri = Uri.parse(path)
//            afd = cr.openAssetFileDescriptor(uri, "r")
//            if (afd == null) {
//                return false
//            } else {
//                afd?.close()
//            }
//        } catch (e: FileNotFoundException) {
//            return false
//        } finally {
//            afd?.close()
//        }
//        return true
//    }

}