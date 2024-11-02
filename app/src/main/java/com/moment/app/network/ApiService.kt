package com.moment.app.network

import com.moment.app.utils.MomentCoreParams
import okhttp3.Interceptor
import retrofit2.Retrofit

object ApiService {
    var userAgent: String? = null
    lateinit var retrofit: Retrofit
    var cacheService: java.util.HashMap<Class<*>, Any> = java.util.HashMap()

    fun initRetrofit(vararg interceptors: Interceptor?) {
        retrofit = Retrofit.Builder()
            .baseUrl(MomentCoreParams.BASE_URL)
            .build()
    }

    //fun createHttpBuilder(respGuard: Boolean, vararg interceptors: Interceptor?): Builder {
//        val basicParamsInterceptor: BasicParamsInterceptor = BasicParamsInterceptor()
//        val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor(object : Logger() {
//            fun log(message: String?) {
//                LogUtils.d("LitNet", message)
//            }
//        })
//        val builder: Builder = Builder()
//            .addInterceptor(HostInterceptor())
//            .addInterceptor(basicParamsInterceptor)
//            .addInterceptor(SignInterceptor())
//            .addInterceptor(EyeInterceptor())
//            .addInterceptor(interceptor)
//            .addInterceptor(UserAgentInterceptor(userAgent))
//            .addInterceptor(TransferInterceptor())
//
//        if (BuildConfig.DEBUG) {
//            builder.addInterceptor(MockInterceptor())
//        }
//
//        if (respGuard) {
//            builder.addInterceptor(GuardInterceptor())
//        }
//
//        if (interceptors != null && interceptors.size > 0) {
//            for (custom in interceptors) {
//                builder.addInterceptor(custom)
//            }
//        }
//
//        InspectBridge.configureInterceptor(builder)
//
//        builder.connectTimeout(15, TimeUnit.SECONDS)
//            .writeTimeout(15, TimeUnit.SECONDS)
//            .readTimeout(30, TimeUnit.SECONDS)
//            .retryOnConnectionFailure(ConfigModel.getInstance().getConfig().enableRetrofitRetry)
//            .cache(null)
//
//        if ((!BuildConfig.DEBUG && Constants.BASE_URL.contains("https")) || (BuildConfig.DEBUG && Constants.HTTP_NO_PROXY)) {
//            builder.proxy(Proxy.NO_PROXY)
//        }
//        return builder
   // }

//    fun <T> getService(service: Class<T>?): T? {
//        var target = cacheService[service as Class<*>] as T?
//        if (target != null) {
//            return target
//        }
//        target = retrofit.create(service)
//        cacheService.put(service, target as Any)
//        return target
//    }
}