package com.moment.app.ui.uiLibs

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.loadmore.LoadMoreView
import com.moment.app.R
import com.moment.app.databinding.BasicRefreshHeaderBinding
import com.moment.app.databinding.MomentRefreshviewBinding
import com.moment.app.utils.dp
import com.scwang.smart.refresh.header.FalsifyHeader
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshHeader
import com.scwang.smart.refresh.layout.api.RefreshKernel
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.constant.RefreshState
import com.scwang.smart.refresh.layout.constant.SpinnerStyle
import kotlin.math.max


class MomentRefreshView<D>(context: Context?, attrs: AttributeSet?) :
    SmartRefreshLayout(context, attrs) {

    private val binding = MomentRefreshviewBinding.inflate(LayoutInflater.from(context), this)
    private var adapter: BaseQuickAdapter<D, *>? = null
    private val layoutManager = LinearLayoutManager(context)

    fun initWith(
        adapter: BaseQuickAdapter<D, *>,
        emptyView: EmptyView,
        refreshHeader: RefreshHeader = RefreshView(context),
        loadDataListener: (it: Boolean) -> Unit
    ) {
        this.adapter = adapter

        // refreshLayout logic
        setRefreshHeader(refreshHeader)
        setOnRefreshListener {
            loadDataListener.invoke(false)
        }

        //RecyclerView, adapter loadMore and emptyView config
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter.apply {
            setupLoadMoreAndEmptyView(
                emptyView = emptyView,
                loadDataListener = loadDataListener,
                rv = binding.recyclerView)
            //preload 5 items
            setPreLoadNumber(0)
        }

        //RetryPage logic
        emptyView.getRetryPage().setOnClickListener {
            emptyView.showLoading()
            loadDataListener.invoke(false)
        }

        //init the ui state
        emptyView.showLoading()
    }

    fun onSuccess(newData: MutableList<D>, isLoadMore: Boolean, hasMore: Boolean) {
        finishRefresh()
        adapter?.onNewItemData(newData, isLoadMore, hasMore)

        //if no more data and just one page, we shouldn't show the loadEnd
        if (!hasMore) {
            binding.recyclerView.postDelayed({
                adapter?.onExamineFullScreen(layoutManager)
            }, 50L)
        }
    }

    fun onFail(isLoadMore: Boolean, msg: String?) {
        finishRefresh()
        adapter?.onFail(isLoadMore, msg)
    }

    fun getRecyclerView(): RecyclerView {
        return binding.recyclerView
    }
}

fun <D> BaseQuickAdapter<D, *>.onNewItemData(newData: MutableList<D>, isLoadMore: Boolean, hasMore: Boolean) {
    if (isLoadMore) {
        addData(newData)
    } else {
        setNewData(newData)
        (emptyView as ViewGroup).children.find { it -> it is  EmptyView}?.run {
            (this as EmptyView).showNoData()
        }
    }
    if (hasMore) {
        loadMoreComplete()
    } else {
        setEnableLoadMore(false)
    }
}

fun <D> BaseQuickAdapter<D, *>.onFail(isLoadMore: Boolean, msg: String?) {
    (emptyView as ViewGroup).children.find { it -> it is  EmptyView}.run {
        if (isLoadMore) {
            loadMoreFail()

        } else {

            if (data.isEmpty()) {
                (this as EmptyView).showRetry(msg)
            } else {

            }
        }
    }
}

fun <D> BaseQuickAdapter<D, *>.onExamineFullScreen(layoutManager: LinearLayoutManager) {
    if (fullScreen(layoutManager)) {
        setEnableLoadMore(true)
        loadMoreEnd(false)
    }
}

private fun BaseQuickAdapter<*, *>.fullScreen(llm: LinearLayoutManager): Boolean {
    return llm.findLastCompletelyVisibleItemPosition() + 1 != this.itemCount || llm.findFirstCompletelyVisibleItemPosition() != 0
}

fun BaseQuickAdapter<*, *>.setupLoadMoreAndEmptyView(emptyView: EmptyView?, loadDataListener: ((isLoadMore: Boolean) -> Unit)?, rv: RecyclerView) {
    setHeaderAndEmpty(true)
    this.emptyView = (emptyView as View)
    setOnLoadMoreListener({
        loadDataListener?.invoke(true)
    }, rv)
    setEnableLoadMore(true)
    setLoadMoreView(MomentLoadMoreView())
}

class RefreshView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs), RefreshHeader {

    private val binding = BasicRefreshHeaderBinding.inflate(LayoutInflater.from(context), this)

    init {
        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    }

    @SuppressLint("RestrictedApi")
    override fun onStateChanged(
        refreshLayout: RefreshLayout,
        oldState: RefreshState,
        newState: RefreshState
    ) {

    }

    override fun getView(): View {
        return this
    }

    override fun getSpinnerStyle(): SpinnerStyle {
        return SpinnerStyle.Translate
    }

    @SuppressLint("RestrictedApi")
    override fun setPrimaryColors(vararg colors: Int) {

    }

    @SuppressLint("RestrictedApi")
    override fun onInitialized(kernel: RefreshKernel, height: Int, maxDragHeight: Int) {

    }

    @SuppressLint("RestrictedApi")
    override fun onMoving(
        isDragging: Boolean,
        percent: Float,
        offset: Int,
        height: Int,
        maxDragHeight: Int
    ) {
       // binding.progress.stop()
    }

    @SuppressLint("RestrictedApi")
    override fun onReleased(refreshLayout: RefreshLayout, height: Int, maxDragHeight: Int) {

    }

    @SuppressLint("RestrictedApi")
    override fun onStartAnimator(refreshLayout: RefreshLayout, height: Int, maxDragHeight: Int) {
        binding.progress.start()
    }

