package com.moment.app.models

import com.moment.app.utils.BaseBean

object AppConfigManager {
    var momentConfig: MomentConfig? = MomentConfig()

    fun updateConfig() {

    }
}

class MomentConfig: BaseBean() {
    var enableFacebookTokenCheck: Boolean = true
    var disableHXLogin: Boolean = false
}