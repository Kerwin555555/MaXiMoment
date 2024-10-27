package com.moment.app.login_page

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.os.Bundle
import android.text.TextUtils
import com.blankj.utilcode.util.LogUtils
import com.didi.drouter.annotation.Router
import com.didi.drouter.api.DRouter
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.moment.app.datamodel.UserInfo
import com.moment.app.eventbus.LogCancelEvent
import com.moment.app.hilt.app_level.MockData
import com.moment.app.login_page.service.LoginService
import com.moment.app.models.IMLoginModel
import com.moment.app.models.LoginModel
import com.moment.app.network.startCoroutine
import com.moment.app.network.toast
import com.moment.app.utils.BaseActivity
import com.moment.app.utils.ProgressDialog
import com.moment.app.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject


@Router(scheme = ".*", host = ".*", path = "/login/google")
@AndroidEntryPoint
class GoogleLoginActivity : BaseActivity() {
    var mGoogleSignInClient: GoogleSignInClient? = null

    @Inject
    @MockData
    lateinit var loginService: LoginService

    @Inject
    @MockData
    lateinit var imLoginModel: IMLoginModel

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(SERVER_CLIENT_ID)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        //if (Gsr.INSTANCE.getInt("googleLoginMode", 0) === 1) {
           // newSignIn()
       // } else {
            signIn()
        //}
    }

    private fun newSignIn() {
        val request =
            GetSignInIntentRequest.builder()
                .setServerClientId(SERVER_CLIENT_ID)
                .build()

        Identity.getSignInClient(this)
            .getSignInIntent(request)
            .addOnSuccessListener { result: PendingIntent ->
                try {
                    startIntentSenderForResult(
                        result.intentSender,
                        RC_SIGN_IN_NEW,  /* fillInIntent= */
                        null,  /* flagsMask= */
                        0,  /* flagsValue= */
                        0,  /* extraFlags= */
                        0,  /* options= */
                        null
                    )
                } catch (e: SendIntentException) {
                    signIn()
                    LogUtils.e(TAG, "Google Sign-in failed")
                }
            }
            .addOnFailureListener { e: Exception? ->
                signIn()
                LogUtils.e(TAG, e)
            }
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        } else if (requestCode == RC_SIGN_IN_NEW) {
            if (resultCode != RESULT_OK) {
                "Login Cancel[0]".toast()
                finish()
                return
            }
            try {
                val credential = Identity.getSignInClient(
                    this
                ).getSignInCredentialFromIntent(data)
                // Signed in successfully - show authenticated UI
                handleLogin(credential.googleIdToken)
            } catch (e: ApiException) {
                // The ApiException status code indicates the detailed failure reason.
                "Login Error[" + e.statusCode + "]".toast()
                finish()
            }
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(
                ApiException::class.java
            )

            if (TextUtils.isEmpty(account.idToken)) {
                // The ApiException status code indicates the detailed failure reason.
                // Please refer to the GoogleSignInStatusCodes class reference for more information.
                LogUtils.w("GoogleLogin", "token is null")
                // updateUI(null);
                "Login Error![300]".toast()
                finish()
                return
            }

            // Signed in successfully, show authenticated UI.
            // updateUI(account);
            handleLogin(account.idToken!!)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            LogUtils.d("GoogleLogin", "signInResult:failed code=" + e.statusCode)
            // updateUI(null);
            if (e.statusCode != 12501) {
                ("Login Error!" + e.statusCode).toast()
            }
            finish()
        }
    }

    private fun handleLogin(token: String) {
        val progressDialog = ProgressDialog.show(this)
        progressDialog.isCancelable = false
        startCoroutine({
             val map = mutableMapOf("token" to token)
             val result = loginService.googleLogin(map)
            val info: UserInfo? = result.data
            if (info == null || TextUtils.isEmpty(info.userId)) {
                //onFail(-1, getString(R.string.data_error))
                return@startCoroutine
            }
            imLoginModel.loginNew(info.huanxin!!.user_id!!,
                info.huanxin!!.password!!, object : LoginCallback {
                    override fun onSuccess() {
                        progressDialog.dismiss()
                        LoginModel.onLoginSuccess(this@GoogleLoginActivity, "GO", info)
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


    override fun onDestroy() {
        super.onDestroy()
        if (!LoginModel.isLogin()) {
            EventBus.getDefault().post(LogCancelEvent())
        }
    }

    companion object {
        private const val TAG = "GoogleLoginActivity"

        private const val RC_SIGN_IN = 100
        private const val RC_SIGN_IN_NEW = 101
        private const val SERVER_CLIENT_ID =
            "72687572250-i5659eubkl38ck9n17mrijl0neh7rgkc.apps.googleusercontent.com"

        fun start(context: Context?) {
            DRouter.build("/login/google").start(context)
        }
    }
}
