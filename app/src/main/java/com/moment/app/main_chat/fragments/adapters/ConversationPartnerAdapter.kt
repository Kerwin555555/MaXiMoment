package com.moment.app.main_chat.fragments.adapters

import TimeUtils
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.hyphenate.chat.EMConversation
import com.hyphenate.chat.EMCustomMessageBody
import com.moment.app.databinding.ConversationItemViewBinding
import com.moment.app.main_chat.fragments.entities.MomentConversation
import com.moment.app.utils.dp
import com.moment.app.utils.loadImage
import com.moment.app.utils.setBadgeBackground

class ConversationPartnerAdapter: BaseQuickAdapter<MomentConversation, ConversationPartnerAdapter.Holder>(null) {

    override fun onCreateDefViewHolder(parent: ViewGroup?, viewType: Int): ConversationPartnerAdapter.Holder {
        val binding = ConversationItemViewBinding.inflate(LayoutInflater.from(mContext)).apply {
            root.layoutParams = RecyclerView.LayoutParams(-1,-2)
        }
        return Holder(binding)
    }

    override fun convert(helper: Holder, conversation: MomentConversation) {
         helper.binding.avatar.loadImage(conversation.userInfo!!.userId, 60.dp, 60.dp)
         helper.binding.name.text = conversation.userInfo!!.name
         helper.binding.gender.bindGender(conversation.userInfo!!)
        helper.binding.redDot.setBadgeBackground(2)

        if (true){
            return
        }
         // lastmessage
        val item: EMConversation? = conversation.emConversation
        val lastMessage = if (item == null) null else item.lastMessage

        if (lastMessage != null && TextUtils.isEmpty(conversation.draft)) {
            conversation.updateTime = lastMessage.msgTime
        }
        helper.binding.time.text = if (conversation.updateTime > 0) TimeUtils.parseChatListTime(conversation.updateTime) else ""

//        helper.binding.lastMessage.text =
//            (lastMessage!!.body as EMCustomMessageBody).params["content"] ?: ""
    }


    data class Holder(val binding: ConversationItemViewBinding) : BaseViewHolder(binding.root)
}