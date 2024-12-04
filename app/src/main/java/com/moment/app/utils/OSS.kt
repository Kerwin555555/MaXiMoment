package com.moment.app.utils

import android.util.Log
import com.alibaba.sdk.android.oss.ClientConfiguration
import com.alibaba.sdk.android.oss.OSS
import com.alibaba.sdk.android.oss.OSSClient
import com.alibaba.sdk.android.oss.common.auth.OSSFederationCredentialProvider
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken
import com.moment.app.MomentApp
import com.moment.app.datamodel.Results
import com.moment.app.login_page.service.LoginService
import com.moment.app.models.UserLoginManager
import retrofit2.Response


class MomentOSSDelegate(val loginService: LoginService) {

    private val OSS_URL = "http://oss-cn-hongkong.aliyuncs.com"
    private val TAG = "MomentOSSDelegate"

    private var oss: OSS? = null
    private var filePrefix: String? = null
    private var config = ClientConfiguration().also {
        it.connectionTimeout = 15 * 1000 // 连接超时，默认15秒。
        it.socketTimeout = 15 * 1000 // socket超时，默认15秒。
        it.maxConcurrentRequest = 10 // 最大并发请求数，默认5个。
        it.maxErrorRetry = 2 // 失败后最大重试次数，默认2次。
    }

    fun prepare() {
        if (oss != null) {
            return
        }
        oss = createOssService()
    }

    private fun createOssService(): OSS {
        //config = ClientConfiguration.getDefaultConf()
        val ossClient = OSSClient(MomentApp.appContext,
            OSS_URL,
            object : OSSFederationCredentialProvider() {
                override fun getFederationToken(): OSSFederationToken? {
                    return try {
                        val response : Response<Results<OSSToken>>? =
                            loginService.getOssToken()?.execute()
                        if (response != null && response.isSuccessful && response.body()?.success() == true && response.body()?.data != null) {
                            val token = response.body()?.data?.token as Token
                            return OSSFederationToken(
                                token.access_key_id,
                                token.access_key_secret,
                                token.security_token,
                                token.expiration
                            )
                        }
                        null
                    } catch (e: Exception) {
                        clear()
                        null
                    }
                }
            }, config)
        return ossClient
    }

    fun clear() {
        oss = null
    }

    fun getOSS(): OSS? {
        if (oss == null) {
            oss = createOssService()
        }
        return oss
    }

    fun getFilePrefix(): String? {
        return UserLoginManager.getUserId() + "_"
    }

    class OSSToken: BaseBean() {
        var prefix: String? = ""
        var token: Token? = null
    }

    class Token: BaseBean() {
        var access_key_id: String? = null
        var access_key_secret: String? = null
        var security_token : String? = null
        var request_id: String? = null
        var expiration: Long = 0
    }


}