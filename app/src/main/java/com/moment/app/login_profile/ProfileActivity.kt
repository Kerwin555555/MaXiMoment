package com.moment.app.login_profile

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bigkoo.pickerview.listener.OnDismissListener
import com.bigkoo.pickerview.listener.OnTimeSelectChangeListener
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.bigkoo.pickerview.view.TimePickerView
import com.blankj.utilcode.util.KeyboardUtils
import com.didi.drouter.annotation.Router
import com.didi.drouter.api.DRouter
import com.didi.drouter.api.Extend
import com.moment.app.R
import com.moment.app.databinding.ActivityProfileBinding
import com.moment.app.datamodel.FINISHED_INFO
import com.moment.app.datamodel.UserInfo
import com.moment.app.eventbus.LoginEvent
import com.moment.app.hilt.app_level.MockData
import com.moment.app.login_page.service.LoginService
import com.moment.app.models.AppConfigManager
import com.moment.app.models.UserLoginManager
import com.moment.app.network.LoadingStatus
import com.moment.app.network.ProgressDialogStatus
import com.moment.app.network.refreshProgressDialog
import com.moment.app.network.startCoroutine
import com.moment.app.network.toast
import com.moment.app.utils.BaseActivity
import com.moment.app.utils.DateManagingHub
import com.moment.app.utils.MOMENT_APP
import com.moment.app.utils.ProgressIndicatorFragment
import com.moment.app.utils.SerializeManager
import com.moment.app.utils.cleanSavedFragments
import com.moment.app.utils.getScreenWidth
import com.moment.app.utils.immersion
import com.moment.app.utils.requestNewSize
import com.moment.app.utils.saveView
import com.moment.app.utils.setBgEnableStateListDrawable
import com.moment.app.utils.setOnAvoidMultipleClicksListener
import com.moment.app.utils.setTextColorStateSelectList
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import java.util.Calendar
import java.util.Date
import javax.inject.Inject


@AndroidEntryPoint
@Router(scheme = ".*", host = ".*", path = "/user/init")
class ProfileActivity: BaseActivity(), OnImageConfirmListener{
    private val viewModel by viewModels<ProfileViewModel>()
    private var progressDialog: ProgressIndicatorFragment? = null

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSwipeBackEnable(false)
        cleanSavedFragments()
        immersion()

