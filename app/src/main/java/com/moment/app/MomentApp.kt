package com.moment.app

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.didi.drouter.api.DRouter
import com.moment.app.hilt.app_level.MockData
import com.moment.app.models.UserImManager
import com.moment.app.models.UserLoginManager
import com.moment.app.utils.AppInfo
import com.moment.app.utils.emoji
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject


@HiltAndroidApp
class MomentApp : Application() {
    @SuppressLint("StaticFieldLeak")
    companion object {
        lateinit var appContext: Context
    }

    @Inject
    @MockData
    lateinit var imLoginModel: UserImManager

    override fun onCreate() {
        super.onCreate()
        DRouter.init(this)
        appContext = this

        AppInfo.init(this)
        imLoginModel.initIM(this)

        UserLoginManager.getUserInfo()
        emoji()
    }


}