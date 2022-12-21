package com.im.app.coinwatcher.auto_trading

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.im.app.coinwatcher.MainActivity
import com.im.app.coinwatcher.R
import com.im.app.coinwatcher.common.*
import com.im.app.coinwatcher.json_data.Order
import com.im.app.coinwatcher.json_data.Orders
import com.im.app.coinwatcher.json_data.OnError
import com.im.app.coinwatcher.model.factory.UpbitViewModel
import com.im.app.coinwatcher.okhttp_retrofit.RetrofitOkHttpManagerUpbit
import com.im.app.coinwatcher.repository.UpbitRepository
import com.im.app.coinwatcher.sqlite.SQLiteManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class AutoTradingService: Service() {
    private var flag = true
    //시장가 매매이므로 한번 주문하면 체결되므로 다시 주문할 필요 없음
    private var buyFlag = true
    private var sellFlag = true
    private var dbInstance = SQLiteManager.getDBInstance(this)
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate() {
        super.onCreate()
        createNotification()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        autoTradingService(intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    @RequiresApi(Build.VERSION_CODES.S)
    private fun createNotification() {
        //Notification을 사용자가 터치하면 돌아갈 컴포넌트를 설정
        val notifyIntent = Intent(this, MainActivity::class.java)
        notifyIntent.putExtra("autoTrading", true)
        val pIntent = PendingIntent.getActivity(this, 0, notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            //채널 생성
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                channel
            )
        }
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, CHANNEL_ID)
        builder.setSmallIcon(R.drawable.logo_white_size)
            .setContentTitle("자동매매")
            .setContentText("자동매매 서비스 실행중")
            .setContentIntent(pIntent)
        //통지바에 해당 Notification 을 전송한다
        startForeground(10235, builder.build())
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun autoTradingService(intent: Intent){
        val market = intent.getStringExtra("market") ?: ""
        val unit = intent.getStringExtra("unit") ?: ""
        val rsiLess = intent.getStringExtra("rsiLess") ?: ""
        val buySlowK = intent.getStringExtra("buySlowK") ?: ""
        val buySlowD = intent.getStringExtra("buySlowD") ?: ""
        val priceLess = intent.getStringExtra("priceLess") ?: ""
        val buyPrice = intent.getStringExtra("buyPrice") ?: ""
        val rsiMore = intent.getStringExtra("rsiMore") ?: ""
        val sellSlowK = intent.getStringExtra("sellSlowK") ?: ""
        val sellSlowD = intent.getStringExtra("sellSlowD") ?: ""
        val priceMore = intent.getStringExtra("priceMore") ?: ""
        val volume = intent.getStringExtra("volume") ?: ""
        CoroutineScope(Dispatchers.IO).launch {
            while(flag){
                CoroutineScope(Dispatchers.Main).launch {
                    toastMessage("자동매매 시작")
                }
                val candles = getCandles(market, unit, 200)
                val rsi = calculateRSI(candles)
                val stochasticFastSlow = stochasticFastSlow(candles)
                //빈값이면 통과
                val isBuy: Boolean = (rsiLess.isEmpty() || rsiLess.toFloat() >= rsi.toFloat()) &&
                        (buySlowK.isEmpty() || buySlowK.toFloat() >= stochasticFastSlow[0].toFloat()) &&
                        (buySlowD.isEmpty() || buySlowD.toFloat() >= stochasticFastSlow[1].toFloat()) &&
                        (priceLess.isEmpty() || priceLess.toFloat() >= candles[0].trade_price.toFloat()) &&
                        buyPrice.isNotEmpty()
                val isSell: Boolean = (rsiMore.isEmpty() || rsiMore.toFloat() <= rsi.toFloat()) &&
                        (sellSlowK.isEmpty() || sellSlowK.toFloat() <= stochasticFastSlow[0].toFloat()) &&
                        (sellSlowD.isEmpty() || sellSlowD.toFloat() <= stochasticFastSlow[1].toFloat()) &&
                        (priceMore.isEmpty() || priceMore.toFloat() <= candles[0].trade_price.toFloat()) &&
                        volume.isNotEmpty()

                if(isBuy && buyFlag){
                    val map = mutableMapOf<String, String>()
                    map["market"] = market
                    map["side"] = "bid" //매수
                    map["volume"] = ""
                    map["price"] = buyPrice
                    map["ord_type"] = "price" //시장가
                    //map["identifier"] = ""
                    buyFlag = !requestTrading(map)

                }

                if(isSell && sellFlag){
                    val map = mutableMapOf<String, String>()
                    map["market"] = market
                    map["side"] = "ask" //매도
                    map["volume"] = volume
                    map["price"] = ""
                    map["ord_type"] = "market" //시장가
                    //map["identifier"] = ""

                    sellFlag = !requestTrading(map)
                }
                
                delay(10000 *   //10초 단위
                        when(unitMapping(unit)){
                            "1" -> 3L
                            "3" -> 9L
                            "5" -> 15L
                            "10", "15" -> 30L //300초 = 5분
                            "30", "60" -> 60L //600초 = 10분
                            "240" -> 180L //1800초 = 30분
                            else -> 360L // 1시간
                    }
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private suspend fun requestTrading(map: MutableMap<String, String>): Boolean{
        try {
            val requestBody = Gson().toJson(map).toString()
                .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            val rest = RetrofitOkHttpManagerUpbit(map).restService
            val responseOrders = responseSyncUpbitAPI(rest.requestOrders(requestBody))
            if(responseOrders.contains("error")){
                val error = getGsonData(responseOrders, OnError::class.java)
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    toastMessage(error.error.message)
                }, 1000L)
                return false
            }
            val orders = getGsonData(responseOrders, Orders::class.java)
            val uuidMap = mutableMapOf<String, String>()
            uuidMap["uuid"] = orders.uuid
            insertTradingHD(orders)
            val rest2 = RetrofitOkHttpManagerUpbit(uuidMap).restService
            val responseOrder = responseSyncUpbitAPI(rest2.requestOrder(uuidMap["uuid"]!!))
            val order = getGsonData(responseOrder, Order::class.java)
            insertTradingDT(order)
        } catch (e: Exception){
            Log.e("UpbitAPI", e.message?.toString() ?: "")
            return false
        }
        return true
    }

    private fun insertTradingHD(orders: Orders) {
        val cv = ContentValues()
        with(cv){
            put("uuid", orders.uuid)
            put("side", orders.side)
            put("ord_type", orders.ord_type)
            put("price", orders.price)
            put("state", orders.state)
            put("market", orders.market)
            put("created_at", orders.created_at)
            put("volume", orders.volume)
            put("remaining_volume", orders.remaining_volume)
            put("reserved_fee", orders.reserved_fee)
            put("remaining_fee", orders.remaining_fee)
            put("paid_fee", orders.paid_fee)
            put("locked", orders.locked)
            put("executed_volume", orders.executed_volume)
            put("trades_count", orders.trades_count)
        }
        dbInstance.insertTrading(openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null), cv, TABLE_TRADING_HD)
    }

    private fun insertTradingDT(order: Order) {
        order.trades.forEach {
            val cv = ContentValues()
            with(cv){
                put("uuid", order.uuid)
                put("side", order.side)
                put("ord_type", order.ord_type)
                put("price", order.price)
                put("state", order.state)
                put("market", order.market)
                put("created_at", order.created_at)
                put("volume", order.volume)
                put("remaining_volume", order.remaining_volume)
                put("reserved_fee", order.reserved_fee)
                put("remaining_fee", order.remaining_fee)
                put("paid_fee", order.paid_fee)
                put("locked", order.locked)
                put("executed_volume", order.executed_volume)
                put("trades_count", order.trades_count.toInt())
                put("trades_market", it.market)
                put("trades_uuid", it.uuid)
                put("trades_price", it.price)
                put("trades_volume", it.volume)
                put("trades_funds", it.funds)
                put("trades_side", it.side)
                put("trades_trend", it.trend)
                put("trades_created_at", it.created_at)
            }
            dbInstance.insertTrading(openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null), cv, TABLE_TRADING_DT)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        flag = false
        buyFlag = false
        sellFlag = false
    }
}