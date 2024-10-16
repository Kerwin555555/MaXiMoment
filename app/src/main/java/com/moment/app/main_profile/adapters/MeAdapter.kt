//package com.moment.app.main_profile.adapters
//
//import com.booking.tripsassignment.adapters.custom.ImageFeedLogicFactory
//import com.chad.library.adapter.base.BaseQuickAdapter
//import com.chad.library.adapter.base.BaseViewHolder
//import com.moment.app.main_profile.entities.FeedList
//
//import android.util.Log
//import android.util.SparseArray
//import android.view.ViewGroup
//import com.booking.tripsassignment.adapters.custom.AbstractCustomHolderFactory
//import com.booking.tripsassignment.adapters.custom.NormalFeedFactory
//import com.booking.tripsassignment.adapters.custom.FallbackLogicFactory
///**/
//class MeAdapter : BaseQuickAdapter<FeedList.FeedsBean, BaseViewHolder>(null) {
//
//    companion object {
//        val NORMAL_TYPE = 0
//        val IMAGE_TYPE = 1
//    }
//
//    val holderFactoryCache: SparseArray<AbstractCustomHolderFactory> = SparseArray()
//
//    override fun getDefItemViewType(position: Int): Int {
//        if (data[position].isPictureFeed()) {
//            return IMAGE_TYPE
//        } else {
//            return NORMAL_TYPE
//        }
//    }
//
//    override fun onCreateDefViewHolder(parent: ViewGroup?, viewType: Int): BaseViewHolder {
//        return when (viewType) {
//            IMAGE_TYPE -> {
////                val binding = .inflate(LayoutInflater.from(mContext),parent, false)
////                /HeadViewHolder(binding)
//            }
//            else -> {
//                val binding = .inflate(LayoutInflater.from(mContext), parent, false);
//                CardViewHolder(binding)
//            }
//        }
//    }
//
//    override fun convert(helper: BaseViewHolder, item: FeedList.FeedsBean?) {
//        kotlin.runCatching {
//            val holderFactory = getCustomHolderFactoryByName(helper, item)
//            holderFactory.createLogic()?.handleHolderLogic(helper, item)
//        }.onFailure {
//            Log.d("Adapter", it.message ?: "error")
//        }
//    }
//
//    private fun getCustomHolderFactoryByName(helper: BaseViewHolder, item: FeedList.FeedsBean?):
//            AbstractCustomHolderFactory {
//        if (holderFactoryCache.get(helper.itemViewType) == null) {
//            holderFactoryCache.put(helper.itemViewType, when (helper.itemViewType) {
//                NORMAL_TYPE -> {
//                    NormalFeedFactory()
//                }
//
//                IMAGE_TYPE -> {
//                    ImageFeedLogicFactory()
//                }
//                else -> {
//                    //fall back
//                    FallbackLogicFactory()
//                }
//            })
//        }
//        return holderFactoryCache.get(helper.itemViewType)
//    }
//
//
////    data class CardViewHolder(val binding: TripCardItemLayoutBinding) : BaseViewHolder(binding.root)
////    data class HeadViewHolder(val binding: TripsHeaderItemLayoutBinding) : BaseViewHolder(binding.root)
//}