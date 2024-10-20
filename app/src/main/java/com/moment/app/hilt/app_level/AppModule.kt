package com.moment.app.hilt.app_level

import android.app.Application
import android.util.Log
import androidx.room.Room
import com.blankj.utilcode.util.UriUtils
import com.moment.app.datamodel.Result
import com.moment.app.datamodel.UserInfo
import com.moment.app.main_home.subfragments.db.HomeRecommendationListDatabase
import com.moment.app.main_home.subfragments.db.UserInfoEntityDao
import com.moment.app.main_home.subfragments.models.UserInfoList
import com.moment.app.main_home.subfragments.repository.EntityToModelMapper
import com.moment.app.main_home.subfragments.repository.Repository
import com.moment.app.main_home.subfragments.repository.UserInfoRecomImpl
import com.moment.app.main_home.subfragments.service.HomeService
import com.moment.app.main_home.subfragments.service.UserInfoEntityRemoteMediator
import com.moment.app.utils.Constants.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class NetWorkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(client)
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideHomeService(retrofit: Retrofit) : HomeService {
        return retrofit.create(HomeService::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
class RoomModule {
    @Provides
    @Singleton
    fun provideAppDataBase(application: Application) : HomeRecommendationListDatabase {
        return Room.databaseBuilder(application, HomeRecommendationListDatabase::class.java, "recom_user_info_entities.db").build()
    }

    @Provides
    @Singleton
    fun provideUserInfoEntityDao(db: HomeRecommendationListDatabase): UserInfoEntityDao {
        return db.UserInfoEntityDao()
    }
}


@Module
@InstallIn(ViewModelComponent::class)
class RepModule {
    @Provides
    @ViewModelScoped
    fun provideAppDataBase(db: HomeRecommendationListDatabase, m: UserInfoEntityRemoteMediator) : Repository {
        return UserInfoRecomImpl(db, m, EntityToModelMapper())
    }

    @Provides
    @ViewModelScoped
    fun provideMediator(@MockData api:HomeService, db: HomeRecommendationListDatabase) : UserInfoEntityRemoteMediator {
        return UserInfoEntityRemoteMediator(api, db)
    }
}

class MockHomeService: HomeService {
    override suspend fun getOnlineUsersForSlide(startPos: Int, num: Int): Result<UserInfoList> {
        Log.d("zhouzheng", "dafdasfdfasfadasfadsdfafdafdada")
        val result = UserInfoList()
        val list = mutableListOf<UserInfo>()
        Log.d("zhouzheng", "dafdasfdfasfadasfadsdfafdafdada:" + Thread.currentThread().name)
        try {
                delay(500)
        } catch (e: Exception) {
            Log.d("zhouzheng", "dafdasfdfasfadasfadsdfafdafdada:" + e.message)
        }
        for (i in startPos until  startPos + num) {
            list.add(UserInfo().apply {
                this.userId = ""+java.util.UUID.randomUUID()
                this.gender = "male"
                this.name = "MomentFan"+ userId
                this.age = i
            })
        }
        Log.d("zhouzheng", "dafdasfdfasfadasfadsdfafdafdada")
        result.user_infos = list
        Log.d("zhouzheng", "dafdasfdfasfadasfadsdfafdafdada")
        result.has_next = if (startPos == 30) false else true
        result.next_start = startPos + num
        return com.moment.app.datamodel.Result<UserInfoList>().apply {
            data = result
            isOk = true
            this.result = 0
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
class MockNetWorkModule {
    @Provides
    @Singleton
    @MockData
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().build()
    }

    @Singleton
    @Provides
    @MockData
    fun provideRetrofit(@MockData client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(client)
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    @MockData
    fun provideMockHomeService(@MockData retrofit: Retrofit): HomeService {
        return MockHomeService()
    }
}

@Qualifier
annotation class MockData

