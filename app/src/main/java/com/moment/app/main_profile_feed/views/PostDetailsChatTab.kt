package com.moment.app.main_profile_feed.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.moment.app.databinding.BottomNavigationProfileBinding
import com.moment.app.databinding.PostDetailsChatTabBinding

class PostDetailsChatTab: FrameLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    private val binding = PostDetailsChatTabBinding.inflate(LayoutInflater.from(context), this)
}