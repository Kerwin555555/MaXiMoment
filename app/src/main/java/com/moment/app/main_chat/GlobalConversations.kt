package com.moment.app.main_chat

import android.text.TextUtils
import android.util.Log
import com.blankj.utilcode.util.LogUtils
import com.facebook.login.Login
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMConversation.EMConversationType
import com.hyphenate.chat.EMMessage
import com.moment.app.main_chat.fragments.entities.MomentConversation
import com.moment.app.models.LoginModel
import com.moment.app.utils.coroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.userAgent
import java.lang.ref.WeakReference

class GlobalConversationHub(val conversationDao: ConversationDao, val threadService: ThreadService) {
    val conversations = mutableListOf<MomentConversation>()
        get() = field

    val conversationChangeListeners = mutableListOf<WeakReference<ConversationChangeListener>>()

    private var isLocalLoading = false

    init {
//        EMClient.getInstance().chatManager().addMessageListener { messages ->
//            messages?.map {
//                handleMessageReceive(it)
//            }
//        }
    }


    fun loadMetaDataFromBackend() {
        if (ChatSPUtil.getBoolean(ChatSPUtil.HAS_LOAD_CLOUD_CONVERSATION, false)) return
        coroutineScope.launch(Dispatchers.IO){
            val ids = ArrayList<String>()
            val result = threadService.conversations().data
            Log.d("zhouzheng", ""+result?.conversations?.size ?: "0")
            val res = mutableListOf<Deferred<*>>()
            for (bean in result!!.conversations!!) {
                res.add(
                    this.async (Dispatchers.IO){
                        val code = insertNewConversation(bean.conversation_id ?: "", bean)
                        Log.d("zhouzheng", "insert code"+(code))
                    }
                )
//                conversationDao.updatePin(bean.conversation_id, if (bean.pinned) 1 else 0)
//                conversationDao.updateFlag(bean.conversation_id, if (bean.reply_later) 1 else 0)
            }
            res.awaitAll()


            //update messages
//            val partIds = Lists.partition(ids, 20)
//            for (temp in partIds) {
//                infoProvider.loadUserInfo(temp)
//            }
            //update userinfos
            //asyncLoadMessages(undeletedConversations)
            val list = getAllFromDb()
            Log.d("zhouzheng", "list size"+(list.size))
            withContext(Dispatchers.Main) {
                ChatSPUtil.save(ChatSPUtil.HAS_LOAD_CLOUD_CONVERSATION, true)
                conversations.clear()
                conversations.addAll(list)
                conversationChangeListeners.forEach { it.get()?.onConversationsChange() }
            }
        }
    }

    fun loadMetaDataFromLocalDb() {
        coroutineScope.launch(Dispatchers.IO) {
            if (ChatSPUtil.getBoolean(ChatSPUtil.HAS_LOAD_LOCAL_CONVERSATION, false) || isLocalLoading) return@launch
            isLocalLoading = true
            val chatConversations = EMClient.getInstance().chatManager().getConversationsByType(
                EMConversationType.Chat)
            for (emConversation in chatConversations) {
              //  val code = insertNewConversation(emConversation.conversationId(), )
//                if (code > 0) {
//                    ids.add(emConversation.conversationId())
//                }

                // for now, comment out the code
//                emConversation?.lastMessage?.let {
//                    conversationDao.updateTime(it.msgTime, emConversation.conversationId())
//                }
            }
            //            val partIds = Lists.partition(ids, 20)
//            for (temp in partIds) {
//                infoProvider.loadUserInfo(temp)
//            }
            val list = getAllFromDb()
            withContext(Dispatchers.Main) {
                ChatSPUtil.save(ChatSPUtil.HAS_LOAD_LOCAL_CONVERSATION, true)
                conversations.clear()
                conversations.addAll(list)
                conversationChangeListeners.forEach { it.get()?.onConversationsChange() }
                isLocalLoading = false
            }
        }
    }

    private suspend fun insertNewConversation(id: String, backend: BackendThread): Long {
        val bean = MomentConversation(id = id, userId = LoginModel.getUserId()).apply {
            this.userInfo = backend.userInfo
        }
        bean.conversationType = 0
        Log.d("zhouzheng", id +"xxxfdsafd" + bean.userId)
        if (TextUtils.isEmpty(bean.id) || TextUtils.isEmpty(bean.userId)) {
            return -1
        }
//        EMClient.getInstance().chatManager().getConversation(id)?.lastMessage?.let {
//            bean.updateTime = it.msgTime
//        }
        bean.updateTime = System.currentTimeMillis() // mock data
        var code: Long = -1
        try {
            code = conversationDao.insert(bean)
        } catch (e: Throwable) {
            LogUtils.d("Conversation", e)
        }
        if (code >= 0) {
            LogUtils.d("Conversation", "insert conversation:$id")
            withContext(Dispatchers.Main) {
                conversations.add(bean)
            }
        }
        return code
    }


    private fun getAllFromDb(): List<MomentConversation> {
        kotlin.runCatching {
            return conversationDao.getAll(LoginModel.getUserId())
        }
        return ArrayList()
    }
    fun refreshListFromDB() {
        coroutineScope.launch(Dispatchers.IO) {
            val list = getAllFromDb()
            withContext(Dispatchers.Main) {
                conversations.clear()
                conversations.addAll(list)
                conversationChangeListeners.forEach { it.get()?.onConversationsChange() }
            }
        }
    }


    fun handleMessageReceive(message: EMMessage) {

    }
}
//
//class InfoProvider {
//    private val loadingIds: MutableSet<String> = HashSet()
//
//    @JvmOverloads
//    fun loadUserInfo(ids: List<String>, from: String = "", callback: Callback? = null) {
//        IThread.runOnUiThread { loadInner(ids, from, callback) }
//    }
//
//    private fun loadInner(ids: List<String>, from: String, callback: Callback?) {
//        val temp: MutableList<String> = java.util.ArrayList(ids)
//        temp.removeAll(loadingIds)
//
//        if (temp.isEmpty()) {
//            return
//        }
//        val data: MutableMap<String, Any> = HashMap()
//        data["ids"] = temp
//        data["from"] = from
//        loadingIds.addAll(temp)
//        ApiService.getIMService().getUserInfoByImId(data)
//            .enqueue(object : ICallback<Result<Map<String?, UserInfo?>?>?>() {
//                fun onSuccess(`object`: Result<Map<String?, UserInfo?>?>) {
//                    for (id in `object`.getData().keySet()) {
//                        val info: UserInfo = `object`.getData().get(id)
//                        if (info != null && TextUtils.isEmpty(info.getUser_id())) {
//                            //可能这个用户不存在
//                            ConversationManager.INSTANCE.deleteConversation(id)
//                        }
//                    }
//                    loadingIds.removeAll(temp)
//                    if (!`object`.getData().isEmpty()) {
//                        ConversationManager.INSTANCE.updateUserInfo(`object`.getData())
//                        EventBus.getDefault().post(IMUserInfoUpdateEvent(`object`.getData()))
//                    }
//                    callback?.onGetInfoMap(`object`.getData())
//                }
//
//                fun onFail(code: Int, msg: String?) {
//                    loadingIds.removeAll(temp)
//                }
//            })
//    }
//
//    interface Callback {
//        fun onGetInfoMap(infoMap: Map<String?, UserInfo?>?)
//    }
//}

interface ConversationChangeListener {
    fun onConversationsChange()
}