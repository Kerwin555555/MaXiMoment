package com.moment.app.login_page

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.didi.drouter.annotation.Router
import com.facebook.AccessToken
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
import com.moment.app.models.ConfigModel
import com.moment.app.models.IMModel
import com.moment.app.models.LoginModel
import com.moment.app.network.startCoroutine
import com.moment.app.network.toast
import com.moment.app.utils.BaseActivity
import com.moment.app.utils.ProgressDialog
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject
import kotlin.math.log


@Router(scheme = ".*", host = ".*", path = "/login/facebook")
@AndroidEntryPoint
class FacebookLoginActivity : BaseActivity() {
    private lateinit var binding: FacebookLoginActivityBinding
    private lateinit var callbackManager: CallbackManager

    @Inject
    @MockData
    lateinit var loginService: LoginService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ImmersionBar.with(this)
            .statusBarDarkFont(false)
            .init()

        kotlin.runCatching {
            binding = FacebookLoginActivityBinding.inflate(layoutInflater)
            setContentView(binding.getRoot())
            initFacebookLogin()
            val accessToken = AccessToken.getCurrentAccessToken()
            val isLoggedIn = accessToken != null && !accessToken.isExpired
            if (isLoggedIn && ConfigModel.momentConfig?.enableFacebookTokenCheck == true) {
                login(accessToken!!.token)
            } else {
                binding.loginButton.performClick()
            }
        }.onFailure {
           //
            login("metaToken")
        }
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
         val progressDialog = ProgressDialog.show(this)
         progressDialog.isCancelable = false
         startCoroutine({
             val map = mutableMapOf("token" to metaToken)
             val result = loginService.facebookLogin(map)
             val info: UserInfo? = result.data
             if (info == null || TextUtils.isEmpty(info.userId)) {
                 //onFail(-1, getString(R.string.data_error))
                 return@startCoroutine
             }
             IMModel.loginNew(info.huanxin!!.user_id!!,
                 info.huanxin!!.password!!, object : LoginCallback {
                     override fun onSuccess() {
                         progressDialog.dismiss()
                         LoginModel.onLoginSuccess(this@FacebookLoginActivity, "FB", info)
                         finish()
                     }

                     override fun onError(code: Int, msg: String?) {
                         //onFail(code, msg)
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
