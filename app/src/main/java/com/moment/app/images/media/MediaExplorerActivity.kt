package com.moment.app.images.media

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES.M
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.core.os.bundleOf
import com.moment.app.R
import com.moment.app.databinding.ActivityMediaExplorerBinding
import com.moment.app.images.Explorer
import com.moment.app.images.bean.MediaFile
import com.moment.app.images.engine.MediaStoreHelper
import com.moment.app.images.media.adapter.MediaAdapter
import com.moment.app.images.utils.ExpUtil

/**
 * media文件选择器
 * 访问storage权限暂时在外部调用，等组件化依赖的工具类抽离出来再在内部判断运行时权限
 */
class MediaExplorerActivity : AppCompatActivity(), IMediaContract.IMediaDataView {

    private val permissionRequestCode = 1001

    private val adapter by lazy {
        MediaAdapter(this)
    }

    private val window by lazy {
        MediaDirectoryWindow(this)
    }

    private val loader by lazy {
        IMediaContract.MediaLoader(this,intent.extras,this)
    }

    private lateinit var binding: ActivityMediaExplorerBinding
    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        } else {
            getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            )
        }
        super.onCreate(savedInstanceState)
        binding = ActivityMediaExplorerBinding.inflate(layoutInflater)
        binding.explorerRecycler.setHasFixedSize(true)
        setContentView(binding.root)
        binding.cancel.setOnClickListener { this.finish() }
        binding.explorerRecycler.layoutManager = androidx.recyclerview.widget.GridLayoutManager(this, intent?.extras?.getInt(Explorer.EXTRA_SPAN_COUNT)?:Explorer.DEFAULT_SPAN_COUNT)
        binding.explorerRecycler.adapter = adapter

        window.anchorView = binding.explorerMenuDirectory

        binding.explorerMenuDirectory.setOnClickListener {
            window.show()
        }

        window.setOnItemClickListener { _, _, position, _ ->
            window.setSelectedIndex(position)
            val item = window.getItem(position)?:return@setOnItemClickListener
            adapter.selectDirectoryById(item.id)
            binding.explorerTvDirectory.text = item.name
            binding.explorerRecycler.scrollToPosition(0)
            window.dismiss()
        }

       binding.explorerBottomBtnPreview.setOnClickListener {
            if (MediaStoreHelper.fetchSelectedFileList().filter { it.isVideo }.toList().isNotEmpty()){
                Explorer.videoPreview(this,MediaStoreHelper.selectedFiles[0])
            }else {
                Explorer.mediaPreview(this, true, 0, "", intent.extras)
            }
        }

        binding.explorerBottomBtnSend.setOnClickListener {
//            Log.d("@@@==>", "explorer_bottom_btn_apply clicked...")
            setResultToPrevious()
        }

        adapter.selectAction = {
            updateBottomBar()
        }

        adapter.videoSelectAction = {
            setVideoResultToPrevious(it)
        }
        ExpUtil.setTopPadding(binding.explorerToolbar, this.applicationContext)
        handleIntent()
    }

    private fun handleIntent() {
        adapter.parseBundle(intent.extras)
    }

    override fun onPause() {
        super.onPause()
        loader.release()
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            tiramisuLogic()
        }else{
            lowerOfTiramisuLogic()
        }
    }

    @RequiresApi(M)
    private fun tiramisuLogic(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PERMISSION_GRANTED){
            initData()
        }else{
            requestPermissions(
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO),
                permissionRequestCode
            )
        }
    }

    private fun lowerOfTiramisuLogic(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {
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

    private fun initData() {
        loader.fetchMediaData()
        updateBottomBar()
        adapter.notifyDataSetChanged()
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

    override fun finish() {
        MediaStoreHelper.release()
        super.finish()
    }

    private fun bindData2Adapter() {
        MediaStoreHelper.updateSelect()
        adapter.update()
    }

    private fun updateBottomBar() {
        when {
            MediaStoreHelper.selectedFiles.isEmpty() -> {
                binding.explorerBottomBtnPreview.isEnabled = false
                binding.explorerBottomBtnSend.isEnabled = false
                binding.explorerBottomBtnSend.text = getString(R.string.explorer_apply)
            }
            else -> {
                binding.explorerBottomBtnPreview.isEnabled = true
                binding.explorerBottomBtnSend.isEnabled = true
                binding.explorerBottomBtnSend.text =
                    getString(R.string.explorer_apply) + "(${MediaStoreHelper.selectedFiles.size})"
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
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
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun setVideoResultToPrevious(file: MediaFile) {
        val intent = Intent()
        val selectedUris = ArrayList<Uri>()
        selectedUris.add(Uri.parse(file.path))
        intent.putParcelableArrayListExtra(Explorer.EXTRA_RESULT_SELECTION, selectedUris)
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onRefreshData() {
        bindData2Adapter()
    }

    override fun onDestroy() {
        MediaStoreHelper.release()
        super.onDestroy()
    }
}