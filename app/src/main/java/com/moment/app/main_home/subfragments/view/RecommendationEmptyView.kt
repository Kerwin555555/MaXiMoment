package com.moment.app.main_home.subfragments.view

import android.content.Context
import android.util.AttributeSet
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
    }
    override fun getNoDataPage(): View {
        return binding.emptyLayout
    }

    override fun getRetryPage(): View {
        return binding.errorLayout
    }

    override fun getLoadingPage(): View {
        return binding.progress
    }

    override fun showNoData() {
        showEmptyView(binding.emptyLayout)
    }

    override fun showRetry(msg: String?) {
        showEmptyView(binding.errorLayout)
    }

    override fun showLoading() {
        showEmptyView(binding.progress)
    }

    override fun hideAll() {
        showEmptyView(null)
    }

    private fun showEmptyView(view: View?) {
        binding.progress.isVisible = view == binding.progress
        binding.errorLayout.isVisible = view == binding.errorLayout
        binding.emptyLayout.isVisible = view == binding.emptyLayout
    }
}