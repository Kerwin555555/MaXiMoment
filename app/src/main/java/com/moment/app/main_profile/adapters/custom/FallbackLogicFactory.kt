package com.booking.tripsassignment.adapters.custom

import com.chad.library.adapter.base.BaseViewHolder
import com.moment.app.main_profile.entities.FeedList

class FallbackLogicFactory: AbstractCustomHolderFactory(){
    override fun createLogic(): AbstractCustomHolderLogic {
        return Logic()
    }

    inner class Logic : AbstractCustomHolderLogic() {
        override fun handleHolderLogic(helper: BaseViewHolder?, item: FeedList.FeedsBean?) {
        }
    }
}