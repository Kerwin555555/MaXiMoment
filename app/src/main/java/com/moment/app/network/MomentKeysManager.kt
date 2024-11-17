package com.moment.app.network

import android.text.TextUtils
import com.moment.app.datamodel.UserInfo
import com.moment.app.utils.BaseBean
import com.moment.app.utils.SerializeManager
import com.tencent.mmkv.MMKV
//
object MomentKeysManager {

    const val SAVE_KEY = "moment_key_info"

    private var keyInfo: KeyInfo? = null

    private val mmkv by lazy {
        MMKV.defaultMMKV()
    }

    fun saveMMKVSessionAndHuanxinPasswordIfNeed(info: UserInfo) {
        val session = info.session
        val hxPwd = info.huanxin?.password ?: ""

        info.session = ""
        info.huanxin?.password = ""

        if (session.isNullOrEmpty() || session == getSession()) {
            return
        }
        keyInfo = KeyInfo(session, hxPwd)
        mmkv.putString(SAVE_KEY, SerializeManager.toJson(keyInfo))
    }

    fun getSessionOrForbid(): String {
        val session = getSession()
        if (TextUtils.isEmpty(session)) {
            return ""
        }
        return session
    }

    fun getSession(): String {
        ensureKeyInfo()
        return keyInfo?.session ?: ""
    }

    fun getHXPwd(): String {
        ensureKeyInfo()
        return keyInfo?.hxPwd ?: ""
    }

    fun clear() {
        mmkv.remove(SAVE_KEY)
        this.keyInfo = null
    }

    private fun ensureKeyInfo() {
        if (keyInfo == null) {
            if (mmkv.containsKey(SAVE_KEY)) {
                val json = mmkv.getString(SAVE_KEY, "")
                if (!TextUtils.isEmpty(json)) {
                    keyInfo = SerializeManager.parse(json, KeyInfo::class.java)
                }
            }
        }
    }

}

data class KeyInfo(val session: String, val hxPwd: String) : BaseBean() {}