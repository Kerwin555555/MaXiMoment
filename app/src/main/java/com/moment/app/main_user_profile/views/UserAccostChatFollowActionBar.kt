package com.moment.app.main_user_profile.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.blankj.utilcode.util.SizeUtils
import com.moment.app.databinding.UserBottomBarBinding
import com.moment.app.datamodel.UserInfo
import com.moment.app.utils.applyPaddingsWithDefaultZero
import com.sxu.shadowdrawable.ShadowDrawable

class UserAccostChatFollowActionBar: ConstraintLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    val binding = UserBottomBarBinding.inflate(LayoutInflater.from(context), this)

//(View view, int bgColor, int shapeRadius, int shadowColor, int shadowRadius, int offsetX, int offsetY
    init {
        initBgShadow(binding.chat)
        initBgShadow(binding.container)
        ShadowDrawable.setShadowDrawable(
        binding.follow,
        0xffffffff.toInt(),
        SizeUtils.dp2px(25f),
        0x33ff2b8b.toInt(),
        SizeUtils.dp2px(9.5f),
        0,
        SizeUtils.dp2px(2.5f))
        applyPaddingsWithDefaultZero(start = SizeUtils.dp2px(10.5f), end = SizeUtils.dp2px(10.5f))
    }

    fun bindUserInfo(info: UserInfo?) {

    }

    fun initBgShadow(v: View) {
        ShadowDrawable.setShadowDrawable(
            v,
            0xffffeff4.toInt(),
            SizeUtils.dp2px(25f),
            0x33ff2b8b.toInt(),
            SizeUtils.dp2px(9.5f),
            0,
            SizeUtils.dp2px(2.5f))
    }
}