package com.moment.app

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager.DecorView
import com.didi.drouter.api.DRouter
import com.didi.drouter.api.Extend
import com.gyf.immersionbar.ImmersionBar
import com.moment.app.databinding.ActivitySplashBinding
import com.moment.app.eventbus.AEvent
import com.moment.app.models.ConfigModel
import com.moment.app.models.LoginModel
import com.moment.app.user_profile.ProfileActivity
import com.moment.app.utils.BaseActivity
import com.moment.app.utils.sntp.SntpClock
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

//https://github.com/didi/DRouter/wiki/1.-Router
class SplashActivity : BaseActivity(){

    private lateinit var binding: ActivitySplashBinding
    private var loginDialog: LoginDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root
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
        if (LoginModel.isLogin() && LoginModel.getUserInfo()?.finished_info == true) {
            var intent: Intent? = null
            if (LoginModel.getUserInfo()?.finished_info == true) {
                /*跳转主页*/
                intent = Intent(this@SplashActivity, MainActivity::class.java)
                if (getIntent().extras != null) {
                    intent.putExtras(getIntent().extras!!)
                }
                intent.setData(getIntent().data)
                DRouter.build("/main")
                    .putExtra(Extend.START_ACTIVITY_VIA_INTENT, intent)
                    .start()
            } else {
                intent = Intent(this@SplashActivity, ProfileActivity::class.java)
                if (getIntent().extras != null) {
                    intent.putExtras(getIntent().extras!!)
                }
                intent.setData(getIntent().data)
                DRouter.build("/user/init")
                    .putExtra(Extend.START_ACTIVITY_VIA_INTENT, intent)
                    .start()
            }

            var loc = "EN"
            if (LoginModel.getUserInfo() != null
                && !LoginModel.getUserInfo()?.country.isNullOrEmpty()) {
                loc = LoginModel.getUserInfo()?.country!!
            }

            //LitString.getInstance().checkResources(loc, Arrays.asList(RR.allStrings().clone()))

            this@SplashActivity.finish()
            overridePendingTransition(0, 0)
        } else {
            if (LoginModel.isLogin()) {
                val forbidSid: String? = LoginModel.forbidden_session
                //登录但是有完善信息
                LoginModel.logout(true)
                if (!TextUtils.isEmpty(forbidSid)) {
                    LoginModel.forbidden_session = forbidSid
                }
            }
            loginDialog = LoginDialog().apply {
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.login_root, this)
                    .commitAllowingStateLoss()
            }

            ConfigModel.updateConfig()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe
    fun onEvent(event: AEvent) {

    }
}