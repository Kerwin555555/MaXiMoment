package com.moment.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.didi.drouter.api.DRouter
import com.gyf.immersionbar.ImmersionBar
import com.moment.app.databinding.DialogLoginBinding
import com.moment.app.utils.BaseFragment
import com.moment.app.utils.setOnAvoidMultipleClicksListener

class MomentLoginFragment: BaseFragment() {
    private lateinit var binding: DialogLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogLoginBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ImmersionBar.with(this)
            .statusBarDarkFont(false)
            .fitsSystemWindows(false)
            .init()

        binding.facebookLogin.setOnAvoidMultipleClicksListener({
            DRouter.build("/login/facebook").start()
        }, 500)
        binding.googleLogin.setOnAvoidMultipleClicksListener({
            DRouter.build("/login/google").start()
        }, 500)
    }
}