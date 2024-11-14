package com.moment.app.main_chat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.moment.app.databinding.FragmentConversationsBinding
import com.moment.app.hilt.app_level.MockData
import com.moment.app.main_chat.ConversationChangeListener
import com.moment.app.main_chat.GlobalConversationManager
import com.moment.app.main_chat.fragments.adapters.ConversationPartnerAdapter
import com.moment.app.main_chat.fragments.rvheaders.CvsNotificationHeader
import com.moment.app.main_home.subfragments.view.RecommendationEmptyView
import com.moment.app.ui.uiLibs.DataDividerItemDecoration
import com.moment.app.utils.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import java.lang.ref.WeakReference
import javax.inject.Inject


@AndroidEntryPoint
class ConversationsListFragment: BaseFragment() , ConversationChangeListener {

    private lateinit var binding: FragmentConversationsBinding
    private lateinit var adapter: ConversationPartnerAdapter
    private lateinit var cvsHeader: CvsNotificationHeader

    @Inject
    @MockData
    lateinit var conversationManager: GlobalConversationManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentConversationsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()

        conversationManager.conversationChangeListeners.add(WeakReference(this))

    }

    private fun initUI() {
        adapter = ConversationPartnerAdapter()
        adapter.setHeaderAndEmpty(true)
        cvsHeader = CvsNotificationHeader(requireContext())
        adapter.setHeaderView(cvsHeader)

        binding.refreshView.initWith(
            adapter = adapter,
            emptyView = RecommendationEmptyView(this.requireContext())) { isLoadMore ->
            conversationManager.simplyRefreshListFromDB()
        }
        binding.refreshView.getRecyclerView().addItemDecoration(
            DataDividerItemDecoration(
            adapter = adapter,
            size = 0.5f,
            dividerColor = 0xffF4F4F4.toInt(),
            horizontalMargin = 15)
        )
    }

    override fun onResume() {
        super.onResume()
        conversationManager.simplyRefreshListFromDB()
    }

    override fun onConversationsChange() {
        binding.refreshView.onSuccess(
            ArrayList(conversationManager.conversations),
            false,
            false
        )
    }
}