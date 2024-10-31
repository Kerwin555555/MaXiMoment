package com.moment.app.main_chat_private

import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat.MessagingStyle.Message
import androidx.lifecycle.ViewModel
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMConversation
import com.hyphenate.chat.EMConversation.EMConversationType
import com.hyphenate.chat.EMCursorResult
import com.hyphenate.chat.EMMessage
import com.moment.app.network.startCoroutine
import com.moment.app.network.toast
import com.moment.app.utils.MOMENT_APP
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.withContext


interface MVPView{
    fun notifyMessages(list: List<EMMessage>?)
    fun notifyMessagesFail()
}


class ThreadViewModel: ViewModel() {
    private lateinit var mpvView: MVPView
    private lateinit var conversation_huanxin_id: String
    private var moreDataFromNetWork = true
    private var PAGE_COUNT = 15

    fun setView(c: MVPView, conversation_huanxin_id: String) {
        this.mpvView = c
        this.conversation_huanxin_id = conversation_huanxin_id
    }


    fun fetchChatRecords(initialD: String) {
        (mpvView as? AppCompatActivity?)?.startCoroutine({
            val messages = withContext(Dispatchers.IO) {
                val emConversation = EMClient.getInstance().chatManager().getConversation(conversation_huanxin_id,
                    EMConversationType.Chat, false)
                if (emConversation == null) {
                    Log.d(MOMENT_APP, "load#EMConversation... isEmpty")
                    return@withContext null
                } else {
                    if (initialD.isEmpty()) {
                        val memoryMessages = emConversation.allMessages.toMutableList(). let {
                            if (it.size in 1..9) {
                                val dbMessages = emConversation.loadMoreMsgFromDB(it[0].msgId, PAGE_COUNT)
                                it.addAll(0, dbMessages)
                            }
                            it
                        }.toMutableList()
                        return@withContext memoryMessages
                    } else {
                        return@withContext getMessagesFromDb(emConversation, initialD).toMutableList()
                    }
                }
            }
            mpvView.notifyMessages(messages as List<EMMessage>?)
        }){
            it.toast()
            mpvView.notifyMessagesFail()
        }
    }

    /**
     * emConversation 是手机当前缓存管理者
     */
    private fun getMessagesFromDb(emConversation: EMConversation, leastRecentId: String): List<EMMessage> {
        var messages = emConversation.loadMoreMsgFromDB(leastRecentId, PAGE_COUNT)
        if (!moreDataFromNetWork) {
            return messages
        }
        if (messages == null || messages.size in 0 until PAGE_COUNT) {
            val checkServer = getMessagesFromServer(emConversation, leastRecentId)
            moreDataFromNetWork = checkServer.data.size >= PAGE_COUNT
            messages = emConversation.loadMoreMsgFromDB(leastRecentId, PAGE_COUNT)
        }
        return messages
    }

    private fun getMessagesFromServer(emConversation: EMConversation, leastRecentId: String) : EMCursorResult<EMMessage> {
       return EMClient.getInstance().chatManager().fetchHistoryMessages(emConversation.conversationId(),  EMConversationType.Chat , 20,leastRecentId)
    }
}