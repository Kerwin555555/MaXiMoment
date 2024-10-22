package com.moment.app.main_home.subfragments.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.moment.app.datamodel.UserInfo
import com.moment.app.main_home.subfragments.db.HomeRecommendationListDatabase
import com.moment.app.main_home.subfragments.db.UserInfoEntity
import com.moment.app.main_home.subfragments.service.HomeService
import com.moment.app.main_home.subfragments.service.UserInfoEntityRemoteMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map


interface Repository {
    fun fetchUserInfoList(): Flow<PagingData<UserInfo>>
}


class UserInfoRecomImpl(
    private val db: HomeRecommendationListDatabase,
    private val mediator: UserInfoEntityRemoteMediator,
    private val mapper: Map<UserInfoEntity, UserInfo>
): Repository{
    @OptIn(ExperimentalPagingApi::class)
    override fun fetchUserInfoList(): Flow<PagingData<UserInfo>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                prefetchDistance = 0,   //预加载 加载更多
                initialLoadSize = 10
            ),
            remoteMediator = mediator,
        ){
            db.UserInfoEntityDao().getUserInfoEntities()
        }.flow.flowOn(Dispatchers.IO).map { pagingData ->
            pagingData.map {
                mapper.map(it)
            }
        }
    }
}

/**
 * 在Kotlin协程的Flow中，map函数并不是在emit后执行，而是在Flow的收集过程中执行。让我们更详细地解释一下这个流程。
 *
 * ‌Flow的创建与发射（emit）‌：
 *
 * 当你创建一个Flow时，你定义了一个数据流，这个数据流可以包含多个值，这些值通过emit函数发射出去。
 * Flow在创建时并不会立即执行，它是一个冷流，意味着它会在有消费者开始收集时才执行。
 * ‌中间操作符（如map）‌：
 *
 * map是一个中间操作符，它接收一个转换函数，并将这个函数应用于Flow中发射的每个值。
 * 当Flow开始被收集时，map操作符会拦截每个发射的值，应用转换函数，然后向下游发射转换后的值。
 */

interface Map<I, O> {
    fun map(input: I): O
}

class EntityToModelMapper : Map<UserInfoEntity, UserInfo> {
    override fun map(input: UserInfoEntity): UserInfo {
         return UserInfo().apply{
             this.age = input.age
             this.userId = input.userId
             this.gender = input.gender
             this.name = input.name
             this.followed = input.followed
         }
    }
}