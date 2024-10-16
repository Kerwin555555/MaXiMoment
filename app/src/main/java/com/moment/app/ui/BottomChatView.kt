package com.moment.app.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import com.moment.app.databinding.BottomNavigationChatBinding

class BottomChatView : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    private val binding = BottomNavigationChatBinding.inflate(LayoutInflater.from(context), this)

    fun getBinding() : BottomNavigationChatBinding {
        return binding
    }
}