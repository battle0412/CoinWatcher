package com.im.app.coinwatcher.common

import com.im.app.coinwatcher.json_data.Accounts
import com.im.app.coinwatcher.json_data.Candles
import com.im.app.coinwatcher.json_data.MarketAll
import com.im.app.coinwatcher.json_data.MarketTicker
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface UpbitRestService {
    /*
    unit 분 단위. 가능한 값 : 1, 3, 5, 15, 10, 30, 60, 240
    market 마켓 코드 (ex. KRW-BTC)
    count 캔들 개수(최대 200개까지 요청 가능)
     */
    @GET("v1/candles/minutes/{unit}")
    suspend fun requestMinuteCandles(
        @Path("unit") unit: Int,
        @Query("market") market: String,
        @Query("count") count: Int
    ): Response<MutableList<Candles>>
    /*
    unit 일: days, 주: weeks, 월: months
     */
    @GET("v1/candles/{unit}")
    suspend fun requestCandles(
        @Path("unit") unit: String,
        @Query("market") market: String,
        @Query("count") count: Int
    ): Response<MutableList<Candles>>

    /*
    market *	마켓 ID (필수)	String
    side *	주문 종류 (필수)
    - bid : 매수
    - ask : 매도	String
    volume *	주문량 (지정가, 시장가 매도 시 필수)	NumberString
    price *	주문 가격. (지정가, 시장가 매수 시 필수)
    ex) KRW-BTC 마켓에서 1BTC당 1,000 KRW로 거래할 경우, 값은 1000 이 된다.
    ex) KRW-BTC 마켓에서 1BTC당 매도 1호가가 500 KRW 인 경우, 시장가 매수 시 값을 1000으로 세팅하면 2BTC가 매수된다.
    (수수료가 존재하거나 매도 1호가의 수량에 따라 상이할 수 있음)	NumberString
    ord_type *	주문 타입 (필수)
    - limit : 지정가 주문
    - price : 시장가 주문(매수)
    - market : 시장가 주문(매도)	String
    identifier	조회용 사용자 지정값 (선택)	String (Uniq 값 사용)
    */
    @POST("v1/orders")
    fun requestOrders(
        /*@Query("market") market: String,
        @Query("side") side: String,
        @Query("volume") volume: String,
        @Query("price") price: String,
        @Query("ord_type") ord_type: String,
        @Query("identifier") identifier: String*/
        @Body orders: RequestBody
    ): Call<ResponseBody>

    /*
    내가 보유한 자산 리스트
     */
    @GET("v1/accounts")
    suspend fun requestAccounts(): Response<MutableList<Accounts>>

    /*
    업비트에서 거래 가능한 마켓 목록
     */
    @GET("v1/market/all")
    suspend fun requestMarketAll(): Response<MutableList<MarketAll>>

    /*
    markets 반점으로 구분되는 마켓 코드 (ex. KRW-BTC, BTC-ETH)
     */
    @GET("v1/ticker")
    suspend fun requestMarketsTicker(
        @Query("markets") market: String
    ): Response<MutableList<MarketTicker>>

    /*
    주문번호로 주문조회
     */
    @GET("v1/order")
    fun requestOrder(
        @Query("uuid") uuid: String
    ): Call<ResponseBody>
}