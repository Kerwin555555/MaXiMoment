package com.moment.app.main_profile_feed.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.moment.app.databinding.DetailsToolbarBinding
import com.moment.app.datamodel.UserInfo

class DetailsToolbar: ConstraintLayout{
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    private val binding = DetailsToolbarBinding.inflate(LayoutInflater.from(context), this)



    fun getBinding(): DetailsToolbarBinding {
        return binding
    }

    fun bindData(userInfo: UserInfo) {
        binding.name.text = userInfo.name
        binding.gender.bindGender(userInfo)
        Glide.with(this).load(userInfo.avatar)
            .into(binding.avatar)
    }
}