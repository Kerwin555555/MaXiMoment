package com.moment.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.BarUtils
import com.github.iielse.imageviewer.adapter.ItemType.PHOTO
import com.github.iielse.imageviewer.core.Photo
import com.moment.app.R
import com.moment.app.image_viewer.show
import com.moment.app.localimages.datamodel.AlbumItemFile
import com.moment.app.network.startCoroutine
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.TimeUnit


fun ImageFilterView.showInImageViewer(dataList: List<ViewerPhoto>, clickedData: ViewerPhoto) {
    for (i in 0 until dataList.size) {
        dataList[i].pos = i.toLong()
    }
    show(dataList, clickedData)
}

fun ImageView.showInImageViewer(dataList: List<String>, clickedData: String) {
    kotlin.runCatching {
        val list = dataList.map { it ->
            ViewerPhoto.WallOrAvatarPhoto().apply {
                fileId = it
            }
        }
        var data: ViewerPhoto.WallOrAvatarPhoto? = null
        for (i in 0 until dataList.size) {
            list[i].pos = i.toLong()
            if (list[i].fileId == clickedData) data = list[i]
        }
        show(list, data!!)
    }
}

fun ImageView.showInImageViewer(dataList: List<Uri>, clickedData: Uri) {
    kotlin.runCatching {
        val list = dataList.map { it ->
            ViewerPhoto.UriPhoto().apply {
                uri = it
            }
        }
        var data: ViewerPhoto.UriPhoto? = null
        for (i in 0 until dataList.size) {
            list[i].pos = i.toLong()
            if (list[i].uri == clickedData) data = list[i]
        }
        show(list, data!!)
    }
}

fun ImageFilterView.showInImageViewer(dataList: List<String>, clickedData: String) {
    kotlin.runCatching {
        val list = dataList.map { it ->
            ViewerPhoto.WallOrAvatarPhoto().apply {
                fileId = it
            }
        }
        var data: ViewerPhoto.WallOrAvatarPhoto? = null
        for (i in 0 until dataList.size) {
            list[i].pos = i.toLong()
            if (list[i].fileId == clickedData) data = list[i]
        }
        show(list, data!!)
    }
}

fun ImageView.showAlbumInImageViewer(dataList: List<String>, clickedData: String, item: AlbumItemFile? = null) {
    kotlin.runCatching {
        val list = dataList.map { it ->
            Log.d(MOMENT_APP, item?.mimeType ?: "notype")
            ViewerPhoto.FeedAlbumFileIdPhoto().apply {
                fileId = it
                isGif = item?.mimeType == "image/gif"
            }
        }
        var data: ViewerPhoto.FeedAlbumFileIdPhoto? = null
        for (i in 0 until dataList.size) {
            list[i].pos = i.toLong()
            if (list[i].fileId == clickedData) data = list[i]
        }
        show(list, data!!)
    }
}




sealed class ViewerPhoto : BaseBean(), Photo {
    var pos: Long = 0

    override fun id(): Long {
        return pos
    }

    override fun itemType(): Int {
        return PHOTO
    }

    //头像，头像墙(非小图) (网络图)
    class WallOrAvatarPhoto : ViewerPhoto() {
        var fileId: String? = null
    }

    //相机 (本地图)
    class UriPhoto : ViewerPhoto() {
        var uri: Uri? = null
    }

    //feed流 (网络图)
    class PicShape(var fileKey: String, var width: Int? = null, var height: Int? = null): ViewerPhoto() {

        override fun id(): Long {
            return pos
        }

        override fun itemType(): Int {
            return PHOTO
        }
    }

    //发布feed页 (本地图)
    class FeedAlbumFileIdPhoto : ViewerPhoto() {
        var fileId: String? = null
        var isGif = false
    }
}

fun View.getChatBg() {
    val gradientDrawable = GradientDrawable()
    gradientDrawable.gradientType = GradientDrawable.RADIAL_GRADIENT
    val colors = intArrayOf(
        Color.parseColor("#23ff2ec4"),  // 起始颜色
        Color.parseColor("#00f2a1ff"),  // 中间颜色
        Color.parseColor("#00ffffff") // 结束颜色
    )
    gradientDrawable.colors = colors
    val centerX = 0.8f
    val centerY = 0.4f
    gradientDrawable.setGradientCenter(centerX , centerY )

    val gradientRadius = (getScreenHeight() * 0.75).toFloat()
    gradientDrawable.gradientRadius = gradientRadius
    gradientDrawable.shape = GradientDrawable.RECTANGLE

    gradientDrawable.useLevel = false
    setBackground(gradientDrawable)
}

