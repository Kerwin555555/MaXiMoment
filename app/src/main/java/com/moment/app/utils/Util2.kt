package com.moment.app.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.BaseColumns
import android.provider.MediaStore
import android.util.LayoutDirection.RTL
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.core.content.ContextCompat
import androidx.core.database.getStringOrNull
import androidx.core.os.bundleOf
import androidx.core.provider.FontRequest
import androidx.emoji2.text.EmojiCompat
import androidx.emoji2.text.FontRequestEmojiCompatConfig
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import com.blankj.utilcode.util.IntentUtils
import com.blankj.utilcode.util.LanguageUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ScreenUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.didi.drouter.api.DRouter
import com.google.common.collect.ImmutableList
import com.gyf.immersionbar.ImmersionBar
import com.moment.app.MomentApp
import com.moment.app.R
import com.moment.app.datamodel.UserInfo
import com.moment.app.localimages.AlbumSearcher
import com.moment.app.localimages.datamodel.Album
import com.moment.app.localimages.logic.loaders.PhotoGalleryLoader
import com.moment.app.localimages.album.IAlbumInterface.Companion.LOAD_ID_IMAGE
import com.moment.app.main_profile.entities.PostBean
import com.moment.app.models.UserLoginManager
import com.moment.app.user_rights.UserPermissionManager
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


// TextView 设置textColor扩展函数
internal fun TextView.applyEnabledColorStateList(@ColorRes enableId: Int,
                                                 @ColorRes disableId: Int) {
    val status: Array<IntArray> =
        arrayOf<IntArray>(intArrayOf(android.R.attr.state_enabled), intArrayOf())
    val colors: IntArray = intArrayOf(
        ContextCompat.getColor(this.context, enableId),
        ContextCompat.getColor(context, disableId))
    setTextColor(ColorStateList(status, colors))
}

internal fun TextView.applyEnabledColorIntStateList(@ColorInt enableId: Int,
                                                    @ColorInt disableId: Int) {
    val status: Array<IntArray> =
        arrayOf<IntArray>(intArrayOf(android.R.attr.state_enabled), intArrayOf())
    val colors: IntArray = intArrayOf(enableId, disableId)
    setTextColor(ColorStateList(status, colors))
}


internal fun TextView.applySelectedColorStateList(@ColorRes selectedId: Int,
                                                  @ColorRes unSelectedId: Int) {
    val status: Array<IntArray> =
        arrayOf<IntArray>(intArrayOf(android.R.attr.state_selected), intArrayOf())
    val colors: IntArray = intArrayOf(
        ContextCompat.getColor(this.context, selectedId),
        ContextCompat.getColor(context, unSelectedId))
    setTextColor(ColorStateList(status, colors))
}

internal fun TextView.applySelectedColorIntStateList(@ColorInt selectedId: Int,
                                                     @ColorInt unSelectedId: Int) {
    val status: Array<IntArray> =
        arrayOf<IntArray>(intArrayOf(android.R.attr.state_selected), intArrayOf())
    val colors: IntArray = intArrayOf(
        selectedId,
        unSelectedId)
    setTextColor(ColorStateList(status, colors))
}

fun FragmentManager.popBackStackNowAllowingStateLoss() {
    try {
        popBackStackImmediate()
    } catch (e: Exception) {}
}

fun AppCompatActivity.immersion() {
    ImmersionBar.with(this)
    .statusBarDarkFont(false)
    .fitsSystemWindows(false)
    .init()
}

/**
 * 防止多次点击
 */
inline fun View.setOnAvoidMultipleClicksListener(crossinline onClick: (view: View) -> Unit, delayMillis: Long) {
    this.setOnClickListener {
        if (this.isClickable) {
            this.isClickable = false
            onClick(it)
            this.postDelayed({
                this.isClickable = true
            }, delayMillis)
        }
    }
}

internal fun View.isRTL() : Boolean  {
    return this.layoutDirection == RTL
}

internal fun View.resetGravity(g: Int) {
    if (this.layoutParams is FrameLayout.LayoutParams) {
        (this.layoutParams as FrameLayout.LayoutParams).gravity = g
    }
}

