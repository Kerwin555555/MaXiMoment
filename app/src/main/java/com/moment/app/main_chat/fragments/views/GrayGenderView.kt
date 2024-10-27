package com.moment.app.main_chat.fragments.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.LevelListDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.moment.app.R
import com.moment.app.databinding.LayoutGenderBinding
import com.moment.app.datamodel.UserInfo


class GrayGenderView  : LinearLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val binding = LayoutGenderBinding.inflate(LayoutInflater.from(context), this)

    init {
        setBackgroundResource(R.drawable.bg_gray_gender_view)
        orientation = HORIZONTAL


        val tintColor = ColorStateList.valueOf(0xff999999.toInt())
        binding.genderView.setImageTintList(tintColor)

        binding.text.setTextColor(0xff999999.toInt())
    }

    fun bindGender(userInfo: UserInfo) {
        (binding.genderView.drawable as? LevelListDrawable)?.setLevel(if (userInfo.gender == "male")1 else 0)
        binding.text.text = "${userInfo.age}"
    }
}