package com.moment.app.main_profile_edit

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
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
import com.moment.app.R
import com.moment.app.databinding.ActivityEditInfoBinding
import com.moment.app.datamodel.UserInfo
import com.moment.app.hilt.app_level.MockData
import com.moment.app.localimages.AlbumSearcher
import com.moment.app.login_page.service.LoginService
import com.moment.app.login_profile.ChooseAlbumFragment
import com.moment.app.login_profile.PictureCroppingView
import com.moment.app.login_profile.OnImageConfirmListener
import com.moment.app.login_profile.ProfileViewModel
import com.moment.app.models.UserLoginManager
import com.moment.app.network.ProgressDialogStatus
import com.moment.app.network.refreshProgressDialog
import com.moment.app.network.startCoroutine
import com.moment.app.network.toast
import com.moment.app.user_rights.UserPermissionManager
import com.moment.app.utils.BaseActivity
import com.moment.app.utils.DateManagingHub
import com.moment.app.utils.SerializeManager
import com.moment.app.utils.MOMENT_APP
import com.moment.app.utils.ProgressIndicatorFragment
import com.moment.app.utils.applyEnabledColorIntStateList
import com.moment.app.utils.applyMargin
import com.moment.app.utils.bottomInBottomOut
import com.moment.app.utils.cleanSavedFragments
import com.moment.app.utils.dp
import com.moment.app.utils.getScreenHeight
import com.moment.app.utils.getScreenWidth
import com.moment.app.utils.immersion
import com.moment.app.utils.saveView
import com.moment.app.utils.setOnAvoidMultipleClicksListener
import com.moment.app.utils.setTextColorStateSelectList
import com.moment.app.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import javax.inject.Inject


@AndroidEntryPoint
@Router(scheme = ".*", host = ".*", path = "/edit/userInfo")
class EditInfoActivity : BaseActivity(), OnImageConfirmListener{

    private lateinit var binding: ActivityEditInfoBinding
    private val viewModel by viewModels<EditProfileViewModel>()
    private var initialProfileData: ProfileViewModel.ProfileData? = null
    private var progressDialog: ProgressIndicatorFragment? = null

    @Inject
    @MockData
    lateinit var loginService: LoginService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cleanSavedFragments()
        binding = ActivityEditInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.back.applyMargin(top = 15.dp + BarUtils.getStatusBarHeight())
        immersion()
        binding.save.applyEnabledColorIntStateList(enableId =
        0xff1d1d1d.toInt() , disableId = 0xffE5E5E5.toInt())

        binding.back.setOnClickListener {
            finish()
        }

        binding.save.setOnAvoidMultipleClicksListener({
            viewModel.submitData(initialProfileData!!, this@EditInfoActivity, loginService)
        }, 500)

        viewModel.liveData.observe(this) {

            Glide.with(this).asBitmap().load(it.avatar).into(binding.avatar)

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

        UserLoginManager.getUserInfo()?.let {
            initUI(userInfo = it)
            initialProfileData = ProfileViewModel.ProfileData(
                avatar = it.avatar ?: "",
                nickName = it.nickname,
                bio = it.bio,
                gender = it.gender,
                timeSelect = if (it.birthday.isNullOrEmpty()) Date() else DateManagingHub.birthdayToDate(it.birthday!!)
            )
            Log.d("zhouzheng copy", initialProfileData.toString())
            val copy = CloneUtils.deepClone(initialProfileData!!, ProfileViewModel.ProfileData::class.java)
            Log.d("zhouzheng copy", copy.toString())
            viewModel.liveData.value = copy
        } ?: let {
            "Initial profile not set".toast()
             viewModel.liveData.value = ProfileViewModel.ProfileData()
        }

        viewModel.showProgressDialog.observe(this) {
            progressDialog = it.refreshProgressDialog(progressDialog, this)
        }

        binding.root.postDelayed({
            binding.bioEditText.requestFocus()
        },50)
    }

    private fun initUI(userInfo: UserInfo) {
        binding.avatar.setOnClickListener {
            onChooseFromLibrary()
        }
        binding.nicknameEditText.setText(userInfo.nickname ?: "")
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
        return DateManagingHub.chooseDate(
            context,
            defaultDate,
            listener,
            cancelListener,
            selectChangeListener
        )
    }

