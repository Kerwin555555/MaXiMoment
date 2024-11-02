package com.moment.app.router

import com.didi.drouter.router.Request

/**
 * 路由拦截容错获取参数
 */
object DRouterHelper {


    fun getString(request: Request, key: String, defaultValue: String = ""): String {
        return request.getString(key) ?: request.uri.getQueryParameter(key) ?: defaultValue
    }

    fun getInt(request: Request, key: String): Int {
        val queryParams = request.uri.getQueryParameter(key)?.toInt()
        val extraParams = request.getInt(key)
        return queryParams ?: extraParams
    }

    fun getLong(request: Request, key: String): Long {
        val queryParams = request.uri.getQueryParameter(key)?.toLong()
        val extraParams = request.getLong(key)
        return queryParams ?: extraParams
    }

    fun getFloat(request: Request, key: String): Float {
        val queryParams = request.uri.getQueryParameter(key)?.toFloat()
        val extraParams = request.getFloat(key)
        return queryParams ?: extraParams
    }

    fun getDouble(request: Request, key: String): Double {
        val queryParams = request.uri.getQueryParameter(key)?.toDouble()
        val extraParams = request.getDouble(key)
        return queryParams ?: extraParams
    }

}