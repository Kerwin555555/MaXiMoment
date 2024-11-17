package com.moment.app

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import com.didi.drouter.api.DRouter
import com.didi.drouter.api.Extend
import com.moment.app.databinding.ActivitySplashBinding
import com.moment.app.datamodel.FINISHED_INFO
import com.moment.app.eventbus.LoginEvent
import com.moment.app.hilt.app_level.MockData
import com.moment.app.login_page.service.LoginService
import com.moment.app.models.AppConfigManager
import com.moment.app.models.UserLoginManager
import com.moment.app.utils.BaseActivity
import com.moment.app.utils.MOMENT_APP
import com.moment.app.utils.sntp.SntpClock
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

//https://github.com/didi/DRouter/wiki/1.-Router

@AndroidEntryPoint
class SplashActivity : BaseActivity(){

    private lateinit var binding: ActivitySplashBinding
    private var loginDialog: MomentEntryFragment? = null

    @Inject
    @MockData
    lateinit var loginService: LoginService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (!isTaskRoot && intent.hasCategory(Intent.CATEGORY_LAUNCHER)
            &&  intent.action == Intent.ACTION_MAIN) {
            if (!intent.getStringExtra("route").isNullOrEmpty()) {
                jump()
                return
            }
            finish()
            return
        }

        val data = intent.data
        if (data?.toString()?.contains("moment") == true) {
            jump()
            return
        }
        EventBus.getDefault().register(this)
        SntpClock.syncTime(MomentApp.appContext)

        if (!startFullscreenAd()) {
            jump()
            return
        }
        //ads and debug logic here
    }

    private fun startFullscreenAd() : Boolean{
        return false
    }

    private fun jump() {
        if (UserLoginManager.isLogin() && UserLoginManager.getUserInfo()?.register_status == FINISHED_INFO) {
            var intent: Intent? = null
                /*跳转主页*/
            intent = Intent(this@SplashActivity, MainActivity::class.java)
            if (getIntent().extras != null) {
                intent.putExtras(getIntent().extras!!)
            }
            intent.setData(getIntent().data)
            DRouter.build("/main")
                .putExtra(Extend.START_ACTIVITY_VIA_INTENT, intent)
                .start()

            var loc = "EN"
            if (UserLoginManager.getUserInfo() != null
                && !UserLoginManager.getUserInfo()?.country.isNullOrEmpty()) {
                loc = UserLoginManager.getUserInfo()?.country!!
            }

            //LitString.getInstance().checkResources(loc, Arrays.asList(RR.allStrings().clone()))

            this@SplashActivity.finish()
            overridePendingTransition(0, 0)
        } else {
            Log.e(MOMENT_APP, ""+UserLoginManager.isLogin())
            if (UserLoginManager.isLogin()) {
                UserLoginManager.logout(true, loginService = loginService)
            }
            loginDialog = MomentEntryFragment().apply {
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.login_root, this)
                    .commitAllowingStateLoss()
            }
            AppConfigManager.updateConfig()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }


    @Subscribe
    fun onLogin(event: LoginEvent) {
        //GooglePaymentManager.getInstance().refreshDiamondProducts()
        finish()
    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}