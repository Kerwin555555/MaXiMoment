package com.moment.app.hilt.viewmodel_level

import com.moment.app.hilt.app_level.MockData
import com.moment.app.main_home.subfragments.db.HomeRecommendationListDatabase
import com.moment.app.main_home.subfragments.repository.EntityToModelMapper
import com.moment.app.main_home.subfragments.repository.Repository
import com.moment.app.main_home.subfragments.repository.UserInfoRecomImpl
import com.moment.app.main_home.subfragments.service.HomeService
import com.moment.app.main_home.subfragments.service.UserInfoEntityRemoteMediator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

//
//@Module
//@InstallIn(ViewModelComponent::class)
//class ViewModelModule {
//
//}

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
    fun provideMediator(@MockData api: HomeService, db: HomeRecommendationListDatabase) : UserInfoEntityRemoteMediator {
        return UserInfoEntityRemoteMediator(api, db)
    }
}