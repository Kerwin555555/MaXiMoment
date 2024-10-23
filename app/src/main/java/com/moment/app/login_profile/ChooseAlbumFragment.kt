package com.moment.app.login_profile

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Outline
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES.M
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.facebook.common.Common
import com.moment.app.MomentApp
import com.moment.app.R
import com.moment.app.databinding.FragmentChooseAlbumBinding
import com.moment.app.images.Explorer
import com.moment.app.images.bean.MediaFile
import com.moment.app.images.engine.MediaStoreHelper
import com.moment.app.images.engine.OkDisplayCompat
import com.moment.app.images.media.IMediaContract
import com.moment.app.images.media.MediaDirectoryWindow
import com.moment.app.images.media.adapter.MediaLimitChecker
import com.moment.app.images.utils.ExpUtil
import com.moment.app.permissions.PermissionHelper
import com.moment.app.utils.BaseFragment
import com.moment.app.utils.CommonUtil
import com.moment.app.utils.DialogUtils
import com.moment.app.utils.popBackStackNowAllowingStateLoss
import com.moment.app.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception
import javax.inject.Inject



class ChooseAlbumFragment:  BaseFragment() , IMediaContract.IMediaDataView{

    private val permissionRequestCode = 1001

    private lateinit var adapter: MediaAdapter

    private lateinit var window: MediaDirectoryWindow

    private lateinit var loader : IMediaContract.MediaLoader

    private lateinit var binding: FragmentChooseAlbumBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChooseAlbumBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.isClickable = true

        adapter = MediaAdapter(this, requireContext())
        window = MediaDirectoryWindow((requireContext()))
        loader = IMediaContract.MediaLoader(DialogUtils.getActivity(requireContext()) as AppCompatActivity, requireArguments(),this)
        binding.explorerRecycler.layoutManager = androidx.recyclerview.widget.GridLayoutManager(requireContext(), 4)
        binding.explorerRecycler.adapter = adapter
        window.anchorView = binding.explorerMenuDirectory

        window.setOnItemClickListener { _, _, position, _ ->
            window.setSelectedIndex(position)
            val item = window.getItem(position)?:return@setOnItemClickListener
            adapter.selectDirectoryById(item.id)
            binding.explorerTvDirectory.text = item.name
            binding.explorerRecycler.scrollToPosition(0)
            window.dismiss()
        }
        ExpUtil.setTopPadding(binding.explorerToolbar, MomentApp.appContext)
        binding.cancel.setOnClickListener {
            (activity as? AppCompatActivity?)?.supportFragmentManager?.popBackStackNowAllowingStateLoss()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            tiramisuLogic()
        }else{
            lowerOfTiramisuLogic()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionRequestCode && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initData()
            }
        }
    }


    @RequiresApi(M)
    private fun tiramisuLogic(){
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) == PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_VIDEO) == PERMISSION_GRANTED
        ){
            initData()
        }else{
            requestPermissions(
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO),
                permissionRequestCode
            )
        }
    }

    private fun lowerOfTiramisuLogic(){
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {
            initData()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    permissionRequestCode
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        if (requestCode == Explorer.EXPLORER_MEDIA_PREVIEW) {
            Log.d("@@@==>", "requestCode == EXPLORER_MEDIA_PREVIEW")
            //这里接收通知，处理数据给最上层
            setResultToPrevious()
        }
    }

    // 切换Fragment的方法
    private fun showClip(showClip: Boolean) {
        activity?.let {
            val fragmentManager: FragmentManager = (activity as AppCompatActivity).supportFragmentManager
            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
            if (showClip) {
                fragmentTransaction.setCustomAnimations(
                    R.anim.f_slide_out_left,  // A Fragment退出动画
                    R.anim.f_slide_in_right,  // B Fragment进入动画
                    R.anim.f_slide_out_right,  // B Fragment退出动画（之后使用）
                    R.anim.f_slide_in_left // A Fragment重新进入动画（之后使用）
                )

                fragmentTransaction
                    .add(R.id.root_layout, ClipImageFragment())
                    .addToBackStack(null)
                    .commitNowAllowingStateLoss()
            }
        }
    }

    private fun setResultToPrevious() {
        val intent = Intent()
        val selectedUris = ArrayList<Uri>()
        MediaStoreHelper.selectedFiles.forEach { path ->
            selectedUris.add(Uri.parse(path))
        }
        intent.putParcelableArrayListExtra(Explorer.EXTRA_RESULT_SELECTION, selectedUris)
        intent.putStringArrayListExtra(
            Explorer.EXTRA_RESULT_SELECTION_PATH,
            MediaStoreHelper.selectedFiles as java.util.ArrayList<String>?
        )
        intent.putExtras(Bundle().also {
            val data = MediaStoreHelper.fetchSelectedFileList()
            it.putParcelableArrayList(Explorer.EXTRA_RESULT_SELECTION_DATA, data as ArrayList<MediaFile>)
        })
//        setResult(RESULT_OK, intent)
//        finish()
    }

    private fun initData() {
        loader.fetchMediaData()
        adapter.notifyDataSetChanged()
    }

    override fun onRefreshData() {
        bindData2Adapter()
    }


    private fun bindData2Adapter() {
        MediaStoreHelper.updateSelect()
        adapter.update()
    }
}


