package com.moment.app.ui

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.moment.app.R
import com.moment.app.utils.buildHorizontalChain


class BottomBar: ConstraintLayout {
    private var currentPosition = -1
    private var tabsCache : MutableList<NaviTab>? = null
    var listener: OnTabStatusListener? = null
        set(value) {
            field = value
        }

    companion object {
        val DEFAULT_NAVI_TABS = mutableListOf(
            NaviTab().apply {
                tab_icon_Id = R.drawable.home_icon_selector
                tab_name_Id = R.string.title_home
                page_name = ""
                real_page_index = 0
            },
            NaviTab().apply {
                tab_icon_Id = R.drawable.chat_icon_selector
                tab_name_Id = R.string.title_chat
                page_name = ""
                real_page_index = 1
            },
            NaviTab().apply {
                tab_icon_Id = R.drawable.profile_icon_selector
                tab_name_Id = R.string.title_profile
                page_name = ""
                real_page_index = 2
            },
        )
    }
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var fragmentNavigator: FragmentNavigator? = null

    fun bindTabs(tabs: MutableList<NaviTab>) {
        generateLayout(tabs)
    }

    fun attach(fragmentNavigator: FragmentNavigator) {
        this.fragmentNavigator = fragmentNavigator
    }

    private fun generateLayout(tabs: MutableList<NaviTab>) {
        this.removeAllViews()
        tabsCache = tabs
        val numberOfNaviTabs = tabs.size
        val navTabIds = IntArray(numberOfNaviTabs)

        for (i in 0 until numberOfNaviTabs) {
            val tab = tabs[i]
            val tabView = when (tab.page_name) {
                "" -> {
                    BottomHomeView(context).apply {
                        getBinding().home.setImageResource(tab.tab_icon_Id)
                    }
                }

                "" -> {
                    BottomChatView(context).apply {
                        getBinding().chat.setImageResource(tab.tab_icon_Id)
                    }
                }

                "" -> {
                    BottomProfileView(context).apply {
                        getBinding().profile.setImageResource(tab.tab_icon_Id)
                    }
                }

                else -> {
                    // fall back use general views
                    BottomChatView(context)
                }
            }
            tabView.id = generateViewId()
            addView(tabView)
            navTabIds[i] = tabView.id
            tabView.setOnClickListener {
                onNaviTabClick(position = i, true)
            }

        }

        buildHorizontalChain(navTabIds)
    }


    private fun onNaviTabClick(position: Int, isClick: Boolean) {
        if (tabsCache == null || childCount != tabsCache?.size) return
        kotlin.runCatching {
            tabsCache?.let { tabs ->
                for (i in tabs.indices) {
                    val tab: NaviTab = tabs.get(i)
                    if (listener == null) return
                    if (position == i) { //当前页面被点击
                        if (tab.no_page) { //特殊navi点击
                            listener?.onSpecialTabSelected(position, tab)
                        } else {
                            //更新UI状态
                            onTabSelected(position)
                            if (currentPosition == position) { //当前页面已存在
                                listener?.onTabReselected(tab.real_page_index, tab)
                            } else { //当前页面不存在
                                listener?.onTabPreSelected(tab.real_page_index, tab)

                                if (fragmentNavigator != null) {
                                    fragmentNavigator!!.showFragment(tab.real_page_index)
                                }
                                listener?.onTabSelected(tab.real_page_index, tab, isClick)
                                currentPosition = position
                            }
                        }
                    } else { //其他未被选中页面
                        if (!tab.no_page) {
                            listener?.onTabUnselected(tab.real_page_index, tab)
                        }
                    }
                }
            }
        }
    }

    fun selectPage(pageName: String) {
        tabsCache?.forEachIndexed{ i, tab->
            if (tab.page_name ==  pageName) {
                onNaviTabClick(position = i, false)
                return
            }
        }
    }

    /**
     * page位置
     * @param pagePosition
     */
    private fun onTabSelected(pagePosition: Int) {
        if (childCount != tabsCache?.size) return

        tabsCache?.forEachIndexed { i, tab ->
            if (tab.no_page) {
                return@forEachIndexed
            }

            getChildAt(i).isSelected = pagePosition == i
        }
    }
}


interface OnTabStatusListener {
    fun onTabPreSelected(position: Int, tab: NaviTab?)

    fun onTabSelected(position: Int, tab: NaviTab?, isClick: Boolean)

    fun onTabUnselected(position: Int, tab: NaviTab?)

    fun onTabReselected(position: Int, tab: NaviTab?)

    fun onSpecialTabSelected(position: Int, tab: NaviTab?)
}
