package com.moment.app.utils

import android.app.Activity
import com.blankj.utilcode.util.ActivityUtils
import com.moment.app.MainActivity
import java.lang.ref.WeakReference

object ActivityHolder {

    var mainRef: WeakReference<MainActivity>? = null


    fun mainActivity(): MainActivity? {
        if (mainRef == null) {
            return null
        }
        return mainRef!!.get()
    }

    fun onMainCreate(mainActivity: MainActivity) {
        mainRef = WeakReference<MainActivity>(mainActivity)
    }

    fun getCurrentActivity(): BaseActivity? {
        val activity: Activity = ActivityUtils.getTopActivity()
        if (activity is BaseActivity) {
            return activity
        }
        return null
    }
}
