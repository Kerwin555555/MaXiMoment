package com.moment.app.third_party.billing

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.BarUtils
import com.didi.drouter.annotation.Router
import com.moment.app.R
import com.moment.app.databinding.ActivityRechargeIncomeBinding
import com.moment.app.third_party.billing.framents.IncomeFragment
import com.moment.app.third_party.billing.framents.RechargeFragment
import com.moment.app.utils.BaseActivity
import com.moment.app.utils.applyMargin
import com.moment.app.utils.dp
import com.moment.app.utils.immersion
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
@Router(scheme = ".*", host = ".*", path = "/recharge_income")
class RechargeIncomeActivity: BaseActivity() {
    companion object {
        val TYPE_RECHARGE = 0
        val TYPE_INCOME = 1
    }
    private lateinit var binding: ActivityRechargeIncomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRechargeIncomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        immersion()
        binding.back.applyMargin(top = 10.dp + BarUtils.getStatusBarHeight())
        binding.back.setOnClickListener {
            finish()
        }
        when (intent.getIntExtra("type", 0)) {
            TYPE_INCOME -> {
                binding.settingTitle.text = "Withdrawal"
                jumpTarget(IncomeFragment())
            }
            TYPE_RECHARGE -> {
                binding.settingTitle.text = "Recharge"
                jumpTarget(RechargeFragment())
            }
            else -> {}
        }
    }

    fun jumpTarget(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commitAllowingStateLoss()
    }
}