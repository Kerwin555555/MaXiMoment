package com.moment.app.main_chat_private

import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.KeyboardUtils
import com.didi.drouter.annotation.Router
import com.hyphenate.EMMessageListener
import com.hyphenate.chat.EMConversation
import com.hyphenate.chat.EMGroupReadAck
import com.hyphenate.chat.EMMessage
import com.moment.app.R
import com.moment.app.databinding.ActivityChatBinding
import com.moment.app.datamodel.UserInfo
import com.moment.app.hilt.app_level.MockData
import com.moment.app.main_chat.GlobalConversationManager
import com.moment.app.main_chat.ThreadService
import com.moment.app.main_chat_private.adapters.ThreadAdapter
import com.moment.app.main_home.subfragments.view.RecommendationEmptyView
import com.moment.app.models.UserIMManagerBus
import com.moment.app.network.UserCancelException
import com.moment.app.network.startCoroutine
import com.moment.app.network.toast
import com.moment.app.utils.BaseActivity
import com.moment.app.utils.MOMENT_APP
import com.moment.app.utils.cancelIfActive
import com.moment.app.utils.immersion
import com.moment.app.utils.scrollToBottom
import com.moment.app.utils.setOnAvoidMultipleClicksListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject


@AndroidEntryPoint
@Router(scheme = ".*", host = ".*", path = "/chat/thread")
class MessagingThreadInterfaceActivity: BaseActivity(){
    private lateinit var binding: ActivityChatBinding
    private val PAGE_COUNT = 15
    private var moreDataFromNetWork = true
    private var currentJob: Job? = null
    @Inject
    @MockData
    lateinit var conversationHub: GlobalConversationManager

    @Inject
    @MockData
    lateinit var imLoginModel: UserIMManagerBus

    @Inject
    @MockData
    lateinit var threadService: ThreadService

    private lateinit var adapter: ThreadAdapter

    val listener = object : EMMessageListener{
        /**
         * \~english
         * Occurs when a message is received.
         * This callback is triggered to notify the user when a message such as texts or an image, video, voice, location, or file is received.
         */
        override fun onMessageReceived(messages: MutableList<EMMessage>?) {
             startCoroutine({
                 if (messages == null) return@startCoroutine
                 for (message in messages) {
                     if (message.chatType == EMMessage.ChatType.Chat) {
                         adapter.addData(message)
                     }
                 }
             }){
                 it.toast()
             }
        }

        override fun onCmdMessageReceived(messages: List<EMMessage?>?) {

        }

        override fun onMessageRead(messages: List<EMMessage?>?) {}


        override fun onGroupMessageRead(groupReadAcks: List<EMGroupReadAck?>?) {}


        override fun onReadAckForGroupMessageUpdated() {}


        override fun onMessageDelivered(messages: List<EMMessage?>?) {}


        override fun onMessageRecalled(messages: List<EMMessage?>?) {}

    }


    private var conversation_partner_huanxin_id: String? = null //conversation partner id
    private var conversation_partner_user_info: UserInfo? = null
    private var partner_user_id : String? = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        immersion()
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUi()

        ensureParams()

        if (conversation_partner_huanxin_id.isNullOrEmpty()) {
            finish()
            return
        }
        adapter = ThreadAdapter(huanxinId = conversation_partner_huanxin_id!!, conversation_partner_user_info = conversation_partner_user_info)

        resolvePartnerInfo()

        resolvePage()

        initHuanxinWebSocketCallback()

