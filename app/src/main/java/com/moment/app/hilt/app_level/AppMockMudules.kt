package com.moment.app.hilt.app_level

import android.util.Log
import com.moment.app.datamodel.CommentItem
import com.moment.app.datamodel.CommentItem.TimeInfoBean
import com.moment.app.datamodel.CommentsList
import com.moment.app.datamodel.HuanxinBean
import com.moment.app.datamodel.LoginSessionResult
import com.moment.app.datamodel.Results
import com.moment.app.datamodel.UserInfo
import com.moment.app.login_page.service.FeedService
import com.moment.app.login_page.service.LoginService
import com.moment.app.main_chat.BackendThread
import com.moment.app.main_chat.GlobalConversationManager
import com.moment.app.main_chat.MessagingListDao
import com.moment.app.main_chat.ThreadList
import com.moment.app.main_chat.ThreadService
import com.moment.app.main_home.subfragments.models.UserInfoList
import com.moment.app.main_home.subfragments.service.HomeService
import com.moment.app.main_profile.entities.CreateTimeBean
import com.moment.app.main_profile.entities.FeedList
import com.moment.app.main_profile.entities.PostBean
import com.moment.app.models.UserIMManagerBus
import com.moment.app.models.UserImManager
import com.moment.app.utils.MOMENT_APP
import com.moment.app.utils.MomentCoreParams.BASE_URL
import com.moment.app.utils.MomentOSSDelegate
import com.moment.app.utils.ViewerPhoto
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Call
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
    fun provideConversationHub(conversationDao: MessagingListDao, @MockData service: ThreadService): GlobalConversationManager {
        return GlobalConversationManager(conversationDao, service)
    }

    @Singleton
    @Provides
    @MockData
    fun provideIMLoginModel(@MockData hub: GlobalConversationManager): UserIMManagerBus{
        return UserImManager(hub)
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
                this.huanxin = HuanxinBean().apply {
                    this.user_id = ""+java.util.UUID.randomUUID()
                    this.password = "dsds"
                }
                this.user_id = ""+java.util.UUID.randomUUID()
                this.gender = if (i%2 ==0)"boy" else "girl"
                this.nickname = "MomentFan" + i
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
        }
    }
}

class MockLoginService: LoginService {
    override suspend fun login(map: Map<String, String>): Results<LoginSessionResult> {
        val res = withContext(Dispatchers.IO) {
            delay(800)
            UserInfo(
                user_id = UUID.randomUUID().toString(),
                nickname = "Momentfanxxx",
                session = "mysession",
                huanxin = HuanxinBean().apply{
                    password  = "045xxxx"
                    user_id = "loveabscdessss"
                },
                gender = "boy",
                imagesWallList = mutableListOf(),
                follower_count = 10000,
                following_count= 0,
                friends_count = 0,
                bio = ""
            )
        }
        return Results<LoginSessionResult>().apply {
            this.data = LoginSessionResult().apply {
                this.user_info = res
                this.session = "fajsdfijhaiodfjaoidfjioa"
            }
        }

    }

    override suspend fun logout(): Results<Any> {
        delay(300)
        return Results()
    }

