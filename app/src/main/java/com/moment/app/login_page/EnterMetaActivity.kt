package com.moment.app.login_page

import android.os.Bundle
import android.text.TextUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.didi.drouter.annotation.Router
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.gyf.immersionbar.ImmersionBar
import com.moment.app.databinding.FacebookLoginActivityBinding
import com.moment.app.datamodel.UserInfo
import com.moment.app.eventbus.LogCancelEvent
import com.moment.app.hilt.app_level.MockData
import com.moment.app.login_page.service.LoginService
import com.moment.app.models.UserIMManagerBus
import com.moment.app.models.UserLoginManager
import com.moment.app.network.startCoroutine
import com.moment.app.network.toast
import com.moment.app.utils.BaseActivity
import com.moment.app.utils.ProgressIndicatorFragment
import com.moment.app.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject


@Router(scheme = ".*", host = ".*", path = "/login/facebook")
@AndroidEntryPoint
class EnterMetaActivity : BaseActivity() {
    private lateinit var binding: FacebookLoginActivityBinding
    private lateinit var callbackManager: CallbackManager

    @Inject
    lateinit var loginService: LoginService

    @Inject
    @MockData
    lateinit var imLoginModel: UserIMManagerBus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ImmersionBar.with(this)
            .statusBarDarkFont(false)
            .init()

        binding = FacebookLoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())
        initFacebookLogin()

        login("metatoken")

//        val accessToken = AccessToken.getCurrentAccessToken()
//        val isLoggedIn = accessToken != null && !accessToken.isExpired
//        if (isLoggedIn && ConfigModel.momentConfig?.enableFacebookTokenCheck == true) {
//            login(accessToken!!.token)
//        } else {
//            binding.loginButton.performClick()
//        }
    }

    private fun initFacebookLogin() {
        callbackManager = CallbackManager.Factory.create()
        binding.loginButton.setPermissions(
            "email",
            "public_profile",
            "user_age_range",
            "contact_email"
        )
        binding.loginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onCancel() {

                // App code
                LogUtils.d("Login", "login cancel")
                ToastUtils.showShort("login cancel")
                EventBus.getDefault().post(LogCancelEvent())
                finish()
            }

            override fun onError(exception: FacebookException) {

                // App code
                LogUtils.d("Login", "FBError: " + exception.toString())
                ToastUtils.showShort("FBError: " + exception.toString())
                EventBus.getDefault().post(LogCancelEvent())
                //trackFail(-100, exception.toString())
                finish()
            }

            override fun onSuccess(loginResult: LoginResult) {

                // App code
                LogUtils.d(
                    "Login",
                    loginResult.accessToken.userId + "\n" + loginResult.accessToken.token
                )
                login(loginResult.accessToken.token)
            }
        })
    }

    private fun login(metaToken: String) {
         val progressDialog = ProgressIndicatorFragment.show(this)
         progressDialog.isCancelable = false
         startCoroutine({
             val map = mutableMapOf("login_token" to metaToken, "login_type" to "facebook")
             val result = loginService.login(map)
             val info: UserInfo? = result.data?.user_info?.apply {
                 session = result.data?.session
             }
             if (info == null || TextUtils.isEmpty(info.user_id)) {
                 //onFail(-1, getString(R.string.data_error))
                 return@startCoroutine
             }
             imLoginModel.loginNew(info.huanxin!!.user_id!!,
                 info.huanxin!!.password!!, object : LoginCallback {
                     override fun onSuccess() {
                         progressDialog.dismiss()
                         UserLoginManager.onLoginSuccess(this@EnterMetaActivity, "FB", info)
                         finish()
                     }

                     override fun onError(code: Int, msg: String?) {
                         //onFail(code, msg)
                         progressDialog.dismiss()
                         msg?.toast()
                         finish()
                     }
                 })
         }){
             it.throwable.printStackTrace()
             it.toast()
         }
    }
}


interface LoginCallback {
    fun onSuccess()

    fun onError(code: Int, msg: String?)

    companion object {
        val NOP: LoginCallback = object : LoginCallback {
            override fun onSuccess() {
            }

            override fun onError(code: Int, msg: String?) {
            }
        }
    }
}
