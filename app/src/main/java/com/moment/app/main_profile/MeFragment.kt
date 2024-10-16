package com.moment.app.main_profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.moment.app.databinding.FragmentProfileBinding
//import com.moment.app.main_profile.adapters.MeAdapter
import com.moment.app.utils.BaseFragment

class MeFragment : BaseFragment() {
    private lateinit var binding: FragmentProfileBinding
    //private val adapter = MeAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}