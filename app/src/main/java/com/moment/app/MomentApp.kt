package com.moment.app

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.didi.drouter.api.DRouter
import com.moment.app.models.IMModel
import com.moment.app.models.LoginModel
import com.moment.app.utils.AppInfo
import com.tencent.mmkv.MMKV
import dagger.hilt.android.HiltAndroidApp
import internal.com.getkeepsafe.relinker.ReLinker


@HiltAndroidApp
class MomentApp : Application() {
    @SuppressLint("StaticFieldLeak")
    companion object {
        lateinit var appContext: Context
    }

    override fun onCreate() {
        super.onCreate()
        DRouter.init(this)
        appContext = this

        AppInfo.init(this)
        IMModel.initIM(this)

        LoginModel.getUserInfo()
//
//        MMKV.initialize(
//            this
//        ) { libName -> ReLinker.loadLibrary(this, libName) }
    }
}