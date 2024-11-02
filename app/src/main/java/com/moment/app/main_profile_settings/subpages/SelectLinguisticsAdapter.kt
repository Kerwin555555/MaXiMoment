package com.moment.app.main_profile_settings.subpages

import android.text.TextUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.moment.app.R
import com.moment.app.utils.BaseBean
import com.moment.app.utils.getSelectedLoc
import java.util.Locale

class LinguisticsAdapter : BaseQuickAdapter<LocEntity, BaseViewHolder>(R.layout.language_item_view) {
    var selected: Locale? = getSelectedLoc()

    fun setType(type: Int) {
        notifyDataSetChanged()
    }

    override fun convert(holder: BaseViewHolder, locEntity: LocEntity) {
        holder.setText(R.id.language, locEntity.language)

        val selectLanguage = if (selected == null) "" else selected!!.language
        holder.itemView.isSelected = locEntity.locale.language.equals(selectLanguage)
    }
}

class LocEntity(var language: String, var country: String, var code: String) : BaseBean() {
    var locale: Locale = if (TextUtils.equals(code, "zh")) Locale.TRADITIONAL_CHINESE else Locale(
        code
    )
}
