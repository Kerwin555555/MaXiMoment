package com.moment.app.main_profile

//import com.moment.app.main_profile.adapters.MeAdapter
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.blankj.utilcode.util.BarUtils
import com.didi.drouter.api.DRouter
import com.moment.app.databinding.FragmentProfileBinding
import com.moment.app.eventbus.UpdateUserInfoEvent
import com.moment.app.hilt.app_level.MockData
import com.moment.app.login_page.service.FeedService
import com.moment.app.login_page.service.LoginService
import com.moment.app.main_home.subfragments.view.RecommendationEmptyView
import com.moment.app.main_profile.adapters.ProfilePostsAdapter
import com.moment.app.main_profile.views.ViewMeHeader
import com.moment.app.models.UserLoginManager
import com.moment.app.network.UserCancelException
import com.moment.app.network.startCoroutine
import com.moment.app.network.toast
import com.moment.app.ui.uiLibs.RefreshView
import com.moment.app.utils.BaseFragment
import com.moment.app.utils.applyMargin
import com.moment.app.utils.applyPaddingsWithDefaultZero
import com.moment.app.utils.cancelIfActive
import com.moment.app.utils.dp
import com.moment.app.utils.getScreenWidth
import com.moment.app.utils.loadAvatarBig
import com.moment.app.utils.requestNewSize
import com.moment.app.utils.resetGravity
import com.moment.app.utils.setBgWithCornerRadiusAndColor
import com.moment.app.utils.setOnAvoidMultipleClicksListener
import com.scwang.smart.refresh.layout.util.SmartUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject


@AndroidEntryPoint
class MyProfileFragment : BaseFragment() {
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
        binding.toolbar.isClickable = true
        binding.toolbar.applyPaddingsWithDefaultZero(top = BarUtils.getStatusBarHeight())
        binding.toolbar.requestNewSize(width = -1, height = BarUtils.getStatusBarHeight() + 50.dp)
        binding.income.setBgWithCornerRadiusAndColor(50.dp.toFloat(), 0x80000000.toInt())
        binding.recharge.setBgWithCornerRadiusAndColor(50.dp.toFloat(), 0x80000000.toInt())
        binding.setting.setOnAvoidMultipleClicksListener({
            DRouter.build("/settings").start()
        }, 500)
        binding.feedPublish.setOnAvoidMultipleClicksListener({
            DRouter.build("/feed/publish").start()
        }, 500)
        adapter = ProfilePostsAdapter(isMe = true)
        adapter.setHeaderAndEmpty(true)
        viewMeHeader = ViewMeHeader(requireContext())
        UserLoginManager.getUserInfo()?.let {
            loadAvatarBig(binding.avatar, userInfo = it)
            viewMeHeader.bindData(userInfo = it, isMe = true)
        }
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
        (binding.refreshView.getRecyclerView().itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false //remove shimmer
        binding.refreshView.getRecyclerView().addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {}
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (recyclerView.computeVerticalScrollOffset().toFloat() >= 2* getScreenWidth() /3) {
                    val mBgColor = Color.parseColor("#ffffff")
                    binding.toolbar.setBackgroundColor(mBgColor)
                } else {
                    val scrollRange = 2* getScreenWidth() /3
                    val mAlpha = Math.abs(255f / scrollRange * recyclerView.computeVerticalScrollOffset().toFloat()).toInt()
                    val mBgColor = Color.argb(mAlpha, 0xff, 0xff, 0xff)
                    binding.toolbar.setBackgroundColor(mBgColor)
                }
            }
        })

        binding.refreshView.setReboundInterpolator(SmartUtil(1))
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
                feedService.getFeeds(UserLoginManager.getUserId(), startPos, pageSize)
            }
            result.data?.feeds?.forEach{ it.isMe = true }
            UserLoginManager.getUserInfo()?.let { adapter.userInfo = it }
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
        UserLoginManager.getUserInfo()?.let {
            viewMeHeader.bindData(userInfo = it, isMe = true)
            adapter.userInfo = it
            adapter.notifyItemRangeChanged(0, adapter.data.size)
            loadAvatarBig(binding.avatar, userInfo = it)
        }
    }

    @Subscribe
    fun updateUserInfo(event: UpdateUserInfoEvent) {
        val a= 1
    }
}