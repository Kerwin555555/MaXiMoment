package com.moment.app.models

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.didi.drouter.api.DRouter
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.moment.app.MomentApp
import com.moment.app.datamodel.FINISHED_INFO
import com.moment.app.datamodel.UserInfo
import com.moment.app.eventbus.LoginEvent
import com.moment.app.eventbus.LogoutEvent
import com.moment.app.login_page.service.LoginService
import com.moment.app.login_profile.ProfileActivity
import com.moment.app.network.MomentKeysManager
import com.moment.app.utils.AppPrefs
import com.moment.app.utils.coroutineScope2
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

object UserLoginManager {
    private var info: UserInfo? = null

    init {
            try {
                setMemoryUserInfoWhileCheckIfNeedToSaveMMKVAndHuanxinId(AppPrefs.getUserInfo())
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (info == null) {
                setMemoryUserInfoWhileCheckIfNeedToSaveMMKVAndHuanxinId(UserInfo())
            }

    }

    private fun setMemoryUserInfoWhileCheckIfNeedToSaveMMKVAndHuanxinId(newInfo: UserInfo?) {
        if (newInfo != null && !TextUtils.isEmpty(newInfo.session)) {
            MomentKeysManager.saveMMKVSessionAndHuanxinPasswordIfNeed(newInfo)
        }
        info = newInfo
    }

    fun isLogin() : Boolean{
        return info != null && !TextUtils.isEmpty(MomentKeysManager.getSession())
    }

    fun getUserInfo(): UserInfo? {
        return info
    }

    fun getUserId(): String {
        return if (info != null) (info!!.user_id ?: "") else ""
    }

    fun setMemoryUserInfoAndSaveUserInfoToMMKVAndTryToSaveMMKVSessionAndHuanxinPasswordIfNeed(info: UserInfo?) {
        setMemoryUserInfoWhileCheckIfNeedToSaveMMKVAndHuanxinId(info)
        AppPrefs.saveUserInfo(info)
        //fetchUserSettings()
    }

    fun onLoginSuccess(context: Context, type: String?, info: UserInfo) {
        val bundle = Bundle()
        bundle.putString("method", type)
        //FirebaseAnalytics.getInstance(LitApplication.getAppContext()).logEvent("login", bundle)

        AppPrefs.saveLoginType(type)
        //GAModel.getInstance().login(info.getUser_id())
//        val map: MutableMap<String, Any> = HashMap()
//        map["ta_account_id"] = info.userId
//        map["uuid"] = AppInfo.uuid
//        AppsFlyerLib.getInstance()
//            .logEvent(LitApplication.getAppContext(), AFInAppEventType.LOGIN, map)
        MomentKeysManager.saveMMKVSessionAndHuanxinPasswordIfNeed(info)
        setMemoryUserInfoAndSaveUserInfoToMMKVAndTryToSaveMMKVSessionAndHuanxinPasswordIfNeed(info)
        //Sea.getInstance().config().uid(info.getUser_id())
        //EyeConnector.getInstance().fetchEyeToken()
        if (info.register_status != FINISHED_INFO) {
            //这里暂时使用原始的，不使用router，router拦截器可能异步执行，导致黑屏体验问题
            val intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra("type", type)
            context.startActivity(intent)
        } else {
            DRouter.build("/main").start(context)
        }
        EventBus.getDefault().post(LoginEvent())

//        LoginAEvent("login_success")
//            .put("login_type", type)
//            .track()

        var loc: String? = "EN"
        if (!TextUtils.isEmpty(info.country)) {
            loc = info.country
        }
        //LitString.getInstance().checkResources(loc, Arrays.asList(RR.allStrings().clone()))
    }

    fun logout(loginService: LoginService?) {
        logout(true, loginService)
    }

    fun logout(logoutIm: Boolean, loginService: LoginService?) {
        if (!isLogin()) {
            return
        }
        kotlin.runCatching {
            if (logoutIm) {
                //IMModel.logout(null)
            }
        }

//        val properties = JSONObject()
//        try {
//            properties.put("current_diamond", PaymentManager.getInstance().getLeftDiamonds())
//            GAModel.getInstance().getSsInstance().user_set(properties)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//        GAModel.getInstance().getSsInstance().logout()
        AppPrefs.saveUserInfo(null)
       // UserModel.getInstance().logout()
       // CallModel.getInstance().endVoiceChat()

        //META LOGINOUT
        kotlin.runCatching {
            //LoginManager.getInstance().logOut()
        }
        EventBus.getDefault().post(LogoutEvent())
       // AliasModel.getInstance().clearAlias()
       // ConversationManager.getInstance().clearConversations()
        AppPrefs.setAccountInfo(null)
        MomentKeysManager.clear()
       //PartyModel.getInstance().clear()
//        if (TalkGroupManager.INSTANCE.getCurrentSession() != null) {
//            TalkGroupManager.INSTANCE.getCurrentSession().destroy()
//        }
       // EditAvatarController.getInstance().destroy()
        //ImageUploader.getInstance().clear()
        //NotificationUtil.getInstance().clear()
        //TLModel.INSTANCE.release()
        //FamilyApi.INSTANCE.clear(true)
        //InspectBridge.updateBlockCanary()
        kotlin.runCatching {
            //signOutGoogle()

            //logoutApi(loginService)
        }

        info = null
    }

    private fun logoutApi(loginService: LoginService) {
        coroutineScope2.launch {
            kotlin.runCatching {
                loginService.logout()
            }
        }
    }

    fun isMe(id: String?): Boolean {
        if (info != null) {
            return TextUtils.equals(id, info?.user_id)
        }
        return false
    }

    private fun signOutGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("272687572250-i5659eubkl38ck9n17mrijl0neh7rgkc.apps.googleusercontent.com")
            .requestEmail()
            .build()
        val mGoogleSignInClient = GoogleSignIn.getClient(MomentApp.appContext, gso)
        mGoogleSignInClient.signOut()
            .addOnCompleteListener {
                // ...
            }
    }
}