package com.moment.app.hilt.app_level

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase.JournalMode
import com.moment.app.login_page.service.FeedService
import com.moment.app.login_page.service.LoginService
import com.moment.app.main_chat.MessagingListDao
import com.moment.app.main_chat.ConversationDatabase
import com.moment.app.main_chat.GlobalConversationHub
import com.moment.app.main_chat.ThreadService
import com.moment.app.main_home.subfragments.db.HomeRecommendationListDatabase
import com.moment.app.main_home.subfragments.db.UserInfoEntityDao
import com.moment.app.main_home.subfragments.service.HomeService
import com.moment.app.models.UserImManager
import com.moment.app.utils.MomentCoreParams.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
    fun provideConversationHub(conversationDao: MessagingListDao, service: ThreadService): GlobalConversationHub {
        return GlobalConversationHub(conversationDao, service)
    }

    @Singleton
    @Provides
    fun provideIMLoginModel(hub: GlobalConversationHub): UserImManager {
        return UserImManager(hub)
    }
}

