package com.im.app.coinwatcher

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.im.app.coinwatcher.common.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AutoTradingService: Service() {
    private var flag = true
    //시장가 매매이므로 한번 주문하면 체결되므로 다시 주문할 필요 없음
    private var buyFlag = true
    private var sellFlag = true
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
        builder.setSmallIcon(R.mipmap.ic_launcher)
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
                    toastMessage("포어그라운드 서비스 가동중")
                }
                delay(5000L)
                /*val candles = getCandles(market, unit)
                val rsi = calculateRSI(candles)
                val stochasticFastSlow = stochasticFastSlow(candles)
                //빈값이면 통과
                val isBuy: Boolean = (rsiLess.isEmpty() || rsiLess.toFloat() <= rsi.toFloat()) &&
                        (buySlowK.isEmpty() || buySlowK.toFloat() <= stochasticFastSlow[0].toFloat()) &&
                        (buySlowD.isEmpty() || buySlowD.toFloat() <= stochasticFastSlow[1].toFloat()) &&
                        (priceLess.isEmpty() || priceLess.toFloat() <= candles[0].trade_price.toFloat())
                val isSell: Boolean = (rsiMore.isEmpty() || rsiMore.toFloat() >= rsi.toFloat()) &&
                        (sellSlowK.isEmpty() || sellSlowK.toFloat() >= stochasticFastSlow[0].toFloat()) &&
                        (sellSlowD.isEmpty() || sellSlowD.toFloat() >= stochasticFastSlow[1].toFloat()) &&
                        (priceMore.isEmpty() || priceMore.toFloat() >= candles[0].trade_price.toFloat())

                if(isBuy && buyFlag){
                    val map = mutableMapOf<String, String>()
                    map["market"] = market
                    map["side"] = "bid" //매수
                    map["volume"] = ""
                    map["price"] = buyPrice
                    map["ord_type"] = "price" //시장가
                    map["identifier"] = ""
                    val requestBody = Gson().toJson(map).toString()
                        .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
                    val rest = RetrofitOkHttpManagerUpbit(generateJWT(map)).restService
                    val requestOrders = UpbitAPIService(rest.requestOrders(requestBody))
                        .responseUpbitAPI()
                    buyFlag = false
                }

                if(isSell && sellFlag){
                    val map = mutableMapOf<String, String>()
                    map["market"] = market
                    map["side"] = "ask" //매도
                    map["volume"] = volume
                    map["price"] = ""
                    map["ord_type"] = "price" //시장가
                    map["identifier"] = ""
                    val requestBody = Gson().toJson(map).toString()
                        .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
                    val rest = RetrofitOkHttpManagerUpbit(generateJWT(map)).restService
                    val requestOrders = UpbitAPIService(rest.requestOrders(requestBody))
                        .responseUpbitAPI()
                    sellFlag = false
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
                    },
                )*/
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        flag = false
        buyFlag = false
        sellFlag = false
    }
}