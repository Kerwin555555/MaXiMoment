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
import com.hyphenate.chat.EMMessage
import com.hyphenate.chat.EMOptions
import com.hyphenate.push.EMPushConfig
import com.hyphenate.push.EMPushHelper
import com.hyphenate.push.EMPushType
import com.hyphenate.push.PushListener
import com.hyphenate.util.NetUtils
import com.moment.app.BuildConfig
import com.moment.app.MomentApp
import com.moment.app.eventbus.ConnectState
import com.moment.app.login_page.LoginCallback
import com.moment.app.main_chat.GlobalConversationHub
import com.moment.app.utils.AppInfo
import com.moment.app.utils.Constants
import com.moment.app.utils.ProcessUtil
import com.moment.app.utils.coroutineScope
import com.moment.app.utils.coroutineScope2
import com.moment.app.utils.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.util.Locale
import javax.inject.Inject


class IMLoginModel(val globalConversationHub: GlobalConversationHub) {
    private var retryCount = 0
    private val handler = Handler(Looper.getMainLooper())

    @Inject
    lateinit var app: Application

    fun initIM(context: Context?) {
        if (true) {
            return
        }
        if (!ProcessUtil.isMainProcess(context)) {
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
            if (Constants.isRelease) {
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
        try {
            EMClient.getInstance().init(context, options)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        val config: MomentConfig = ConfigModel.momentConfig!!
            //在做打包混淆时，关闭debug模式，避免消耗不必要的资源
        EMClient.getInstance().setDebugMode(BuildConfig.DEBUG)
        //注册一个监听连接状态的listener
        EMClient.getInstance().addConnectionListener(MyConnectionListener())
        EMClient.getInstance().chatManager().addMessageListener(messageListener)
        loadChatData()
    }

    private fun loadChatData() {
        if (EMClient.getInstance().isLoggedInBefore) {
            coroutineScope.launch(Dispatchers.IO){
                initChat()
            }
        }
    }

    fun loginNew(userName: String, password: String, callback: LoginCallback) {
        if (true) {
            coroutineScope.launch(Dispatchers.Main) {
                //simulate the IM login
                delay(300)
                "mock im end".toast()
                onLoginSuccess(callback)
            }

            return
        }
        val config = ConfigModel.momentConfig
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

    fun login(userName: String?, password: String?, callback: LoginCallback?) {
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
                        if (!LoginModel.isLogin()) {
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

    fun logout(callBack: EMCallBack?) {
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

    private fun onLoginFail(code: Int, message: String, callback: LoginCallback?) {
        LogUtils.d("main", "登录聊天服务器失败！ code = $code, message = $message")
        coroutineScope.launch(Dispatchers.Main) {
            callback?.onError(code, message)
        }
    }

    private fun onLoginSuccess(callback: LoginCallback?) {
        initChat()
        LogUtils.d("main", "登录聊天服务器成功！")
        coroutineScope.launch(Dispatchers.Main) {
           callback?.onSuccess()
        }
    }

    fun initChat() {
       // EMClient.getInstance().chatManager().loadAllConversations()
    }

    private fun isUS(): Boolean {
        return Locale.US.country.equals(AppInfo.countryId, ignoreCase = true)
    }


    private val connectionListener: EMConnectionListener = object : EMConnectionListener {
        fun onMessageReceived(list: List<EMMessage>) {

        }

        fun onCmdMessageReceived(list: List<EMMessage>) {

        }

        fun onMessageRecalled(list: List<EMMessage>) {

        }

        override fun onConnected() {
        }

        override fun onDisconnected(errorCode: Int) {
        }
    }

    inner class MyConnectionListener : EMConnectionListener {
        override fun onConnected() {
            coroutineScope2.launch {
                EventBus.getDefault().post(ConnectState(true))
                delay(1000)
                globalConversationHub.loadMetaDataFromLocalDb()
            }
        }

        override fun onDisconnected(error: Int) {
            coroutineScope2.launch {
                EventBus.getDefault().post(ConnectState(false))
                if (error == EMError.USER_REMOVED) {
                    // 显示帐号已经被移除
                } else if (error == EMError.USER_LOGIN_ANOTHER_DEVICE) {
                    // 显示帐号在其他设备登录
                    LoginModel.logout(false, loginService = null)
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
                    if (NetUtils.hasNetwork(app)) {
                        //连接不到聊天服务器
                        //当前网络不可用，请检查网络设置
                    }
                }
            }

        }
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


}