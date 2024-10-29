package com.moment.app.main_profile_edit

import android.os.Bundle
import com.didi.drouter.annotation.Router
import com.gyf.immersionbar.ImmersionBar
import com.moment.app.databinding.ActivityEditInfoBinding
import com.moment.app.utils.BaseActivity
import com.moment.app.utils.applyEnabledColorIntStateList
import com.moment.app.utils.immersion
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
@Router(scheme = ".*", host = ".*", path = "/edit/userInfo")
class EditInfoActivity : BaseActivity(){

    private lateinit var binding: ActivityEditInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditInfoBinding.inflate(layoutInflater)
        immersion()
        binding.save.applyEnabledColorIntStateList(enableId =
        0xff1d1d1d.toInt() , disableId = 0xffE5E5E5.toInt())


    }

}