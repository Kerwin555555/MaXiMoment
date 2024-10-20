package com.moment.app.main_home.subfragments.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.moment.app.datamodel.UserInfo
import com.moment.app.main_home.subfragments.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class RecommendationPagingViewModel
    @Inject constructor(private val repository: Repository)
    : ViewModel() {

    val data : LiveData<PagingData<UserInfo>> =
        repository.fetchUserInfoList().cachedIn(viewModelScope).asLiveData()
}