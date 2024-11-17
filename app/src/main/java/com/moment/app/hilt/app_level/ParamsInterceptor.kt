package com.moment.app.hilt.app_level

import android.os.Build
import android.text.TextUtils
import com.moment.app.network.MomentKeysManager
import com.moment.app.utils.AppInfo
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class ParamsInterceptor : Interceptor{
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val requestBuilder: Request.Builder = request.newBuilder()

        val queryBuilder: HttpUrl.Builder = request.url.newBuilder()
        val session: String = MomentKeysManager.getSessionOrForbid()
        if (!TextUtils.isEmpty(session)) {
            queryBuilder.addQueryParameter("sid", session)
        }
        queryBuilder.addQueryParameter("loc", AppInfo.countryId)
        queryBuilder.addQueryParameter("uuid", AppInfo.uuid)
        queryBuilder.addQueryParameter("version", AppInfo.versionName)
        queryBuilder.addQueryParameter("lang", AppInfo.lang)
        queryBuilder.addQueryParameter("platform", "android")
        queryBuilder.addQueryParameter("model", AppInfo.model)
        queryBuilder.addQueryParameter("loctype", java.lang.String.valueOf(AppInfo.loctype))
        queryBuilder.addQueryParameter("os", Build.VERSION.SDK_INT.toString())

        val url = queryBuilder.build()
        requestBuilder.url(url)

        return chain.proceed(requestBuilder.build())
    }
}