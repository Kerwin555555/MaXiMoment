package com.moment.app.main_home.subfragments.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import com.moment.app.datamodel.HuanxinBean

@Database(entities = [UserInfoEntity::class], version = 1, exportSchema = false)
abstract class  HomeRecommendationListDatabase : RoomDatabase(){
    abstract fun  UserInfoEntityDao(): UserInfoEntityDao
}

@Dao
interface UserInfoEntityDao {
    @Query("DELETE FROM userInfoEntity")
    suspend fun clearUserInfoEntity()

    @Insert(onConflict = OnConflictStrategy.REPLACE)    //如果数据库中已经存在一个具有相同主键的User对象，那么现有的对象将被新对象替换。
    suspend fun insertUserInfo(userInfo: List<UserInfoEntity>)

    @Query("SELECT * FROM userInfoEntity")
    fun getUserInfoEntities(): PagingSource<Int, UserInfoEntity> //自带分页处理

    @Query("SELECT * FROM userInfoEntity LIMIT :pageSize OFFSET :offset")
    suspend fun getUserInfoEntitiesPaged(pageSize: Int, offset: Int): List<UserInfoEntity>
}

@Entity
data class UserInfoEntity (
    @PrimaryKey
    val userId: String,
    val gender: String,
    val age: Int,
    val name: String,
    var page:Int
)