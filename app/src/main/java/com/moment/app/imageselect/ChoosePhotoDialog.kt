package com.moment.app.imageselect

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.blankj.utilcode.util.IntentUtils
import com.blankj.utilcode.util.LogUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.moment.app.R
import com.moment.app.databinding.DialogPhotoChooseBottomSheetBinding
import com.moment.app.images.Explorer
import com.moment.app.images.bean.MediaDirectory
import com.moment.app.images.bean.MediaFile
import com.moment.app.permissions.PermissionHelper
import com.moment.app.utils.AppInfo
import com.moment.app.utils.DialogUtils
import com.moment.app.utils.toast

class ChoosePhotoDialog : BottomSheetDialogFragment() {
    private var mCurrentPhotoPath: Uri? = null
    private var count = 0
    private var chooseVideo = false
    private var fromChat = false
    private var actionListener: ActionListener? = null
    private var chooseListener: ChooseListener? = null
    private var chooseVideoListener: ChooseVideoListener? = null
    private lateinit var binding: DialogPhotoChooseBottomSheetBinding

    interface ActionListener {
        fun onChooseLibraryClick()
        fun onTakePhotosClick()
    }

    interface ChooseListener {
        fun onPhotoChoose(uris: List<Uri?>?)
        fun onTakePhoto(uri: Uri?)
    }

    interface ChooseVideoListener {
        fun onVideoChoose(uris: List<Uri?>?)
    }

    fun setActionListener(listener: ActionListener?) {
        this.actionListener = listener
    }

    fun setChooseListener(listener: ChooseListener?) {
        this.chooseListener = listener
    }

    fun setChooseVideoListener(listener: ChooseVideoListener?) {
        this.chooseVideoListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        if (savedInstanceState != null) {
            mCurrentPhotoPath = savedInstanceState.getParcelable("photo")
        }
    }

    private fun dispatchTakePictureIntent() {
        if (activity == null) {
            return
        }

        mCurrentPhotoPath = createImageUri()

        val captureIntent = IntentUtils.getCaptureIntent(mCurrentPhotoPath)
        try {
            startActivityForResult(captureIntent, REQUEST_CODE_TAKE)
        } catch (e: Exception) {
            LogUtils.d("capture", e)
        }
    }

