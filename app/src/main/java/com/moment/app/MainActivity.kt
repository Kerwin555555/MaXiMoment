package com.moment.app

import android.os.Bundle
import androidx.activity.viewModels
import com.didi.drouter.annotation.Router
import com.gyf.immersionbar.ImmersionBar
import com.moment.app.databinding.ActivityMainBinding
import com.moment.app.eventbus.LogCancelEvent
import com.moment.app.ui.FragmentNavigator
import com.moment.app.ui.MainNaviConfig
import com.moment.app.ui.NaviTab
import com.moment.app.ui.OnTabStatusListener
import com.moment.app.utils.ActivityHolder
import com.moment.app.utils.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.Subscribe

@AndroidEntryPoint
@Router(scheme = ".*", host = ".*", path = "/main")
class MainActivity : BaseActivity(){

    private lateinit var binding: ActivityMainBinding
    private lateinit var pageAdapter: MainNaviConfig.MainPageAdapter
    private lateinit var fragmentNavigator: FragmentNavigator
    private val mainViewModel by viewModels<MainActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ImmersionBar.with(this)
            .statusBarDarkFont(false)
            .init()
        setSwipeBackEnable(false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ActivityHolder.onMainCreate(this)

        pageAdapter = MainNaviConfig.MainPageAdapter(this)
        fragmentNavigator = FragmentNavigator(supportFragmentManager,pageAdapter, R.id.fragment_container)
        mainViewModel.fetchTabs()

        initObservers()
    }

    private fun initObservers() {
        mainViewModel.naviTab.observe(this) {
            binding.bottomNav.attach(fragmentNavigator)
            binding.bottomNav.bindTabs(it)
            binding.bottomNav.listener = object : OnTabStatusListener {
                override fun onTabPreSelected(position: Int, tab: NaviTab?) {

                }

                override fun onTabSelected(position: Int, tab: NaviTab?, isClick: Boolean) {

                }

                override fun onTabUnselected(position: Int, tab: NaviTab?) {

                }

                override fun onTabReselected(position: Int, tab: NaviTab?) {

                }

                override fun onSpecialTabSelected(position: Int, tab: NaviTab?) {

                }

            }
            binding.bottomNav.selectPage("home")
        }
    }

    @Subscribe
    fun onLoginCancel(event: LogCancelEvent?) {
        binding.bottomNav.selectPage("home")
    }


    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}