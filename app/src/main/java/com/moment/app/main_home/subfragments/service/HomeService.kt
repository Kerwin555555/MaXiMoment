package com.moment.app.main_home.subfragments.service

import com.moment.app.main_home.subfragments.models.UserInfoList
import retrofit2.http.GET
import retrofit2.http.Query

interface HomeService {
    @GET("api/sns/v1/moment/home/recommendation_users")
    suspend fun getOnlineUsersForSlide(@Query("start_pos") startPos:
                                           Int, @Query("num") num: Int): com.moment.app.datamodel.Results<UserInfoList>
}