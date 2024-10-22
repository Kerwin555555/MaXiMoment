package com.moment.app.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.blankj.utilcode.util.LogUtils

object DialogUtils {
    @JvmOverloads
    fun show(context: Context?, fragment: DialogFragment, tag: String? = fragment.tag): Boolean {
        val activity = getActivity(context)
        if (activity is AppCompatActivity) {
            val compatActivity = activity
            if (!compatActivity.isFinishing) {
                try {
                    fragment.show(compatActivity.supportFragmentManager, tag)
                    return true
                } catch (var6: Exception) {
                    val e = var6
                    LogUtils.d("DialogUtils", e)
                }
            } else {
                LogUtils.d("DialogUtils", "show false :activity is null")
            }
        }

        return false
    }

    @JvmOverloads
    fun showNow(context: Context?, fragment: DialogFragment, tag: String? = fragment.tag): Boolean {
        val activity = getActivity(context)
        if (activity is AppCompatActivity) {
            val compatActivity = activity
            if (!compatActivity.isFinishing) {
                try {
                    fragment.showNow(compatActivity.supportFragmentManager, tag)
                    return true
                } catch (var6: Exception) {
                    val e = var6
                    LogUtils.d("DialogUtils", e)
                }
            } else {
                LogUtils.d("DialogUtils", "show false :activity is null")
            }
        }

        return false
    }

    private fun getActivity(cont: Context?): Activity? {
        return if (cont == null) {
            null
        } else if (cont is Activity) {
            cont
        } else {
            if (cont is ContextWrapper) getActivity(cont.baseContext) else null
        }
    }
}