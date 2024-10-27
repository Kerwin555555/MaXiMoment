package com.moment.app.main_chat

import android.text.TextUtils
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMConversation
import com.moment.app.datamodel.Results
import com.moment.app.datamodel.UserInfo
import com.moment.app.utils.BaseBean
import retrofit2.http.GET

interface ThreadService {
    @GET("api/sns/v1/lit/user/conversations")
    suspend fun conversations(): Results<ThreadList>
}


class ThreadList : BaseBean() {
    var conversations: List<BackendThread>? = null
}

class BackendThread : BaseBean {
    var conversation_id: String? = null
    var create_time: String? = null
    var user_id: String? = null
    var userInfo: UserInfo? = null
    var pinned: Boolean = false
    var conversation_type: Int = 0
    var reply_later: Boolean = false
    var deleted: Boolean = false

    @Transient
    var emConversation: EMConversation? = null
        get() {
            if (field == null) {
                field = EMClient.getInstance().chatManager().getConversation(conversation_id)
            }
            return field
        }
        private set

    constructor()

    constructor(conversation_id: String?) {
        this.conversation_id = conversation_id
    }

    constructor(conversation_id: String?, create_time: String?, user_id: String?) {
        this.conversation_id = conversation_id
        this.create_time = create_time
        this.user_id = user_id
    }

    override fun hashCode(): Int {
        if (TextUtils.isEmpty(conversation_id)) {
            return super.hashCode()
        }
        return conversation_id.hashCode()
    }

    override fun equals(obj: Any?): Boolean {
        return if (obj is BackendThread) {
            TextUtils.equals(obj.conversation_id, conversation_id)
        } else {
            false
        }
    }

    override fun toString(): String {
        return ("ConversationInfo.Bean: [conversation_id = " + conversation_id
                + ", user_id = " + user_id
                + ", create_time = " + create_time)
    }
}