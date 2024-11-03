package com.moment.app.third_party.billing.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.moment.app.databinding.GoldItemViewBinding
import com.moment.app.utils.BaseBean
import com.moment.app.utils.dp
import com.moment.app.utils.getScreenWidth

class RechargeAdapter: BaseQuickAdapter<GoldInfo, RechargeAdapter.BindingHolder>(null) {
    val itemWidth = (getScreenWidth() - 24.dp)/3 -6.dp

    override fun onCreateDefViewHolder(parent: ViewGroup?, viewType: Int): BindingHolder {
        val binding = GoldItemViewBinding.inflate(LayoutInflater.from(mContext), parent, false)
        binding.root.apply {
            layoutParams = RecyclerView.LayoutParams(itemWidth, -2)
        }
        return BindingHolder(binding)
    }

    override fun convert(helper: BindingHolder, item: GoldInfo?) {
        helper.binding.coinCount.text = "${item?.coin ?: 0}"
        helper.binding.currencyPrice.text = "${item?.currency_price ?: 0}"
    }

    data class BindingHolder(val binding: GoldItemViewBinding): BaseViewHolder(binding.root)
}

data class GoldInfo(
    var coin: Int,
    var currency_price: Int
): BaseBean()
//
//class IncomeAdapter: BaseQuickAdapter<GoldInfo, RechargeAdapter.BindingHolder>(null) {
//    val width = (getScreenWidth() - 24.dp)/3
//
//    override fun onCreateDefViewHolder(parent: ViewGroup?, viewType: Int): BindingHolder {
//        val binding = GoldItemViewBinding.inflate(LayoutInflater.from(mContext), parent, false)
//        binding.root.apply {
//            layoutParams = RecyclerView.LayoutParams(width, -2)
//        }
//        return BindingHolder(binding)
//    }
//
//    override fun convert(helper: BindingHolder, item: GoldInfo?) {
//        helper.binding.coinCount.text = "${item?.coin ?: 0}"
//        helper.binding.currencyPrice.text = "${item?.currency_price ?: 0}"
//    }
//
//    data class BindingHolder(val binding: GoldItemViewBinding): BaseViewHolder(binding.root)
//}