class MediaAdapter(private val f: Fragment, private val context: Context) :
    RecyclerView.Adapter<com.moment.app.login_profile.MediaAdapter.MediaViewHolder>() {

    private var size = context.resources.displayMetrics.widthPixels / Explorer.DEFAULT_SPAN_COUNT

    private val imageList: MutableList<MediaFile> = mutableListOf()

    var selectAction: (() -> Unit)? = null

    var videoSelectAction: ((file: MediaFile) -> Unit)? = null

    var latestDirId = "ALL"

    private var extras: Bundle? = null

    fun parseBundle(bundle: Bundle?) {
        this.extras = bundle
        bundle?.let {
            size = context.resources.displayMetrics.widthPixels / it.getInt("extra_span_count",
                Explorer.DEFAULT_SPAN_COUNT)
        }
    }

    fun update() {
        selectDirectoryById(latestDirId)
    }

    fun selectDirectoryById(id: String) {
        MediaStoreHelper.fetchFilesByDirId(id) { files, latestDirId ->
            setData(files, true)
            this.latestDirId = latestDirId
        }
    }

    private fun setData(data: MutableList<MediaFile>, forceClear: Boolean = false) {
        if (data.size == 0) return

        if (forceClear) {
            imageList.clear()
        }

        imageList.addAll(data)
        notifyDataSetChanged()
    }

    private fun removeDirtyFile(file: MediaFile) {
        MediaStoreHelper.removeDirtyFile(latestDirId, file) { files ->
            setData(files, true)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_view_nothing, parent, false)
                .also {
                    it.layoutParams = RecyclerView.LayoutParams(size, size)
                }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.findViewById<ImageView>(R.id.explorer_media_thumb_view).outlineProvider = object : ViewOutlineProvider(){
                override fun getOutline(target: View?, outline: Outline?) {
                    val rect = Rect()
                    target?.getGlobalVisibleRect(rect);
                    val selfRect = Rect(0, 0,
                        target!!.width, target.height);
                    //这里还可以使用setRect()矩形  setOval()圆形
                    outline?.setRoundRect(selfRect, ExpUtil.dp2px(4f, context).toFloat())
                }

            }
            view.findViewById<ImageView>(R.id.explorer_media_thumb_view).clipToOutline = true
        }

        return MediaViewHolder(view)
    }


    override fun getItemCount(): Int = imageList.size

    override fun onBindViewHolder(holder: com.moment.app.login_profile.MediaAdapter.MediaViewHolder, position: Int) {
        holder.setData(imageList[position])
    }

    inner class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun setData(file: MediaFile) {
            try {
                OkDisplayCompat.loadThumb(itemView.findViewById<ImageView>(R.id.explorer_media_thumb_view), file, 0.3f, {
                    removeDirtyFile(it)
                })


                itemView.setOnClickListener {
                    // clipiamgeview
//                    Explorer.mediaPreview(context as AppCompatActivity,
//                        false,
//                        adapterPosition,
//                        latestDirId,
//                        extras)
                    (context as? AppCompatActivity?)?.supportFragmentManager
                        ?.beginTransaction()
                        ?.setCustomAnimations(
                            R.anim.f_slide_in_right, // 进入动画
                            R.anim.f_slide_out_left, // 退出动画（这里没有设置，所以为0）, // 退出动画（这里没有设置，所以为0）
                            R.anim.f_slide_in_left, // 弹出动画（这里没有设置，所以为0）
                            R.anim.f_slide_out_right  // 弹入动画（这里没有设置，所以为0）
                        )
                        ?.hide(f)
                        ?.add(R.id.root_layout, ClipImageFragment().apply {
                            arguments = bundleOf("extra_mode" to Explorer.MODE_ONLY_IMAGE)
                        })?.addToBackStack(null)
                        ?.commitAllowingStateLoss()
                }


                itemView.isEnabled = true

            } catch (e: Exception) {
                Explorer.getDataTrack()?.onError("MediaAdapter#setData", e)
            }
        }
    }
}