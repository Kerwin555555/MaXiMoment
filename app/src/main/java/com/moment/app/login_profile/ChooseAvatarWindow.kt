package com.moment.app.login_profile

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.PopupWindow
import com.moment.app.R
import com.moment.app.databinding.AvatarPopWindowBinding
import com.moment.app.utils.toast

class ChooseAvatarWindow(val context: Context?) : PopupWindow(context){
    private var binding = AvatarPopWindowBinding.inflate(LayoutInflater.from(context))

    init {
        contentView = binding.root
        width = WindowManager.LayoutParams.MATCH_PARENT
        height = WindowManager.LayoutParams.MATCH_PARENT
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        isOutsideTouchable = false
        animationStyle = R.style.MyPopupWindow

        binding.avatar.setOnClickListener {
            context?.let {
                "去剪裁图片页面".toast()
            }
        }
    }
}