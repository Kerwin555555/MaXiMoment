package com.moment.app.third_party.billing.framents

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.didi.drouter.api.DRouter
import com.moment.app.databinding.FragmentRechargeBinding
import com.moment.app.html.JsActivity.Companion.TYPE_GET
import com.moment.app.third_party.billing.adapters.GoldInfo
import com.moment.app.third_party.billing.adapters.RechargeAdapter
import com.moment.app.third_party.billing.views.RechargeHeaderView
import com.moment.app.utils.BaseFragment
import com.moment.app.utils.dp

class RechargeFragment: BaseFragment() {
    private lateinit var binding: FragmentRechargeBinding

    private val rechargeAdapter by lazy {
        RechargeAdapter()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRechargeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rv.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rv.adapter = rechargeAdapter
        binding.rv.addItemDecoration(object : ItemDecoration(){
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                super.getItemOffsets(outRect, view, parent, state)
                outRect.set(3.dp,3.dp,3.dp,3.dp)
            }
        })

        rechargeAdapter.setOnItemClickListener { adapter, view, position ->
            //goto google
            DRouter.build("/html").putExtra("url", "https://www.google.com").putExtra("type", TYPE_GET).start()
        }

        rechargeAdapter.setNewData(mutableListOf(GoldInfo(100, 100), GoldInfo(100, 100)))
    }
}