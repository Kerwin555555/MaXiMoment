package com.moment.app.ui.uiLibs

import android.content.Context
import android.graphics.drawable.LevelListDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.moment.app.R
import com.moment.app.databinding.LayoutGenderBinding
import com.moment.app.datamodel.UserInfo

class GenderView  : LinearLayout{

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val binding = LayoutGenderBinding.inflate(LayoutInflater.from(context), this)

    init {
        setBackgroundResource(R.drawable.bg_gender_level_list)
        orientation = HORIZONTAL
    }

    fun bindGender(userInfo: UserInfo) {
        (background as? LevelListDrawable)?.setLevel(if (userInfo.gender == "male")1 else 0)
        (binding.genderView.drawable as? LevelListDrawable)?.setLevel(if (userInfo.gender == "male")1 else 0)
        binding.text.text = "${userInfo.age}"
        binding.text.isSelected = userInfo.gender == "male"
    }
}