    fun onChooseFromLibrary() {
        try {
            UserPermissionManager.check(
                this, "Choose from library",
                arrayOf<String>(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ), object : UserPermissionManager.Callback {
                    override fun result(res: Int) {
                        if (res == 0) {
                            Log.d(MOMENT_APP, Thread.currentThread().name)
                            this@EditInfoActivity.bottomInBottomOut().add(R.id.root_layout, ChooseAlbumFragment().apply {
                                    arguments = bundleOf("extra_mode" to AlbumSearcher.MODE_ONLY_IMAGE)
                                }, "ChooseAlbumFragment").addToBackStack(null).commitAllowingStateLoss()
                        }
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
            (e.message + "requestPermissions error").toast()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        kotlin.runCatching {
            KeyboardUtils.hideSoftInput(this)
        }
    }

    override fun onConfirm(clipImageView: PictureCroppingView, map: Map<String, Any?>?) {
        supportFragmentManager.let {
            val f = it.findFragmentByTag("CroppingPictureFragment")
            val f2 = it.findFragmentByTag("ChooseAlbumFragment")
            it.beginTransaction()
                .setCustomAnimations(0,R.anim.slide_down,0,0)
                .remove(f!!)
                .remove(f2!!)
                .commitNowAllowingStateLoss()
        }

        if (map?.get("file") != null) {
            viewModel.liveData.value?.avatar = map["file"] as? String?
            viewModel.refresh()
        } else if (map?.get("uri") != null) {
            viewModel.liveData.value?.avatar = map["uri"] as? Uri?
            viewModel.refresh()
        }
    }
}

class EditProfileViewModel : ViewModel() {
    var liveData = MutableLiveData<ProfileViewModel.ProfileData>()
    val _showProgressDialog = MutableLiveData<ProgressDialogStatus>()
    val showProgressDialog = _showProgressDialog

    fun refresh() {
        liveData.value = liveData.value
    }

    fun submitData(initData: ProfileViewModel.ProfileData, context: Context, loginService: LoginService) {
        _showProgressDialog.value = ProgressDialogStatus.ShowProgressDialog(true)
        startCoroutine({
            val profileData = liveData.value
            val map = mutableMapOf<String?,String?>()
            if (profileData?.avatar != null && initData.avatar != profileData.avatar) {
                val file = withContext(Dispatchers.IO) {
                    val bitmap =
                        Glide.with(context).asBitmap().load(profileData.avatar).centerInside().submit(
                            getScreenWidth(), getScreenHeight()
                        ).get()
                    saveView(context, bitmap)?.absolutePath ?: ""
                }
                "clip and save to local ok".toast()
                //upload file to cloud
                delay(300)
                "upload to cloud".toast()
                map["avatar"] = file
            }
            if (profileData?.nickName!= null && initData.gender != profileData.nickName) {
                map["name"] = profileData.nickName!!
            }
            if (profileData?.birthDateToString() != null && initData.birthDateToString() != profileData.birthDateToString()) {
                map["birthday"] = profileData.birthDateToString()
            }
            if (profileData?.gender != null && initData.gender != profileData.gender) {
                map["gender"] = profileData.gender!!
            }
            if (profileData?.bio!= null && initData.bio != profileData.bio) {
                map["bio"] = profileData.bio!!
            }
            val result = loginService.updateInfo(map)
            "upload to backend".toast()
            UserLoginManager.getUserInfo()?.apply {
                map["name"]?.let{
                    this.nickname = it
                }
                map["gender"]?.let{
                    this.gender = it
                }
                map["avatar"]?.let{
                    this.avatar = it
                    this.imagesWallList[0] = it
                }
                map["bio"]?.let{
                    this.bio = it
                }
                map["birthday"]?.let{
                    this.birthday = it
                }
                Log.d("zhouzheng save", SerializeManager.toJson(this))
                UserLoginManager.setMemoryUserInfoAndSaveToMMKVAndTryToSaveMMKVSessionAndHuanxinPasswordIfNeed(this)
                _showProgressDialog.value = ProgressDialogStatus.CancelProgressDialog
                (context as? AppCompatActivity)?.finish()
            }
        }) {
            it.toast()
            _showProgressDialog.value = ProgressDialogStatus.CancelProgressDialog
        }
    }
}

// Date -> "xxxx:xx:xx" -> 15