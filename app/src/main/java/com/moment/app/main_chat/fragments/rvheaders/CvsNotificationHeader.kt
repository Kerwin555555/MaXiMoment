package com.moment.app.main_chat.fragments.rvheaders

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.moment.app.databinding.ConversationNotificationHeaderBinding

class CvsNotificationHeader : ConstraintLayout{

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val binding = ConversationNotificationHeaderBinding.inflate(LayoutInflater.from(context), this)

    fun bindData(count: Int , function: () -> Unit) {
        binding.redDot.isVisible = count > 0
        binding.arrow.isVisible = count > 0
        if (count > 0) {
            binding.redDot.text = "${count}"
            binding.redDot.setOnClickListener {
                function.invoke()
            }
            binding.arrow.setOnClickListener {
                function.invoke()
            }
        }
    }
}