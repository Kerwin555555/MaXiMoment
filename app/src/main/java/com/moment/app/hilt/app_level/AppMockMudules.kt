package com.moment.app.hilt.app_level

import android.util.Log
import com.moment.app.datamodel.HuanxinBean
import com.moment.app.datamodel.Results
import com.moment.app.datamodel.UpdateInfoResult
import com.moment.app.datamodel.UserInfo
import com.moment.app.login_page.service.FeedService
import com.moment.app.login_page.service.LoginService
import com.moment.app.main_chat.BackendThread
import com.moment.app.main_chat.ConversationDao
import com.moment.app.main_chat.GlobalConversationHub
import com.moment.app.main_chat.ThreadList
import com.moment.app.main_chat.ThreadService
import com.moment.app.main_home.subfragments.models.UserInfoList
import com.moment.app.main_home.subfragments.service.HomeService
import com.moment.app.main_profile.entities.CreateTimeBean
import com.moment.app.main_profile.entities.FeedList
import com.moment.app.main_profile.entities.PicShape
import com.moment.app.main_profile.entities.PostBean
import com.moment.app.models.IMLoginModel
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


    @Singleton
    @Provides
    @MockData
    fun provideFeedService(retrofit: Retrofit) : FeedService {
        return MockFeedService()
    }

    @Singleton
    @Provides
    @MockData
    fun provideThreadService(retrofit: Retrofit) : ThreadService {
        return MockThreadService()
    }

    @Provides
    @Singleton
    @MockData
    fun provideConversationHub(conversationDao: ConversationDao, @MockData service: ThreadService): GlobalConversationHub {
        return GlobalConversationHub(conversationDao, service)
    }

    @Singleton
    @Provides
    @MockData
    fun provideIMLoginModel(@MockData hub: GlobalConversationHub): IMLoginModel {
        return IMLoginModel(hub)
    }
}

