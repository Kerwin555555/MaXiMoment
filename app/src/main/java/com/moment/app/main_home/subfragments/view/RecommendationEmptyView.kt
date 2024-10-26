package com.moment.app.main_home.subfragments.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.moment.app.databinding.RecommendationEmptyViewBinding
import com.moment.app.ui.uiLibs.EmptyView

class RecommendationEmptyView : FrameLayout, EmptyView{
    private val binding = RecommendationEmptyViewBinding.inflate(LayoutInflater.from(context), this)
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        setBackgroundColor(0xffffffff.toInt())
    }
    override fun getNoDataPage(): View {
        return binding.emptyLayout
    }

    override fun getRetryPage(): View {
        return binding.errorLayout
    }

    override fun getLoadingPage(): View {
        return binding.loading
    }

    override fun showNoData() {
        showEmptyView(binding.emptyLayout)
    }

    override fun showRetry(msg: String?) {
        showEmptyView(binding.errorLayout)
    }

    override fun showLoading() {
        showEmptyView(binding.loading)
    }

    override fun hideAll() {
        showEmptyView(null)
    }

    private fun showEmptyView(view: View?) {
        binding.loading.isVisible = view == binding.loading
        binding.errorLayout.isVisible = view == binding.errorLayout
        binding.emptyLayout.isVisible = view == binding.emptyLayout
        if (binding.loading == view) {
            binding.loading.start()
        } else {
            binding.loading.stop()
        }
    }
}