package com.moment.app.models

import com.moment.app.datamodel.UserInfo

object LoginModel {
    private val info: UserInfo? = UserInfo()
    var forbidden_session: String? = null
        get() = field
        set(value) {
            field = value
        }

    fun isLogin() : Boolean{
        return true
    }

    fun getUserInfo(): UserInfo? {
        return info
    }

    fun logout(logoutIm: Boolean) {
        forbidden_session = null
    }
}