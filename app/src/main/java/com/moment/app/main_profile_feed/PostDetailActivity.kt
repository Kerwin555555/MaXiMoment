package com.moment.app.main_profile_feed

import android.os.Bundle
import android.view.Gravity
import android.view.View
import com.blankj.utilcode.util.KeyboardUtils
import com.didi.drouter.annotation.Router
import com.moment.app.databinding.ActivityFeedPostBinding
import com.moment.app.hilt.app_level.MockData
import com.moment.app.login_page.service.FeedService
import com.moment.app.main_profile_feed.adapters.PostDetailCommentsAdapter
import com.moment.app.main_profile_feed.views.ViewFeedContentHeader
import com.moment.app.main_profile_feed.views.ViewFeedPicturesHeader
import com.moment.app.main_home.subfragments.view.RecommendationEmptyView
import com.moment.app.main_profile.entities.PostBean
import com.moment.app.main_profile_feed.views.DetailsFeedView
import com.moment.app.models.UserLoginManager
import com.moment.app.network.UserCancelException
import com.moment.app.network.startCoroutine
import com.moment.app.network.toast
import com.moment.app.ui.uiLibs.RefreshView
import com.moment.app.utils.BaseActivity
import com.moment.app.utils.applyMargin
import com.moment.app.utils.cancelIfActive
import com.moment.app.utils.dp
import com.moment.app.utils.immersion
import com.moment.app.utils.resetGravity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import javax.inject.Inject


@Router(scheme = ".*", host = ".*", path = "/feed/detail")
@AndroidEntryPoint
class PostDetailActivity : BaseActivity(){
    //注意 到达这个页面，一定要有userInfo!
    private lateinit var binding: ActivityFeedPostBinding
    private var header: DetailsFeedView? = null
    private var postId: String? = ""
    private var postBean: PostBean? = null
    private var startPos = -1
    private var currentJob: Job? = null
    private val adapter by lazy {
        PostDetailCommentsAdapter()
    }

    @Inject
    @MockData
    lateinit var feedService: FeedService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        immersion()
        binding = ActivityFeedPostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // place_holder
        if (intent.getStringExtra("post_id") != null) {
            postId = intent.getStringExtra("post_id")
        }
        if (intent.getSerializableExtra("post") != null) {
            postBean = intent.getSerializableExtra("post") as PostBean?
            if (postBean?.id != null && postId == null) postId = postBean!!.id
        }

        initUI()
        loadPost()
        loadData(false)
    }

    private fun initUI() {
        adapter.setHeaderAndEmpty(true)
        postBean?.let {
            bindPost(it)
            adapter.setHeaderView(header as View)
            header!!.bindData(it, binding.toolBar.getBinding())
        } ?: let {
            header = ViewFeedContentHeader(this@PostDetailActivity)
            adapter.setHeaderView(header as View) //placeholder
        }
        binding.refreshView.initWith(
            adapter = adapter,
            emptyView = RecommendationEmptyView(this).apply {
                getLoadingPage().resetGravity(Gravity.CENTER_HORIZONTAL)
                getLoadingPage().applyMargin(top = 80.dp)
            },
            refreshHeader = RefreshView(this)
        ) { isLoadMore ->
            loadData(isLoadMore as Boolean)
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
                feedService.getComments(UserLoginManager.getUserId(), startPos)
            }
            startPos = result.data!!.cursor
            binding.refreshView.onSuccess(result.data!!.comments!!.toMutableList(), isLoadMore, result.data!!.cursor > 0)
        }){
            if (it.throwable is UserCancelException) {
                return@startCoroutine
            }
            it.toast()
            binding.refreshView.onFail(isLoadMore, it.message)
        }
    }

    fun bindPost(postBean: PostBean?) {
        header = if (postBean?.isPictureFeed() == true) {
             ViewFeedPicturesHeader(this)
        } else {
             ViewFeedContentHeader(this)
        }
    }

    private fun loadPost() {
        if (true) {
            return
        }
        startCoroutine({
            val result = feedService.getFeedDetail(postId)
            postBean = result.data
            bindPost(postBean)
        }){
            it.toast()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        kotlin.runCatching {
            KeyboardUtils.hideSoftInput(this)
        }
    }
}