package com.moment.app.hilt.app_level

import android.util.Log
import com.moment.app.datamodel.HuanxinBean
import com.moment.app.datamodel.Results
import com.moment.app.datamodel.UserInfo
import com.moment.app.login_page.service.LoginService
import com.moment.app.main_home.subfragments.models.UserInfoList
import com.moment.app.main_home.subfragments.service.HomeService
import com.moment.app.utils.Constants.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID
import javax.inject.Qualifier
import javax.inject.Singleton

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

    @Singleton
    @Provides
    @MockData
    fun provideMockLoginService(@MockData retrofit: Retrofit): LoginService {
        return MockLoginService()
    }
}


class MockHomeService: HomeService {
    override suspend fun getOnlineUsersForSlide(startPos: Int, num: Int): Results<UserInfoList> {
        Log.d("Moment", "dafdasfdfasfadasfadsdfafdafdada")
        val result = UserInfoList()
        val list = mutableListOf<UserInfo>()
        Log.d("Moment", "dafdasfdfasfadasfadsdfafdafdada:" + Thread.currentThread().name)
        try {
            delay(500)
        } catch (e: Exception) {
            Log.d("Moment", "dafdasfdfasfadasfadsdfafdafdada:" + e.message)
        }
        for (i in startPos until  startPos + num) {
            list.add(UserInfo().apply {
                this.userId = ""+java.util.UUID.randomUUID()
                this.gender = if (i%2 ==0)"male" else "female"
                this.name = "MomentFan" + i
                this.age = i
                this.followed = i%2 ==0
            })
        }
        Log.d("Moment", "dafdasfdfasfadasfadsdfafdafdada")
        result.user_infos = list
        Log.d("Moment", "dafdasfdfasfadasfadsdfafdafdada")
        result.has_next = if (startPos == 30) false else true
        result.next_start = startPos + num
        return Results<UserInfoList>().apply {
            data = result
            isOk = true
            this.result = 0
        }
    }
}

class MockLoginService: LoginService {
    override suspend fun facebookLogin(map: Map<String, String>): Results<UserInfo> {
        val res = withContext(Dispatchers.IO) {
            delay(500)
            UserInfo(
                userId = UUID.randomUUID().toString(),
                name = "Momentfanxxx",
                session = "mysession",
                finished_info = false,
                huanxin = HuanxinBean().apply{
                    password  = "045xxxx"
                    user_id = "loveabscdessss"
                },
                gender = "male"
            )
        }
        return Results<UserInfo>().apply {
            data = res
        }

    }

    override suspend fun logout(): Results<Any> {
        delay(300)
        return Results()
    }

    override suspend fun updateInfo(data: Map<String?, String?>?): Results<UserInfo> {
        withContext(Dispatchers.IO) {
            delay(400)
        }
        return Results()
    }

}


@Qualifier
annotation class MockData