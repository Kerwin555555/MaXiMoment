package com.moment.app.main_home.subfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.room.withTransaction
import com.moment.app.databinding.SubFragmentRecommendationBinding
import com.moment.app.hilt.app_level.MockData
import com.moment.app.main_home.subfragments.adapters.RecommendationAdapter
import com.moment.app.main_home.subfragments.db.HomeRecommendationListDatabase
import com.moment.app.main_home.subfragments.db.UserInfoEntity
import com.moment.app.main_home.subfragments.models.UserInfoList
import com.moment.app.main_home.subfragments.service.HomeService
import com.moment.app.main_home.subfragments.view.RecommendationEmptyView
import com.moment.app.models.LoginModel
import com.moment.app.network.MomentNetwork
import com.moment.app.network.UserCancelException
import com.moment.app.network.startCoroutine
import com.moment.app.network.toast
import com.moment.app.ui.uiLibs.DataDividerItemDecoration
import com.moment.app.utils.BaseFragment
import com.moment.app.utils.cancelIfActive
import com.scwang.smart.refresh.layout.util.SmartUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class RecommendationFragment: BaseFragment() {
    private var startPos = -1
    private var pageSize = 10


    private lateinit var binding: SubFragmentRecommendationBinding
    private var currentJob: Job? = null

    @Inject
    @MockData
    lateinit var homeService: HomeService

    @Inject
    lateinit var userInfoDb: HomeRecommendationListDatabase

    val adapter by lazy {
         RecommendationAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SubFragmentRecommendationBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()

        loadData(false)
    }

    private fun initUI() {
        binding.refreshView.initWith(
            adapter = adapter,
            emptyView = RecommendationEmptyView(this.requireContext()).apply {
                setBackgroundColor(0x00000000)
            }) { isLoadMore ->
             loadData(isLoadMore as Boolean)
        }
        binding.refreshView.getRecyclerView().addItemDecoration(DataDividerItemDecoration(
            adapter = adapter,
            size = 0.5f,
            dividerColor = 0xffF4F4F4.toInt(),
            horizontalMargin = 15
        ))
    }

    private fun loadData(isLoadMore: Boolean) {
        currentJob?.cancelIfActive()
        currentJob = startCoroutine({
            if (!isLoadMore) {
                startPos = 0
                val key = "${LoginModel.getUserInfo()?.userId?:""}_recommendation"
                if (!MomentNetwork.coldStartMap.containsKey(key)) {
                    kotlin.runCatching {
                        withContext(Dispatchers.IO) {
                            userInfoDb.UserInfoEntityDao().getAllUserInfoEntities()
                                .map {
                                    it.userInfo
                                }
                        }.apply {
                            if (size != 0) {
                                binding.refreshView.onSuccess(
                                    this.toMutableList(),
                                    isLoadMore,
                                    false
                                )
                            }
                            MomentNetwork.coldStartMap[key] = true
                        }
                    }
                }
            }
            val result = withContext(Dispatchers.IO) {
//                if (!NetworkUtils.isConnected()) {
//                    throw RuntimeException("Net work Error")
//                }
                homeService.getOnlineUsersForSlide(startPos, pageSize)
            }
            //存档到数据库！
            if (!isLoadMore) {
                this.async(Dispatchers.IO) {
                    val item = (result.data as UserInfoList).user_infos?.map { it ->
                        UserInfoEntity(
                            userId = it.userId!!,
                            userInfo = it
                        )
                    }
                    userInfoDb.withTransaction {
                        userInfoDb.UserInfoEntityDao().clearUserInfoEntity()
                        userInfoDb.UserInfoEntityDao().insertUserInfo(item!!)
                    }
                }
            }
            startPos = result.data!!.next_start
            binding.refreshView.onSuccess(result.data!!.user_infos!!, isLoadMore, result.data!!.has_next)
        }){
            if (it.throwable is UserCancelException) {
                return@startCoroutine
            }
            it.toast()
            binding.refreshView.onFail(isLoadMore, it.message)
        }
    }
}