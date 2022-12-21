package com.im.app.coinwatcher.repository

import com.im.app.coinwatcher.common.UpbitRestService
import com.im.app.coinwatcher.common.unitMapping
import okhttp3.RequestBody

class UpbitRepository(private val upbitRestService: UpbitRestService) {
    suspend fun getAllMarkets() = upbitRestService.requestMarketAll()
    suspend fun getMarketTicker(markets: String) = upbitRestService.requestMarketsTicker(markets)
    suspend fun getCandles(unit: String, market: String, count: Int) = when(unit) {
        "일", "주", "월" -> upbitRestService.requestCandles(unitMapping(unit), market, count)
        else -> upbitRestService.requestMinuteCandles(unitMapping(unit).toInt(), market, count)
    }
    //suspend fun getCandles(unit: Int, market: String, count: Int) = upbitRestService.requestMinuteCandles(unit, market, count)
    suspend fun getAccounts() = upbitRestService.requestAccounts()

}