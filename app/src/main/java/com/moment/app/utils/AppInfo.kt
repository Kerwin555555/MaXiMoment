package com.moment.app.utils

import android.content.Context
import android.os.Build
import android.util.Pair
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.LanguageUtils
import com.moment.app.utils.MachineUUIDManager.MachineUUIDManager
import java.util.Locale

object AppInfo {
    var versionName: String? = null
    var versionCode: Long = 0
    var uuid: String? = null
    var countryId: String? = null
    var lang: String? = null
    var model: String? = null
    var loctype: Int = 0
    fun init(context: Context) {
        try {
            val pInfo = AppUtils.getAppInfo()
            versionName = pInfo?.versionName ?: ""
            versionCode = pInfo?.versionCode?.toLong() ?: 0L

            model = Build.MODEL
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        try {
            uuid = MachineUUIDManager(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val countryPair: Pair<Int, String> = CommonUtil.getCountryCode()
        countryId = countryPair.second
        loctype = countryPair.first
        val locale: Locale = LanguageUtils.getSystemLanguage()
        lang = if (locale != null) locale.language else "en"
    }


    fun getLocString(): String {
        var locale = LanguageUtils.getAppliedLanguage()
        if (locale == null) {
            locale = LanguageUtils.getSystemLanguage()
        }
        return getCounty(locale)
    }

    fun getCounty(locale: Locale):String {
        return when(locale.language) {
            "en" -> "EN"
            "th" -> "TH"
            "vi" -> "VN"
            "id", "in" -> "ID"
            "zh" -> "TW"
            "ar" -> "AE"
            "es" -> "AR"
            "pt" -> "BR"
            "ru" -> "RU"
            "tr" -> "TR"
            "ja" -> "JP"
            else -> if (locale.country.isNotEmpty()) locale.country.toString().toUpperCase() else "EN"
        }
    }
}