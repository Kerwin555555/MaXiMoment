package com.moment.app.main_profile

//import com.moment.app.main_profile.adapters.MeAdapter
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ScreenUtils
import com.bumptech.glide.Glide
import com.didi.drouter.api.DRouter
import com.moment.app.R
import com.moment.app.databinding.FragmentProfileBinding
import com.moment.app.eventbus.UpdateUserInfoEvent
import com.moment.app.hilt.app_level.MockData
import com.moment.app.login_page.service.FeedService
import com.moment.app.login_page.service.LoginService
import com.moment.app.main_home.subfragments.view.RecommendationEmptyView
import com.moment.app.main_profile.adapters.ProfilePostsAdapter
import com.moment.app.main_profile.views.ViewMeHeader
import com.moment.app.models.LoginModel
import com.moment.app.network.UserCancelException
import com.moment.app.network.startCoroutine
import com.moment.app.network.toast
import com.moment.app.ui.uiLibs.RefreshView
import com.moment.app.utils.BaseFragment
import com.moment.app.utils.applyMargin
import com.moment.app.utils.cancelIfActive
import com.moment.app.utils.dp
import com.moment.app.utils.loadAvatarBig
import com.moment.app.utils.resetGravity
import com.moment.app.utils.setBgWithCornerRadiusAndColor
import com.moment.app.utils.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject


@AndroidEntryPoint
class MeFragment : BaseFragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var adapter: ProfilePostsAdapter
    private var startPos = -1
    private var pageSize = 10
    private var currentJob: Job? = null
    private lateinit var viewMeHeader: ViewMeHeader


    @Inject
    @MockData
    lateinit var feedService: FeedService
    @Inject
    lateinit var loginService: LoginService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()

        loadData(false)
    }

    private fun initUI() {
        binding.income.setBgWithCornerRadiusAndColor(50.dp.toFloat(), 0x80000000.toInt())
        binding.recharge.setBgWithCornerRadiusAndColor(50.dp.toFloat(), 0x80000000.toInt())
        binding.setting.applyMargin(top = BarUtils.getStatusBarHeight() + 9.dp)
        binding.setting.setOnSingleClickListener({
            DRouter.build("/settings").start()
        }, 500)
        adapter = ProfilePostsAdapter()
        adapter.setHeaderAndEmpty(true)
        viewMeHeader = ViewMeHeader(requireContext())
        loadAvatarBig(binding.avatar)
        viewMeHeader.bindData(LoginModel.getUserInfo()!!)
        adapter.setHeaderView(viewMeHeader)

        binding.refreshView.initWith(
            adapter = adapter,
            emptyView = RecommendationEmptyView(this.requireContext()).apply {
                getLoadingPage().resetGravity(Gravity.CENTER_HORIZONTAL)
                getLoadingPage().applyMargin(top = 80.dp)
            },
            refreshHeader = RefreshView(requireContext()).apply {
                getBinding().progress.resetGravity(Gravity.CENTER_HORIZONTAL)
                getBinding().progress.applyMargin(top = BarUtils.getStatusBarHeight() + 30.dp)
            }
        ) { isLoadMore ->
            loadData(isLoadMore as Boolean)
        }
        binding.refreshView.setEnableHeaderTranslationContent(false)
    }

    private fun loadData(isLoadMore: Boolean) {
        currentJob?.cancelIfActive()
        currentJob = startCoroutine({
            if (!isLoadMore) {
                startPos = 0
            }
            val result = withContext(Dispatchers.IO) {
//                if (!NetworkUtils.isConnected()) {
//                    throw RuntimeException("Net work Error")
//                }
                feedService.getFeeds(LoginModel.getUserId(), startPos, pageSize)
            }
            startPos = result.data!!.next_start
            binding.refreshView.onSuccess(result.data!!.feeds!!.toMutableList(), isLoadMore, result.data!!.has_next)
        }){
            if (it.throwable is UserCancelException) {
                return@startCoroutine
            }
            it.toast()
            binding.refreshView.onFail(isLoadMore, it.message)
        }
    }


    override fun onResume() {
        super.onResume()
        viewMeHeader.bindData(LoginModel.getUserInfo()!!)
        loadAvatarBig(binding.avatar)
    }

    @Subscribe
    fun updateUserInfo(event: UpdateUserInfoEvent) {
        val a= 1
    }
}