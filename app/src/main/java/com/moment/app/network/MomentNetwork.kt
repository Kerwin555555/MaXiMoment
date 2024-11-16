package com.moment.app.network

import okhttp3.Request
import java.util.concurrent.ConcurrentHashMap


object MomentNetwork {
    val coldStartMap : MutableMap<String, Boolean> = ConcurrentHashMap()
}

object NetErrorHandler {
    private var listener: OnApiErrorListener? = null

    fun setListener(listener: OnApiErrorListener?) {
        this.listener = listener
    }

    fun handleNetError(throwable: Throwable?) {
        this.handleNetError(null as Request?, throwable)
    }

    fun handleNetError(request: Request?, throwable: Throwable?) {
        if (this.listener != null) {
            listener!!.onApiError(request, throwable)
        }
    }

    interface OnApiErrorListener {
        fun onApiError(var1: Request?, var2: Throwable?)
    }
}