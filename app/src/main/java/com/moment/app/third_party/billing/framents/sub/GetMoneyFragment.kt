package com.moment.app.third_party.billing.framents.sub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.didi.drouter.api.DRouter
import com.moment.app.databinding.ItemViewExchangeCoinBinding
import com.moment.app.databinding.ItemViewWithdrawalBinding
import com.moment.app.html.JsActivity.Companion.TYPE_GET
import com.moment.app.third_party.billing.framents.sub.GetMoneyFragment.Adapter
import com.moment.app.third_party.billing.views.RechargeHeaderView
import com.moment.app.utils.BaseBean
import com.moment.app.utils.BaseFragment

class GetMoneyFragment : BaseFragment() {
    private var adapter: Adapter? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return RecyclerView(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = Adapter()
        (view as RecyclerView).layoutManager = LinearLayoutManager(requireContext())
        (view as RecyclerView).adapter = adapter
        adapter?.setOnItemClickListener { adapter, view, position ->
            //goto google
            DRouter.build("/html").putExtra("url", "https://www.google.com").putExtra("type", TYPE_GET).start()
        }
        adapter?.setNewData(mutableListOf(WithDrawalPrams(diamond =  100, currency_price = 1000),
            WithDrawalPrams(diamond =  100, currency_price = 1000),
            WithDrawalPrams(diamond =  100, currency_price = 1000),WithDrawalPrams(diamond =  100, currency_price = 1000)))
    }

    private class Adapter: BaseQuickAdapter<WithDrawalPrams, Adapter.BindingHolder>(null) {

        override fun onCreateDefViewHolder(parent: ViewGroup?, viewType: Int): BindingHolder {
            val binding = ItemViewWithdrawalBinding.inflate(LayoutInflater.from(mContext)).apply {
                root.apply { layoutParams = RecyclerView.LayoutParams(-1,- 2) }
            }
            return BindingHolder(binding)
        }

        data class BindingHolder(val binding: ItemViewWithdrawalBinding): BaseViewHolder(binding.root)

        override fun convert(helper: BindingHolder, item: WithDrawalPrams?) {
             helper.binding.text.text = "${item?.diamond ?: ""}"
            helper.binding.price.text = "${"$"+item?.currency_price ?: ""}"
        }
    }
}

class GetMomentCoinFragment: BaseFragment() {
    private var adapter: Adapter? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return RecyclerView(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = Adapter()
        (view as RecyclerView).layoutManager = LinearLayoutManager(requireContext())
        (view as RecyclerView).adapter = adapter
        adapter?.setOnItemClickListener { adapter, view, position ->
            //goto google
            DRouter.build("/html").putExtra("url", "https://www.google.com").putExtra("type", TYPE_GET).start()
        }
        adapter?.setNewData(mutableListOf(ExchangeCoinPrams(diamond =  100, coin = 100)))
    }


    private class Adapter: BaseQuickAdapter<ExchangeCoinPrams, Adapter.BindingHolder>(null) {

        override fun onCreateDefViewHolder(parent: ViewGroup?, viewType: Int): BindingHolder {
            val binding = ItemViewExchangeCoinBinding.inflate(LayoutInflater.from(mContext)).apply {
                root.apply { layoutParams = RecyclerView.LayoutParams(-1,- 2) }
            }
            return BindingHolder(binding)
        }

        data class BindingHolder(val binding: ItemViewExchangeCoinBinding): BaseViewHolder(binding.root)

        override fun convert(helper: BindingHolder, item: ExchangeCoinPrams?) {
            helper.binding.text.text = "${item?.diamond ?: ""}"
            helper.binding.price.text = "${item?.coin ?: ""}"
        }
    }
}

data class WithDrawalPrams(var diamond: Int, var currency_price: Int): BaseBean() {

}
data class ExchangeCoinPrams(var diamond: Int, var coin: Int): BaseBean() {

}