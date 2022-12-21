package com.im.app.coinwatcher.model.factory

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.im.app.coinwatcher.common.getGsonData
import com.im.app.coinwatcher.json_data.*
import com.im.app.coinwatcher.repository.UpbitRepository
import kotlinx.coroutines.*
import okhttp3.RequestBody

class UpbitViewModel(private val repository: UpbitRepository): ViewModel() {
    //업비트에서 제공중인 시장 정보
    val marketList = MutableLiveData<MutableList<MarketAll>>()
    //요청당시 마켓들의 가격정보
    val marketTicker = MutableLiveData<MutableList<MarketTicker>>()
    //분 캔들
    //val minuteCandles = MutableLiveData<MutableList<Candles>>()
    //캔들 통합
    val candles = MutableLiveData<MutableList<Candles>>()
    //보유자산
    val accounts = MutableLiveData<MutableList<Accounts>>()
    //에러
    val errorMessage = MutableLiveData<OnError>()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("exceptionHandler: ","예외: ${throwable.localizedMessage}" )
    }

    fun getAllMarketsFromViewModel(){
        CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
            val response = repository.getAllMarkets()
            withContext(Dispatchers.Main){
                if(response.isSuccessful){
                    marketList.postValue(response.body())
                }
                else
                    errorMessage.postValue(onError(response.errorBody()!!.string()))
            }
        }
    }
    fun getMarketTickerFromViewModel(markets: String){
        CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
            val response = repository.getMarketTicker(markets)
            withContext(Dispatchers.Main){
                if(response.isSuccessful){
                    marketTicker.postValue(response.body())
                }
                else
                    errorMessage.postValue(onError(response.errorBody()!!.string()))
            }
        }
    }
    //분 캔들 -> 캔들 함수 통합
    /*fun getCandles(unit: Int, market: String, count: Int){
        CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
            val response = repository.getCandles(unit, market, count)
            withContext(Dispatchers.Main){
                if(response.isSuccessful){
                    minuteCandles.postValue(response.body())
                }
                else
                    onError("onError: ${response.errorBody()!!.string()}")
            }
        }
    }*/
    //캔들
    fun getCandlesFromViewModel(unit: String, market: String, count: Int){
        CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
            val response = repository.getCandles(unit, market, count)
            withContext(Dispatchers.Main){
                if(response.isSuccessful){
                    candles.postValue(response.body())
                }
                else
                    errorMessage.postValue(onError(response.errorBody()!!.string()))
            }
        }
    }

    fun getAccountsFromViewModel(){
        CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
            val response = repository.getAccounts()
            withContext(Dispatchers.Main){
                if(response.isSuccessful){
                    accounts.postValue(response.body())
                }
                else
                    errorMessage.postValue(onError(response.errorBody()!!.string()))
            }
        }
    }

    private fun onError(message: String): OnError{
        return getGsonData(message, OnError::class.java)
    }

    override fun onCleared() {
        super.onCleared()
    }
}