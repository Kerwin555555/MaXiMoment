package com.moment.app.datamodel

import com.moment.app.utils.BaseBean
import java.io.File

data class UserInfo(
    var userId: String? = "",
    var gender: String? = "",
    var birthday: String? = "",
    var age: Int? = 0,
    var name: String? = "MomentFan",
    var session: String? = "",
    var bio: String? = "",
    var finished_info: Boolean = true,
    var country: String? = "",
    var forbidden_session: String? ="",
    var huanxin:HuanxinBean? = null,
    var followed: Boolean? = false,
    var avatar: String? = null,
    var imagesWallList: MutableList<String> = mutableListOf(),
    var friends_count: Int? = 0,
    var following_count: Int? = 0,
    var follower_count: Int? = 0,
): BaseBean() {
}

class HuanxinBean : BaseBean() {
    /**
     * password : b7702c283c823038ce4776cf9a3735fb
     * user_id : love1236383185000000731
     */
    var password: String? = null
    var user_id: String? = null
}


class UserSettings : BaseBean() {
    //var allowCrossRegionMatch: Boolean = true //默认勾选
}


data class UpdateInfoResult(
    var update_info_loc: Boolean = false,
    var show_loc_as_region: Boolean = false,
    var loc_list: List<String> = emptyList(),
    var updated_info: UserInfo?,
    var register_prefer_select: Boolean = false,
//    var user_settings: UserSift?
): BaseBean()