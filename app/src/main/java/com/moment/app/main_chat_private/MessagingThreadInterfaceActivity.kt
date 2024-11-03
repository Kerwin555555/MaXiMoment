package com.moment.app.main_chat_private

import android.os.Bundle
import androidx.activity.viewModels
import com.blankj.utilcode.util.KeyboardUtils
import com.didi.drouter.annotation.Router
import com.hyphenate.EMMessageListener
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMMessage
import com.moment.app.R
import com.moment.app.databinding.ActivityChatBinding
import com.moment.app.datamodel.UserInfo
import com.moment.app.hilt.app_level.MockData
import com.moment.app.main_chat.GlobalConversationHub
import com.moment.app.main_chat.ThreadService
import com.moment.app.main_chat_private.adapters.ThreadAdapter
import com.moment.app.main_home.subfragments.view.RecommendationEmptyView
import com.moment.app.models.UserImManager
import com.moment.app.network.startCoroutine
import com.moment.app.network.toast
import com.moment.app.utils.BaseActivity
import com.moment.app.utils.getChatBg
import com.moment.app.utils.immersion
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import javax.inject.Inject


@AndroidEntryPoint
@Router(scheme = ".*", host = ".*", path = "/chat/thread")
class MessagingThreadInterfaceActivity: BaseActivity(), MVPView{
    private lateinit var binding: ActivityChatBinding
    private val viewModel by viewModels<ThreadViewModel>()

    @Inject
    @MockData
    lateinit var conversationHub: GlobalConversationHub

    @Inject
    @MockData
    lateinit var imLoginModel: UserImManager

    @Inject
    @MockData
    lateinit var threadService: ThreadService

    private lateinit var adapter: ThreadAdapter


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
        viewModel.setView(this, conversation_partner_huanxin_id!!)

        resolvePartnerInfo()

        resolvePage()

        //initHuanxinWebSocketData()

        getChatContent()
    }

    private fun initHuanxinWebSocketData() {
        EMClient.getInstance().chatManager().addMessageListener(object : EMMessageListener {
            override fun onMessageReceived(messages: MutableList<EMMessage>?) {

            }
        })
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
        viewModel.fetchChatRecords(if (adapter.data.size > 0) { adapter.getItem(0)!!.msgId } else "")
    }

    private fun resolvePartnerInfo() {
        if (conversation_partner_user_info == null) {
            conversation_partner_user_info = conversationHub.conversations.find { it -> it.id == conversation_partner_huanxin_id }?.userInfo
        }

        if (conversation_partner_user_info == null) {
            startCoroutine({
                if (partner_user_id == null) {
                    val list = listOf(this.async {
                        (imLoginModel.loadUserInfosAccordingToHXids(listOf(conversation_partner_huanxin_id)))[0]
                    }, this.async {
                        getChatContent()
                    })
                    val userInfo = (imLoginModel.loadUserInfosAccordingToHXids(listOf(conversation_partner_huanxin_id)))[0]
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
            getChatContent()
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

    override fun notifyMessages(list: List<EMMessage>?) {
        list?.let {
            adapter.addData(0, list)
        }
        binding.refreshView.finishRefresh()
    }

    override fun notifyMessagesFail() {
        binding.refreshView.finishRefresh()
        binding.refreshView.onFail(false, "error happend")
    }

    override fun onDestroy() {
        super.onDestroy()
        KeyboardUtils.hideSoftInput(this)
    }

    private fun initUi() {
        binding.root.setBackgroundResource(R.drawable.theme_chat_background)
    }
}