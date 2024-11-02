package com.moment.app.main_chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.gyf.immersionbar.ImmersionBar
import com.moment.app.R
import com.moment.app.databinding.FragmentChatBinding
import com.moment.app.main_chat.fragments.ConversationsListFragment
import com.moment.app.main_chat.fragments.FriendListFragment
import com.moment.app.utils.BaseFragment
import com.moment.app.utils.applyMargin
import com.moment.app.utils.applyPaddingsWithDefaultZero
import com.moment.app.utils.getStatusBarHeight
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MessagingContactListFragment : BaseFragment() {

    private lateinit var binding: FragmentChatBinding
    private lateinit var adapter: Adapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ImmersionBar.with(this)
            .statusBarDarkFont(false)
            .fitsSystemWindows(false)
            .init()
        initUI()
    }

    private fun initUI() {
        binding.tabs.applyMargin(top = getStatusBarHeight())
        adapter = Adapter(this)
        binding.viewpager2.adapter = adapter
        binding.viewpager2.isSaveEnabled = false
        binding.viewpager2.offscreenPageLimit = 1
        TabLayoutMediator(
            binding.tabs, binding.viewpager2
        ) { tab, position ->
            kotlin.runCatching {
                tab.setCustomView(R.layout.tags_select_tab_textview)
                tab.customView!!.findViewById<TextView>(R.id.text).text =
                    when (position) {
                        0 -> "Chat"
                        else -> "Friends"
                    }
            }.onFailure {
            }
        }.attach()
        resetTabLayoutPadding(binding.tabs)
    }

    fun resetTabLayoutPadding(tabLayout: TabLayout) {
        for (i in 0 until tabLayout.tabCount) {
            tabLayout.getTabAt(i)?.view?.apply {
                applyPaddingsWithDefaultZero()
//                applyMargin(start = SizeUtils.dp2px(if (i ==0) 15f else 10f),
//                    end = SizeUtils.dp2px(if (i == tabLayout.tabCount - 1) 15f else 0f))
            }
        }
    }

    class Adapter(f: Fragment): androidx.viewpager2.adapter.FragmentStateAdapter(f) {

        override fun getItemCount(): Int {
            return 2
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ConversationsListFragment()
                else -> FriendListFragment()
            }
        }
    }
}