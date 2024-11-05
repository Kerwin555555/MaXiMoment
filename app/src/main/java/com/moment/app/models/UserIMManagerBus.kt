package com.moment.app.models

import android.content.Context
import com.hyphenate.EMCallBack
import com.hyphenate.EMMessageListener
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMConversation
import com.hyphenate.chat.EMCursorResult
import com.hyphenate.chat.EMMessage
import com.moment.app.datamodel.UserInfo
import com.moment.app.login_page.LoginCallback


interface UserIMManagerBus {

    /**
     * 初始化chat,必须链接websocket
     */
    fun init(context: Context)

    /**
     * 预加载加载所有的回话
     */
    fun initChat()

    /**
     * 根据环信IDs 加载userinfos
     */
    suspend fun loadUserInfosAccordingToHXids(ids: List<String>) : MutableList<UserInfo>

    /**
     *  登录函数
     */
    fun login(userName: String?, password: String?, callback: LoginCallback?)

    /**
     * 登录新用户
     */
    fun loginNew(userName: String, password: String, callback: LoginCallback)

    /**
     * 登出
     */
    fun logout(callBack: EMCallBack?)

    /**
     * 保存消息到内存和本地数据库 EMClient.getInstance().chatManager().saveMessage
     */
    fun saveMessage(message: EMMessage?)

    /**
     * 该id的conversation 是空或者只有CMD消息
     *
     */
    fun isFirstMessage(id: String?): Boolean


    /**
     * 拿到当前conversation的本地数据库中会话的全部消息数
     */
    fun getConversationChatCount(conversationId: String?): Int

    /**
     * 根据环信信息，info信息更新数据库
     */
    fun updateUserInfo(hxid: String?, info: UserInfo?)


    fun register(messageListener: EMMessageListener)

    fun unregister(messageListener: EMMessageListener)

    fun getEMConversationAllMessages(em: EMConversation?): List<EMMessage>?

    fun getEMConversation(conversationId: String): EMConversation?

    fun loadEmConversationMessagesFromDb(conversation: EMConversation?, msgId: String, count: Int): List<EMMessage>

    fun getMessagesFromServer(emConversation: EMConversation?, leastRecentId: String, pageSize: Int) : EMCursorResult<EMMessage>

    fun generateTextMessage(content: String, to: String): EMMessage?

    fun sendMessageToPartner(message: EMMessage?)
}