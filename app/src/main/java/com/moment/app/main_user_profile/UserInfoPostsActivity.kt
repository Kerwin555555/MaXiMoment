package com.moment.app.main_user_profile

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.BarUtils
import com.didi.drouter.annotation.Router
import com.moment.app.databinding.ActivityUserDetailBinding
import com.moment.app.datamodel.Results
import com.moment.app.datamodel.UserInfo
import com.moment.app.hilt.app_level.MockData
import com.moment.app.login_page.service.FeedService
import com.moment.app.main_home.subfragments.view.RecommendationEmptyView
import com.moment.app.main_profile.adapters.ProfilePostsAdapter
import com.moment.app.main_profile.entities.FeedList
import com.moment.app.main_profile.entities.PostBean
import com.moment.app.main_profile.views.ViewMeHeader
import com.moment.app.models.UserLoginManager
import com.moment.app.network.UserCancelException
import com.moment.app.network.startCoroutine
import com.moment.app.network.toast
import com.moment.app.ui.uiLibs.RefreshView
import com.moment.app.utils.BaseActivity
import com.moment.app.utils.MomentLoadingDrawable
import com.moment.app.utils.applyMargin
import com.moment.app.utils.applyPaddingsWithDefaultZero
import com.moment.app.utils.cancelIfActive
import com.moment.app.utils.dp
import com.moment.app.utils.getScreenWidth
import com.moment.app.utils.immersion
import com.moment.app.utils.loadAvatarBig
import com.moment.app.utils.requestNewSize
import com.moment.app.utils.resetGravity
import com.scwang.smart.refresh.layout.util.SmartUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
@Router(scheme = ".*", host = ".*", path = "/user")
class UserInfoPostsActivity : BaseActivity(){

    private lateinit var binding: ActivityUserDetailBinding
    private var userId: String? = ""
    private var userInfo: UserInfo? = null
    private lateinit var viewMeHeader: ViewMeHeader
    private lateinit var adapter: ProfilePostsAdapter
    private var currentJob: Job? = null
    private var startPos = -1
    private var pageSize = 10


    @Inject
    @MockData
    lateinit var feedService: FeedService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        immersion()
        binding = ActivityUserDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.avatar.setImageDrawable(MomentLoadingDrawable(this))
        binding.back.setOnClickListener {
            finish()
        }

        binding.toolbar.applyPaddingsWithDefaultZero(top = BarUtils.getStatusBarHeight())
        binding.toolbar.requestNewSize(width = -1, height = BarUtils.getStatusBarHeight() + 50.dp)
        ensureParam()

        viewMeHeader = ViewMeHeader(this)
        userInfo?.let {
            viewMeHeader.bindData(userInfo = it, isMe = false)
            loadAvatarBig(binding.avatar, it)
        } ?: let {
            viewMeHeader.init(isMe = false)
        }

        adapter = ProfilePostsAdapter(isMe = false)
        adapter.setHeaderView(viewMeHeader)
        binding.refreshView.initWith(
            adapter = adapter,
            emptyView = RecommendationEmptyView(this).apply {
                getLoadingPage().resetGravity(Gravity.CENTER_HORIZONTAL)
                getLoadingPage().applyMargin(top = 80.dp)
            },
            refreshHeader = RefreshView(this).apply {
                getBinding().progress.resetGravity(Gravity.CENTER_HORIZONTAL)
                getBinding().progress.applyMargin(top = BarUtils.getStatusBarHeight() + 30.dp)
            }
        ) { isLoadMore ->
            val loadMore = isLoadMore as Boolean
            if (loadMore) {
                loadData(loadMore)
            } else {
                firstLoad()
            }

        }
        binding.refreshView.setEnableHeaderTranslationContent(false)
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
        firstLoad()
        binding.refreshView.setReboundInterpolator(SmartUtil(1))
    }

    private fun ensureParam() {
        val userInfo = intent.getSerializableExtra("userInfo")  as UserInfo?
        userInfo?.let {
            it.user_id?.let { id ->
                this.userId = id
            }
        }
        if (!intent.getStringExtra("id").isNullOrEmpty()) {
            userId = intent.getStringExtra("id")
        }
    }

    private fun firstLoad() {
        currentJob?.cancelIfActive()
        currentJob = startCoroutine({
            startPos = 0
            val list = listOf(this.async(Dispatchers.IO){
                return@async withContext(Dispatchers.IO) {
//                if (!NetworkUtils.isConnected()) {
//                    throw RuntimeException("Net work Error")
//                }
                    feedService.getFeeds(userId, startPos, pageSize)
                }
            }, this.async(Dispatchers.IO) {
                   feedService.getUserInfo(userId)
            })
            val r = list.awaitAll()
            userInfo = (r[1] as Results<UserInfo>).data ?: userInfo
            val feedList = (r[0] as Results<FeedList?>).data
            feedList?.feeds?.forEach{ it.isMe = false }
            userInfo?.let { it ->
                adapter.userInfo = it
                viewMeHeader.bindData(userInfo = it, isMe = false)
                loadAvatarBig(binding.avatar, it)
            }
            startPos = feedList?.next_start ?: -1
            binding.refreshView.onSuccess(feedList?.feeds?.toMutableList() ?: mutableListOf<PostBean>(),
                false, feedList?.has_next ?: false)
        }){
            if (it.throwable is UserCancelException) {
                return@startCoroutine
            }
            it.toast()
            binding.refreshView.onFail(false, it.message)
        }
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
            result.data?.feeds?.forEach{ it.isMe = false }
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
}