package com.moment.app.main_home.subfragments.service

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.blankj.utilcode.util.NetworkUtils
import com.moment.app.main_home.subfragments.db.HomeRecommendationListDatabase
import com.moment.app.main_home.subfragments.db.UserInfoEntity
import com.moment.app.main_home.subfragments.models.UserInfoList


@OptIn(ExperimentalPagingApi::class)
class UserInfoEntityRemoteMediator (
    private val api: HomeService,
    private val database: HomeRecommendationListDatabase
) : RemoteMediator<Int, UserInfoEntity>(){

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, UserInfoEntity>
    ): MediatorResult {
        // 1. check loadType
        kotlin.runCatching {
            // 1. check loadType
            val pageKey = when (loadType) {
                // 首次访问
                LoadType.REFRESH -> {
                    null
                }

                //加载更多
                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull() ?: return MediatorResult.Success(endOfPaginationReached = true)
                    lastItem.page
                }

                //头部添加
                LoadType.PREPEND -> {
                     return MediatorResult.Success(endOfPaginationReached = true)
                }
            }
            //2. paging
            if (!NetworkUtils.isConnected()) {
                return MediatorResult.Success(true)
            }
            val page =  pageKey ?: 0
            val result = api.getOnlineUsersForSlide(page * state.config.pageSize, state.config.pageSize)

            var item = (result.data as UserInfoList).user_infos?.map { it ->
                UserInfoEntity(
                    name = it.name!!,
                    userId = it.userId!!,
                    gender = it.gender!!,
                    page = page + 1,
                    age = it.age!!,
                    followed = it.followed!!
                )
            }
            //3 database
            val userInfoDao = database.UserInfoEntityDao()
            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    userInfoDao.clearUserInfoEntity()
                }
                userInfoDao.insertUserInfo(item!!)
            }
            Log.d("zhouzheng", "写入数据库了这个数字以后的" + page * state.config.pageSize)
            return MediatorResult.Success(endOfPaginationReached = !result.data!!.has_next)
        }.onFailure {
            return MediatorResult.Error(it)
        }
        return MediatorResult.Error(RuntimeException())
    }
}