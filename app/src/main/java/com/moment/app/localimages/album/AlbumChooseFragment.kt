package com.moment.app.localimages.album

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.moment.app.R
import com.moment.app.localimages.AlbumSearcher
import com.moment.app.localimages.logic.MediaStoreHelper
import com.moment.app.localimages.album.adapter.AlbumAdapter
import java.lang.IllegalArgumentException

class AlbumChooseFragment : Fragment(), IAlbumInterface.IAlbumDataView {

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle?): AlbumChooseFragment {
            return AlbumChooseFragment().also {
                it.arguments = bundle
            }
        }
    }

    private val adapter by lazy {
        activity?.let {
            AlbumAdapter(it)
        }
    }

    private val loader by lazy {
        activity?.let {
            IAlbumInterface.MediaLoader(it as AppCompatActivity, arguments, this)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context !is AppCompatActivity) throw IllegalArgumentException("parent activity must be AppCompatActivity")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_media_collect_lit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val explorer_recycler = view.findViewById<RecyclerView>(R.id.explorer_recycler)
        explorer_recycler.layoutManager = androidx.recyclerview.widget.GridLayoutManager(activity, arguments?.getInt(AlbumSearcher.EXTRA_SPAN_COUNT)?: AlbumSearcher.DEFAULT_SPAN_COUNT)
        explorer_recycler.adapter = adapter
        adapter?.parseBundle(arguments)
        adapter?.selectAction = {

        }
    }

    override fun onDestroyView() {
        MediaStoreHelper.release()
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        loader?.fetchMediaData()
    }

    override fun onPause() {
        super.onPause()
        loader?.release()
    }

    override fun onRefreshData() {
        MediaStoreHelper.updateSelect()
        adapter?.selectDirectoryById(MediaStoreHelper.directories[0].id)
    }
}