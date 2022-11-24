package com.im.app.coinwatcher.okhttp_retrofit

import com.im.app.coinwatcher.common.BASE_URL
import com.im.app.coinwatcher.common.UpbitRestService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitOkHttpManagerUpbit(private val JWT_Token: String) {
    private var okHttpClient: OkHttpClient

    private val retrofitBuiler: Retrofit.Builder = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
    val restService: UpbitRestService
    get() = retrofitBuiler.build().create(UpbitRestService::class.java)
    init {
        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val request = chain.request()
                val newRequest: Request = request.newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", JWT_Token)
                    .build()
                chain.proceed(newRequest)
            }).addInterceptor(httpInterceptor())
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS).build()
        retrofitBuiler.client(okHttpClient)
    }

    private fun httpInterceptor(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return interceptor
    }
}