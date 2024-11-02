package com.moment.app.image_viewer

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build.VERSION
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.ThreadUtils.isMainThread
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.github.iielse.imageviewer.ImageViewerBuilder
import com.github.iielse.imageviewer.core.ImageLoader
import com.github.iielse.imageviewer.core.Photo
import com.github.iielse.imageviewer.core.SimpleDataProvider
import com.github.iielse.imageviewer.core.Transformer
import com.moment.app.R
import com.moment.app.utils.DialogFragmentManager
import com.moment.app.utils.ViewerPhoto
import com.moment.app.utils.getScreenHeight
import com.moment.app.utils.getScreenWidth

// 基本是可以作为固定写法.
class SimpleTransformer : Transformer {
    override fun getView(key: Long): ImageView? = provide(key)

    companion object {
        private val transition = HashMap<ImageView, Long>()
        fun put(photoId: Long, imageView: ImageView) {
            require(isMainThread())
            if (!imageView.isAttachedToWindow) return
            imageView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {}
                override fun onViewDetachedFromWindow(v: View) {
                    transition.remove(imageView)
                    imageView.removeOnAttachStateChangeListener(this)
                }
            })
            transition[imageView] = photoId
        }

        private fun provide(photoId: Long): ImageView? {
            transition.keys.forEach {
                if (transition[it] == photoId)
                    return it
            }
            return null
        }
    }
}

class SimpleImageLoader : ImageLoader {

    override fun load(view: ImageView, data: Photo, viewHolder: RecyclerView.ViewHolder) {
        super.load(view, data, viewHolder)
        when (data) {
            is ViewerPhoto.FeedAlbumFileIdPhoto -> {
                kotlin.runCatching {
                    //val fileFilter = FileUtils.getFileExtension(getRealPathFromURI(view.context!!, Uri.parse(data.fileId)))
                    if (data.isGif) {
                        Glide.with(view)
                            .setDefaultRequestOptions(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE))
                            .asGif()
                            .load(data.fileId)
                            .dontTransform()
                            .placeholder(R.drawable.moment)
                            .error(R.drawable.moment)
                            .into(view)
                    } else {
                        view.loadNoAnimResource(data.fileId)
                    }
                } .onFailure {
                   view.context?.let {
                       view.loadNoAnimResource(data.fileId)
                   }
                }
            }
            is ViewerPhoto.PicShape -> { //来自feed网络图 (feed支持GIF) 不裁剪
                view.loadFeedRemoteResource(data.fileKey)
            }
            is ViewerPhoto.WallOrAvatarPhoto -> {
                view.loadNoAnimResource(data.fileId)
            }
            is ViewerPhoto.UriPhoto -> { //来自相机的图
                Glide.with(view).load(data.uri).centerInside()
                    .override(ScreenUtils.getAppScreenWidth() * 2/3, ScreenUtils.getAppScreenHeight() * 2/3).into(view)
            }
        }
    }
}


fun getRealPathFromURI(context: Context?, uri: Uri): String? {
    val isKitKat = VERSION.SDK_INT >= 19
    if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
        val docId: String
        val split: Array<String>
        val type: String
        if (isExternalStorageDocument(uri)) {
            docId = DocumentsContract.getDocumentId(uri)
            split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            type = split[0]
            if ("primary".equals(type, ignoreCase = true)) {
                return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
            }
        } else {
            if (isDownloadsDocument(uri)) {
                docId = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    docId.toLong()
                )
                return getDataColumn(
                    context!!,
                    contentUri,
                    null as String?,
                    null
                )
            }

            if (isMediaDocument(uri)) {
                docId = DocumentsContract.getDocumentId(uri)
                split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                return getDataColumn(
                    context!!,
                    contentUri,
                    "_id=?",
                    selectionArgs
                )
            }
        }
    } else {
        if ("content".equals(uri.scheme, ignoreCase = true)) {
            if (isGooglePhotosUri(uri)) {
                return uri.lastPathSegment
            }

            return getDataColumn(
                context!!,
                uri,
                null as String?,
                null as Array<String>?
            )
        }

        if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
    }

    return null
}


fun ImageView.loadNoAnimResource(fileId: String?){
    Glide.with(this)
        .setDefaultRequestOptions(RequestOptions.noAnimation().diskCacheStrategy(DiskCacheStrategy.RESOURCE))
        .load(fileId)
        .dontTransform()
        .placeholder(R.drawable.moment)
        .centerInside()
        .override(getScreenWidth() * 3/5, getScreenHeight() * 3/5)
        .error(R.drawable.moment)
        .into(this)
}