        getChatContent()
    }

    private fun initHuanxinWebSocketCallback() {
       imLoginModel.register(listener)
    }

    private fun resolvePage() {
        binding.refreshView.initWith(
            adapter = adapter,
            emptyView = RecommendationEmptyView(this).apply {
                setBackgroundColor(0x00000000)
            }
        ) { isLoadMore ->
            getChatContent()
        }
        adapter.setEnableLoadMore(false)
        binding.chatTab.contentView  = binding.refreshView
        binding.refreshView.getRecyclerView()
            .addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
                if (bottom < oldBottom && adapter.itemCount > 0) {
                    binding.root.post{
                        if (adapter.itemCount >= 1)  binding.refreshView.getRecyclerView().scrollToPosition(adapter.itemCount - 1)
                    }
                }
            }
    }

    private fun getChatContent() {
        val initialD = if (adapter.data.size > 0) { adapter.getItem(0)!!.msgId } else ""
        currentJob?.cancelIfActive()
        currentJob = startCoroutine({
            val messages = withContext(Dispatchers.IO) {
                //https://blog.csdn.net/CrackgmKey/article/details/82381192
                //https://www.easemob.com/apidoc/android/chat/com/easemob/chat/EMChatManager.html
                val emConversation = imLoginModel.getEMConversation(conversation_partner_huanxin_id?:"")
                if (emConversation == null) {
                    Log.d(MOMENT_APP, "load#EMConversation... isEmpty")
                    return@withContext null
                } else {
                    if (initialD.isEmpty()) {
                        val memoryMessages = imLoginModel.getEMConversationAllMessages(emConversation)?.toMutableList() ?.let {
                            if (it.size in 1..9) {
                                val dbMessages = imLoginModel.loadEmConversationMessagesFromDb(emConversation, it[0].msgId, PAGE_COUNT)
                                it.addAll(0, dbMessages)
                            }
                            it
                        }?.toMutableList()
                        return@withContext memoryMessages
                    } else {
                        return@withContext getMessagesFromDb(emConversation, initialD).toMutableList()
                    }
                }
            }
            notifyMessages(messages as List<EMMessage>?)
        }){
            if (it.throwable is UserCancelException) {
                return@startCoroutine
            }
            it.toast()
            notifyMessagesFail()
        }
    }

    private fun getMessagesFromDb(emConversation: EMConversation?, leastRecentId: String): List<EMMessage> {
        var messages = imLoginModel.loadEmConversationMessagesFromDb(emConversation, leastRecentId, PAGE_COUNT)
        if (!moreDataFromNetWork) {
            return messages
        }
        if (messages.size in 0 until PAGE_COUNT) {
            val checkServer = imLoginModel.getMessagesFromServer(emConversation, leastRecentId, PAGE_COUNT)
            moreDataFromNetWork = checkServer.data.size >= PAGE_COUNT
            messages = imLoginModel.loadEmConversationMessagesFromDb(emConversation, leastRecentId, PAGE_COUNT)
        }
        return messages
    }

    private fun resolvePartnerInfo() {
        if (conversation_partner_user_info == null) {
            conversation_partner_user_info = conversationHub.conversations.find { it -> it.id == conversation_partner_huanxin_id }?.userInfo
        }

        if (conversation_partner_user_info == null) {
            startCoroutine({
                if (partner_user_id == null) {
                    //来自环信ID
                    val userInfo = (imLoginModel.loadUserInfosAccordingToHXids(listOf(conversation_partner_huanxin_id!!)))[0]
                    fillUserInfo(userInfo = userInfo)
                } else {
                    val userInfo = threadService.getUserInfo(partner_user_id).data
                    fillUserInfo(userInfo = userInfo)
                }
            }) {
                it.toast()
            }
        } else  {
            //assert userid non-null!
            fillUserInfo(userInfo = conversation_partner_user_info)
            startCoroutine({
                val userInfo = threadService.getUserInfo(partner_user_id).data
                fillUserInfo(userInfo = userInfo)
            }){
                it.toast()
            }
        }
    }

    private fun ensureParams() {
        conversation_partner_huanxin_id = intent.getStringExtra("id") ?: ""
        partner_user_id = intent.getStringExtra("userId")
        conversation_partner_user_info = intent.getSerializableExtra("info") as UserInfo?
    }

    private fun fillUserInfo(userInfo: UserInfo?) {
        this.conversation_partner_user_info = userInfo
        userInfo?.let {
            binding.toolBar.bindData(it)
            adapter.bindConversationPartnerUserInfo(userInfo)
        }
    }

    fun notifyMessages(list: List<EMMessage>?) {
        list?.let {
            if (adapter.data.size == 0) {
                binding.refreshView.onSuccess(it.toMutableList(), false, false)
            } else {
                adapter.addData(0, it.toMutableList())
            }
        }
    }

    fun notifyMessagesFail() {
        binding.refreshView.finishRefresh()
        binding.refreshView.onFail(false, "error happend")
    }

    override fun onDestroy() {
        super.onDestroy()
        KeyboardUtils.hideSoftInput(this)
        imLoginModel.unregister(listener)
    }

    private fun initUi() {
        binding.root.setBackgroundResource(R.drawable.theme_chat_background)
        binding.toolBar.getBinding().back.setOnClickListener{
            finish()
        }
        binding.chatTab.getBinding().apply {
            sendButton.setOnAvoidMultipleClicksListener({
                if (conversation_partner_huanxin_id.isNullOrEmpty()) return@setOnAvoidMultipleClicksListener
                //检查userInfo 是否被blocked
                //检查是否风险控制
                imLoginModel.getEMConversation(conversation_partner_huanxin_id ?: "")?.let {
                    val msg = imLoginModel.generateTextMessage(editText.text?.toString()?.trim() ?: "", conversation_partner_huanxin_id!!)
                    if (msg == null)return@setOnAvoidMultipleClicksListener
                    imLoginModel.sendMessageToPartner(msg)
                    adapter.addData(msg)
                    binding.refreshView.getRecyclerView().scrollToBottom()
                }
            }, 500)
        }
    }
}

interface ChatListener {
    fun onMessage(message: String?)

    fun onVoice(file: File?, audioTime: Int)

    fun onGif(uri: Uri?)
}