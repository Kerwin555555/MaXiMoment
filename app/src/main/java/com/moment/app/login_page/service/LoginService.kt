package com.moment.app.login_page.service

import com.moment.app.datamodel.CommentsList
import com.moment.app.datamodel.LoginSessionResult
import com.moment.app.datamodel.Results
import com.moment.app.datamodel.UserInfo
import com.moment.app.main_profile.entities.FeedList
import com.moment.app.main_profile.entities.PostBean
import com.moment.app.utils.MomentOSSDelegate
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface LoginService {
    @POST("api/v1/user/login")
    suspend fun login(@Body map: Map<String, String>): Results<LoginSessionResult>

    @GET("api/sns/v1/moment/user/logout")
    suspend fun logout(): Results<Any>

    @POST("api/v1/user/update-info")
    suspend fun updateInfo(@Body data: @JvmSuppressWildcards Map<String, Any>): Results<UserInfo>

    @GET("api/sns/v1/moment/user/get_info/{userId}")
    suspend fun getUserInfo(@Path("userId") userId: String?): Results<UserInfo>

    @GET("/api/v1/oss/fetch-sts-token")
    fun getOssToken(): Call<Results<MomentOSSDelegate.OSSToken>>?
}


interface FeedService {
    @GET("api/sns/v1/moment/feed/view/{user}")
    suspend fun getFeeds(@Path("user") user: String?, @Query("start_ts") startPos: Int, @Query("num") num: Int): Results<FeedList?>

    @GET("api/sns/v1/moment/feed/info/{id}")
    suspend fun getFeedDetail(@Path("id") id: String?): Results<PostBean>


    @GET("api/sns/v1/moment/feed/comment_page/{id}")
    suspend fun getComments(@Path("id") id: String?, @Query("cursor") cursor: Int): Results<CommentsList>

    @GET("api/sns/v1/moment/user/get_info/{userId}")
    suspend fun getUserInfo(@Path("userId") userId: String?): Results<UserInfo>
}