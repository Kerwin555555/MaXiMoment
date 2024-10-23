package com.moment.app.login_profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.moment.app.databinding.ClipImagePopUpWindowBinding
import com.moment.app.utils.BaseFragment
import com.moment.app.utils.popBackStackNowAllowingStateLoss

class ClipImageFragment: BaseFragment() {

    private lateinit var binding: ClipImagePopUpWindowBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ClipImagePopUpWindowBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.setOnClickListener {
            activity?.supportFragmentManager?.popBackStackNowAllowingStateLoss()
        }
    }
}