package com.moment.app.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.moment.app.main_chat.ChatFragment
import com.moment.app.main_home.HomeFragment
import com.moment.app.main_profile.MeFragment

class MainNaviConfig {

    companion object {
        const val TAB_HOME: String = ""
        const val TAB_CHAT: String = ""
        const val TAB_ME: String = "me"

        val MAIN_PAGE: Array<Class<*>> = arrayOf<Class<*>>(
            HomeFragment::class.java,
            ChatFragment::class.java,
            MeFragment::class.java
        )
    }

    class MainPageAdapter(private val context: AppCompatActivity) : FragmentNavigatorInterface {
        override fun onCreateFragment(i: Int): Fragment {
            val fragmentManager = context.supportFragmentManager
            val fragmentFactory = fragmentManager.fragmentFactory
            return fragmentFactory.instantiate(context.classLoader,
                    MAIN_PAGE[i].name)
        }

        override fun getTag(i: Int): String {
            return MAIN_PAGE[i].name
        }

        override fun getCount(): Int {
            return MAIN_PAGE.size
        }
    }
}

interface FragmentNavigatorInterface {
    fun onCreateFragment(var1: Int): Fragment

    fun getTag(var1: Int): String

    fun getCount(): Int
}
