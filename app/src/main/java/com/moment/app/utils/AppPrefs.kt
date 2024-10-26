package com.moment.app.utils

import android.text.TextUtils
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.moment.app.datamodel.UserInfo
import com.moment.app.models.LoginModel
import com.moment.app.models.MomentConfig


object AppPrefs {
    private const val SP_TIME_OFFSET = "sp_time_offset"
    private const val SP_USER = "sp_user_info"
    private const val SP_USER_SIFT = "sp_user_sift_%s"
    private const val SP_RATE_KEY = "sp_rate_5_start"
    private const val SP_YOUTUBE_WEB = "sp_youtube_web"
    private const val SP_MOMENT_CONFIG = "sp_moment_config"
    private const val SP_MOMENT_IM_WARN_CONFIG = "sp_moment_im_warn_config"
    private const val SP_MOMENT_REPORT_CONFIG = "sp_moment_report_config"
    private const val SP_PRIVACY_SETTINGS = "sp_privacy_settings"
    private const val SP_MOMENT_VOICE_MATCH_REPORT_CONFIG = "sp_moment_voice_match_report_config"
    private const val SP_LASTED_FOLLOWING_FEED = "sp_lasted_following_feed"
    private const val SP_SHOW_PUSH_VIEW_DATE = "sp_show_push_view_date"
    private const val SP_COPY_DIALOG_SHOW = "sp_copyLink_dialog"
    private const val SP_ACCOUNT_INFO = "sp_account_info"
    private const val SP_COMMIT_HINT = "sp_commit_hit"
    private const val SP_FEED_RULE_HINT = "sp_feed_rule_hint_time"
    private const val SP_LOGIN_TYPE = "sp_feed_login_type"
    private const val SP_OSS_UPLOAD_PREFIX = "sp_oss_upload_prefix"

    val timeOffset: Long
        get() = SPUtil.getLong(SP_TIME_OFFSET, 0)

    fun saveTimeOffset(offset: Long) {
        SPUtil.save(SP_TIME_OFFSET, offset)
    }

    fun saveUserInfo(info: UserInfo?) {
        SPUtil.save(SP_USER, if (info == null) "" else JsonUtil.toJson(info))
    }

    fun getUserInfo(): UserInfo? {
        val json: String = SPUtil.getString(SP_USER, "") ?: ""
        if (android.text.TextUtils.isEmpty(json)) {
            return null
        }
        return JsonUtil.parse(json, UserInfo::class.java)
    }

    fun isNeedLogin(): Boolean {
        return true
    }

    fun setSift(sift: UserSift?) {
        if (!LoginModel.isLogin()) return
        val siftKey = java.lang.String.format(SP_USER_SIFT, LoginModel.getUserId())
        SPUtil.save(siftKey, Gson().toJson(sift))
    }

    fun getSift(): UserSift? {
        if (!LoginModel.isLogin()) return null

        val siftKey = java.lang.String.format(SP_USER_SIFT, LoginModel.getUserId())

        val json = SPUtil.getString(siftKey, "")
        if (TextUtils.isEmpty(json)) {
            return null
        }
        return JsonUtil.parse(json, UserSift::class.java)
    }

    fun saveRateState(state: Int) {
        SPUtil.save(SP_RATE_KEY, state)
    }

    val rateState: Int
        get() = SPUtil.getInt(SP_RATE_KEY, 0)

    fun saveConfig(config: MomentConfig?) {
        SPUtil.save(SP_MOMENT_CONFIG, JsonUtil.toJson(config))
    }

    fun getConfig(): MomentConfig? {
        val json = SPUtil.getString(SP_MOMENT_CONFIG, "")
        if (TextUtils.isEmpty(json)) {
            return MomentConfig()
        }
        return JsonUtil.parse(json, MomentConfig::class.java)
    }

    fun saveReportConfig(config: ReportSettings?) {
        SPUtil.save(SP_MOMENT_REPORT_CONFIG, Gson().toJson(config))
    }

    val reportConfig: ReportSettings
        get() {
            val json: String = SPUtil.getString(SP_MOMENT_REPORT_CONFIG, "") ?: ""
            if (android.text.TextUtils.isEmpty(json)) {
                return ReportSettings()
            }
            return Gson().fromJson(json, ReportSettings::class.java)
        }

    fun savePrivacySettings(setting: PrivacySetting?) {
        SPUtil.save(SP_PRIVACY_SETTINGS, Gson().toJson(setting))
    }

    val privacySetting: PrivacySetting
        get() {
            val json: String = SPUtil.getString(SP_PRIVACY_SETTINGS, "")?: ""
            if (android.text.TextUtils.isEmpty(json)) {
                return PrivacySetting()
            }
            return Gson().fromJson(json, PrivacySetting::class.java)
        }

    fun saveVoiceMatchConfig(config: ReportSettings?) {
        SPUtil.save(SP_MOMENT_VOICE_MATCH_REPORT_CONFIG, Gson().toJson(config))
    }

    val voiceMatchConfig: ReportSettings
        get() {
            val json: String = SPUtil.getString(SP_MOMENT_VOICE_MATCH_REPORT_CONFIG, "") ?: ""

            if (android.text.TextUtils.isEmpty(json)) {
                return ReportSettings()
            }
            return Gson().fromJson(json, ReportSettings::class.java)
        }