    private fun createImageUri(): Uri? {
        val status = Environment.getExternalStorageState()
        // 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
        return if (status == Environment.MEDIA_MOUNTED) {
            requireContext().contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                ContentValues()
            )
        } else {
            requireContext().contentResolver.insert(
                MediaStore.Images.Media.INTERNAL_CONTENT_URI,
                ContentValues()
            )
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("photo", mCurrentPhotoPath)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogPhotoChooseBottomSheetBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        if (arguments != null) {
//            count = requireArguments().getInt("count", 9)
//            chooseVideo = requireArguments().getBoolean("chooseVideo", false)
//            fromChat = requireArguments().getBoolean("fromChat", false)
//            val canTakePhoto = requireArguments().getBoolean("canTakePhoto", true)
//            if (!canTakePhoto) {
//                binding.takePhoto.setVisibility(View.GONE)
//            }
//            if (fromChat) {
//                chooseVideo = false
//            }
 //           if (requireArguments().getBoolean("showVideoItem", false)) {
//                binding.takeVideo.setVisibility(View.VISIBLE)
//                binding.takeVideo.setOnClickListener {  onChooseVideo() }
 //           }
 //       }

        binding.takePhoto.setOnClickListener { 
            takePhoto()
        }

        binding.chooseLibrary.setOnClickListener { 
            onChooseFromLibrary()
        }
    }

    private fun onChooseLibrary() {
        if (activity == null || !isAdded || actionListener == null) return
        actionListener!!.onChooseLibraryClick()
    }

    private fun onTakePhotos() {
        if (activity == null || !isAdded || actionListener == null) return
        actionListener!!.onTakePhotosClick()
    }

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            super.show(manager, tag)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onCancel() {
        dismiss()
    }

    fun takePhoto() {
        try {
            onTakePhotos()
            PermissionHelper.check(
                requireContext(), "Take Photo",
                arrayOf<String>(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            , object : PermissionHelper.Callback {
                    override fun result(res: Int) {
                        if (res == 0) {
                            dispatchTakePictureIntent()
                        }
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
            e.message?.toast()
        }
    }

//    fun onChooseVideo() {
//        try {
//            PermissionHelper.check(
//                context, getString(R.string.choose_from_library),
//                arrayOf<String>(
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                    Manifest.permission.READ_EXTERNAL_STORAGE
//                )
//            ) { res ->
//                if (res === 0) {
//                    Explorer.makeRequest()
//                        .spanCount(3) //显示的列数
//                        .pickCount(1)
//                        .maxVideoDuration(Gsr.INSTANCE.getInt("maxSelectVideoSeconds", 25))
//                        .maxVideoSize(Gsr.INSTANCE.getInt("maxSelectVideoSize", 20))
//                        .maxScanGifSize(Gsr.INSTANCE.getInt("maxScanGifSize", 10))
//                        .maxPreviewGifSize(Gsr.INSTANCE.getInt("maxPreviewGifSize", 5))
//                        .pickMode(Explorer.MODE_ONLY_VIDEO)
//                        .start(
//                            REQUEST_CODE_CHOOSE_VIDEO,
//                            this
//                        )
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            "requestPermissions error".toast()
//        }
//    }

    fun onChooseFromLibrary() {
        try {
            onChooseLibrary()
            PermissionHelper.check(
                requireContext(), "Choose from library",
                arrayOf<String>(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                , object : PermissionHelper.Callback {
                    override fun result(res: Int) {
                        if (res == 0) {
                            choosePhoto()
                        }
                    }
                })
        } catch (e: Exception) {
            "requestPermissions error".toast()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_CHOOSE) {
                if (chooseListener != null) chooseListener!!.onPhotoChoose(
                    Explorer.obtainResult(
                        data
                    )
                )
            } else if (requestCode == REQUEST_CODE_TAKE) {
                //MediaUploader.compressImage(mCurrentPhotoPath.toString(), null)
                if (chooseListener != null) chooseListener!!.onTakePhoto(mCurrentPhotoPath)
            } else if (requestCode == REQUEST_CODE_CHOOSE_VIDEO) {
                if (chooseVideoListener != null) chooseVideoListener!!.onVideoChoose(
                    Explorer.obtainResult(
                        data
                    )
                )
            }
        }
        dismissAllowingStateLoss()
    }

    private fun choosePhoto() {
        if (activity == null) return

        //Explorer暂时不支持video，所以需要各种兼容
        val needVideoFile = false

        // ver5.5.0_1109 feed also support upload .gif
        var pickMode = Explorer.MODE_IMAGE_GIF

        if (needVideoFile) pickMode = Explorer.MODE_IMAGE_VIDEO

        Explorer.makeRequest()
            .spanCount(4) //显示的列数
            .pickCount(count)
            .maxVideoDuration(25)
            .maxVideoSize(20.0)
            .setDataTrack(object : Explorer.DataTrackListener {
                override fun onError(method: String, e: Exception) {
                    LogUtils.e(
                        "@@@Explorer",
                        ("os-" + Build.VERSION.SDK_INT + "-ver-" + AppInfo.versionName).toString() + "-method-" + method + "-error-" + e.message
                    )
                    e.message?.toast()
                }

                override fun onData(method: String, list: List<MediaDirectory>) {
                    LogUtils.d(
                        "@@@Explorer",
                        ("os-" + Build.VERSION.SDK_INT + "-ver-" + AppInfo.versionName).toString() + "-method-" + method + "-list-" + list.size
                    )
                }

                override fun onSelect(mediaFile: MediaFile, isSelected: Boolean) {
                    if (isSelected && !mediaFile.isVideo()) {
                        //MediaUploader.compressImage(mediaFile.path, null)
                    }
                }
            }) //这里可以处理点击的数据
            .pickMode(pickMode) //选取类型
            .start(REQUEST_CODE_CHOOSE, this)
    }

    companion object {
        const val REQUEST_CODE_CHOOSE: Int = 500
        const val REQUEST_CODE_TAKE: Int = 600
        const val REQUEST_CODE_CHOOSE_VIDEO: Int = 700

        /**
         * 给NewReportDialog 使用
         * @param context 运行context
         * @param count 图片选择张数
         * @return ChoosePhotoDialog
         */
        fun showWithVideo(context: Context?, count: Int, hasVideo: Boolean): ChoosePhotoDialog {
            val bundle = Bundle()
            bundle.putInt("count", count)
            bundle.putBoolean("chooseVideo", false)
            bundle.putBoolean("fromChat", false)
            bundle.putBoolean("canTakePhoto", true)
            bundle.putBoolean("showVideoItem", hasVideo)
            val dialog = ChoosePhotoDialog()
            dialog.arguments = bundle
            DialogUtils.show(context, dialog)
            return dialog
        }

        @JvmOverloads
        fun show(
            context: Context?,
            count: Int,
            chooseVideo: Boolean = false,
            fromChat: Boolean = false,
            canTakePhoto: Boolean = true
        ): ChoosePhotoDialog {
            val bundle = Bundle()
            bundle.putInt("count", count)
            bundle.putBoolean("chooseVideo", chooseVideo)
            bundle.putBoolean("fromChat", fromChat)
            bundle.putBoolean("canTakePhoto", canTakePhoto)
            val dialog = ChoosePhotoDialog()
            dialog.arguments = bundle
            DialogUtils.show(context, dialog)
            return dialog
        }
    }
}