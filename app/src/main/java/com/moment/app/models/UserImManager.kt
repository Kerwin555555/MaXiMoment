package com.moment.app.models

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.blankj.utilcode.util.LogUtils
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.hyphenate.EMCallBack
import com.hyphenate.EMConnectionListener
import com.hyphenate.EMError
import com.hyphenate.EMMessageListener
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMConversation
import com.hyphenate.chat.EMConversation.EMConversationType
import com.hyphenate.chat.EMCursorResult
import com.hyphenate.chat.EMMessage
import com.hyphenate.chat.EMMessage.ChatType
import com.hyphenate.chat.EMOptions
import com.hyphenate.push.EMPushConfig
import com.hyphenate.push.EMPushHelper
import com.hyphenate.push.EMPushType
import com.hyphenate.push.PushListener
import com.hyphenate.util.NetUtils
import com.moment.app.BuildConfig
import com.moment.app.MomentApp
import com.moment.app.datamodel.UserInfo
import com.moment.app.eventbus.ConnectState
import com.moment.app.login_page.LoginCallback
import com.moment.app.main_chat.GlobalConversationManager
import com.moment.app.utils.AppInfo
import com.moment.app.utils.MOMENT_APP
import com.moment.app.utils.MomentCoreParams
import com.moment.app.utils.ProcessHelper
import com.moment.app.utils.coroutineScope
import com.moment.app.utils.coroutineScope2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.util.Locale
import javax.inject.Inject
import kotlin.math.max


class UserImManager(val globalConversationHub: GlobalConversationManager): UserIMManagerBus {
    private var retryCount = 0
    private val handler = Handler(Looper.getMainLooper())

    @Inject
    lateinit var app: Application
    override fun init(context: Context) {
        if (true) {
            return
        }
        if (!ProcessHelper.isMainProcess(context)) {
            return
        }
        val options = EMOptions()
        // 默认添加好友时，是不需要验证的，改成需要验证
        options.acceptInvitationAlways = false
        // 是否自动将消息附件上传到环信服务器，默认为True是使用环信服务器上传下载，如果设为 false，需要开发者自己处理附件消息的上传和下载
        options.autoTransferMessageAttachments = true
        // 是否自动下载附件类消息的缩略图等，默认为 true 这里和上边这个参数相关联
        options.setAutoDownloadThumbnail(true)

        // 是否开启送达回调通知
//        options.setRequireDeliveryAck(true);
        options.autoLogin = true

        if (BuildConfig.DEBUG) {
            if (MomentCoreParams.isRelease) {
                options.appKey = "1102190223222824#moment" //app key
            }
        }

        val builder = EMPushConfig.Builder(context)
        builder.enableFCM("787479292864sender_id")
        options.isUseFCM = true
        options.pushConfig = builder.build()
        EMPushHelper.getInstance().setPushListener(object : PushListener() {
            override fun onError(pushType: EMPushType, errorCode: Long) {
                Log.e("PushClient", "Push client occur a error: $pushType - $errorCode")
            }

            override fun isSupportPush(pushType: EMPushType, pushConfig: EMPushConfig): Boolean {
                if (pushType == EMPushType.FCM) {
                    return GoogleApiAvailability.getInstance()
                        .isGooglePlayServicesAvailable(context!!) == ConnectionResult.SUCCESS
                }
                return super.isSupportPush(pushType, pushConfig)
            }
        })
        //初始化
        kotlin.runCatching {
            EMClient.getInstance().init(context, options)
        }.onFailure {
            it.printStackTrace()
        }
        val config: MomentConfig = AppConfigManager.momentConfig!!
            //在做打包混淆时，关闭debug模式，避免消耗不必要的资源
        EMClient.getInstance().setDebugMode(BuildConfig.DEBUG)
        //注册一个监听连接状态的listener
        EMClient.getInstance().addConnectionListener(MyConnectionListener(globalConversationHub))
        EMClient.getInstance().chatManager().addMessageListener(IMessageListener())
        loadChatData()
    }

    private fun loadChatData() {
        if (true) return
        if (EMClient.getInstance().isLoggedInBefore) {
             coroutineScope.launch(Dispatchers.IO) {
                 initChat()
             }
        }
    }

    override fun initChat() {
        if (true) return
        EMClient.getInstance().chatManager().loadAllConversations()
    }

