package com.webasyst.x

import android.app.Application
import androidx.annotation.MainThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.webasyst.x.api.ApiClient
import com.webasyst.x.api.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.openid.appauth.AuthState

class MainActivityViewModel(app: Application) : AndroidViewModel(app) {
    private val apiClient by lazy { ApiClient.getInstance(getApplication()) }

    private val mutableUserName = MutableLiveData<String>()
    val userName: LiveData<String> = mutableUserName

    private val mutableUserEmail = MutableLiveData<String>()
    val userEmail: LiveData<String> = mutableUserEmail

    private val mutableUserpicUrl = MutableLiveData<String>()
    val userpicUrl: LiveData<String> = mutableUserpicUrl

    private val mutableAuthState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = mutableAuthState
    @MainThread
    fun setAuthState(state: AuthState) {
        mutableAuthState.value = state
        if (state.isAuthorized) {
            updateUserInfo()
        }
    }

    init {
        updateUserInfo()
    }

    private fun updateUserInfo() {
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) { apiClient.getUserInfo() }
                .onSuccess { setUserInfo(it) }
                .onFailure { println(it)/* TODO */ }
        }
    }

    @MainThread
    private fun setUserInfo(userInfo: UserInfo) {
        mutableUserName.value = userInfo.name
        mutableUserEmail.value = userInfo.getEmail()
        mutableUserpicUrl.value = userInfo.userpic
    }
}