fun AppCompatImageView.loadImage(file: String?, sizeW: Int, sizeH: Int) {
    Glide.with(this)
        .setDefaultRequestOptions(RequestOptions.noAnimation().diskCacheStrategy(DiskCacheStrategy.RESOURCE))
        .load(R.mipmap.local_avatar)
        .override(sizeW, sizeH)
        .timeout(3000)
        .addListener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>,
                isFirstResource: Boolean
            ): Boolean {
                //dirtyFileAction?.invoke(file)
                return false
            }

            override fun onResourceReady(
                resource: Drawable,
                model: Any,
                target: Target<Drawable>?,
                dataSource: DataSource,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }

        })
        .into(this)
}


const val MILLION = 1000000L

const val BILLION = 1000000000L

const val THOUSAND = 1000L
fun formatScore(calculator_score: Long): String {
    if (calculator_score >= MILLION) {
        val sb = StringBuilder()
        val g = calculator_score.toString().toCharArray()
        val length = g.size
        var needAddOne = false
        if (g[length - 4] >= '5') {
            needAddOne = true
        }
        if (needAddOne) {
            var plusone = 1
            for (i in length - 5 downTo 0) {
                g[i] = (g[i].code + plusone).toChar()
                if (g[i] > '9') {
                    g[i] = '0'
                    plusone = 1
                } else {
                    plusone = 0
                    break
                }
            }
            if (plusone == 1) {
                sb.append('1')
            }
            for (i in 0..length - 7) {
                sb.append(g[i])
            }
            sb.append('.')
            sb.append(g[length - 6])
            sb.append(g[length - 5])
            sb.append('M')
            return sb.toString()
        } else {
            for (i in 0..length - 7) {
                sb.append(g[i])
            }
            sb.append(".")
            sb.append(g[length - 6])
            sb.append(g[length - 5])
            sb.append('M')
            return sb.toString()
        }
    } else if (calculator_score >= THOUSAND) {
        val sb = StringBuilder()
        val g = calculator_score.toString().toCharArray()
        val length = g.size
        var needAddOne = false
        if (g[length - 1] >= '5') {
            needAddOne = true
        }
        if (needAddOne) {
            var plusone = 1
            for (i in length - 2 downTo 0) {
                g[i] = (g[i].code + plusone).toChar()
                if (g[i] > '9') {
                    g[i] = '0'
                    plusone = 1
                } else {
                    plusone = 0
                    break
                }
            }
            if (plusone == 1) {
                sb.append('1')
            }
            for (i in 0..length - 4) {
                sb.append(g[i])
            }
            sb.append('.')
            sb.append(g[length - 3])
            sb.append(g[length - 2])
            sb.append('K')
            return sb.toString()
        } else {
            for (i in 0..length - 4) {
                sb.append(g[i])
            }
            sb.append('.')
            sb.append(g[length - 3])
            sb.append(g[length - 2])
            sb.append('K')
            return sb.toString()
        }
    } else {
        return calculator_score.toString()
    }
}

