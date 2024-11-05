package com.moment.app.main_profile_feed.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.widget.addTextChangedListener
import com.moment.app.R
import com.moment.app.databinding.PostDetailsChatTabBinding
import com.moment.app.utils.setBgEnableStateListDrawable

class PostDetailsChatTab: androidx.constraintlayout.widget.ConstraintLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    private val binding = PostDetailsChatTabBinding.inflate(LayoutInflater.from(context), this)

    init {
        binding.sendButton.setBgEnableStateListDrawable(
            enableId = R.drawable.bg_send_icon,
            disableId = R.drawable.bg_gray
        )
        binding.sendButton.isEnabled = false
        binding.editText.addTextChangedListener(
            onTextChanged = {txt,_,_,_ ->
                binding.sendButton.isEnabled = !txt?.toString()?.trim().isNullOrEmpty()
            })
    }
}