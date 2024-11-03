package com.moment.app.third_party.billing.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.moment.app.databinding.FragmentRechargeHeaderBinding
import com.moment.app.databinding.HeaderIncomeBinding

class RechargeHeaderView : ConstraintLayout{
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    private val binding = FragmentRechargeHeaderBinding.inflate(LayoutInflater.from(context), this)
    fun bindData() {

    }
}

class IncomeHeaderView : ConstraintLayout{
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    private val binding = HeaderIncomeBinding.inflate(LayoutInflater.from(context), this)

    fun bindData() {

    }
}