    override suspend fun loadUserInfosAccordingToHXids(ids: List<String>): MutableList<UserInfo> {
        if (true) return mutableListOf()
        val map = mutableMapOf("ids" to ids)
        return globalConversationHub.threadService.getUserInfoByImId(map).data?.values?.toMutableList() ?: mutableListOf()
    }

//    override suspend fun loadUserInfosAccordingToHXids(ids: List<String>): MutableList<UserInfo> {
//        globalConversationHub.threadService.getUserInfoByImId(ids)
//    }

    override fun login(userName: String?, password: String?, callback: LoginCallback?) {
        if (true) return
        EMClient.getInstance().login(userName, password, object : EMCallBack {
            //回调
            override fun onSuccess() {
                retryCount = 0
                onLoginSuccess(callback)
            }

            override fun onProgress(progress: Int, status: String) {
            }

            override fun onError(code: Int, message: String) {
                onLoginFail(code, message, callback)
                if (retryCount < 5) {
                    retryCount++
                    handler.postDelayed(Runnable {
                        if (!UserLoginManager.isLogin()) {
                            return@Runnable
                        }
                        login(userName, password, object : LoginCallback {
                            override fun onSuccess() {
                            }

                            override fun onError(code: Int, msg: String?) {
                            }
                        })
                    }, 3 * 1000L)
                }
            }
        })
    }


    override fun logout(callBack: EMCallBack?) {
        if (true) return
        EMClient.getInstance().logout(true, object : EMCallBack {
            override fun onSuccess() {
                callBack?.onSuccess()
            }

            override fun onProgress(progress: Int, status: String) {
                callBack?.onProgress(progress, status)
            }

            override fun onError(code: Int, message: String) {
                callBack?.onError(code, message)
            }
        })
    }

    override fun saveMessage(message: EMMessage?) {
        if (true) return
        try {
            EMClient.getInstance().chatManager().saveMessage(message)
            globalConversationHub.handleMessageSend(message)
        } catch (e: Exception) {
            //ignore
        }
    }

    override fun isFirstMessage(id: String?): Boolean {
        if (true) return true
        if (id == null) {
            return false
        }
        val conversation =
            EMClient.getInstance().chatManager().getConversation(id, EMConversationType.Chat, false)
                ?: return true

        val messages = conversation.allMessages
        if (messages == null || messages.isEmpty()) {
            return true
        }
        for (message in messages) {
            if (message.type == EMMessage.Type.CMD) {
                continue
            }
            return false
        }
        return true
    }

    override fun getConversationChatCount(conversationId: String?): Int {
        if (true) return 1
        val conversation = EMClient.getInstance().chatManager().getConversation(conversationId)
            ?: return 0
        return max(conversation.allMsgCount.toDouble(), 0.0).toInt()
    }

    override fun updateUserInfo(hxid: String?, info: UserInfo?) {
        if (true) return
        globalConversationHub.updateUserInfo(hxid, info)
    }


    override fun loginNew(userName: String, password: String, callback: LoginCallback) {
        if (true) {
            coroutineScope2.launch{
                delay(500)
                callback.onSuccess()
            }
            return
        }
        val config = AppConfigManager.momentConfig
        if (config!!.disableHXLogin || isUS()) {
            callback.onSuccess()
            login(userName, password, LoginCallback.NOP)
            return
        }
        EMClient.getInstance().login(userName, password, object : EMCallBack {
            //回调
            override fun onSuccess() {
                onLoginSuccess(callback)
            }

            override fun onProgress(progress: Int, status: String) {
            }

            override fun onError(code: Int, message: String) {
                if (code == EMError.USER_ALREADY_LOGIN_ANOTHER) {
                    logout(null)
                    onLoginFail(code, message, callback)
                } else if (code == EMError.USER_ALREADY_LOGIN) {
                    onLoginSuccess(callback)
                } else {
                    onLoginFail(code, message, callback)
                }
            }
        })
    }

    private fun isUS(): Boolean {
        return Locale.US.country.equals(AppInfo.countryId, ignoreCase = true)
    }

    private fun onLoginSuccess(callback: LoginCallback?) {
        if (true) return
        initChat()
        LogUtils.d("main", "登录聊天服务器成功！")
        handler.post { callback?.onSuccess() }
    }

    private fun onLoginFail(code: Int, message: String, callback: LoginCallback?) {
        if (true) return
        LogUtils.d("main", "登录聊天服务器失败！ code = $code, message = $message")
        handler.post { callback?.onError(code, message) }
    }