class MockHomeService: HomeService {
    override suspend fun getOnlineUsersForSlide(startPos: Int, num: Int): Results<UserInfoList> {
        Log.d("Moment", "dafdasfdfasfadasfadsdfafdafdada 1")
        val result = UserInfoList()
        val list = mutableListOf<UserInfo>()
        Log.d("Moment", "dafdasfdfasfadasfadsdfafdafdada: 2" + Thread.currentThread().name)

        delay(1000L)

        for (i in startPos until  startPos + num) {
            list.add(UserInfo().apply {
                this.userId = ""+java.util.UUID.randomUUID()
                this.gender = if (i%2 ==0)"boy" else "girl"
                this.name = "MomentFan" + i
                this.age = i
                this.followed = i%2 ==0
                this.imagesWallList = mutableListOf("0","1", "2", "3", "1")
            })
        }
        Log.d("Moment", "dafdasfdfasfadasfadsdfafdafdada 4")
        result.user_infos = list
        Log.d("Moment", "dafdasfdfasfadasfadsdfafdafdada 5")
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
            delay(800)
            UserInfo(
                userId = UUID.randomUUID().toString(),
                name = "Momentfanxxx",
                session = "mysession",
                finished_info = false,
                huanxin = HuanxinBean().apply{
                    password  = "045xxxx"
                    user_id = "loveabscdessss"
                },
                gender = "boy",
                imagesWallList = mutableListOf("0","1", "2", "3", "1"),
                follower_count = 10000,
                following_count= 10,
                friends_count = 32,
                bio = ""
            )
        }
        return Results<UserInfo>().apply {
            data = res
        }

    }

    override suspend fun googleLogin(map: Map<String, String>): Results<UserInfo> {
        val res = withContext(Dispatchers.IO) {
            delay(800)
            UserInfo(
                userId = UUID.randomUUID().toString(),
                name = "Momentfanxxx",
                session = "mysession",
                finished_info = false,
                huanxin = HuanxinBean().apply{
                    password  = "045xxxx"
                    user_id = "loveabscdessss"
                },
                gender = "boy",
                imagesWallList = mutableListOf("0","1", "2", "3", "1"),
                follower_count = 10000,
                following_count= 100000,
                friends_count = 1000000,
                bio = "hello this is the default from the backend"
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
        val res = withContext(Dispatchers.IO) {
            delay(800)
            UserInfo(
                userId = UUID.randomUUID().toString(),
                name = "Momentfanxxx",
                session = "mysession",
                finished_info = false,
                huanxin = HuanxinBean().apply{
                    password  = "045xxxx"
                    user_id = "loveabscdessss"
                },
                gender = "boy",
                imagesWallList = mutableListOf("0","1", "2", "3", "1"),
                follower_count = 10,
                following_count= 1000,
                friends_count = 100,
                bio = "hello this is the default from the backend"
            )
        }
        return Results<UserInfo>().apply {
            this.data = res
        }
    }

//    override suspend fun updateInfo(data: Map<String, String>): Results<UserInfo> {
//        withContext(Dispatchers.IO) {
//            delay(400)
//        }
//        return Results()
//    }

    override suspend fun getUserInfo(userId: String?): Results<UserInfo> {
        withContext(Dispatchers.IO) {
            delay(400)
        }
        return Results()
    }
}


/**
 *     var id: String? = null
 *     var pics_shape : MutableList<PicShape>? = null
 *     var user_id: String? = ""
 *     var user_info: UserInfo? = null
 *     var content: String? = null
 *     var create_time: CreateTimeBean? = null
 *     var comment_num: Int? = 0
 *     var like_num: Int? = 0
 *     var liked = false
 *
 *     fun isPictureFeed(): Boolean {
 *         return pics_shape != null && !pics_shape!!.isEmpty()
 *     }
 */
class MockFeedService : FeedService{
    override suspend fun getFeeds(user: String?, startPos: Int, num: Int): Results<FeedList?> {
        return withContext(Dispatchers.IO) {
            val user = UserInfo(
                userId = UUID.randomUUID().toString(),
                name = "Momentfanxxx",
                session = "mysession",
                finished_info = true,
                huanxin = HuanxinBean().apply{
                    password  = "045xxxx"
                    user_id = "loveabscdessss"
                },
                gender = "girl",
            )
            delay(1500)
            val list = mutableListOf<PostBean>()
            for (i in 0 until 10) {
                val f = PostBean().apply {
                    id = "feed"+UUID.randomUUID().toString()
                    pics_shape = if (i %3 != 0) mutableListOf(
                        PicShape(
                            fileKey = ""
                        ),
                        PicShape(
                            fileKey = ""
                        ),
                        PicShape(
                            fileKey = ""
                        )
                    ) else mutableListOf()
                    user_info = user
                    create_time = CreateTimeBean().apply {
                        time = System.currentTimeMillis()
                    }
                    content = "hi, today is a nice, I'm thrilling to meet you in Moment, you can chat with me!"
                }
                list.add(f)
            }
            Results<FeedList?>().apply {
                data = FeedList().apply {
                    next_start = startPos + num
                    has_next = next_start < 30
                }
                data!!.feeds = list
                isOk = true
                this.result = 0

            }
        }
    }
}


class MockThreadService: ThreadService {
    override suspend fun conversations(): Results<ThreadList> {
        delay(1500)
        val datas = ThreadList().apply {
            conversations = mutableListOf()
            for (t in 0 until 50) {
                (conversations as MutableList<BackendThread>).add(BackendThread().apply {
                    this.conversation_id =UUID.randomUUID().toString()
                    this. create_time = null
                    this.user_id = UUID.randomUUID().toString()
                    this. userInfo  =   UserInfo(
                    userId = UUID.randomUUID().toString(),
                    name = "Momentfanxxx",
                    gender = "girl",
                )
                })
            }
        }
        Log.d("zhouzheng", "怕了点对点")
         return Results<ThreadList>().apply {
             data = datas

         }
    }
}


@Qualifier
annotation class MockData