fun ImageView.loadFeedRemoteResource(fileId: String?) {
    kotlin.runCatching {
        Glide.with(this)
            .setDefaultRequestOptions(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE))
            .load(R.mipmap.xog)
            .dontTransform()
            .placeholder(R.drawable.moment)
            .error(R.drawable.moment)
            .into(this)
    }
}

fun isExternalStorageDocument(uri: Uri): Boolean {
    return "com.android.externalstorage.documents" == uri.authority
}

fun isDownloadsDocument(uri: Uri): Boolean {
    return "com.android.providers.downloads.documents" == uri.authority
}

fun isMediaDocument(uri: Uri): Boolean {
    return "com.android.providers.media.documents" == uri.authority
}

fun isGooglePhotosUri(uri: Uri): Boolean {
    return "com.google.android.apps.photos.content" == uri.authority
}

fun getDataColumn(
    context: Context,
    uri: Uri?,
    selection: String?,
    selectionArgs: Array<String>?
): String? {
    var cursor: Cursor? = null
    val column = "_data"
    val projection = arrayOf("_data")

    val var8: String
    try {
        cursor = context.contentResolver.query(
            uri!!,
            projection,
            selection,
            selectionArgs,
            null as String?
        )
        if (cursor == null || !cursor.moveToFirst()) {
            return null
        }

        val index = cursor.getColumnIndexOrThrow("_data")
        var8 = cursor.getString(index)
    } finally {
        cursor?.close()
    }

    return var8
}

fun AppCompatActivity.show(dataList: List<Photo>, clickedData: Photo) { //
    val builder = ImageViewerBuilder(
        context = this,
        dataProvider = SimpleDataProvider(clickedData, dataList), // 一次性全量加载 // 实现DataProvider接口支持分页加载
        imageLoader = SimpleImageLoader(), // 可使用demo固定写法 // 实现对数据源的加载.支持自定义加载数据类型，加载方案
        transformer = SimpleTransformer(), // 可使用demo固定写法 // 以photoId为标示，设置过渡动画的'配对'.
    )
    builder.show()
}

fun Fragment.show(dataList: List<Photo>, clickedData: Photo){ //
    val builder = ImageViewerBuilder(
        context = context,
        dataProvider = SimpleDataProvider(clickedData, dataList), // 一次性全量加载 // 实现DataProvider接口支持分页加载
        imageLoader = SimpleImageLoader(), // 可使用demo固定写法 // 实现对数据源的加载.支持自定义加载数据类型，加载方案
        transformer = SimpleTransformer(), // 可使用demo固定写法 // 以photoId为标示，设置过渡动画的'配对'.
    )
    builder.show()
}

fun ImageView.show(dataList: List<Photo>, clickedData: Photo){ //
    val builder = ImageViewerBuilder(
        context =  DialogFragmentManager.getActivity(context),
        dataProvider = SimpleDataProvider(clickedData, dataList), // 一次性全量加载 // 实现DataProvider接口支持分页加载
        imageLoader = SimpleImageLoader(), // 可使用demo固定写法 // 实现对数据源的加载.支持自定义加载数据类型，加载方案
        transformer = SimpleTransformer(), // 可使用demo固定写法 // 以photoId为标示，设置过渡动画的'配对'.
    )
    builder.show()
}

fun ImageFilterView.show(dataList: List<Photo>, clickedData: Photo){ //
    val builder = ImageViewerBuilder(
        context = DialogFragmentManager.getActivity(context),
        dataProvider = SimpleDataProvider(clickedData, dataList), // 一次性全量加载 // 实现DataProvider接口支持分页加载
        imageLoader = SimpleImageLoader(), // 可使用demo固定写法 // 实现对数据源的加载.支持自定义加载数据类型，加载方案
        transformer = SimpleTransformer(), // 可使用demo固定写法 // 以photoId为标示，设置过渡动画的'配对'.
    )
    builder.show()
}

/**
 * View的Context为FragmentContextWrapper的原因‌
 * Context的包装‌：在Android中，Context是一个抽象类，它代表了全局的应用环境或应用组件的状态。
 * Fragment为了在其内部View中提供合适的上下文，会对Activity的Context进行包装，形成FragmentContextWrapper。
 * Fragment的特性‌：Fragment作为Activity中的一部分，具有自己的生命周期和视图层级。FragmentContextWrapper
 * 允许Fragment对其内部的View进行更精细的控制，同时保持与Activity的Context的关联。
 * View的Context来源‌：当View在Fragment中创建时，它的Context自然来源于Fragment，因此是FragmentContextWrapper
 * ，而非直接是Activity。
 * 综上所述，View在Fragment中的Context为FragmentContextWrapper，是由于Fragment对Activity的Context进行了包装，以适应其内部View的需求
 */