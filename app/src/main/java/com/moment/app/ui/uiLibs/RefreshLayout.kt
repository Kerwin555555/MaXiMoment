package com.moment.app.ui.uiLibs

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
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

    private var adapter: RefreshAdapter<D, *>? = null

    private var loadDataListener: ((isLoadMore: Boolean) -> Unit)? = null

    override fun onFinishInflate() {
        super.onFinishInflate()
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        val refreshView = RefreshView(context)
        setRefreshHeader(refreshView)
        setOnRefreshListener {
            if (loadDataListener != null) {
                loadDataListener?.invoke(false)
            }
        }
    }

    fun setAdapter(
        adapter: RefreshAdapter<D, *>,
        emptyView: EmptyView?,
        loadDataListener: ((isLoadMore: Boolean) -> Unit)?
    ) {
        this.adapter = adapter
        if (this.adapter != null) {
            binding.recyclerView.adapter = adapter
            adapter.refreshEmptyView = emptyView as? EmptyView?
            adapter.setHeaderAndEmpty(true)
            adapter.emptyView = (emptyView as View)
            adapter.setEnableLoadMore(true)
            adapter.setOnLoadMoreListener({

                Log.d("zhouzheng", "急吼吼")
                loadDataListener?.invoke(true)
            }, binding.recyclerView)
            adapter.refreshEmptyView?.showLoading()
            adapter.setSetUpListener(object : OnRetryClickListener {
                override fun onRetry() {
                    loadDataListener?.invoke(false)
                }
            })
        }
    }

    fun onSuccess(newData: MutableList<D>, isLoadMore: Boolean, hasMore: Boolean) {
        if (getState() == RefreshState.Refreshing) {
            finishRefresh()
        }
        adapter?.onNewItemData(newData, isLoadMore, hasMore)
    }

    fun onFail(isLoadMore: Boolean, msg: String?) {
        if (getState() == RefreshState.Refreshing) {
            finishRefresh()
        }
        adapter?.onFail(isLoadMore, msg)
    }

    fun getBinding(): MomentRefreshviewBinding {
        return binding
    }
}

open class RefreshAdapter<D, H : BaseViewHolder> : BaseQuickAdapter<D, H> {
    var refreshEmptyView: EmptyView? = null
        set(value) {
            field = value
        }

    constructor(layoutResId: Int) : super(layoutResId)
    constructor(data: MutableList<D>?) : super(data)
    constructor(layoutResId: Int, data: MutableList<D>) : super(layoutResId, data)

    private var listener: OnRetryClickListener? = null// 仅在FeedAnonymity页面有

    fun setSetUpListener(listener: OnRetryClickListener?) {
        this.listener = listener
        listener?.let {
            refreshEmptyView?.getRetryButton()?.setOnClickListener {
                forceRefresh()
                listener.onRetry()
            }
            setEmptyView(refreshEmptyView as View)
            setEnableLoadMore(true)
            setLoadMoreView(MomentLoadMoreView())
        }
    }

    fun onNewItemData(newData: MutableList<D>, isLoadMore: Boolean, hasMore: Boolean) {
        Log.d("zhouzheng", "急吼吼2"+isLoadMore)
        if (isLoadMore) {
            addData(newData)
        } else {
            setNewData(newData)
            refreshEmptyView?.run {
                showNoData()
            }
        }
        if (hasMore) {
            loadMoreComplete()
        } else {
            setEnableLoadMore(false)
            recyclerView?.let {
                val manager = recyclerView.layoutManager
                manager?.let {
                    if (manager is LinearLayoutManager) {
                        recyclerView.postDelayed({
                            if (this@RefreshAdapter.fullScreen(manager)) {
                                this@RefreshAdapter.setEnableLoadMore(true)
                                loadMoreEnd(false)
                            }
                        }, 50L)
                    }
                }
            }
        }
    }

    private fun fullScreen(llm: LinearLayoutManager): Boolean {
        return llm.findLastCompletelyVisibleItemPosition() + 1 != this.itemCount || llm.findFirstCompletelyVisibleItemPosition() != 0
    }

    fun onFail(isLoadMore: Boolean, msg: String?) {
        refreshEmptyView?.run {
            if (isLoadMore) {
                loadMoreFail()
            } else {
                if (mData != null && mData.isEmpty()) {
                    refreshEmptyView?.run {
                        showRetry(msg)
                    }
                }
            }
        }
    }

    private fun forceRefresh() {
        refreshEmptyView?.run {
            showLoading()
        }
    }

    override fun convert(helper: H, item: D) {

    }
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

interface OnLoadDataListener {
    fun onLoad(isLoadMore: Boolean)
}

interface EmptyView {
    fun getEmpty(): View
    fun getRetryButton(): View
    fun getLoading(): View

    fun showNoData()
    fun showRetry(msg: String?)
    fun showLoading()
    fun hideAll()
}

interface OnRetryClickListener {
    fun onRetry()
}

class MomentLoadMoreView : LoadMoreView() {
    override fun getLayoutId(): Int {
        return R.layout.anonymous_loading_more_view
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