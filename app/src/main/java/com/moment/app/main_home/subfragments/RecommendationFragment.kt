package com.moment.app.main_home.subfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.moment.app.databinding.SubFragmentRecommendationBinding
import com.moment.app.main_home.subfragments.adapters.RecommendationAdapter
import com.moment.app.main_home.subfragments.view.RecommendationEmptyView
import com.moment.app.main_home.subfragments.viewmodels.RecommendationViewModel
import com.moment.app.network.UserCancelException
import com.moment.app.network.startCoroutine
import com.moment.app.network.toast
import com.moment.app.utils.BaseFragment
import com.moment.app.utils.generateMockUserInfos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext

class RecommendationFragment: BaseFragment() {
    private val recommendationViewModel by viewModels<RecommendationViewModel>()

    private var startPos = -1
    private lateinit var binding: SubFragmentRecommendationBinding
    private var currentJob: Job? = null

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
            adapter = RecommendationAdapter(),
            emptyView = RecommendationEmptyView(this.requireContext())) { isLoadMore ->
             loadData(isLoadMore as Boolean)
        }
    }

    private fun loadData(isLoadMore: Boolean) {
        currentJob?.let {
            if (it.isActive) {
                it.cancel(UserCancelException())
            }
        }
        currentJob = startCoroutine({
            if (!isLoadMore) {
                startPos = 0
            }
            val result = withContext(Dispatchers.IO) {
                generateMockUserInfos(startPos, limit = 10)
                //generateEmptyMockUserInfos(startPos, 10)
                //generateErrorMockUserInfos(startPos, 10)
            }
            startPos = result.next_start
            binding.refreshView.onSuccess(result.user_infos!!, isLoadMore, result.has_next)
        }){
            if (it.throwable is UserCancelException) {
                return@startCoroutine
            }
            it.toast()
            binding.refreshView.onFail(isLoadMore, it.message)
        }
    }
}