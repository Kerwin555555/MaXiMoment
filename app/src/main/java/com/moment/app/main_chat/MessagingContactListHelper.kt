package com.moment.app.main_chat

import com.tencent.mmkv.MMKV

object MessagingContactListHelper {
    const val REMIND_MSG_END_TIME = "remind_msg_end_time" // 消息置顶结束时间
    const val REMIND_TIP_SHOW = "remind_tip_show" // 消息置顶提示是否展示
    const val REMIND_TOP_COUNT = "remind_top_count" // 消息置顶次数
    const val CUSTOM_ACCOST_CONTENT = "custom_accost_content" //搭讪模板，自定义搭讪文案

    const val PAT_CHAT_LIST_GET_TIME = "pat_chat_list_get_time"

    const val CONVERSATION_DELETE_NOT_SHOW_DIALOG = "conversation_delete_not_show_dialog" // 删除会话不在提示二次确认

    private const val SPECIAL_EFFECT_LIST = "special_effect_lit" // 特效表情列表
    const val HAS_SHOW_ROCK_GAME_TIP = "has_show_rock_game_tip" // 是否已展示猜拳问答引导
    const val HAS_SHOW_ROCK_GAME_BUTTON = "has_show_rock_game_button" // 是否已点击猜拳问答
    const val HAS_SHOW_ROCK_GAME_MSG_TIP = "has_show_rock_game_msg_tip" // 是否展示过猜拳问答消息引导
    const val IS_SHOW_ROCK_GAME = "is_show_rock_game" // 是否展示猜拳问答图标
    const val HAS_SHOW_BOTTOM_EMOJI_ANIMATE = "has_show_bottom_emoji_animate" // 是否展示过底部表情栏横向滚动提示
    const val CHAT_SORT_TYPE = "chat_sort_type" // chat列表排序方式，0：最后聊天时间排序，1：在线状态排序
    const val QUICK_REPLY_SHOW_TIME = "quick_reply_show_time" // quick reply 上次展示时间，下次展示需要间隔24小时
    const val HAS_LOAD_CLOUD_CONVERSATION = "has_load_cloud_conversation" // 是否已经加载过线上会话列表
    const val HAS_LOAD_LOCAL_CONVERSATION = "has_load_local_conversation" // 是否已经加载过本地会话列表
    const val HAS_SHOW_FEED_CHAT_SAY_HI_DIALOG = "has_show_feed_chat_say_hi_dialog" // 是否展示过帖子say hi点击弹框提示
    const val IM_EFFECT_KEYWORD = "im_effect_keyword" // 消息特效 关键词匹配
    const val HAS_SHOW_CHAT_SORT_DOT = "has_show_chat_sort_dot" // 是否展示过会话排序按钮小红点
    const val HAS_CLICK_STAR_TIP = "has_click_star_tip" // 是否点击过特别关注提示
    const val HAS_SHOW_STAR_TIP = "has_show_star_tip" // 是否展示过特别关注提示
    const val HAS_SHOW_FRIEND_SCROLL_TIP = "has_show_friend_scroll_tip_new" // 是否展示过chat顶部滑动提示

    private val mmkv by lazy {
        MMKV.mmkvWithID("lit_chat_sp")
    }

    @JvmStatic
    fun getBoolean(key: String?, value: Boolean = false): Boolean {
        return mmkv.getBoolean(key, value)
    }

    @JvmStatic
    fun getInt(key: String?, defValue: Int = 0): Int {
        return mmkv.getInt(key, defValue)
    }

    @JvmStatic
    fun getString(key: String?, defValue: String? = ""): String? {
        return mmkv.getString(key, defValue)
    }

    @JvmStatic
    fun getLong(key: String?, defValue: Long = 0): Long {
        return mmkv.getLong(key, defValue)
    }

    @JvmStatic
    fun save(key: String?, value: Boolean) {
        mmkv.edit().putBoolean(key, value).apply()
    }

    @JvmStatic
    fun save(key: String?, value: Int) {
        mmkv.edit().putInt(key, value).apply()
    }

    @JvmStatic
    fun save(key: String?, value: Long) {
        mmkv.edit().putLong(key, value).apply()
    }

    @JvmStatic
    fun save(key: String?, value: String?) {
        mmkv.edit().putString(key, value).apply()
    }

    @JvmStatic
    fun remove(key: String?) {
        mmkv.edit().remove(key).apply()
    }
}