    fun saveImWarnConfig(config: ImWarnSetting?) {
        SPUtil.save(SP_MOMENT_IM_WARN_CONFIG, Gson().toJson(config))
    }

    fun getimWarnConfig(): ImWarnSetting {
            val json: String = SPUtil.getString(SP_MOMENT_IM_WARN_CONFIG, "") ?: ""
            if (android.text.TextUtils.isEmpty(json)) {
                return ImWarnSetting()
            }
            return Gson().fromJson(json, ImWarnSetting::class.java)
        }

    fun saveLastedFollowingFeed(create_time: Long) {
        SPUtil.save(SP_LASTED_FOLLOWING_FEED, create_time)
    }

    val lastedFollowingFeed: Long
        get() = SPUtil.getLong(SP_LASTED_FOLLOWING_FEED, 0)

    var showPushViewDate: Long
        get() = SPUtil.getLong(SP_SHOW_PUSH_VIEW_DATE, 0)
        set(date) {
            SPUtil.save(SP_SHOW_PUSH_VIEW_DATE, date)
        }

    fun saveCopyLinkDialog(lastTime: Long) {
        SPUtil.save(SP_COPY_DIALOG_SHOW, lastTime)
    }

    val copyLinkDialogShowTime: Long
        get() = SPUtil.getLong(SP_COPY_DIALOG_SHOW, 0)

    fun getAccountInfo(): AccountInfo {
        val json: String = SPUtil.getString(SP_ACCOUNT_INFO, "") ?: ""
        if (!android.text.TextUtils.isEmpty(json)) {
            try {
                return Gson().fromJson(json, AccountInfo::class.java)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        return AccountInfo()
    }

    fun setAccountInfo(info: AccountInfo?) {
        if (info == null) {
            SPUtil.remove(SP_ACCOUNT_INFO)
        } else {
            SPUtil.save(SP_ACCOUNT_INFO, Gson().toJson(info))
        }
    }

    fun setSpCommitHint(hint: Boolean) {
        SPUtil.save(SP_COMMIT_HINT, hint)
    }

    val commitHint: Boolean
        get() = SPUtil.getBoolean(SP_COMMIT_HINT, false)

    fun setSpFeedRuleHint(time: Long) {
        SPUtil.save(SP_FEED_RULE_HINT, time)
    }

    val feedRuleHint: Long
        get() = SPUtil.getLong(SP_FEED_RULE_HINT, 0)

    fun saveLoginType(type: String?) {
        SPUtil.save(SP_LOGIN_TYPE, type)
    }

    fun getLoginType(): String {
        return SPUtil.getString(SP_LOGIN_TYPE, "") ?: ""
    }

    fun saveOssUploadPrefix(prefix: String?) {
        SPUtil.save(SP_OSS_UPLOAD_PREFIX, prefix)
    }

}

class UserSift : BaseBean() {
    var age_low: Int = 13
    var age_high: Int = 20
    var gender: String? = ""

    var filter_max: Int = 0
    var filter_min: Int = 0

    var prefer: Int = 0 //0未知的 1 make friends 2 game 3 voice 4 love
    var allow_push_invitation: Boolean = false

    val filterAge: String
        get() = "$age_low-$age_high"

    /**
     *
     * @return gender 格式化后的内容
     */
    @JvmName("getGenderExplicit")
    fun getGender(): String? {
        if (gender == null) {
            return ""
        }
        if ("-1" == gender) {
            return "both"
        }
        return gender
    }
}

class ReportSettings : BaseBean() {
    var remark_limit: Int = 0
    var reasons: List<Map<String, String>>? = null
    var preface: String? = null
    var preface2: String? = null

    fun getKey(index: Int): String {
        if (index < 0 || index >= reasons!!.size || reasons!![index].size == 0) {
            return ""
        }
        return reasons!![index].keys.iterator().next()
    }

    fun getValue(index: Int): String {
        if (index < 0 || index >= reasons!!.size || reasons!![index].size == 0) {
            return ""
        }
        return reasons!![index].entries.iterator().next().value
    }
}

class AccountInfo : BaseBean() {
    /**
     * diamonds : 84
     * video_member_time : 0
     */
    var diamonds: Long = 0
    var video_member_time: Int = 0
    var match_membership_time: Int = 0
    var is_first_charge_diamonds: Boolean = false
}

class ImWarnSetting: BaseBean() {
    @SerializedName("child_im_tip")
    var childImTip = ""

    @SerializedName("adult_im_tip")
    var adultImTip = ""

    @SerializedName("default_show")
    var defaultShow = ""
}

class PrivacySetting : BaseBean() {
    var show_active: Boolean = true // 是否展示自己活跃状态
    var show_party_active = true // 是否展示自己在pary里的活跃状态
    //var accost_limit_gift: Gift? = null // 已经选的礼物，别人打招呼时需要送该礼物
    var allow_everyone_accost: Boolean = true // 其他人是否可以免费打招呼
    var charm_level_threshold = 4 // 魅力等级阈值，默认4，超过阈值才可以关闭免费搭讪
    var charm_level_icon: String? = null // 魅力等级阈值icon
    var accosted_gift_func_diamonds = 2000 // 关闭免费搭讪功能钻石费用，默认2000钻
    var bought_accost_limit_func = false // 关闭免费搭讪功能是否已购买

}



