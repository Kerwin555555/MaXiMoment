package com.moment.app.network

import java.util.concurrent.ConcurrentHashMap


object MomentNetwork {
    val coldStartMap : MutableMap<String, Boolean> = ConcurrentHashMap()
}