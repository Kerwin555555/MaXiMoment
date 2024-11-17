package com.moment.app.hilt.app_level

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase.JournalMode
import com.moment.app.BuildConfig
import com.moment.app.hilt.app_level.interceptors.LogInterceptor
import com.moment.app.hilt.app_level.interceptors.converter.MyGsonConverterFactory
import com.moment.app.login_page.service.FeedService
import com.moment.app.login_page.service.LoginService
import com.moment.app.main_chat.MessagingListDao
import com.moment.app.main_chat.ConversationDatabase
import com.moment.app.main_chat.GlobalConversationManager
import com.moment.app.main_chat.ThreadService
import com.moment.app.main_home.subfragments.db.HomeRecommendationListDatabase
import com.moment.app.main_home.subfragments.db.UserInfoEntityDao
import com.moment.app.main_home.subfragments.service.HomeService
import com.moment.app.models.UserIMManagerBus
import com.moment.app.models.UserImManager
import com.moment.app.utils.MomentCoreParams.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.Proxy
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetWorkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(ParamsInterceptor())
            .addInterceptor(LogInterceptor())
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .cache(null)
        if (!BuildConfig.DEBUG && BASE_URL.contains("https")) {
            builder.proxy(Proxy.NO_PROXY)
        }
        return builder.build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(client)
            .baseUrl(BASE_URL)
            .addConverterFactory(MyGsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideHomeService(retrofit: Retrofit) : HomeService {
        return retrofit.create(HomeService::class.java)
    }

    @Singleton
    @Provides
    fun provideLoginService(retrofit: Retrofit) : LoginService {
        return retrofit.create(LoginService::class.java)
    }

    @Singleton
    @Provides
    fun provideFeedService(retrofit: Retrofit) : FeedService {
        return retrofit.create(FeedService::class.java)
    }

    @Singleton
    @Provides
    fun provideThreadService(retrofit: Retrofit) : ThreadService {
        return retrofit.create(ThreadService::class.java)
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

    @Provides
    @Singleton
    fun provideConversationDataBase(application: Application) : ConversationDatabase {
        return Room.databaseBuilder(
            application,
            ConversationDatabase::class.java, "moment_user_db_v2")
            .allowMainThreadQueries()
            .setJournalMode(JournalMode.TRUNCATE)
            .build()
    }

    @Provides
    @Singleton
    fun provideConversationDao(db: ConversationDatabase): MessagingListDao {
        return db.conversationDao()
    }

    @Provides
    @Singleton
    fun provideConversationHub(conversationDao: MessagingListDao, service: ThreadService): GlobalConversationManager {
        return GlobalConversationManager(conversationDao, service)
    }

    @Singleton
    @Provides
    fun provideIMLoginModel(hub: GlobalConversationManager): UserIMManagerBus {
        return UserImManager(hub)
    }
}

