package com.moment.app.login_page

import android.os.Bundle
import android.widget.Toast
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
import com.moment.app.eventbus.LogCancelEvent
import com.moment.app.utils.BaseActivity
import org.greenrobot.eventbus.EventBus


@Router(scheme = ".*", host = ".*", path = "/login/facebook")
class FacebookLoginActivity : BaseActivity() {
    private lateinit var binding: FacebookLoginActivityBinding
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ImmersionBar.with(this)
            .statusBarDarkFont(false)
            .init()
        binding = FacebookLoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())
        initFacebookLogin()
        val accessToken = AccessToken.getCurrentAccessToken()
        val isLoggedIn = accessToken != null && !accessToken.isExpired
            //login(accessToken)

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
                login(loginResult.accessToken)
            }
        })
    }

    private fun login(accessToken: AccessToken) {

    }
}
