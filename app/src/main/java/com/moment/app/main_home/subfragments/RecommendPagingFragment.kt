package com.moment.app.main_home.subfragments

import android.os.Bundle
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.moment.app.databinding.PagingItemBinding
import com.moment.app.databinding.PagingNetworkItemViewBinding
import com.moment.app.databinding.RecommendationFragmentBinding
import com.moment.app.datamodel.UserInfo
import com.moment.app.main_home.subfragments.viewmodels.RecommendationPagingViewModel
import com.moment.app.utils.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest


@Deprecated("No need")
@AndroidEntryPoint
class RecommendPagingFragment: BaseFragment() {

    private val viewModel by viewModels<RecommendationPagingViewModel>()
    private lateinit var binding: RecommendationFragmentBinding
    private val adapter = UserInfoAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = RecommendationFragmentBinding.inflate(inflater)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rv.layoutManager = LinearLayoutManager(this.requireContext())
        binding.rv.adapter = adapter.withLoadStateFooter(FooterAdapter(adapter))
        binding.refreshLayout.setOnRefreshListener {
            // 手动刷新
            adapter.refresh()
        }
        viewModel.data.observe(this.viewLifecycleOwner) { it ->
             adapter.submitData(viewLifecycleOwner.lifecycle, it)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest {
                binding.refreshLayout.isRefreshing = it.refresh == LoadState.Loading
            }
        }
    }
}

class BindingViewHolder(val binding: ViewBinding): RecyclerView.ViewHolder(binding.root)


class UserInfoAdapter: PagingDataAdapter<UserInfo, BindingViewHolder>(
    object : DiffUtil.ItemCallback<UserInfo>() {
        override fun areItemsTheSame(oldItem: UserInfo, newItem: UserInfo): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: UserInfo, newItem: UserInfo): Boolean {
            return oldItem == newItem
        }
    }
) {
    override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
        getItem(position).let { item->
            val binding = holder.binding as PagingItemBinding
            binding.userInfo = item
        }
        Log.d("Moment", "写入UI！" + position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder {
        val binding = PagingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BindingViewHolder(binding)
    }
}

class FooterAdapter(val adapter: UserInfoAdapter): LoadStateAdapter<NetWorkStateItemViewHolder>() {
    override fun onBindViewHolder(holder: NetWorkStateItemViewHolder, loadState: LoadState) {
         holder.bindData((loadState))
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): NetWorkStateItemViewHolder {
         val binding = PagingNetworkItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
         return NetWorkStateItemViewHolder(binding) {
             adapter.retry()
         }
    }
}

class NetWorkStateItemViewHolder(val binding: PagingNetworkItemViewBinding, val retryCallback : () ->Unit): RecyclerView.ViewHolder(binding.root) {

    fun bindData(loadState: LoadState) {
         binding.apply {
             progress.isVisible = loadState is LoadState.Loading
             errorLayout.isVisible = loadState is LoadState.Error
             errorLayout.setOnClickListener {
                 retryCallback()
             }
         }
    }
}




