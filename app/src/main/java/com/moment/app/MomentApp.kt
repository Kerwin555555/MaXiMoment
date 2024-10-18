package com.moment.app

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.didi.drouter.api.DRouter


class MomentApp : Application() {
    @SuppressLint("StaticFieldLeak")
    companion object {
        lateinit var appContext: Context
    }
    override fun onCreate() {
        super.onCreate()
        DRouter.init(this)
        appContext = this
    }
}