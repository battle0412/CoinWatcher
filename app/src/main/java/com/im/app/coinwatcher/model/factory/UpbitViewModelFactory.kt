package com.im.app.coinwatcher.model.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.im.app.coinwatcher.common.UpbitRestService
import com.im.app.coinwatcher.okhttp_retrofit.RetrofitOkHttpManagerUpbit
import com.im.app.coinwatcher.repository.UpbitRepository

@Suppress("UNCHECKED_CAST")
class UpbitViewModelFactory(private val repository: UpbitRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if(modelClass.isAssignableFrom(UpbitViewModel::class.java)){
            UpbitViewModel(repository) as T
        } else {
            throw IllegalArgumentException("뷰모델을 찾을 수 없습니다.")
        }
    }
}