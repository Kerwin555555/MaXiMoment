package com.moment.app.utils

import android.text.TextUtils
import com.moment.app.BuildConfig

object MomentCoreParams {
    const val ONLINE_URL: String = "http://106.14.221.125:8080/"

    const val TEST_URL: String = "http://106.14.221.125:8080/"

    const val OSS: String = "http://oss.moment.com/"
    const val BS_CDN: String = "https://baishan.moment.com/"

    var IMAGE_URL: String = "https://moment-test.oss-cn-hongkong.aliyuncs.com/"
    var IMAGE_URL_LIT: String = BS_CDN + "api/sns/v1/lit/simage/"
    var AUDIO_URL: String = BS_CDN + "api/sns/v1/lit/audio/"
    var VIDEO_URL: String = BS_CDN + "api/sns/v1/lit/video/"
    var AVATAR_URL: String = BS_CDN + "api/sns/v1/lit/avatar_image/"

    var ZIP_URL: String = BS_CDN + "api/sns/v1/lit/oss/zip/"

    var SHARE_APP_LINK: String = "http://www.litmatchapp.com/"

    var BEFORMAL_URL: String = "http://bformal.moment.com/"

    var BASE_URL: String =
        if (TextUtils.isEmpty(BuildConfig.BASE_URL)) ONLINE_URL else BuildConfig.BASE_URL

    var BASE_H5_ACTIVITY_URL: String = "https://activity.static.moment.com/"

    // 数美SDK用的域名
    var SHUMEI_TEST: String = "http://testshumei.moment.com"
    var SHUMEI_ONLINE: String = "http://shumei.moment.com"

    var SHUMEI_URL: String = SHUMEI_ONLINE

    var REQ_BODY_ENCRYPT: Boolean = false
    var HTTP_NO_PROXY: Boolean = false

    fun resetImageUrl(baseUrl: String) {
        IMAGE_URL = baseUrl + "api/sns/v1/lit/image/"
        AUDIO_URL = baseUrl + "api/sns/v1/lit/audio/"
        IMAGE_URL_LIT = baseUrl + "api/sns/v1/lit/simage/"
        VIDEO_URL = baseUrl + "api/sns/v1/lit/video/"
        ZIP_URL = baseUrl + "api/sns/v1/lit/oss/zip/"
        AVATAR_URL = baseUrl + "api/sns/v1/lit/avatar_image/"
    }

    val isDev: Boolean
        get() = TextUtils.equals(BASE_URL, TEST_URL)

    val isRelease: Boolean
        get() {
            if (!BuildConfig.DEBUG) {
                return true
            }
            return TextUtils.equals(BASE_URL, ONLINE_URL) || BASE_URL.contains("formal")
        }

    var MAX_TIME: Int = Int.MAX_VALUE

    const val GENDER_GIRL: String = "girl"
    const val GENDER_BOY: String = "boy"

    const val CODA_TEST_URL: String = "https://sandbox.codapayments.com/airtime/begin"
    const val CODA_PRO_URL: String = "https://airtime.codapayments.com/airtime/begin"

    const val KEY_UUID: String = "CB7F786FC0E6E105E6DA03D1FFF05C0F"
    const val KEY_IMAGE: String = "AC0A60D491D9876D1012FB24DB61ADC6"
}