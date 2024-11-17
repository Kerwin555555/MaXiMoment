package com.moment.app.main_feed_publish

import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.SimpleItemAnimator
import com.blankj.utilcode.util.KeyboardUtils
import com.didi.drouter.annotation.Router
import com.moment.app.databinding.ActivityFeedPublishBinding
import com.moment.app.localimages.datamodel.AlbumItemFile
import com.moment.app.main_feed_publish.adapters.CameraAlbumDataAdapter
import com.moment.app.main_feed_publish.adapters.CameraAlbumDataAdapter.Companion.REQUEST_CODE_TAKE
import com.moment.app.main_feed_publish.adapters.UploadImageAdapter
import com.moment.app.main_feed_publish.extensions.Action
import com.moment.app.main_feed_publish.extensions.PostStatus
import com.moment.app.utils.BaseActivity
import com.moment.app.utils.checkAndGotoCamera
import com.moment.app.utils.checkAndSelectPhotos
import com.moment.app.utils.checkReadWritePermission
import com.moment.app.utils.dp
import com.moment.app.utils.fetchAllAlbumImages
import com.moment.app.utils.immersion
import com.moment.app.utils.isRTL
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
@Router(scheme = ".*", host = ".*", path = "/feed/publish")
class PostSubmissionActivity : BaseActivity() {
    private val viewModel by viewModels<PostSubmissionViewModel>()
    private lateinit var binding: ActivityFeedPublishBinding

    private lateinit var albumAdapter: CameraAlbumDataAdapter
    private lateinit var uploadImageAdapter : UploadImageAdapter
    private var currentPhoto : Uri? = null
    private var hasLoadImages = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        immersion()
        binding = ActivityFeedPublishBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.back.setOnClickListener {
            finish()
        }
        binding.complete.setOnClickListener {

        }
        setSwipeBackEnable(false)
        binding.album.layoutManager = GridLayoutManager(this, 4)
        binding.chosen.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)

        binding.chosen.addItemDecoration(object : ItemDecoration(){
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                super.getItemOffsets(outRect, view, parent, state)
                 if (!view.isRTL())  outRect.right = 5.dp
                 else  outRect.left = 5.dp
            }
        })
        uploadImageAdapter = UploadImageAdapter(viewModel)
        albumAdapter = CameraAlbumDataAdapter(viewModel)
        binding.album.adapter = albumAdapter
        binding.chosen.adapter = uploadImageAdapter

        binding.editText.addTextChangedListener(
            onTextChanged = {s,_,_,_ ->
                viewModel.dispatchAction(Action.UpdateTextAction.apply {
                    this.text = s?.toString()?.trim()?.replace("\n", "") ?: ""
                })
            }
        )

        albumAdapter.goAndTakeAPhoto = {
            checkAndGotoCamera(REQUEST_CODE_TAKE) { it ->
                currentPhoto = it
            }
        }
        albumAdapter.goAndChoosePhotos = {
            checkAndSelectPhotos {
               fetchAllAlbumImages {  album->
                   if (!hasLoadImages) {
                       hasLoadImages = true
                       albumAdapter.addData(album.files)
                   }
               }
            }
        }
        fillCameraAlbumAdapter()

        viewModel.publishLiveData.observe(this) { it ->
            notifyStateChanged(it)
        }
        (binding.album.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false //remove shimmer
        (binding.chosen.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false //remove shimmer

        binding.root.postDelayed({
            binding.editText.requestFocus()
        }, 50)
    }

    private fun notifyStateChanged(it: PostStatus) {
        it.latestImageAction?.let { action ->
            when (action) {
                is Action.AddImageAction  -> {
                    albumAdapter.notifyItemChanged(action.imageAlbumPosition)
                    uploadImageAdapter.addData(it.linkedHashMap[action.imageAlbumPosition] as AlbumItemFile)
                }
                is Action.RemoveImageAction -> {
                    albumAdapter.notifyItemChanged(action.imageAlbumPosition)
                    for ((k,v) in it.linkedHashMap) {
                         //剩下的照片也刷新 （照片adapter pos k>0）
                         if (k > 0) albumAdapter.notifyItemChanged(k)
                    }
                    uploadImageAdapter.remove(action.imageUploadPosition)
                  }
                is Action.AddNewPhotoAction -> {
                    uploadImageAdapter.addData(action.uri!!)
                }
                is Action.RemoveNewPhotoAction -> {
                    for ((k,v) in it.linkedHashMap) {
                        if (k > 0) albumAdapter.notifyItemChanged(k)
                    }
                    uploadImageAdapter.remove(action.photoAdapterPosition)
                }
                else -> {}
            }
        }
        binding.complete.isEnabled = it.capableOfBeingDispatched()
    }

    private fun fillCameraAlbumAdapter() {
        albumAdapter.addData(AlbumItemFile().apply {
            mimeType = "camera_moment"
        })
        if (checkReadWritePermission()) {
            fetchAllAlbumImages {  album ->
                //有的会触发 content observer
                if (!hasLoadImages) {
                    hasLoadImages = true
                    albumAdapter.addData(album.files)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_TAKE && resultCode == RESULT_OK) {
            if (!viewModel.isImagesFull() && currentPhoto != null) {
                viewModel.dispatchAction(Action.AddNewPhotoAction.apply {
                    uri = currentPhoto
                })
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        kotlin.runCatching {
            KeyboardUtils.hideSoftInput(binding.editText)
        }
    }
}
