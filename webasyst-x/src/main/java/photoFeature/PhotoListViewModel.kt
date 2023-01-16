package com.webasyst.x.photoFeature

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import api.PhotoApiClient
import api.PhotoApiClientFactory
import com.webasyst.api.Installation
import com.webasyst.x.WebasystXApplication
import com.webasyst.x.util.ConnectivityUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import models.Photos

class PhotoListViewModel(
    app: Application,
    private val installationId: String?,
    private val installationUrl: String?
) : AndroidViewModel(app) {
    init {
        val connectivityUtil = ConnectivityUtil(getApplication())
        viewModelScope.launch(Dispatchers.Default) {
            connectivityUtil.connectivityFlow()
                .collect {
                    if (it == ConnectivityUtil.ONLINE) {
                        updateData(getApplication())
                    }
                }
        }
    }

    private val mutableState = MutableLiveData<Int>().apply { value = STATE_UNKNOWN }
    val state: LiveData<Int> = mutableState

    private val _error = MutableLiveData<Throwable?>(null)
    val error: LiveData<Throwable?> get() = _error

    private val mutablePhotoList = MutableLiveData<List<Photos.Photo>>()
    val photosList: LiveData<List<Photos.Photo>> = mutablePhotoList

    suspend fun updateData(context: Context) {
        if (mutableState.value == STATE_LOADING_DATA) {
            return
        }
        mutableState.postValue(STATE_LOADING_DATA)
        if (installationId == null || installationUrl == null) {
            return
        }
        val siteApiClient = (getApplication<WebasystXApplication>()
            .getApiClient()
            .getFactory(PhotoApiClient::class.java) as PhotoApiClientFactory)
            .instanceForInstallation(Installation(installationId, installationUrl))
        siteApiClient
            .getPhotoList()
            .onSuccess {
                _error.postValue(null)
                mutableState.postValue(if (it.photos.isEmpty()) STATE_DATA_EMPTY else STATE_DATA_READY)
                mutablePhotoList.postValue(it.photos)
            }
            .onFailure {
                _error.postValue(it)
                mutableState.postValue(STATE_ERROR)
            }
    }

    class Factory(
        private val application: Application,
        private val installationId: String?,
        private val installationUrl: String?
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T =
            PhotoListViewModel(application, installationId, installationUrl) as T
    }

    companion object {
        private const val TAG = "photo_list"
        const val STATE_UNKNOWN = 0
        const val STATE_LOADING_DATA = 1
        const val STATE_DATA_READY = 2
        const val STATE_DATA_EMPTY = 3
        const val STATE_ERROR = 4
    }
}
