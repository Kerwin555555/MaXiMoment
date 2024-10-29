package com.moment.app.login_page.service

import com.moment.app.datamodel.CommentItem
import com.moment.app.datamodel.CommentsList
import com.moment.app.datamodel.Results
import com.moment.app.datamodel.UserInfo
import com.moment.app.main_profile.entities.FeedList
import com.moment.app.main_profile.entities.PostBean
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface LoginService {
    @POST("api/sns/v1/moment/user/facebook_login")
    suspend fun facebookLogin(@Body map: Map<String, String>): Results<UserInfo>

    @POST("api/sns/v1/moment/user/google_login")
    suspend fun googleLogin(@Body map: Map<String, String>): Results<UserInfo>

    @GET("api/sns/v1/moment/user/logout")
    suspend fun logout(): Results<Any>

    @POST("api/sns/v1/moment/user/info")
    suspend fun updateInfo(@Body data: Map<String?, String?>?): Results<UserInfo>

    @GET("api/sns/v1/moment/user/get_info/{userId}")
    suspend fun getUserInfo(@Path("userId") userId: String?): Results<UserInfo>
}


interface FeedService {
    @GET("api/sns/v1/moment/feed/view/{user}")
    suspend fun getFeeds(@Path("user") user: String?, @Query("start_ts") startPos: Int, @Query("num") num: Int): Results<FeedList?>

    @GET("api/sns/v1/moment/feed/info/{id}")
    suspend fun getFeedDetail(@Path("id") id: String?): Results<PostBean>


    @GET("api/sns/v1/moment/feed/comment_page/{id}")
    suspend fun getComments(@Path("id") id: String?, @Query("cursor") cursor: Int): Results<CommentsList>
}