    private val messageListener: EMMessageListener = object: IMessageListener(){

        override fun onMessageReceived(list: List<EMMessage>) {
        }

        override fun onCmdMessageReceived(list: List<EMMessage>) {
        }

        override fun onMessageRead(list: List<EMMessage>) {
        }

        override fun onMessageDelivered(list: List<EMMessage>) {
        }

        override fun onMessageRecalled(list: List<EMMessage>) {
        }

        override fun onMessageChanged(emMessage: EMMessage, o: Any) {
        }
    }

    open class IMessageListener : EMMessageListener {
        /**
         * 处理app_level的环信消息，类似正常 websocket
         */
        override fun onMessageReceived(list: List<EMMessage>) {
            coroutineScope2.launch {
                list.forEach { it ->
                    when (it.chatType) {
                        ChatType.Chat -> {

                        }
                        else -> {

                        }
                    }
                }
            }
        }

        override fun onCmdMessageReceived(list: List<EMMessage>) {
        }

        override fun onMessageRead(list: List<EMMessage>) {
        }

        override fun onMessageDelivered(list: List<EMMessage>) {
        }

        override fun onMessageRecalled(list: List<EMMessage>) {
        }

        override fun onMessageChanged(emMessage: EMMessage, o: Any) {
        }
    }
//
//
//    suspend fun loadUserInfosAccordingToHXids(ids: List<String?>) : MutableList<UserInfo>{
//        delay(500)
//        return mutableListOf()
//    }

    //实现ConnectionListener接口
    private inner class MyConnectionListener(val globalConversationHub: GlobalConversationManager ) : EMConnectionListener {
        override fun onConnected() {
            LogUtils.d(MOMENT_APP, "onConnected")
            handler.post(Runnable {
                EventBus.getDefault().post(ConnectState(true))
            })
            handler.postDelayed(Runnable {
                globalConversationHub.loadMetaDataFromLocalRoomDb()
            }, 1000)
        }

        override fun onDisconnected(error: Int) {
            handler.post(Runnable {
                EventBus.getDefault().post(ConnectState(false))
                if (error == EMError.USER_REMOVED) {
                    // 显示帐号已经被移除
                } else if (error == EMError.USER_LOGIN_ANOTHER_DEVICE) {
                    // 显示帐号在其他设备登录
                    //oginModel.getInstance().logout(false)
                    //单独处理环信退出登录
                    //unbindToken 如果是true的话 第一次可能失败
                    EMClient.getInstance().logout(false, object : EMCallBack {
                        override fun onSuccess() {
                        }

                        override fun onProgress(progress: Int, status: String) {
                        }

                        override fun onError(code: Int, message: String) {
                        }
                    })
                } else {
                    if (NetUtils.hasNetwork(MomentApp.appContext)) {
                        //连接不到聊天服务器
                        //当前网络不可用，请检查网络设置
                    }
                }
            })
        }
    }

    override fun register(messageListener: EMMessageListener) {
        if (true) return
        EMClient.getInstance().chatManager().addMessageListener(messageListener)
    }

    override fun unregister(messageListener: EMMessageListener) {
        if (true) return
        EMClient.getInstance().chatManager().removeMessageListener(messageListener)
    }

    override fun getEMConversationAllMessages(em: EMConversation?): List<EMMessage>? {
        if (true) return null
        return em?.allMessages?: null
    }

    override fun getEMConversation(conversationId: String): EMConversation? {
        if (true) return null
        return EMClient.getInstance().chatManager().getConversation(conversationId)
    }

    override fun loadEmConversationMessagesFromDb(conversation: EMConversation?, msgId: String, count: Int): List<EMMessage> {
        return conversation?.loadMoreMsgFromDB(msgId, count) ?: mutableListOf()
    }

    override fun getMessagesFromServer(emConversation: EMConversation?, leastRecentId: String, pageSize: Int) : EMCursorResult<EMMessage> {
        return EMClient.getInstance().chatManager().fetchHistoryMessages(emConversation!!.conversationId(),  EMConversationType.Chat , pageSize,leastRecentId)
    }

    override fun generateTextMessage(content: String, to: String): EMMessage? {
        if (true) return null
        return EMMessage.createTxtSendMessage(content, to).apply {
               setMessageStatusCallback(object : EMCallBack {
                override fun onSuccess() {
                }

                override fun onError(i: Int, s: String) {
                    LogUtils.d("ONE", "$s --i")
                }

                override fun onProgress(i: Int, s: String) {
                }
            })
        }
    }

    override fun sendMessageToPartner(message: EMMessage?) {
        if (true) return
        EMClient.getInstance().chatManager().sendMessage(message)
    }
}