    @SuppressLint("RestrictedApi")
    override fun onFinish(refreshLayout: RefreshLayout, success: Boolean): Int {
        binding.progress.stop()
        return 0
    }

    @SuppressLint("RestrictedApi")
    override fun onHorizontalDrag(percentX: Float, offsetX: Int, offsetMax: Int) {

    }

    override fun isSupportHorizontalDrag(): Boolean {
        return false
    }

    override fun autoOpen(duration: Int, dragRate: Float, animationOnly: Boolean): Boolean {
        return true
    }

    fun getBinding(): BasicRefreshHeaderBinding{
        return binding
    }
}

class RefreshHeadView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    val drawable = ProgressDrawable()

    init {
        setPadding(15.dp,15.dp,15.dp,15.dp)
        drawable.callback = this
    }

     fun start() {
        drawable.start()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        drawable.setBounds(paddingLeft,paddingTop,width- paddingRight, height-paddingBottom)
    }

    fun stop() {
        drawable.stop()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        drawable.attach()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        drawable.detach()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawable.draw(canvas)
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return super.verifyDrawable(who) || who == drawable
    }
}

class ProgressDrawable : Drawable(), Animatable,
    AnimatorUpdateListener {
    protected var mWidth: Int = 0
    protected var mHeight: Int = 0
    protected var mProgressDegree: Int = 0
    protected var mValueAnimator: ValueAnimator = ValueAnimator.ofInt(30, 3600)
    protected var mPath: Path = Path()

    protected val mPaint = Paint()
    init {
        mPaint.style = Paint.Style.FILL
        mPaint.isAntiAlias = true
        mPaint.color = -0x555556
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        val value = animation.animatedValue as Int
        mProgressDegree = 30 * (value / 30)
        invalidateSelf()
    }

    //<editor-fold desc="Drawable">
    override fun draw( canvas: Canvas) {
        canvas.save()
        val matrix = Matrix()
        matrix.preTranslate(bounds.left.toFloat(), bounds.top.toFloat())
        canvas.setMatrix(matrix)
        val drawable: Drawable = this@ProgressDrawable
        val bounds = drawable.bounds
        val width = bounds.width()
        val height = bounds.height()
        val r = max(1.0, (width / 22f).toDouble()).toFloat()

        if (mWidth != width || mHeight != height) {
            mPath.reset()
            mPath.addCircle(width - r, height / 2f, r, Path.Direction.CW)
            mPath.addRect(
                width - 5 * r,
                height / 2f - r,
                width - r,
                height / 2f + r,
                Path.Direction.CW
            )
            mPath.addCircle(width - 5 * r, height / 2f, r, Path.Direction.CW)
            mWidth = width
            mHeight = height
        }
        canvas.rotate(mProgressDegree.toFloat(), (width) / 2f, (height) / 2f)
        for (i in 0..11) {
            mPaint.alpha = (i + 5) * 0x11
            canvas.rotate(30f, (width) / 2f, (height) / 2f)
            canvas.drawPath(mPath, mPaint)
        }
        canvas.restore()
    }

    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(colorFilter: ColorFilter?) {}
    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    //</editor-fold>
    override fun start() {
        mValueAnimator.start()
    }

    override fun stop() {
        mValueAnimator.cancel()
    }

    override fun isRunning(): Boolean {
        return mValueAnimator.isRunning
    }

    fun detach() {
        mValueAnimator.removeAllUpdateListeners()
        mValueAnimator.cancel()
    }

    fun attach() {
        mValueAnimator.setDuration(8000)
        mValueAnimator.interpolator = null
        mValueAnimator.repeatCount = ValueAnimator.INFINITE
        mValueAnimator.repeatMode = ValueAnimator.RESTART
        mValueAnimator.addUpdateListener(this)
        Log.d("zhouzheng", "attach")
    }
}


// BaseQuickAdapter uses one single page to represent the nodata/retry/loading pages
interface EmptyView {
    fun getNoDataPage(): View
    fun getRetryPage(): View
    fun getLoadingPage(): View

    fun showNoData()
    fun showRetry(msg: String?)
    fun showLoading()
    fun hideAll()
}

class MomentLoadMoreView : LoadMoreView() {
    override fun getLayoutId(): Int {
        return R.layout.moment_loading_more_view
    }

    override fun getLoadingViewId(): Int {
        return R.id.load_more_loading_view
    }

    override fun getLoadFailViewId(): Int {
        return R.id.load_more_load_fail_view
    }

    override fun getLoadEndViewId(): Int {
        return R.id.load_more_load_end_view
    }
}

class DataDividerItemDecoration(val adapter: BaseQuickAdapter<*, *>,
                                var size: Float,
                                var dividerColor: Int,
                                var horizontalMargin: Int):
       ItemDecoration() {
    val paint = Paint()

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (adapter.data.size == 0) {
            return
        }
        super.getItemOffsets(outRect, view, parent, state)
        outRect.bottom = SizeUtils.dp2px(size)
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(canvas, parent, state)
        if (adapter.data.size == 0) {
            return
        }
        canvas.save()
        val childCount = parent.childCount
        val mBounds = Rect()
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            parent.getDecoratedBoundsWithMargins(child, mBounds)
            mBounds.bottom += Math.round(child.translationY)
            mBounds.top = mBounds.bottom - SizeUtils.dp2px(size)
            mBounds.right = parent.width - horizontalMargin.dp
            mBounds.left = horizontalMargin.dp
            canvas.drawRect(mBounds, paint.apply {
                color = dividerColor
            })
        }
        canvas.restore()
    }
}