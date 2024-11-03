package com.moment.app.third_party.billing.framents

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.moment.app.R
import com.moment.app.databinding.FragmentIncomeBinding
import com.moment.app.third_party.billing.framents.sub.GetMomentCoinFragment
import com.moment.app.third_party.billing.framents.sub.GetMoneyFragment
import com.moment.app.utils.BaseFragment
import com.moment.app.utils.applySelectedColorIntStateList

class IncomeFragment: BaseFragment() {
    private lateinit var binding: FragmentIncomeBinding
    private var adapter: Adapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentIncomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = Adapter(this)
        binding.viewpager2.adapter = adapter
        TabLayoutMediator(binding.tagTab, binding.viewpager2){ tab, pos ->
            if (context == null) return@TabLayoutMediator
            tab.setCustomView(R.layout.title_exchange_or_withdrawl)
            tab.customView?.findViewById<TextView>(R.id.text)?.let {
                if (pos == 0) {
                    it.text = "Withdrawal"
                } else {
                    it.text = "Coin Exchange"
                }
                it.applySelectedColorIntStateList(
                    selectedId = 0xfffd257c.toInt(),
                    unSelectedId = 0xff1d1d1d.toInt(),
                )
            }
        }.attach()
    }

    inner class Adapter(val f: Fragment) : FragmentStateAdapter(f) {
        override fun getItemCount(): Int {
            return 2
        }

        override fun createFragment(position: Int): Fragment {
            if (position == 0) {
                return GetMoneyFragment()
            } else {
                return GetMomentCoinFragment()
            }
        }
    }
}