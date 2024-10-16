package com.booking.tripsassignment.adapters.custom

import com.chad.library.adapter.base.BaseViewHolder
import com.moment.app.main_profile.entities.FeedList

abstract class AbstractCustomHolderFactory {
    abstract fun createLogic(): AbstractCustomHolderLogic?
}

abstract class AbstractCustomHolderLogic {
    abstract fun handleHolderLogic(helper: BaseViewHolder?, item: FeedList.FeedsBean?)
}
