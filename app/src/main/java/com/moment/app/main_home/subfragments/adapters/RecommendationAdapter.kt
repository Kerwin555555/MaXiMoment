package com.moment.app.main_home.subfragments.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.didi.drouter.api.DRouter
import com.moment.app.R
import com.moment.app.databinding.FragmentHomeItemViewBinding
import com.moment.app.datamodel.UserInfo
import com.moment.app.models.LoginModel
import com.moment.app.utils.applyDrawable
import com.moment.app.utils.setBgWithCornerRadiusAndColor
import com.moment.app.utils.setOnSingleClickListener

class RecommendationAdapter: BaseQuickAdapter<UserInfo, RecommendationAdapter.FragmentHomeItemHolder>(null) {

    override fun onCreateDefViewHolder(parent: ViewGroup?, viewType: Int): FragmentHomeItemHolder{
         val binding = FragmentHomeItemViewBinding.inflate(LayoutInflater.from(mContext),parent, false)
         binding.location.setBgWithCornerRadiusAndColor(9f, solid = 0x1aed9d1d)
         return FragmentHomeItemHolder(binding)
    }

    override fun convert(helper: FragmentHomeItemHolder, item: UserInfo) {
        val binding= helper.binding
        binding.avatar.setOnSingleClickListener({
            DRouter.build("/user")
                .putExtra("id", item.userId)
                .start()
        }, 500)

        binding.name.text = item.name

        binding.gender.bindGender(item)

        item.followed?.let {
            binding.chat.isSelected = it
            binding.chatText.applyDrawable(
                start = if (it) 0 else R.mipmap.love_small_12dp
            )
            binding.chatText.setText(if (it) R.string.moment_Chat else R.string.moment_accost)
        }
    }



    data class FragmentHomeItemHolder(val binding: FragmentHomeItemViewBinding) : BaseViewHolder(binding.root)
}