    override suspend fun updateInfo(data: Map<String, Any>): Results<UserInfo> {
        val res = withContext(Dispatchers.IO) {
            delay(800)
            UserInfo(
                user_id = UUID.randomUUID().toString(),
                nickname = "Momentfanxxx",
                session = "mysession",
                huanxin = HuanxinBean().apply{
                    password  = "045xxxx"
                    user_id = "loveabscdessss"
                },
                gender = "boy",
                imagesWallList = mutableListOf("0","1", "2", "3", "1"),
                follower_count = 10,
                following_count= 1000,
                friends_count = 100,
                bio = "hello this is the default from the backend hello this is the default hello this is the default hello this is the default hello this is the default hello this is the default"
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

    override fun getOssToken():Call<Results<MomentOSSDelegate.OSSToken>>? {
        return null
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
                user_id = UUID.randomUUID().toString(),
                nickname = "Momentfanxxx",
                session = "mysession",
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
                        ViewerPhoto.PicShape(
                            fileKey = ""
                        ),
                        ViewerPhoto.PicShape(
                            fileKey = ""
                        ),
                        ViewerPhoto.PicShape(
                            fileKey = ""
                        )
                    ) else mutableListOf()
                    user_info = user
                    create_time = CreateTimeBean().apply {
                        time = System.currentTimeMillis()
                    }
                    content = "hi, today is nice, I'm thrilled to meet you at Moment, you can chat with me at any time!"
                }
                list.add(f)
            }
            Results<FeedList?>().apply {
                data = FeedList().apply {
                    next_start = startPos + num
                    has_next = next_start < 30
                }
                data!!.feeds = list

            }
        }
    }

    override suspend fun getFeedDetail(id: String?): Results<PostBean> {
        withContext(Dispatchers.IO) {
            delay(400)
        }
        return Results()
    }

    /**
     *     var comment_id: String? = null
     *     var content: String? = null
     *     var time_info: TimeInfoBean? = null
     *     var hasImpressionTrack: Boolean = false //曝光埋点标志
     *     var user_info: UserInfo? = null
     *     var inner_comments: List<InnerCommentsBean>? = null
     *     var show_outside: List<InnerCommentsBean>? = ArrayList()
     *     var hasClickedSeeMore: Boolean = false // 本地数据
     *     var show_pos: Int = 0
     *     var is_fold: Boolean = false
     *     var isFakeCommentId: Boolean = false
     *     var loadingStatus: Int = 1 // 本地数据 0 isLoading -> 1 load success无需显示 -> 2 LoadFailed
     *     var comment_like_num: Int = 0
     *     var comment_liked: Boolean = false
     */
    override suspend fun getComments(id: String?, cursor: Int): Results<CommentsList> {
        val comment = withContext(Dispatchers.IO) {
            delay(400)
            CommentsList().apply {
                this.cursor = 0
                this.comments = mutableListOf()
                for (i in 0 until 10) {
                    this.comments!!.add(CommentItem().apply {
                        time_info = TimeInfoBean().apply {
                            time = System.currentTimeMillis().toInt()
                        }
                        comment_id = UUID.randomUUID().toString()
                        content = "You are cute let us chat"
                        user_info = UserInfo(
                            avatar = "avatar",
                            user_id = UUID.randomUUID().toString(),
                            nickname = "MomentComment",
                            huanxin = HuanxinBean().apply{
                                password  = "045xxxx"
                                user_id = "loveabscdessss"
                            },
                            gender = "girl",
                        )
                    })
                }
            }
        }
        return Results<CommentsList>().apply {
            data = comment
        }
    }

    override suspend fun getUserInfo(userId: String?): Results<UserInfo> {
        withContext(Dispatchers.IO) {
            delay(400)
        }
        return Results<UserInfo>().apply {
            data =             UserInfo(
                user_id = UUID.randomUUID().toString(),
                nickname = "Momentfanxxx",
                session = "mysession",
                huanxin = HuanxinBean().apply{
                    password  = "045xxxx"
                    user_id = "loveabscdessss"
                },
                gender = "boy",
                imagesWallList = mutableListOf("0","1", "2", "3", "1"),
                follower_count = 10,
                following_count= 1000,
                friends_count = 100,
                bio = "hello this is the default from the backend hello this is the default hello this is the default hello this is the default hello this is the default hello this is the default hello this is the default"
            )
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
                    user_id = UUID.randomUUID().toString(),
                    nickname = "Momentfanxxx",
                    gender = "girl",
                )
                })
            }
        }
        Log.d(MOMENT_APP, "怕了点对点")
         return Results<ThreadList>().apply {
             data = datas

         }
    }

    override suspend fun getUserInfo(userId: String?): Results<UserInfo> {
        val res = withContext(Dispatchers.IO) {
            delay(800)
            UserInfo(
                user_id = UUID.randomUUID().toString(),
                nickname = "Momentfanxxx",
                session = "mysession",
                huanxin = HuanxinBean().apply{
                    password  = "045xxxx"
                    user_id = "loveabscdessss"
                },
                gender = "boy",
                imagesWallList = mutableListOf("0","1", "2", "3", "1"),
                follower_count = 10,
                following_count= 1000,
                friends_count = 100,
                bio = "hello this is the default from the backend hello this is the default hello this is the default hello this is the default hello this is the default hello this is the default"
            )
        }
        return Results<UserInfo>().apply {
            this.data = res
        }
    }

    override suspend fun getUserInfoByImId(map: Map<String, List<String>>): Results<Map<String, UserInfo>> {
        return Results<Map<String, UserInfo>>().apply {
            data = mutableMapOf("other" to UserInfo(
                user_id = UUID.randomUUID().toString(),
                nickname = "Momentfanxxx",
                session = "mysession",
                huanxin = HuanxinBean().apply{
                    password  = "045xxxx"
                    user_id = "loveabscdessss"
                },
                gender = "boy",
                imagesWallList = mutableListOf(),
                follower_count = 10000,
                following_count= 0,
                friends_count = 0,
                bio = ""
            ))
        }
    }
}

data class MockMessage(val content: String, val user: String)
class MockConversation


@Qualifier
annotation class MockData