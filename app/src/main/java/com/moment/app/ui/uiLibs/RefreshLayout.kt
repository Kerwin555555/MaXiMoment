package com.moment.app.ui.uiLibs

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.loadmore.LoadMoreView
import com.moment.app.R
import com.moment.app.databinding.BasicRefreshHeaderBinding
import com.moment.app.databinding.MomentRefreshviewBinding
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshHeader
import com.scwang.smart.refresh.layout.api.RefreshKernel
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.constant.RefreshState
import com.scwang.smart.refresh.layout.constant.SpinnerStyle

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
            setPreLoadNumber(5)
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
        if (state == RefreshState.Refreshing) {
            finishRefresh()
        }
        adapter?.onNewItemData(newData, isLoadMore, hasMore)

        //if no more data and just one page, we shouldn't show the loadEnd
        if (!hasMore) {
            binding.recyclerView.postDelayed({
                adapter?.onExamineFullScreen(layoutManager)
            }, 50L)
        }
    }

    fun onFail(isLoadMore: Boolean, msg: String?) {
        if (state == RefreshState.Refreshing) {
            finishRefresh()
        }
        adapter?.onFail(isLoadMore, msg)
    }

    fun getBinding(): MomentRefreshviewBinding {
        return binding
    }
}

fun <D> BaseQuickAdapter<D, *>.onNewItemData(newData: MutableList<D>, isLoadMore: Boolean, hasMore: Boolean) {
    if (isLoadMore) {
        addData(newData)
    } else {
        setNewData(newData)
        (emptyView as? EmptyView?)?.showNoData()
    }
    if (hasMore) {
        loadMoreComplete()
    } else {
        setEnableLoadMore(false)
    }
}

fun <D> BaseQuickAdapter<D, *>.onFail(isLoadMore: Boolean, msg: String?) {
    (emptyView as? EmptyView?)?.run {
        if (isLoadMore) {
            loadMoreFail()
        } else {
            if (data != null && data.isEmpty()) {
                showRetry(msg)
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

    private var animationDrawable: AnimationDrawable? = null


    init {
        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        binding.icon.setImageResource(R.drawable.refresh_animation_drawable)
        animationDrawable = binding.icon.getDrawable() as? AnimationDrawable?
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
        animationDrawable?.selectDrawable(15)
    }

    @SuppressLint("RestrictedApi")
    override fun onReleased(refreshLayout: RefreshLayout, height: Int, maxDragHeight: Int) {

    }

    @SuppressLint("RestrictedApi")
    override fun onStartAnimator(refreshLayout: RefreshLayout, height: Int, maxDragHeight: Int) {
        animationDrawable?.selectDrawable(0)
        animationDrawable?.start()
    }

    @SuppressLint("RestrictedApi")
    override fun onFinish(refreshLayout: RefreshLayout, success: Boolean): Int {
        animationDrawable?.stop()
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