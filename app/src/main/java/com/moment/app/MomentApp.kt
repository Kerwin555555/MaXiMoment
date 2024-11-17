package com.moment.app

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.bumptech.glide.Glide
import com.didi.drouter.api.DRouter
import com.moment.app.datamodel.BackendException
import com.moment.app.hilt.app_level.MockData
import com.moment.app.login_page.service.LoginService
import com.moment.app.models.UserIMManagerBus
import com.moment.app.models.UserImManager
import com.moment.app.models.UserLoginManager
import com.moment.app.network.NetErrorHandler
import com.moment.app.network.NetErrorHandler.OnApiErrorListener
import com.moment.app.utils.AppInfo
import com.moment.app.utils.emoji
import com.tencent.mmkv.MMKV
import dagger.hilt.android.HiltAndroidApp
import okhttp3.Request
import javax.inject.Inject


@HiltAndroidApp
class MomentApp : Application() {
    @SuppressLint("StaticFieldLeak")
    companion object {
        lateinit var appContext: Context
    }

    @Inject
    @MockData
    lateinit var imLoginModel: UserIMManagerBus

    @Inject
    lateinit var loginService: LoginService

    override fun onCreate() {
        super.onCreate()
        DRouter.init(this)
        appContext = this

        MMKV.initialize(this)
        AppInfo.init(this)
        imLoginModel.init(this)

        UserLoginManager.getUserInfo()
        emoji()
        NetErrorHandler.setListener(object : OnApiErrorListener{
            override fun onApiError(r: Request?, t: Throwable?) {
                when (t) {
                    is BackendException -> {
                        if (t.errorCode == 401) {
                            UserLoginManager.logout(true, loginService = loginService)
                        }
                    } else -> {

                    }
                }
            }
        })
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Glide.with(this).onLowMemory()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            Glide.get(this).clearMemory()
        } else {
            Glide.with(this).onTrimMemory(level)
        }
    }
}