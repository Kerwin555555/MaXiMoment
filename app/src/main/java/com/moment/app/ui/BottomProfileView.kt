package com.moment.app.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.moment.app.databinding.BottomNavigationProfileBinding

class BottomProfileView : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    private val binding = BottomNavigationProfileBinding.inflate(LayoutInflater.from(context), this)

    override fun onFinishInflate() {
        super.onFinishInflate()
    }

    fun getBinding() : BottomNavigationProfileBinding {
        return binding
    }
}