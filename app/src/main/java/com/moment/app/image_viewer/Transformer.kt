package com.moment.app.image_viewer

import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
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
import com.moment.app.utils.DialogUtils
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
            is ViewerPhoto.PicShape -> { //来自feed (feed支持GIF)
                Glide.with(view)
                    .setDefaultRequestOptions(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE))
                    .load(R.mipmap.user_post_test_image)
                    .dontTransform()
                    .placeholder(R.drawable.moment)
                    .thumbnail(0.3f)
                    .centerInside()
//                    .override(getScreenWidth() * 3/5, getScreenHeight() * 3/5)
//                    .error(R.drawable.moment)
//                    .into(view)
            }
            is ViewerPhoto.FileIdPhoto -> {//头像(用户详情页顶部背景或者照片墙的图，非缩略) 或者 相册的图，不支持GIF
                Glide.with(view)
                    .setDefaultRequestOptions(RequestOptions.noAnimation().diskCacheStrategy(DiskCacheStrategy.RESOURCE))
                    .load(data.fileId)
                    .dontTransform()
                    .placeholder(R.drawable.moment)
                    .thumbnail(0.3f)
                    .centerInside()
                    .override(getScreenWidth() * 3/5, getScreenHeight() * 3/5)
                    .error(R.drawable.moment)
                    .into(view)
            }
            is ViewerPhoto.UriPhoto -> { //来自相机的图
                Glide.with(view).load(data.uri).centerInside()
                    .override(ScreenUtils.getAppScreenWidth() * 2/3, ScreenUtils.getAppScreenHeight() * 2/3).into(view)
            }
        }
    }
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
        context =  DialogUtils.getActivity(context),
        dataProvider = SimpleDataProvider(clickedData, dataList), // 一次性全量加载 // 实现DataProvider接口支持分页加载
        imageLoader = SimpleImageLoader(), // 可使用demo固定写法 // 实现对数据源的加载.支持自定义加载数据类型，加载方案
        transformer = SimpleTransformer(), // 可使用demo固定写法 // 以photoId为标示，设置过渡动画的'配对'.
    )
    builder.show()
}

fun ImageFilterView.show(dataList: List<Photo>, clickedData: Photo){ //
    val builder = ImageViewerBuilder(
        context = DialogUtils.getActivity(context),
        dataProvider = SimpleDataProvider(clickedData, dataList), // 一次性全量加载 // 实现DataProvider接口支持分页加载
        imageLoader = SimpleImageLoader(), // 可使用demo固定写法 // 实现对数据源的加载.支持自定义加载数据类型，加载方案
        transformer = SimpleTransformer(), // 可使用demo固定写法 // 以photoId为标示，设置过渡动画的'配对'.
    )
    builder.show()
}

/**
 * ‌View的Context为FragmentContextWrapper的原因‌

 * ‌Context的包装‌：在Android中，Context是一个抽象类，它代表了全局的应用环境或应用组件的状态。
 * Fragment为了在其内部View中提供合适的上下文，会对Activity的Context进行包装，形成FragmentContextWrapper。
 * ‌Fragment的特性‌：Fragment作为Activity中的一部分，具有自己的生命周期和视图层级。FragmentContextWrapper
 * 允许Fragment对其内部的View进行更精细的控制，同时保持与Activity的Context的关联。
 * ‌View的Context来源‌：当View在Fragment中创建时，它的Context自然来源于Fragment，因此是FragmentContextWrapper
 * ，而非直接是Activity。
 * 综上所述，View在Fragment中的Context为FragmentContextWrapper，是由于Fragment对Activity的Context进行了包装，以适应其内部View的需求
 */