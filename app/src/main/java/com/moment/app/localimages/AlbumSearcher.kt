package com.moment.app.localimages

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.annotation.Keep
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.moment.app.localimages.datamodel.Album
import com.moment.app.localimages.datamodel.AlbumItemFile
import com.moment.app.localimages.logic.MediaStoreHelper
import com.moment.app.localimages.album.AlbumSearchActivity
import com.moment.app.localimages.album.AlbumChooseFragment
import com.moment.app.localimages.album.AlbumSkimActivity
import org.jetbrains.annotations.NotNull

object AlbumSearcher {

    const val EXPLORER_MEDIA_SELECT = 500  //兼容老格式
    const val EXPLORER_MEDIA_PREVIEW = 501

    const val EXTRA_RESULT_SELECTION = "extra_result_selection"
    const val EXTRA_RESULT_SELECTION_PATH = "extra_result_selection_path"

    const val EXTRA_RESULT_SELECTION_DATA = "extra_result_selection_data"

    const val EXTRA_SPAN_COUNT = "extra_span_count"
    const val EXTRA_MEDIA_MODE = "extra_mode"
    const val EXTRA_MEDIA_PICK_COUNT = "extra_pick_count"
    const val EXTRA_MAX_PREVIEW_GIF_SIZE = "extra_max_preview_gif_size"
    const val EXTRA_MAX_SCAN_GIF_SIZE = "extra_max_scan_gif_size"
    const val EXTRA_MAX_PICK_VIDEO_SIZE = "extra_max_video_size"
    const val EXTRA_MAX_PICK_VIDEO_DURATION = "extra_max_video_duration"
    const val EXTRA_ENABLE_SELECT_MULTIPLE_TYPE = "extra_enable_select_multiple_type"

    const val EXTRA_PREVIEW_MODE  = "extra_is_preview_mode"
    const val EXTRA_PREVIEW_INDEX = "extra_click_index"
    const val EXTRA_PREVIEW_LATEST_ID = "extra_latest_dir_id"

    const val MODE_ONLY_IMAGE = 1 //只显示图片
    const val MODE_IMAGE_GIF = 2 //显示图片和GIF
    const val MODE_ONLY_VIDEO = 3 //只显示视频
    const val MODE_IMAGE_VIDEO = 4 //显示图片和视频
    const val MODE_ALL_MEDIA = 5 //显示所有

    const val DEFAULT_SPAN_COUNT = 3
    const val DEFAULT_PICK_COUNT = 9
    const val DEFAULT_MAX_VIDEO_SIZE = 30.0
    const val DEFAULT_MAX_PREVIEW_GIF_SIZE = 5.0
    const val DEFAULT_MAX_SCAN_GIF_SIZE = 10.0

    const val DEFAULT_MAX_VIDEO_DURATION = 20L


    const val IMAGE_JPG = "image/jpg"
    private const val IMAGE_JPEG = "image/jpeg"
    private const val IMAGE_PNG = "image/png"
    private const val IMAGE_BMP = "image/bmp"
    private const val IMAGE_WEBP = "image/webp"
    private const val IMAGE_GIF = "image/gif"


    interface DataTrackListener {

        fun onError(method: String, exception: Exception)

        fun onData(method: String, data: List<Album>)

        fun onSelect(file: AlbumItemFile, selected: Boolean)
    }

    interface FileFilter{

        fun contract(bundle: Bundle)

        fun filter(path:String, mineType:String, size: Long, width: Int, height: Int): Boolean

        /**
         * selectedCount > pickCount && !selectedFiles.contains(path)
         */
        fun maxPickCountTrigger(file: AlbumItemFile): Boolean

        /**
         *  file.duration / 1000L > maxVideoDuration
         */
        fun maxVideoDurationTrigger(file: AlbumItemFile): Boolean

        /**
         *  file.size.toDouble() / 1048576.0 > maxVideoSize
         */
        fun maxVideoSizeTrigger(file: AlbumItemFile): Boolean

        /**
         *  file.size.toDouble() / 1048576.0 > maxGifSize
         */
        fun maxPreviewGifSizeTrigger(file: AlbumItemFile): Boolean

        /**
         * 单文件选择模式，不展示勾选框，预览页直接apply
         */
        fun isSingleSelectMode(): Boolean

        /**
         * 是否允许选择不同类型文件
         */
        fun enableSelectMultipleType(): Boolean
    }


    open class DefaultImageFilter : FileFilter{

        private var showGif = false
        private var pickCount: Int = DEFAULT_PICK_COUNT
        private var maxPreviewGifSize: Double = DEFAULT_MAX_PREVIEW_GIF_SIZE
        private var maxScanGifSize: Double = DEFAULT_MAX_SCAN_GIF_SIZE
        private var maxVideoSize: Double = DEFAULT_MAX_VIDEO_SIZE
        private var maxVideoDuration: Long = DEFAULT_MAX_VIDEO_DURATION
        private var enableSelectMultipleType: Boolean = false

