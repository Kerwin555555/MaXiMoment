package com.moment.app.login_profile

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bigkoo.pickerview.listener.OnDismissListener
import com.bigkoo.pickerview.listener.OnTimeSelectChangeListener
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.bigkoo.pickerview.view.TimePickerView
import com.blankj.utilcode.util.KeyboardUtils
import com.didi.drouter.annotation.Router
import com.gyf.immersionbar.ImmersionBar
import com.moment.app.R
import com.moment.app.databinding.ActivityProfileBinding
import com.moment.app.datamodel.UserInfo
import com.moment.app.eventbus.LoginEvent
import com.moment.app.hilt.app_level.MockData
import com.moment.app.login_page.service.LoginService
import com.moment.app.models.ConfigModel
import com.moment.app.models.LoginModel
import com.moment.app.network.LoadingStatus
import com.moment.app.network.ProgressDialogStatus
import com.moment.app.network.refreshProgressDialog
import com.moment.app.network.startCoroutine
import com.moment.app.network.toast
import com.moment.app.utils.BaseActivity
import com.moment.app.utils.DateUtil
import com.moment.app.utils.ProgressDialog
import com.moment.app.utils.setTextColorStateSelectList
import com.moment.app.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import org.greenrobot.eventbus.EventBus
import java.util.Calendar
import java.util.Date
import javax.inject.Inject


@AndroidEntryPoint
@Router(scheme = ".*", host = ".*", path = "/user/init")
class ProfileActivity: BaseActivity() {
    private val viewModel by viewModels<ProfileViewModel>()
    private var progressDialog: ProgressDialog? = null

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSwipeBackEnable(false)
        kotlin.runCatching {
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            for (f in supportFragmentManager.getFragments()) {
                transaction.remove(f!!)
            }
            transaction.commitNow()
        }
        ImmersionBar.with(this)
            .statusBarDarkFont(false)
            .fitsSystemWindows(false)
            .init()
        initUI()
        initObservers()
    }

    private fun initUI() {
        binding.bioEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.liveData.value?.bio = text?.toString()?.trim()?.replace("\n", "") ?: ""
                refresh()
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
                        refresh()
                    }
                })
            pickerView.setOnDismissListener(object : OnDismissListener {
                override fun onDismiss(o: Any?) {

                }
            })
            pickerView.show()
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
            refresh()
        }
        binding.girl.setOnClickListener {
            viewModel.liveData.value?.gender = "girl"
            refresh()
        }
        binding.nicknameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.liveData.value?.nickName = text?.toString()?.trim()?.replace("\n", "") ?: ""
                refresh()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.confirm.setOnClickListener {
            viewModel.submit()
        }
    }

    private fun refresh() {
        viewModel.liveData.value = viewModel.liveData.value
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
        refresh()
        return DateUtil.chooseDate(
            context,
            defaultDate,
            listener,
            cancelListener,
            selectChangeListener
        )
    }

    @SuppressLint("DefaultLocale")
    private fun initObservers() {
         viewModel.liveData.observe(this) {
             it.timeSelect?.let { date ->
                 val calendar = Calendar.getInstance()
                 calendar.time = date
                 binding.birthday.setText(String.format("%d-%d-%d", calendar[Calendar.YEAR],
                         calendar[Calendar.MONTH] + 1, calendar[Calendar.DAY_OF_MONTH]))
             } ?: let {
             }
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

             ("ahaha : "+it.dataOk).toast()
             it.dataOk = it.timeSelect != null && !it.nickName.isNullOrEmpty() && it.gender != null
             binding.confirm.isEnabled = it.dataOk
         }
         viewModel.liveData.value = ProfileViewModel.ProfileData()
         viewModel.netLiveData.observe(this) {
             when (it) {
                 is LoadingStatus.SuccessLoadingStatus -> {
                     val fragmentTransaction = supportFragmentManager.beginTransaction()
                     fragmentTransaction.setCustomAnimations(
                         R.anim.slide_out, // 进入动画
                        0, // 退出动画（这里没有设置，所以为0）
                         0, // 弹出动画（这里没有设置，所以为0）
                       0, // 弹入动画（这里没有设置，所以为0）
                   )
                     fragmentTransaction.add(R.id.root_layout, ChooseAvatarFragment())
                     fragmentTransaction.commitAllowingStateLoss()
                }
                 is LoadingStatus.FailedLoadingStatus -> {
                     it.error?.toast()
                 }
                 else -> {}
             }
         }
        viewModel.showProgressDialog.observe(this) {
            progressDialog = it.refreshProgressDialog(progressDialog, this)
        }
    }
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @MockData private val loginService: LoginService
): ViewModel() {
     private val _liveData = MutableLiveData<ProfileData>()
     val liveData = _liveData

     private val _netLiveData = MutableLiveData<LoadingStatus<UserInfo>>()
     val netLiveData = _netLiveData

    val _showProgressDialog = MutableLiveData<ProgressDialogStatus>()
    val showProgressDialog = _showProgressDialog

    fun submit() {
        startCoroutine({
            showProgressDialog.value = ProgressDialogStatus.ShowProgressDialog(cancellable = true)
            val data = _liveData.value
            val result =  loginService.updateInfo(mutableMapOf(
                "age" to data!!.ageToString(),
                "name" to data!!.nickName,
                "gender" to data!!.gender,
                "bio" to data!!.bio
            ))
            val info: UserInfo? = LoginModel.getUserInfo()
            if (info == null) {
                return@startCoroutine
            }
            info.name = data!!.nickName
            info.birthday = data!!.ageToString()
            info.gender = data!!.gender
            info.bio = data!!.bio
            if (result.data != null) info.age = result.data!!.age
            LoginModel.setUserInfo(info)
            EventBus.getDefault().post(LoginEvent())
            ConfigModel.updateConfig()
            _netLiveData.value = LoadingStatus.SuccessLoadingStatus(info)
            showProgressDialog.value = ProgressDialogStatus.CancelProgressDialog
        }){
            showProgressDialog.value = ProgressDialogStatus.CancelProgressDialog
            _netLiveData.value = LoadingStatus.FailedLoadingStatus(it)
        }
    }

    data class ProfileData(
        var timeSelect: Date? = null,
        var nickName: String? = null,
        var gender: String? = null,
        var bio: String? = null,

        var dataOk: Boolean = false
    ) {
        @SuppressLint("DefaultLocale")
        fun ageToString(): String {
            if (timeSelect == null) return ""
            val calendar = Calendar.getInstance()
            calendar.time = timeSelect
             return  String.format(
                    "%d-%d-%d", calendar[Calendar.YEAR],
                    calendar[Calendar.MONTH] + 1,
                    calendar[Calendar.DAY_OF_MONTH]
                )
        }
    }
}