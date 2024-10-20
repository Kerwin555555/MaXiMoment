package com.moment.app.main_home.subfragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.room.withTransaction
import com.blankj.utilcode.util.NetworkUtils
import com.moment.app.databinding.SubFragmentRecommendationBinding
import com.moment.app.hilt.app_level.MockData
import com.moment.app.main_home.subfragments.adapters.RecommendationAdapter
import com.moment.app.main_home.subfragments.db.HomeRecommendationListDatabase
import com.moment.app.main_home.subfragments.db.UserInfoEntity
import com.moment.app.main_home.subfragments.models.UserInfoList
import com.moment.app.main_home.subfragments.repository.EntityToModelMapper
import com.moment.app.main_home.subfragments.service.HomeService
import com.moment.app.main_home.subfragments.view.RecommendationEmptyView
import com.moment.app.network.UserCancelException
import com.moment.app.network.startCoroutine
import com.moment.app.network.toast
import com.moment.app.utils.BaseFragment
import com.moment.app.utils.cancelIfActive
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class RecommendationFragment: BaseFragment() {
    private var startPos = -1
    private var pageSize = 10


    private lateinit var binding: SubFragmentRecommendationBinding
    private var currentJob: Job? = null

    private var isOnlyDbMode = false

    @Inject
    @MockData
    lateinit var homeService: HomeService

    @Inject
    lateinit var userInfoDb: HomeRecommendationListDatabase

    val mapper by lazy {
        EntityToModelMapper()
    }

    val adapter by lazy {
         RecommendationAdapter()
    }

    var dbstart = 0

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
            emptyView = RecommendationEmptyView(this.requireContext())) { isLoadMore ->
             loadData(isLoadMore as Boolean)
        }
    }

    private fun loadData(isLoadMore: Boolean) {
        currentJob?.cancelIfActive()
        if (!isLoadMore) {
            isOnlyDbMode = true
            dbstart = 0
        }
        currentJob = startCoroutine({
            if (!isLoadMore) {
                startPos = 0
                //加载数据库data覆盖数据
                //如果数据库有数据 取消shimmer
                     //onsuccess 数据
                //如果数据库没有数据 不变shimmer
                //进入数据库模式
                withContext(Dispatchers.IO) {
                    userInfoDb.UserInfoEntityDao().getUserInfoEntitiesPaged(pageSize, dbstart)
                        .map {
                            mapper.map(it)
                        }
                }.apply {
                    if (size != 0) {
                        binding.refreshView.onSuccess(this.toMutableList(), isLoadMore, dbstart < 10 && dbstart + size < 29 && size == pageSize)
                        dbstart += size
                    }
                }
            } else {
                if (isOnlyDbMode) {
                    withContext(Dispatchers.IO) {
                        userInfoDb.UserInfoEntityDao().getUserInfoEntitiesPaged(pageSize, dbstart)
                            .map {
                                mapper.map(it)
                            }
                    }.apply {
                            binding.refreshView.onSuccess(this.toMutableList(), isLoadMore, dbstart < 10 && dbstart + size  < 29 && size == pageSize)
                            dbstart += size
                    }
                    return@startCoroutine
                }
            }
            val result = withContext(Dispatchers.IO) {
                if (!NetworkUtils.isConnected()) {
                    throw RuntimeException("Net work Error")
                }
                homeService.getOnlineUsersForSlide(startPos, pageSize)
            }
            //存档到数据库！
            isOnlyDbMode = false
            if (!isLoadMore || (adapter.data.size + result.data!!.user_infos!!.size <= 20)) {
                withContext(Dispatchers.IO) {
                    val item = (result.data as UserInfoList).user_infos?.map { it ->
                        UserInfoEntity(
                            name = it.name!!,
                            userId = it.userId!!,
                            gender = it.gender!!,
                            age = it.age!!,
                            page = 0
                        )
                    }
                    userInfoDb.withTransaction {
                        if (!isLoadMore) {
                            userInfoDb.UserInfoEntityDao().clearUserInfoEntity()
                        }
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
            if (!isLoadMore && adapter.data.size > 0) {
                // with db data
                return@startCoroutine
            }
            binding.refreshView.onFail(isLoadMore, it.message)
        }
    }
}