package com.moment.app.login_page.service

import com.moment.app.datamodel.Results
import com.moment.app.datamodel.UserInfo
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface LoginService {
    @POST("api/sns/v1/moment/user/facebook_login")
    suspend fun facebookLogin(@Body map: Map<String, String>): Results<UserInfo>

    @POST("api/sns/v1/lit/user/google_login")
    suspend fun googleLogin(@Body map: Map<String, String>): Results<UserInfo>

    @GET("api/sns/v1/moment/user/logout")
    suspend fun logout(): Results<Any>

    @POST("api/sns/v1/moment/user/info")
    suspend fun updateInfo(@Body data: Map<String?, String?>?): Results<UserInfo>
}