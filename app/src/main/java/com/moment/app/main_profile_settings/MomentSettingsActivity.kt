package com.moment.app.main_profile_settings

import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.didi.drouter.annotation.Router
import com.moment.app.R
import com.moment.app.databinding.ActivityLayoutSettingBinding
import com.moment.app.hilt.app_level.MockData
import com.moment.app.login_page.service.LoginService
import com.moment.app.models.UserLoginManager
import com.moment.app.main_profile_settings.subpages.LanguageSettingFragment
import com.moment.app.ui.uiLibs.DataDividerItemDecoration
import com.moment.app.utils.BaseActivity
import com.moment.app.utils.rightInRightOut
import com.moment.app.utils.setOnAvoidMultipleClicksListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
@Router(scheme = ".*", host = ".*", path = "/settings")
class MomentSettingsActivity : BaseActivity(){
    private lateinit var binding: ActivityLayoutSettingBinding
    private val adapter = Adapter()


    @Inject
    @MockData
    lateinit var loginService: LoginService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLayoutSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSwipeBackEnable(false)
        binding.rv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.rv.adapter = adapter
        binding.back.setOnClickListener {
            finish()
        }

        adapter.setNewData(mutableListOf(
            Item("notification", R.mipmap.setting_notification, "Notifications"),
            Item("language", R.mipmap.setting_language, "Languages"),
            Item("privacy", R.mipmap.setting_privacy, "Privacy"),
            Item("aboutus", R.mipmap.setting_about_us, "About Us")
        ))


        adapter.setOnItemClickListener { adapter, view, position ->
            if (position == 1) {
                rightInRightOut().add(R.id.container,  LanguageSettingFragment(), "LanguageSettingFragment").addToBackStack(null).commitAllowingStateLoss()
            }
        }

        binding.rv.adapter = adapter

        binding.rv.addItemDecoration(
            DataDividerItemDecoration(
                adapter = adapter,
                size = 0.5f,
                dividerColor = 0xffF1F1F1.toInt(),
                horizontalMargin = 26)
        )

        binding.logOut.setOnAvoidMultipleClicksListener({
            UserLoginManager.logout(true, loginService)
        }, 500)
    }

    inner class Adapter: BaseQuickAdapter<Item, BaseViewHolder>(R.layout.settings_item_view) {
        override fun convert(helper: BaseViewHolder, item: Item) {
            helper.setText(R.id.text, item.text)
            helper.setImageResource(R.id.icon, item.icon)
        }
    }

    data class Item(var type: String, @DrawableRes var icon: Int, var text: String)
}