        override fun contract(bundle: Bundle) {
            val pickMode = bundle.getInt(EXTRA_MEDIA_MODE, MODE_ONLY_IMAGE)
            showGif = pickMode == MODE_ALL_MEDIA || pickMode == MODE_IMAGE_GIF
            pickCount = bundle.getInt(EXTRA_MEDIA_PICK_COUNT, DEFAULT_PICK_COUNT)
            maxPreviewGifSize = bundle.getDouble(EXTRA_MAX_PREVIEW_GIF_SIZE, DEFAULT_MAX_PREVIEW_GIF_SIZE)
            maxScanGifSize = bundle.getDouble(EXTRA_MAX_SCAN_GIF_SIZE, DEFAULT_MAX_SCAN_GIF_SIZE)
            maxVideoSize = bundle.getDouble(EXTRA_MAX_PICK_VIDEO_SIZE, DEFAULT_MAX_VIDEO_SIZE)
            maxVideoDuration = bundle.getLong(EXTRA_MAX_PICK_VIDEO_DURATION, DEFAULT_MAX_VIDEO_DURATION)
            enableSelectMultipleType = bundle.getBoolean(EXTRA_ENABLE_SELECT_MULTIPLE_TYPE, false)
        }

        override fun filter(path:String, mineType: String, size: Long, width: Int, height: Int): Boolean {
            if (mineType == IMAGE_GIF) {
                return !showGif || size > (maxScanGifSize*1024*1024)
            }
            return false
        }

        override fun maxPickCountTrigger(file: AlbumItemFile): Boolean {
            return MediaStoreHelper.selectedFiles.size == pickCount && !MediaStoreHelper.selectedFiles.contains(file.path)
        }

        override fun maxVideoDurationTrigger(file: AlbumItemFile): Boolean {
            return file.duration / 1000 > maxVideoDuration
        }

        override fun maxVideoSizeTrigger(file: AlbumItemFile): Boolean {
            try {
//                val df = DecimalFormat("#.00")
//                val size = df.format(file.size * 1.0 / (1024 * 1024)).toDouble()
                val size = file.size * 1.0 / (1024 * 1024)
                Log.d("@@@Explorer", "maxVideoSizeTrigger ==> size: $size")
                return size > maxVideoSize  // MB
            }catch (e: Throwable){
            }
            return false
        }

        override fun maxPreviewGifSizeTrigger(file: AlbumItemFile): Boolean {
            try {
                val size = file.size * 1.0 / (1024 * 1024)
                Log.d("@@@Explorer", "maxGifSizeTrigger ==> size: $size")
                return size > maxPreviewGifSize  // MB
            }catch (e: Throwable){
            }
            return false
        }

        override fun isSingleSelectMode():Boolean {
            // fixme.miao 启用单选模式时会导致视频资源无法勾选，后期优化，本期先屏蔽单选模式
            return false
//            return pickCount == 1
        }

        override fun enableSelectMultipleType(): Boolean {
            return enableSelectMultipleType
        }

    }

    @SuppressLint("StaticFieldLeak")
    private var request: Request? = null

    @JvmStatic
    fun getRequest(): Request? = request

    fun release(){
        request = null
    }

    fun getDataTrack(): DataTrackListener ? {
        return getRequest()?.getDataTrack()
    }

    @JvmStatic
    fun makeRequest(): Request {
        request = Request()
        return request as Request
    }

    @Keep
    class Request {

        private var spanCount = DEFAULT_SPAN_COUNT
        var pickCount: Int = DEFAULT_PICK_COUNT
        var maxVideoSize: Double = DEFAULT_MAX_VIDEO_SIZE
        var maxVideoDuration: Long = DEFAULT_MAX_VIDEO_DURATION
        var maxPreviewGifSize: Double = DEFAULT_MAX_PREVIEW_GIF_SIZE
        var maxScanGifSize: Double = DEFAULT_MAX_SCAN_GIF_SIZE
        private var dataTrackListener: DataTrackListener? = null
        private var pickMode: Int = MODE_ONLY_IMAGE
        private var enableSelectMultipleType: Boolean = false
        private var bundle: Bundle = Bundle()
        private var fileFilter: FileFilter = DefaultImageFilter()

        init {
//            context?.let { ctx ->
//                if (ctx !is AppCompatActivity) return@let
//                ctx.lifecycle.addObserver(object : LifecycleEventObserver {
//                    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
//                        if (source.lifecycle == ctx.lifecycle) {
//                            Log.d("@@@Explorer", "${ctx.localClassName} - mediaSelect event: ${event.name}")
//                            if (event == Lifecycle.Event.ON_DESTROY && getRequest() != null){
//                                release()
//                                MediaStoreHelper.release()
//                            }
//                        }
//                    }
//                })
//            }
        }

        /**
         * 配置阈值范围
         * @param pickCount int值
         * @param maxVideoSize 单位MB,例:10.0、11.5
         */
        @Deprecated("废弃")
        fun threshold(pickCount: Int, maxVideoSize: Double): Request {
            this.pickCount = pickCount
            this.maxVideoSize = maxVideoSize
            return this
        }

