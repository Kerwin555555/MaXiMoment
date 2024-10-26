package com.moment.app.utils

import android.content.Context
import com.moment.app.MomentApp
import com.tencent.mmkv.MMKV

object SPUtil {
    private var sp: MMKV? = null
    fun initSP(context: Context?) {
        if (sp == null) {
            kotlin.runCatching {
                sp = MMKV.mmkvWithID("moment_app_default_kv")
            }.onFailure {
                MMKV.initialize(context)
                sp = MMKV.mmkvWithID("moment_app_default_kv")
            }
        }
    }

    fun checkSP() {
        if (sp == null) {
            initSP(MomentApp.appContext)
        }
    }

    fun getBoolean(key: String?, value: Boolean): Boolean {
        checkSP()
        return sp!!.getBoolean(key, value)
    }

    fun getInt(key: String?, defValue: Int): Int {
        checkSP()
        return sp!!.getInt(key, defValue)
    }

    fun getString(key: String?, defValue: String?): String? {
        checkSP()
        return sp!!.getString(key, defValue)
    }

    fun getLong(key: String?, defValue: Long): Long {
        checkSP()
        return sp!!.getLong(key, defValue)
    }

    fun save(key: String?, value: Boolean) {
        checkSP()
        sp!!.edit().putBoolean(key, value).apply()
    }

    fun save(key: String?, value: Int) {
        checkSP()
        sp!!.edit().putInt(key, value).apply()
    }

    fun save(key: String?, value: Long) {
        checkSP()
        sp!!.edit().putLong(key, value).apply()
    }

    fun save(key: String?, value: Float) {
        checkSP()
        sp!!.edit().putFloat(key, value).apply()
    }

    fun save(key: String?, value: String?) {
        checkSP()
        sp!!.edit().putString(key, value).apply()
    }

    fun remove(key: String?) {
        checkSP()
        sp!!.edit().remove(key).apply()
    }

    fun getFloat(key: String?): Float {
        checkSP()
        return sp!!.getFloat(key, 0f)
    }

    fun save(key: String?, value: Double) {
        checkSP()
        sp!!.edit().putString(key, value.toString()).apply()
    }

    fun getDouble(key: String?): Double {
        checkSP()
        var value = 0.0
        try {
            val string = sp!!.getString(key, "0")
            value = string!!.toDouble()
        } catch (e: Exception) {
//            e.printStackTrace();
        }
        return value
    }
}
