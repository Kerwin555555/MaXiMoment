package com.moment.app.main_chat

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.moment.app.datamodel.UserInfo
import com.moment.app.main_chat.fragments.entities.EntityConversation

@Dao
interface MessagingListDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(conversationBean: EntityConversation?): Long

    @Query("select * from user_conversation where userId=:id order by updateTime desc")
    fun getAll(id: String): List<EntityConversation> //id就是user_id 一般是当前用户  Momentconversation.id 是回话也就是 对方环信ID

    @Query("select * from user_conversation where userId=:id order by pinned desc, updateTime desc limit 20 offset 0")
    fun getRecent20Users(id: String?): List<EntityConversation?>?

    @Query("select * from user_conversation where userId=:id and conversationType=0 order by pinned desc, updateTime desc limit 30 offset 0")
    fun getRecent30Users(id: String?): List<EntityConversation?>?

    @Query("select * from user_conversation where userId=:user_id and id=:id")
    fun getConversation(user_id: String?, id: String?): EntityConversation?

    @Query("update user_conversation set pinned=:pinned where id=:id")
    fun updatePin(id: String?, pinned: Int)

    @Query("update user_conversation set userInfo=:user where id=:id")
    fun updateUserInfo(user: UserInfo?, id: String?)

    @Query("update user_conversation set updateTime=:time where id=:id")
    fun updateTime(time: Long, id: String?)

    @Query("update user_conversation set remindEndTime=:time where id=:id")
    fun updateRemindTime(time: Long, id: String?)

    @Query("update user_conversation set draft=:draft, updateTime=:draftTime where id=:id")
    fun updateDraftAndTime(draft: String?, draftTime: Long, id: String?)

    @Query("update user_conversation set draft=:draft where id=:id")
    fun updateDraft(draft: String?, id: String?)

    @Query("update user_conversation set flag=:flag where id=:id")
    fun updateFlag(id: String?, flag: Int)

    @Delete
    fun delete(conversationBean: EntityConversation?)
}