        fun pickCount(pickCount: Int): Request{
            this.pickCount = pickCount
            return this
        }

        fun maxVideoSize(maxVideoSize: Double): Request{
            this.maxVideoSize = maxVideoSize
            return this
        }

        fun maxVideoDuration(maxVideoDuration: Long): Request{
            this.maxVideoDuration = maxVideoDuration
            return this
        }

        fun maxPreviewGifSize(maxPreviewGifSize: Double): Request{
            this.maxPreviewGifSize = maxPreviewGifSize
            return this
        }

        fun maxScanGifSize(maxScanGifSize: Double): Request{
            this.maxScanGifSize = maxScanGifSize
            return this
        }

        /**
         * 列表列数
         * @param spanCount
         */
        fun spanCount(spanCount:Int): Request{
            if (spanCount > DEFAULT_SPAN_COUNT) {
                this.spanCount = spanCount
            }
            return this
        }

        /**
         * 设置数据回调
         * @param listener
         */
        fun setDataTrack(@NonNull listener: DataTrackListener): Request {
            this.dataTrackListener = listener
            return this
        }

        fun getDataTrack(): DataTrackListener? {
            return dataTrackListener
        }

        /**
         * 配置获取数据类型
         * @param mode ==> Explorer.MODE_ONLY_IMAGE
         */
        fun pickMode(mode: Int): Request {
            this.pickMode = mode
            return this
        }

        /**
         * 是否允许选择多种类型文件
         */
        fun enableMultipleSelect(enable: Boolean): Request {
            this.enableSelectMultipleType = enable
            return this
        }

        /**
         * 配置多参数
         * @param extras
         */
        fun putExtras(extras: Bundle): Request {
            bundle.putAll(extras)
            return this
        }

        fun fileFilter(@NotNull filter: FileFilter): Request{
            fileFilter = filter
            return this
        }

        fun getFileFilter(): FileFilter{
            return fileFilter
        }

        private fun installBundle(){
            bundle.putAll(bundleOf(
                EXTRA_MEDIA_MODE to pickMode ,
                EXTRA_ENABLE_SELECT_MULTIPLE_TYPE to enableSelectMultipleType,
                EXTRA_MEDIA_PICK_COUNT to pickCount ,
                EXTRA_MAX_PICK_VIDEO_SIZE to maxVideoSize,
                EXTRA_MAX_PICK_VIDEO_DURATION to maxVideoDuration,
                EXTRA_MAX_PREVIEW_GIF_SIZE to maxPreviewGifSize,
                EXTRA_MAX_SCAN_GIF_SIZE to maxScanGifSize,
                EXTRA_SPAN_COUNT to spanCount)
            )
            fileFilter.contract(bundle)
        }

        fun createFragment(): AlbumChooseFragment{
            installBundle()
            return AlbumChooseFragment.newInstance(bundle)
        }

        fun start(requestCode: Int, context: Fragment?) {
            installBundle()
            context?.let {ctx ->
                val intent = Intent(ctx.activity, AlbumSearchActivity::class.java)
                intent.putExtras(bundle)
                ctx.startActivityForResult(intent, requestCode)
            }
        }

        fun start(requestCode: Int, context: Activity?) {
            installBundle()
            context?.let {ctx ->
                val intent = Intent(ctx, AlbumSearchActivity::class.java)
                intent.putExtras(bundle)
                ctx.startActivityForResult(intent, requestCode)
            }
        }
    }

    /**
     * 打开选择器预览
     * @param context
     * @param preview 是否选中后预览
     * @param index   点击位置
     * @param dir_id  文件夹id
     */
    @JvmStatic
    fun mediaPreview(context: AppCompatActivity?, preview: Boolean, index: Int = 0, dir_id: String,bundle: Bundle?) {
        context?.let {
            val intent = Intent(it, AlbumSkimActivity::class.java)
            intent.putExtra(EXTRA_PREVIEW_MODE, preview)
            intent.putExtra(EXTRA_PREVIEW_INDEX, index)
            intent.putExtra(EXTRA_PREVIEW_LATEST_ID, dir_id)
            bundle?.let {data ->
                intent.putExtras(data)
            }
            it.startActivityForResult(intent, EXPLORER_MEDIA_PREVIEW)
        }
    }

    fun videoPreview(context: Context, path: String){
        try {
            context.startActivity(Intent().also {
                it.action = Intent.ACTION_VIEW
                it.setDataAndType(Uri.parse(path), "video/*")
            })

        } catch (e: Throwable) {

        }
    }

    /**
     * 老代码使用的知乎Matisse库，此处为工具类兼容代码
     */
    @JvmStatic
    fun obtainResult(data: Intent?): List<Uri>? {
        return data?.getParcelableArrayListExtra<Uri>(EXTRA_RESULT_SELECTION)
    }
}