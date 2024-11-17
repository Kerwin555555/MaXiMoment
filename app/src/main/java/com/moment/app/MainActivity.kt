package com.moment.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.didi.drouter.annotation.Router
import com.moment.app.databinding.ActivityMainBinding
import com.moment.app.eventbus.LogCancelEvent
import com.moment.app.eventbus.LogoutEvent
import com.moment.app.hilt.app_level.MockData
import com.moment.app.main_chat.ConversationChangeListener
import com.moment.app.main_chat.GlobalConversationManager
import com.moment.app.models.AppConfigManager
import com.moment.app.ui.FragmentNavigator
import com.moment.app.ui.MainNaviConfig
import com.moment.app.ui.NaviTab
import com.moment.app.ui.OnTabStatusListener
import com.moment.app.utils.ActivityHolder
import com.moment.app.utils.BaseActivity
import com.moment.app.utils.immersion
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

@AndroidEntryPoint
@Router(scheme = ".*", host = ".*", path = "/main")
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var pageAdapter: MainNaviConfig.MainPageAdapter
    private lateinit var fragmentNavigator: FragmentNavigator
    private val mainViewModel by viewModels<MainActivityViewModel>()

    //accept changes from conversation manager
    private val mainActivityConversationChangeListener = object: ConversationChangeListener {
        override fun onConversationsChange() {


        }
    }

    @Inject
    @MockData
    lateinit var conversationsManager: GlobalConversationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        immersion()
        setSwipeBackEnable(false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ActivityHolder.onMainCreate(this)
        AppConfigManager.updateConfig()
        pageAdapter = MainNaviConfig.MainPageAdapter(this)
        fragmentNavigator =
            FragmentNavigator(supportFragmentManager, pageAdapter, R.id.fragment_container)
        mainViewModel.fetchTabs()

        conversationsManager.addConversationListener(mainActivityConversationChangeListener)
        conversationsManager.loadMetaDataFromBackendAndUpdateUserInfos()
        conversationsManager.simplyRefreshListFromDB()

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
        EventBus.getDefault().unregister(this)
        conversationsManager.removeConversationListener(mainActivityConversationChangeListener)
        super.onDestroy()
    }

    @Subscribe
    fun onLogout(event: LogoutEvent) {
        //start login page
        val intent = Intent(this, SplashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}