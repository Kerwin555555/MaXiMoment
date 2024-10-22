package com.moment.app.utils

import android.os.Bundle
import com.moment.app.R
import me.imid.swipebacklayout.lib.app.SwipeBackActivity


/**
 * 这些目录库在/Users/xxx/.gradle/caches/modules-2/files-2.1/xxxxx.widget/swipeback/0.0.2/a07a1f45ad81d512adba12df490c1916c41b276f
 *
 */
open class BaseActivity: SwipeBackActivity() {
    override fun setSwipeBackEnable(enable: Boolean) {
        super.setSwipeBackEnable(enable)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onTransitionCreate()
    }
    protected fun onTransitionCreate() {
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out)
    }

    protected fun onTransitionFinish() {
        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out)
    }

    override fun finish() {
        super.finish()
        onTransitionFinish()
    }
}