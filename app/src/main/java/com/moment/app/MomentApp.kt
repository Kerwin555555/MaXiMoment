package com.moment.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class MomentApp : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}