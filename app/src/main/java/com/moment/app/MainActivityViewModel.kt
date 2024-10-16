package com.moment.app

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.moment.app.ui.BottomBar.Companion.DEFAULT_NAVI_TABS
import com.moment.app.ui.NaviTab
import com.moment.app.utils.StickyLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class MainActivityViewModel @Inject constructor(): ViewModel() {
    private val _naviTab = MutableLiveData<MutableList<NaviTab>>()
    val naviTab: LiveData<MutableList<NaviTab>> = _naviTab


    fun fetchTabs() {
        //from backend get the navitabs must be immediate
        _naviTab.value = DEFAULT_NAVI_TABS
    }
}