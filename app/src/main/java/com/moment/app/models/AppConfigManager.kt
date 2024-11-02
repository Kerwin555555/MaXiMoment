package com.moment.app.models

import com.moment.app.utils.BaseBean

object AppConfigManager {
    var momentConfig: MomentConfig? = null

    fun updateConfig() {

    }
}

class MomentConfig: BaseBean() {
    var enableFacebookTokenCheck: Boolean = false
    var disableHXLogin: Boolean = false
}