fun View.repeatOnLifeCycle(block : suspend CoroutineScope.() -> Unit) {
    this.findViewTreeLifecycleOwner()?.let {
        it.lifecycleScope.startCoroutine({
            it.repeatOnLifecycle(Lifecycle.State.STARTED, block)
        }){}
    }
}

fun RecyclerView.scrollToBottom() {
    kotlin.runCatching {
        smoothScrollToPosition(adapter!!.getItemCount() - 1);
    }
}


class MomentLoadingDrawable(val context: Context): Drawable() {
    val bg = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(0xffEBEBEB.toInt())
    }

    val bitmap = ContextCompat.getDrawable(context, R.mipmap.logo_color)

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
        bg.setBounds(left, top, right, bottom)
    }

    override fun draw(canvas: Canvas) {
        kotlin.runCatching {
            bg.draw(canvas)
            val w = bounds.width()
            val h = bounds.height()
            val top = (256.dp + BarUtils.getStatusBarHeight())/2 - bitmap!!.intrinsicHeight/2
            val targetRect = Rect((w - bitmap!!.intrinsicWidth)/2, top,
                (w + bitmap.intrinsicWidth)/2, top + bitmap.intrinsicHeight)
            canvas.drawBitmap((bitmap as BitmapDrawable).bitmap, null, targetRect, null);
        }
    }

    override fun setAlpha(alpha: Int) {}

    override fun setColorFilter(colorFilter: ColorFilter?) {}

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }
}

fun Bitmap?.isOk() : Boolean{
    return this != null && this.height >0 && this.width > 0
}

inline fun View.clicks(skipDuration: Long = 500, crossinline block: (View) -> Unit) {
    val throttle = MomentThrottle(skipDuration, TimeUnit.MILLISECONDS)
    setOnClickListener {
        if (throttle.needSkip()) return@setOnClickListener
        block(it)
    }
}

inline fun View.clicksPro(skipDuration: Long = 1000, crossinline block: (View) -> Unit) {
    val throttle = MomentThrottle(skipDuration, TimeUnit.MILLISECONDS)
    setOnClickListener {
        if (throttle.needSkip()) return@setOnClickListener
        block(it)
    }
}

class MomentThrottle(
    skipDuration: Long,
    timeUnit: TimeUnit
) {
    private val delayMilliseconds: Long
    private var oldTime = 0L

    init {
        if (skipDuration < 0) {
            delayMilliseconds = 0
        } else {
            delayMilliseconds = timeUnit.toMillis(skipDuration)
        }
    }

    fun needSkip(): Boolean {
        val nowTime = SystemClock.elapsedRealtime()
        val intervalTime = nowTime - oldTime
        if (oldTime == 0L || intervalTime >= delayMilliseconds) {
            oldTime = nowTime
            return false
        }

        return true
    }
}

/**
 * 可定制是否定制数据倒灌的LiveData
 * @param <T>
</T> */
class MomentLiveData<T> : MutableLiveData<T> {
    private var newValueTime = 0L

    /**
     * 是否接受数据倒灌，默认不支持，而MutableLiveData天生支持。
     */
    private var acceptInversion = false

    constructor()

    /**
     *
     * @param acceptInversion 是否接受数据倒灌
     * @param holder 占位，无意义。用于区别开androidx.lifecycle.MutableLiveData#MutableLiveData(java.lang.Object)
     */
    constructor(acceptInversion: Boolean,  holder: Any?) {
        this.acceptInversion = acceptInversion
    }

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        val filterOb: MomentLiveDataObserver<in T> = object : MomentLiveDataObserver<T>() {
            override fun onChanged(t: T) {
                if (acceptInversion || (newValueTime >= registerTime)) {
                    observer.onChanged(t)
                } else {
                    //CommonUtilsKt.printW("UULiveData", "Inversion Data: $t")
                }
            }
        }
        super.observe(owner, filterOb)
    }

    override fun observeForever(observer: Observer<in T>) {
        val filterOb: MomentLiveDataObserver<in T> = object : MomentLiveDataObserver<T>() {
            override fun onChanged(t: T) {
                if (acceptInversion || (newValueTime >= registerTime)) {
                    observer.onChanged(t)
                } else {
                   // CommonUtilsKt.printW("UULiveData", "observeForever Inversion Data: $t")
                }
            }
        }
        super.observeForever(filterOb)
    }

    override fun setValue(value: T) {
        newValueTime = System.currentTimeMillis()
        super.setValue(value)
    }
}
abstract class  MomentLiveDataObserver<T>: Observer<T> {
    var registerTime = System.currentTimeMillis()
}