fun saveView(context: Context, bitmap: Bitmap): File? {
    val fileName = "moment_" + System.currentTimeMillis() + ".jpg"
    val pictureFile = File(context.cacheDir, fileName)

    try {
        val fos = FileOutputStream(pictureFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
        fos.close()
        return pictureFile
    } catch (e: FileNotFoundException) {
        LogUtils.d("ClipImage", "File not found: " + e.message)
        return null
    } catch (e: IOException) {
        LogUtils.d("ClipImage", "Error accessing file: " + e.message)
        return null
    } finally {
    }
}

fun AppCompatActivity.rightInRightOut() : FragmentTransaction{
    return this.supportFragmentManager
        .beginTransaction()
        .setCustomAnimations(
            R.anim.f_slide_in_right, // 进入动画
            0, // 退出动画（这里没有设置，所以为0）, // 退出动画（这里没有设置，所以为0）
            0, // 弹出动画（这里没有设置，所以为0）
            R.anim.f_slide_out_right ,  // 弹入动画（这里没有设置，所以为0）
        )
}

fun AppCompatActivity.bottomInBottomOut() : FragmentTransaction {
    return this.supportFragmentManager
        .beginTransaction()
        .setCustomAnimations(
            R.anim.slide_up, // 进入动画
            0, // 退出动画（这里没有设置，所以为0）, // 退出动画（这里没有设置，所以为0）
            0, // 弹出动画（这里没有设置，所以为0）
            R.anim.slide_down ,  // 弹入动画（这里没有设置，所以为0）
        )
}

fun AppCompatActivity.cleanSaveFragments() {
    kotlin.runCatching {
        val transaction = supportFragmentManager.beginTransaction()
        for (f in supportFragmentManager.fragments) {
            transaction.remove(f)
        }
        transaction.commitNow()
    }
}

fun AppCompatActivity.stackAnimation() : FragmentTransaction{
    return supportFragmentManager
        .beginTransaction()
        .setCustomAnimations(
            R.anim.f_slide_in_right,    // clip enter from right 1
            R.anim.f_slide_out_left,    // album out to left 1
            R.anim.f_slide_in_left,    // album back from left 2
            R.anim.f_slide_out_right   // clip out to right 2
        )
}

fun Fragment.copyFragmentArgumentsToMap() : MutableMap<String, Any?>{
    val map = mutableMapOf<String, Any?>()
    arguments?.let {
        for (key in it.keySet()) {
            val value = it.get(key)
            map[key] = value
        }
    }
    return map
}

val LOCALE_IMMUTABLE_SET: ImmutableList<Locale> = ImmutableList.Builder<Locale>().add(
    Locale("en"),
    Locale("th"),
    Locale("vi"),
    Locale("in"),
    Locale("ms"),
    Locale("es"),
    Locale("pt"),
    Locale("tr"),
    Locale("ru"),
    Locale("ar"),
    Locale("ja"),
    Locale("zh")
).build()

fun getSelectedLoc(): Locale {
    var locale = LanguageUtils.getAppliedLanguage()
    if (locale == null) {
        locale = LanguageUtils.getSystemLanguage()
    }
    if (locale == null) return Locale.ENGLISH
    for (loc in LOCALE_IMMUTABLE_SET) {
        if (loc.language == locale.language) {
            return locale
        }
    }
    return Locale.ENGLISH
}

fun getScreenWidth() : Int{
    val w = ScreenUtils.getAppScreenWidth()
    if (w > 0) {
        return w
    }
    return MomentApp.appContext.resources.displayMetrics.widthPixels
}
fun getScreenHeight() : Int{
    val h = ScreenUtils.getAppScreenHeight()
    if (h > 0) {
        return h
    }
    return MomentApp.appContext.resources.displayMetrics.heightPixels
}


internal fun Fragment.loadAvatarBig(view: ImageView, userInfo: UserInfo) {
    view.loadUserBackgroundOrAlbum(fileId = userInfo.avatar)
    view.setOnClickListener {
        kotlin.runCatching {
            UserLoginManager.getUserInfo()!!.avatar?.let {
                view.showInImageViewer(
                    mutableListOf(UserLoginManager.getUserInfo()!!.avatar!!),
                    UserLoginManager.getUserInfo()!!.avatar!!
                )
            }
        }
    }
}

internal fun AppCompatActivity.loadAvatarBig(view: ImageView, userInfo: UserInfo) {
    view.loadUserBackgroundOrAlbum(fileId = userInfo.avatar)
    view.setOnClickListener {
        kotlin.runCatching {
            UserLoginManager.getUserInfo()!!.avatar?.let {
                view.showInImageViewer(
                    mutableListOf(UserLoginManager.getUserInfo()!!.avatar!!),
                    UserLoginManager.getUserInfo()!!.avatar!!
                )
            }
        }
    }
}

fun ImageFilterView.loadUserBackgroundOrAlbum(fileId: String?) {
    kotlin.runCatching {
       Glide.with(this).load(fileId)
          .placeholder(R.drawable.bg_avatar_big)
          .error(R.drawable.bg_avatar_big)
          .centerInside().override(ScreenUtils.getAppScreenWidth()*3/5,
            ScreenUtils.getAppScreenHeight()*3/5).into(this)
    }
}

fun ImageView.loadUserBackgroundOrAlbum(fileId: String?) {
    kotlin.runCatching {
        Glide.with(this).load(fileId)
            .placeholder(R.drawable.bg_avatar_big)
            .error(R.drawable.bg_avatar_big)
            .centerInside().override(ScreenUtils.getAppScreenWidth()*3/5,
                ScreenUtils.getAppScreenHeight()*3/5).into(this)
    }
}




val MOMENT_APP = "zhouzheng"

//   /data/user/0/com.moment.app/cache/moment_1730249503340.jpg


fun View.gotoPostDetail(post: PostBean) {
    post.user_info?.let {
        DRouter.build("/feed/detail")
            .putExtra("post", post)
            .start()
    } ?: let {
        if (post.isMe) {
            DRouter.build("/feed/detail")
                .putExtra("post", post.apply {
                    user_info = UserLoginManager.getUserInfo()
                })
                .start()
        } else {
            // avoid !!
        }
    }
}

fun formatChatTime(t: Long) : String{

    // 获取当前时间
    val currentCalendar: Calendar = Calendar.getInstance()
    currentCalendar.setTimeInMillis(System.currentTimeMillis())


    // 将时间戳 t 转换为 Date 对象
    val date: Date = Date(t)
    val calendar: Calendar = Calendar.getInstance()
    calendar.setTime(date)

    // 检查时间戳是否在 24 小时内
    val timeDiff: Long = currentCalendar.getTimeInMillis() - calendar.getTimeInMillis()
    if (timeDiff < 24 * 60 * 60 * 1000) {
        // 使用 "hh:mm" 格式化
        val formatter: SimpleDateFormat = SimpleDateFormat("hh:mm")
        return formatter.format(date)
    } else {
        // 检查月份差异是否小于 12
        var monthDifference: Int =
            Math.abs(currentCalendar.get(Calendar.MONTH) - calendar.get(Calendar.MONTH))

        // 考虑年份差异对月份差异的影响
        val yearDifference: Int = currentCalendar.get(Calendar.YEAR) - calendar.get(Calendar.YEAR)
        if (yearDifference != 0) {
            monthDifference += yearDifference * 12
        }

        if (monthDifference < 12) {
            val formatter: SimpleDateFormat = SimpleDateFormat("MM-dd hh:mm")
            return formatter.format(date)
        } else {
            val formatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm")
            return  formatter.format(date)
        }
    }
}

fun ImageView.displayImage(fileId: String) {
    kotlin.runCatching {
        Glide.with(context).load(fileId).into(this)
    }
}

fun getKeyboardHeight(): Int {
    val k = MMKV.defaultMMKV().getInt("keyboard_height", -1)
    return k
}

fun saveKeyboardHeight(h : Int) {
    val k = MMKV.defaultMMKV().putInt("keyboard_height",h)
}

fun Application.emoji() {
    val fontRequest: FontRequest = androidx.core.provider.FontRequest(
        "com.google.android.gms.fonts",
        "com.google.android.gms",
        "Noto Color Emoji Compat",
        R.array.com_google_android_gms_fonts_certs
    )

    //                CERTIFICATES);
    val config: EmojiCompat.Config = FontRequestEmojiCompatConfig(this, fontRequest)

    //        EmojiCompat.Config config = new BundledEmojiCompatConfig(this);
    config.setReplaceAll(true)
    config.registerInitCallback(object : EmojiCompat.InitCallback() {
        override fun onInitialized() {
            LogUtils.e(
                MOMENT_APP,
                "loadEmojiFontFromNetwork()->onInitialized()"
            )
        }

        override fun onFailed(throwable: Throwable?) {
            LogUtils.e(
                MOMENT_APP,
                "loadEmojiFontFromNetwork()->onFailed():" + throwable!!.message
            )
        }
    })
    EmojiCompat.init(config)
}

//相册权限检查
fun Context.checkReadWritePermission(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
    } else {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
}

//相机权限检查
fun Context.checkCameraPermission(): Boolean {
    return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            && this.checkReadWritePermission()
}


fun AppCompatActivity.checkAndGotoCamera(request_code_take_photo: Int, photoPath: ((uri: Uri?) -> Unit)) {
    try {
        UserPermissionManager.check(
            this, "Take Photos",
            arrayOf<String>(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            ,object : UserPermissionManager.Callback {
                override fun result(res: Int) {
                    if (res == 0) {
                        val mCurrentPhotoPath = createImageUri()
                        photoPath.invoke(mCurrentPhotoPath)
                        val captureIntent =
                            IntentUtils.getCaptureIntent(mCurrentPhotoPath)
                        try {
                            startActivityForResult(
                                captureIntent,
                                request_code_take_photo
                            )
                        } catch (e: java.lang.Exception) {
                            LogUtils.d("capture", e)
                        }
                    }
                }
            })
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        "requestPermissions error".toast()
    }
}


fun Context.createImageUri(): Uri? {
    val status = Environment.getExternalStorageState()
    // 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
    return if (status == Environment.MEDIA_MOUNTED) {
        contentResolver
            .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues())
    } else {
        contentResolver
            .insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, ContentValues())
    }
}

