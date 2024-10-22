package com.moment.app.main_home.subfragments.dialogs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.moment.app.databinding.SiftDialogFragmentBinding
import com.moment.app.utils.BaseBottomSheetDialogFragment
import com.moment.app.utils.DialogUtils
import com.moment.app.utils.setTextColorStateSelectList

class SiftDialogFragment : BaseBottomSheetDialogFragment(){
    companion object {
         fun showSiftDialog(context: Context) {
             DialogUtils.show(context, SiftDialogFragment())
         }
    }

    private lateinit var binding: SiftDialogFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SiftDialogFragmentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.seekbar.setRange(18f, 60f, 0f)
        binding.seekbar.setProgress(18f, 20f)
        binding.seekbar.setIndicatorTextDecimalFormat("0")

        binding.all.setTextColorStateSelectList(
            selectedColor = 0xffFFFFFF.toInt(),
            unSelectedColor = 0xff333333.toInt()
        )
        binding.boy.setTextColorStateSelectList(
            selectedColor = 0xffFFFFFF.toInt(),
            unSelectedColor = 0xff333333.toInt())
        binding.girl.setTextColorStateSelectList(
            selectedColor = 0xffFFFFFF.toInt(),
            unSelectedColor = 0xff333333.toInt())
        binding.all.isSelected = true
        binding.boy.isSelected = false
        binding.girl.isSelected = false

        binding.confirm.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }
}