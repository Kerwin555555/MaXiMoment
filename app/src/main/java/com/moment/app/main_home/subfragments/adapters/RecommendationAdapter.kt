package com.moment.app.main_home.subfragments.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.moment.app.R
import com.moment.app.databinding.FragmentHomeItemViewBinding
import com.moment.app.datamodel.UserInfo
import com.moment.app.utils.applyDrawable

class RecommendationAdapter: BaseQuickAdapter<UserInfo, RecommendationAdapter.FragmentHomeItemHolder>(null) {

    override fun onCreateDefViewHolder(parent: ViewGroup?, viewType: Int): FragmentHomeItemHolder{
        val binding = FragmentHomeItemViewBinding.inflate(LayoutInflater.from(mContext),parent, false)
        return FragmentHomeItemHolder(binding)
    }

    override fun convert(helper: FragmentHomeItemHolder, item: UserInfo) {
        val binding= helper.binding
        binding.desc

        binding.name.text = item.name

        binding.gender.bindGender(item)

        binding.avatar

        binding.location

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