fun AppCompatActivity.checkAndSelectPhotos(onPermissionOk: (() -> Unit)? = null) {
    try {
        UserPermissionManager.check(
            this, "Choose from library",
            arrayOf<String>(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            , object : UserPermissionManager.Callback {
                override fun result(res: Int) {
                    if (res == 0) {
                       onPermissionOk?.invoke()
                    }
                }
            })
    } catch (e: Exception) {
        "requestPermissions error".toast()
    }
}

/**
 * 已经有了权限，读取所有相机图片
 */
fun AppCompatActivity.fetchAllAlbumImages(func: ((album: Album)-> Unit)?) {
        LoaderManager.getInstance(this)
            .initLoader(LOAD_ID_IMAGE, bundleOf("extra_mode" to AlbumSearcher.MODE_ONLY_IMAGE), object : LoaderManager.LoaderCallbacks<Cursor> {
                override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
                    return PhotoGalleryLoader(this@fetchAllAlbumImages) //加载图片的cursor
                }

                override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
                    data?.let {
                        val album = getAllMediaDirFromLoadCursor(data)
                        coroutineScope.launch (Dispatchers.Main){
                            func?.invoke(album)
                        }
                    }
                }

                override fun onLoaderReset(loader: Loader<Cursor>) {

                }

            })

}

