package com.bangkit.emergenz.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.bangkit.emergenz.data.api.ApiConfigCloud
import com.bangkit.emergenz.data.local.datastore.UserPreferences
import com.bangkit.emergenz.data.response.GetDetailResponse
import com.bangkit.emergenz.util.extractErrorMessageFromJson
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileViewModel(private val pref: UserPreferences) : ViewModel() {
    private val _detailUser = MutableLiveData<GetDetailResponse?>()
    val detailUser: LiveData<GetDetailResponse?> = _detailUser

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toast = MutableLiveData<String>()
    val toast: LiveData<String> = _toast

    private val _chckErr = MutableLiveData<Boolean>()
    val chckErr: LiveData<Boolean> = _chckErr

    private fun getDetail(q: String) {
        _isLoading.value = true
        val client = ApiConfigCloud.getApiService().getDetailUser(q)
        client.enqueue(object : Callback<GetDetailResponse> {
            override fun onResponse(
                call: Call<GetDetailResponse>,
                response: Response<GetDetailResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful){
                    _chckErr.value = false
                    val responseBody = response.body()
                    if (responseBody != null){
                        _detailUser.value = responseBody
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = extractErrorMessageFromJson(errorBody)
                    _toast.value = errorMessage
                    _chckErr.value = true
                }
            }

            override fun onFailure(call : Call<GetDetailResponse>, t: Throwable) {
                _isLoading.value = false
                _toast.value = t.message
            }
        })
    }
    fun saveToken(Token: String) {
        viewModelScope.launch {
            pref.saveToken(Token)
        }
    }

    fun setSession(Session: Boolean) {
        viewModelScope.launch {
            pref.setSession(Session)
        }
    }

    fun getEmail(): LiveData<String> {
        return pref.getEmail().asLiveData()
    }
    fun getDetailIntent(q : String){
        getDetail(q)
    }

    fun getError(): Boolean? {
        return chckErr.value
    }
}