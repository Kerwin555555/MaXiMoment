package com.moment.app.main_home.subfragments.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.didi.drouter.api.DRouter
import com.moment.app.R
import com.moment.app.databinding.FragmentHomeItemViewBinding
import com.moment.app.datamodel.UserInfo
import com.moment.app.utils.applyDrawable
import com.moment.app.utils.clicks
import com.moment.app.utils.setBgWithCornerRadiusAndColor

class RecommendationAdapter: BaseQuickAdapter<UserInfo, RecommendationAdapter.FragmentHomeItemHolder>(null) {

    override fun onCreateDefViewHolder(parent: ViewGroup?, viewType: Int): FragmentHomeItemHolder{
         val binding = FragmentHomeItemViewBinding.inflate(LayoutInflater.from(mContext),parent, false)
         binding.location.setBgWithCornerRadiusAndColor(9f, solid = 0x1aed9d1d)
         return FragmentHomeItemHolder(binding)
    }

    override fun convert(helper: FragmentHomeItemHolder, item: UserInfo) {
        val binding= helper.binding
        binding.avatar.clicks{
            DRouter.build("/user")
                .putExtra("id", item.user_id)
                .start()
        }

        binding.name.text = item.nickname

        binding.gender.bindGender(item)

        binding.chat.clicks{
            DRouter.build("/chat/thread")
                .putExtra("id", item.huanxin?.user_id ?: "")
                .putExtra("userInfo", item)
                .start()
        }

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