package com.moment.app.main_profile_edit

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bigkoo.pickerview.listener.OnDismissListener
import com.bigkoo.pickerview.listener.OnTimeSelectChangeListener
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.bigkoo.pickerview.view.TimePickerView
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.CloneUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.bumptech.glide.Glide
import com.didi.drouter.annotation.Router
import com.gyf.immersionbar.ImmersionBar
import com.moment.app.databinding.ActivityEditInfoBinding
import com.moment.app.datamodel.UserInfo
import com.moment.app.login_profile.ProfileViewModel
import com.moment.app.models.LoginModel
import com.moment.app.utils.BaseActivity
import com.moment.app.utils.DateUtil
import com.moment.app.utils.JsonUtil
import com.moment.app.utils.applyEnabledColorIntStateList
import com.moment.app.utils.applyMargin
import com.moment.app.utils.dp
import com.moment.app.utils.immersion
import com.moment.app.utils.setTextColorStateSelectList
import com.moment.app.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import java.util.Date


@AndroidEntryPoint
@Router(scheme = ".*", host = ".*", path = "/edit/userInfo")
class EditInfoActivity : BaseActivity(){

    private lateinit var binding: ActivityEditInfoBinding
    private val viewModel by viewModels<EditProfileViewModel>()
    private var initialProfileData: ProfileViewModel.ProfileData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.back.applyMargin(top = 15.dp + BarUtils.getStatusBarHeight())
        immersion()
        binding.save.applyEnabledColorIntStateList(enableId =
        0xff1d1d1d.toInt() , disableId = 0xffE5E5E5.toInt())

        binding.back.setOnClickListener {
            finish()
        }

        viewModel.liveData.observe(this) {

            Glide.with(this).load(it.avatar).into(binding.avatar)

            binding.birthday.setText(it.birthDateToString())

            it.gender?.let { gender ->
                when (gender) {
                    "boy" -> {
                        binding.girl.isSelected = false
                        binding.boy.isSelected = true
                    }
                    "girl" -> {
                        binding.girl.isSelected = true
                        binding.boy.isSelected = false
                    }
                }
            } ?: let {
                binding.girl.isSelected = false
                binding.boy.isSelected = false
            }

            it.dataOk = it != initialProfileData
            binding.save.isEnabled = it.dataOk
        }

        LoginModel.getUserInfo()?.let {
            initUI(userInfo = it)
            initialProfileData = ProfileViewModel.ProfileData(
                avatar = it.avatar!!.absolutePath,
                nickName = it.name,
                bio = it.bio,
                gender = it.gender,
                timeSelect = DateUtil.birthdayToDate(it.birthday!!)
            )
            val copy = CloneUtils.deepClone(initialProfileData!!, ProfileViewModel.ProfileData::class.java)
            viewModel.liveData.value = copy
        } ?: let {
            "Initial profile not set".toast()
             viewModel.liveData.value = ProfileViewModel.ProfileData()
        }
    }

    private fun initUI(userInfo: UserInfo) {

        binding.nicknameEditText.setText(userInfo.name ?: "")
        binding.nicknameEditText.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.liveData.value?.nickName = s?.toString()?.trim()?.replace("\n", "") ?: ""
                viewModel.refresh()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.birthday.setOnClickListener {
            //timeSelect = null
            val pickerView: TimePickerView = chooseDate(
                this,
                null, null,
                object : OnTimeSelectChangeListener {
                    override fun onTimeSelectChanged(date: Date) {
                        viewModel.liveData.value?.timeSelect = date
                        viewModel.refresh()
                    }
                })
            pickerView.setOnDismissListener(object : OnDismissListener {
                override fun onDismiss(o: Any?) {

                }
            })
            pickerView.show()
            clearEditTextsFocus()
            binding.birthday.requestFocus()
            KeyboardUtils.hideSoftInput(this)
        }

        binding.boy.setTextColorStateSelectList(
            selectedColor = 0xffFFFFFF.toInt(),
            unSelectedColor = 0xff333333.toInt())
        binding.girl.setTextColorStateSelectList(
            selectedColor = 0xffFFFFFF.toInt(),
            unSelectedColor = 0xff333333.toInt())
        binding.boy.setOnClickListener {
            viewModel.liveData.value?.gender = "boy"
            viewModel.refresh()
        }
        binding.girl.setOnClickListener {
            viewModel.liveData.value?.gender = "girl"
            viewModel.refresh()
        }

        binding.bioEditText.setText(userInfo.bio)
        binding.bioEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.liveData.value?.bio = text?.toString()?.trim()?.replace("\n", "") ?: ""
                viewModel.refresh()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
    private fun clearEditTextsFocus() {
        binding.bioEditText.clearFocus()
        binding.nicknameEditText.clearFocus()
    }

    fun chooseDate(
        context: Context?,
        listener: OnTimeSelectListener?,
        cancelListener: View.OnClickListener?,
        selectChangeListener: OnTimeSelectChangeListener?
    ): TimePickerView {
        val defaultDate = Calendar.getInstance()
        if (!TextUtils.isEmpty(binding.birthday.text.toString())) {
            try {
                val date = binding.birthday.text.toString().split("-".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                defaultDate[date[0].toInt(), date[1].toInt() - 1] = date[2].toInt()
            } catch (e: Exception) {
                e.printStackTrace()
                defaultDate[2004, 0] = 1
            }
        } else {
            defaultDate[2004, 0] = 1
        }
        viewModel.liveData.value?.timeSelect = defaultDate.time
        viewModel.refresh()
        return DateUtil.chooseDate(
            context,
            defaultDate,
            listener,
            cancelListener,
            selectChangeListener
        )
    }
}

class EditProfileViewModel: ViewModel() {
    var liveData = MutableLiveData<ProfileViewModel.ProfileData>()

    fun refresh() {
        liveData.value = liveData.value
    }

}

// Date -> "xxxx:xx:xx" -> 15