@SuppressLint("Range")
private fun AppCompatActivity.getAllMediaDirFromLoadCursor(data: Cursor): Album {

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

                //这里的 size和width还有height都可能为0
                var size = data.getLong(data.getColumnIndex(MediaStore.Files.FileColumns.SIZE))
                var width = data.getInt(data.getColumnIndex(MediaStore.MediaColumns.WIDTH))
                var height = data.getInt(data.getColumnIndex(MediaStore.MediaColumns.HEIGHT))

                if (size == 0L || width == 0||  height == 0 || path.isEmpty()) continue
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

                if (!isExits || shouldPrevent10MbBigGif(mimeType, size)){
                    Log.d("@@@Cursor","filter ==> path: $path , mimeType: $mimeType , size: $size , width: $width , height: $height")
                }else{
                    all.addPhoto(imageId, path, lowPath, mimeType, size, width, height, date)
                }


            }

            if (all.photoPaths.isNotEmpty()) {
                all.coverPath = all.photoPaths[0]
            }
            return all
        }catch (e:Exception){
            e.printStackTrace()
            AlbumSearcher.getDataTrack()?.onError("parseImagesFromCursor",e)
            return Album()
        }
}

private fun AppCompatActivity.shouldPrevent10MbBigGif(mimeType: String, size: Long): Boolean {
    if (mimeType == "image/gif") {
        return size > (10*1024*1024)
    }
    return false
}


