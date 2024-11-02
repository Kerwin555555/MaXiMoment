package com.moment.app.localimages.album

import android.database.Cursor
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import com.moment.app.localimages.AlbumSearcher
import com.moment.app.localimages.logic.MediaStoreHelper
import com.moment.app.localimages.logic.loaders.PhotoGalleryLoader
import com.moment.app.localimages.logic.loaders.VideoGalleryLoader

interface IAlbumInterface {

    companion object{
        const val LOAD_ID_IMAGE = 1
        const val LOAD_ID_VIDEO = 100
    }

    interface IAlbumDataView{
        fun onRefreshData()
    }



    class MediaLoader(private val context: AppCompatActivity, private val extras: Bundle?, private val view:IAlbumDataView){

        private var mode = AlbumSearcher.MODE_ONLY_IMAGE

        init {
            mode = extras?.getInt("extra_mode")?:AlbumSearcher.MODE_ONLY_IMAGE
        }

        /**
         * 释放CursorLoader，在Activity或Fragment生命周期PAUSE时
         * 必须获取READ_EXTERNAL_STORAGE权限
         * <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
         */
        fun release(){
            LoaderManager.getInstance(context).destroyLoader(LOAD_ID_IMAGE)
            LoaderManager.getInstance(context).destroyLoader(LOAD_ID_VIDEO)
        }

        /**
         * 获取media数据
         * 必须获取READ_EXTERNAL_STORAGE权限
         * <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
         */
        fun fetchMediaData(){

            /**
             * 获取video数据
             */
            fun fetchVideoData(){
                LoaderManager.getInstance(context)
                        .initLoader(LOAD_ID_VIDEO, extras, object : LoaderManager.LoaderCallbacks<Cursor> {
                            override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
                                return VideoGalleryLoader(context)
                            }

                            override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
                                data?.let {
                                    MediaStoreHelper.parseVideoData(context,it,mode == AlbumSearcher.MODE_ONLY_VIDEO)
                                }

                                context.runOnUiThread {
                                    view.onRefreshData()
                                }
                            }

                            override fun onLoaderReset(loader: Loader<Cursor>) {

                            }

                        })
            }

            /**
             * 获取Image数据
             */
            fun fetchImageData(){
                LoaderManager.getInstance(context)
                        .initLoader(LOAD_ID_IMAGE, extras, object : LoaderManager.LoaderCallbacks<Cursor> {
                            override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
                                return PhotoGalleryLoader(context)
                            }

                            override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
                                data?.let {
                                    MediaStoreHelper.parseMediaData(context,it)
                                }

                                if (mode == AlbumSearcher.MODE_IMAGE_VIDEO || mode == AlbumSearcher.MODE_ALL_MEDIA){
                                    fetchVideoData()
                                }else{
                                    context.runOnUiThread {
                                        view.onRefreshData()
                                    }
                                }

                            }

                            override fun onLoaderReset(loader: Loader<Cursor>) {

                            }

                        })
            }



            if (mode == AlbumSearcher.MODE_ONLY_VIDEO){
                fetchVideoData()
            }else{
                fetchImageData()
            }
        }
    }

}