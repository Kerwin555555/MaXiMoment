package com.moment.app.main_chat_private.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.hyphenate.chat.EMMessage
import com.hyphenate.chat.EMTextMessageBody
import com.moment.app.R
import com.moment.app.databinding.ChatItemViewHubBinding
import com.moment.app.datamodel.UserInfo
import com.moment.app.models.LoginModel
import com.moment.app.utils.displayImage
import com.moment.app.utils.formatChatTime
import com.moment.app.utils.getScreenWidth

class ThreadAdapter(val huanxinId: String, var conversation_partner_user_info: UserInfo?): BaseQuickAdapter<EMMessage, ThreadAdapter.BindingHolder>(null) {

    companion object {
        val CHAT_CONTENT_FROM_ME = 100
        val CHAT_CONTENT_FROM_PARTNER = 200
        val CHAT_SYSTEM_MSG = 300
        val MAX_CONTENT_WIDTH = getScreenWidth() * 0.483
        val FIVE_MINUES = 300_000
    }


    fun bindConversationPartnerUserInfo(conversation_partner_user_info: UserInfo?) {
        this.conversation_partner_user_info = conversation_partner_user_info
        kotlin.runCatching {
            notifyDataSetChanged()
        }
    }


    override fun onCreateDefViewHolder(parent: ViewGroup?, viewType: Int): BindingHolder {
        when (viewType) {
            CHAT_CONTENT_FROM_ME -> {
                return BindingHolder(ChatItemViewHubBinding.bind(LayoutInflater.from(mContext).inflate(R.layout.chat_content_me_item_view, parent, false)).apply {
                    chatContent.maxWidth = MAX_CONTENT_WIDTH.toInt()
                })
            }
            CHAT_CONTENT_FROM_PARTNER -> {
                return BindingHolder(ChatItemViewHubBinding.bind(LayoutInflater.from(mContext).inflate(R.layout.chat_content_partner_item_view, parent, false)).apply {
                    chatContent.maxWidth = MAX_CONTENT_WIDTH.toInt()
                })
            }
            else -> {
                return BindingHolder(ChatItemViewHubBinding.bind(LayoutInflater.from(mContext).inflate(R.layout.chat_content_me_item_view, parent, false)))
            }
        }
    }

    override fun getDefItemViewType(position: Int): Int {
        val message = getItem(position)
        val isMe = message!!.direct() == EMMessage.Direct.SEND

        //类型捕获
        if (message.type == EMMessage.Type.TXT) {
            return if (isMe)  CHAT_CONTENT_FROM_ME else CHAT_CONTENT_FROM_PARTNER
        }
        return -1
    }

    override fun convert(helper: BindingHolder, item: EMMessage) {
        kotlin.runCatching {
            handleTime(helper, item)
            when (helper.itemViewType) {
                CHAT_CONTENT_FROM_ME -> {
                    val info = LoginModel.getUserInfo()
                    helper.binding.avatar.displayImage(info?.avatar ?: "")
                    //文本
                    val txtBody = item.body as EMTextMessageBody
                    helper.binding.chatContent.text = (item.body as EMTextMessageBody).message
                }

                CHAT_CONTENT_FROM_PARTNER -> {
                    val info = conversation_partner_user_info
                    helper.binding.avatar.displayImage(info?.avatar ?: "")
                    //文本
                    val txtBody = item.body as EMTextMessageBody
                    helper.binding.chatContent.text = (item.body as EMTextMessageBody).message
                }
            }
        }
    }

    private fun handleTime(helper: BindingHolder, item: EMMessage) {
        val adapterPos = helper.absoluteAdapterPosition
        val time = item.msgTime
        val showTime = adapterPos == 0 || (adapterPos > 0 && time - getItem(adapterPos - 1)!!.msgTime >= FIVE_MINUES)
        helper.binding.time.isVisible = showTime
        if (showTime) {
            helper.binding.time.text = formatChatTime(time)
        }
    }

    data class BindingHolder(val binding: ChatItemViewHubBinding): BaseViewHolder(binding.root)
}