        initUI()
        initObservers()
    }

    private fun initUI() {
        val w = getScreenWidth()
        binding.confirm.setBgEnableStateListDrawable(
            enableId = R.drawable.bg_drawable_dp25,
            disableId = R.drawable.bg_gray
        )
        binding.bg.requestNewSize(width = w, height = w * 313/390)
        binding.bioEditText.addTextChangedListener(
            onTextChanged = { text, _, _, _ ->
                viewModel.liveData.value?.bio = text?.toString()?.trim()?.replace("\n", "") ?: ""
                refresh()
            }
        )
        binding.birthday.setOnClickListener {
            //timeSelect = null
            clearEditTextsFocus()
            KeyboardUtils.hideSoftInput(this)
            val pickerView: TimePickerView = chooseDate(context = this, listener = null, cancelListener = null) { date ->
                viewModel.liveData.value?.timeSelect = date
                refresh()
            }
            pickerView.setOnDismissListener(object : OnDismissListener {
                override fun onDismiss(o: Any?) {

                }
            })
            pickerView.show()
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
        binding.nicknameEditText.addTextChangedListener(
            onTextChanged = { text, _, _, _ ->
                viewModel.liveData.value?.nickName = text?.toString()?.trim()?.replace("\n", "") ?: ""
                refresh()
            }
        )
        binding.confirm.setOnAvoidMultipleClicksListener({
            viewModel.submit()
        }, 500)
    }

    private fun refresh() {
        viewModel.liveData.value = viewModel.liveData.value
    }

    private fun chooseDate(
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
        return DateManagingHub.chooseDate(
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
                 binding.birthday.text = String.format("%d-%02d-%02d", calendar[Calendar.YEAR],
                     calendar[Calendar.MONTH] + 1, calendar[Calendar.DAY_OF_MONTH])
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

             it.dataOk = it.timeSelect != null && !it.nickName.isNullOrEmpty() && it.gender != null
             binding.confirm.isEnabled = it.dataOk
         }
         viewModel.liveData.value = ProfileViewModel.ProfileData()
         viewModel.netLiveData.observe(this) {
             when (it) {
                 is LoadingStatus.SuccessLoadingStatus -> {
                     clearEditTextsFocus()
                     KeyboardUtils.hideSoftInput(this)
                     supportFragmentManager.beginTransaction()
                         .setCustomAnimations(R.anim.slide_up, 0, 0, 0,)
                         .add(R.id.root_layout, ChooseAvatarFragment())
                         .commitAllowingStateLoss()
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

    private fun clearEditTextsFocus() {
        binding.bioEditText.clearFocus()
        binding.nicknameEditText.clearFocus()
    }

    override fun onConfirm(imageView: PictureCroppingView, map: Map<String, Any?>?) {
        viewModel.saveAvatar(imageView)
    }
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val loginService: LoginService
): ViewModel() {
     private val _liveData = MutableLiveData<ProfileData>()
     val liveData = _liveData

     private val _netLiveData = MutableLiveData<LoadingStatus<UserInfo>>()
     val netLiveData = _netLiveData

    val _showProgressDialog = MutableLiveData<ProgressDialogStatus>()
    val showProgressDialog = _showProgressDialog

    val hasAvatarLiveData = MutableLiveData<Boolean>()

    fun submit() {
        showProgressDialog.value = ProgressDialogStatus.ShowProgressDialog(cancellable = true)
        startCoroutine({
            val data = _liveData.value
            val result =  loginService.updateInfo(mutableMapOf(
                "birthdate" to data!!.birthDateToString(),
                "nickname" to data.nickName,
                "gender" to data.gender,
                "bio" to data.bio
            ))
            val info: UserInfo = UserLoginManager.getUserInfo() ?: return@startCoroutine
            info.nickname = data.nickName
            info.birthday = data.birthDateToString()
            info.age = DateManagingHub.getAge(info.birthday)
            info.gender = data.gender
            info.bio = data.bio
            UserLoginManager.setMemoryUserInfoAndSaveToMMKVAndTryToSaveMMKVSessionAndHuanxinPasswordIfNeed(info)
            EventBus.getDefault().post(LoginEvent())
            AppConfigManager.updateConfig()
            _netLiveData.value = LoadingStatus.SuccessLoadingStatus(info)
            showProgressDialog.value = ProgressDialogStatus.CancelProgressDialog
        }){
            showProgressDialog.value = ProgressDialogStatus.CancelProgressDialog
            _netLiveData.value = LoadingStatus.FailedLoadingStatus(it)
        }
    }


    fun saveAvatar(imageView: PictureCroppingView) {
        Log.d(MOMENT_APP, "wro")
        Log.d(MOMENT_APP, "dxx xxxxdasdfas")
        startCoroutine({
            showProgressDialog.value = ProgressDialogStatus.ShowProgressDialog(cancellable = false)
            kotlin.runCatching {
                (imageView.context as AppCompatActivity).supportFragmentManager.let {
                    val f = it.findFragmentByTag("CroppingPictureFragment")
                    val f2 = it.findFragmentByTag("ChooseAlbumFragment")
                    it.beginTransaction()
                        .setCustomAnimations(0,R.anim.slide_down,0,0)
                        .remove(f!!)
                        .remove(f2!!)
                        .commitNowAllowingStateLoss()
                }
            }
            val file = withContext(Dispatchers.IO) {
                saveView(imageView.context, imageView.clipOriginalBitmap()!!)?.absolutePath
            }
            UserLoginManager.setMemoryUserInfoAndSaveToMMKVAndTryToSaveMMKVSessionAndHuanxinPasswordIfNeed(UserLoginManager.getUserInfo()?.apply {
                avatar = file // for test
                imagesWallList = mutableListOf(file!!)
                register_status = FINISHED_INFO
            })
            Log.d(MOMENT_APP, SerializeManager.toJson(UserLoginManager.getUserInfo()))
            hasAvatarLiveData.value = true
            delay(2000) // mock upload to backend and cloud storage
            showProgressDialog.value = ProgressDialogStatus.CancelProgressDialog
            DRouter.build("/main")
                .putExtra(Extend.START_ACTIVITY_FLAGS, Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .putExtra("isFromRegister", true)
                //.putExtra(MainActivity.INTENT_FROM_KEY, MainActivity.INTENT_FROM_LOGIN)
                .start()
        }) {
            showProgressDialog.value = ProgressDialogStatus.CancelProgressDialog
        }
    }

    data class ProfileData(
        var timeSelect: Date? = null,
        var nickName: String? = null,
        var gender: String? = null,
        var bio: String? = null,
        var avatar: Any? = null, //目前只在EditInfoActivity使用

        var dataOk: Boolean = false
    ) {
        @SuppressLint("DefaultLocale")
        fun birthDateToString(): String {
            if (timeSelect == null) return ""
            val calendar = Calendar.getInstance()
            calendar.time = timeSelect
             return  String.format(
                    "%d-%02d-%02d", calendar[Calendar.YEAR],
                    calendar[Calendar.MONTH] + 1,
                    calendar[Calendar.DAY_OF_MONTH]
                )
        }

        override fun equals(other: Any?): Boolean {
            return other is ProfileData && birthDateToString() == other.birthDateToString()
                    && gender == other.gender && bio == other.bio && avatar == other.avatar
                    && nickName == other.nickName
        }
    }
}