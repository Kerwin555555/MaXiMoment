package com.moment.app.main_profile_target

import android.os.Bundle
import com.didi.drouter.annotation.Router
import com.moment.app.databinding.ActivityEditInfoBinding
import com.moment.app.utils.BaseActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
@Router(scheme = ".*", host = ".*", path = "/edit/userInfo")
class EditInfoActivity : BaseActivity(){

    private lateinit var binding: ActivityEditInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditInfoBinding.inflate(layoutInflater)


    }

}