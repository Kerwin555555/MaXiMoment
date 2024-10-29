package com.moment.app.utils

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import org.greenrobot.eventbus.EventBus

open class BaseFragment: Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        kotlin.runCatching {
            EventBus.getDefault().register(this)
        }
    }

    override fun onDestroyView() {
        try {
            EventBus.getDefault().unregister(this)
        } catch (var2: Exception) {
        }

        super.onDestroyView()
    }
}