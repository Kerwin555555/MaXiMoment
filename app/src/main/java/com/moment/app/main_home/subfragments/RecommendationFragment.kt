package com.moment.app.main_home.subfragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.moment.app.databinding.SubFragmentRecommendationBinding
import com.moment.app.main_home.subfragments.adapters.RecommendationAdapter
import com.moment.app.main_home.subfragments.view.RecommendationEmptyView
import com.moment.app.main_home.subfragments.viewmodels.RecommendationViewModel
import com.moment.app.utils.BaseFragment
import com.moment.app.utils.generateEmptyMockUserInfos
import com.moment.app.utils.generateErrorMockUserInfos
import com.moment.app.utils.generateMockUserInfos
import com.moment.app.utils.startCoroutine
import com.moment.app.utils.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecommendationFragment: BaseFragment() {
    private val recommendationViewModel by viewModels<RecommendationViewModel>()

    private var startPos = -1
    private lateinit var binding: SubFragmentRecommendationBinding
    private lateinit var adapter: RecommendationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SubFragmentRecommendationBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()

        loadData(false)
    }

    private fun initUI() {
        adapter = RecommendationAdapter()
        val emptyView = RecommendationEmptyView(this.requireContext())
        binding.refreshView.setAdapter(adapter, emptyView){ a ->
            loadData(a as Boolean)
        }
        binding.refreshView.setOnRefreshListener {
            loadData(false)
        }
    }

    private fun loadData(isLoadMore: Boolean) {
        startCoroutine({
            if (!isLoadMore) {
                startPos = 0
            }
            Log.d("zhouzheng 1", ""+ startPos)
            val result = withContext(Dispatchers.IO) {
                generateMockUserInfos(startPos, limit = 10)
                //generateEmptyMockUserInfos(startPos, 10)
                //generateErrorMockUserInfos(startPos, 10)
            }
            Log.d("zhouzheng", result.toString())
            startPos = result.next_start
            binding.refreshView.onSuccess(result.user_infos!!, isLoadMore, result.has_next)
        }){
            it.toast()
            binding.refreshView.onFail(isLoadMore, it.message)
        }
    }
}