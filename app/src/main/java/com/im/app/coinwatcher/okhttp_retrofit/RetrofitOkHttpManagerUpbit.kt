package com.im.app.coinwatcher.okhttp_retrofit

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.im.app.coinwatcher.common.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.math.BigInteger
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

@RequiresApi(Build.VERSION_CODES.N)
class RetrofitOkHttpManagerUpbit(params: Map<String, String>? = null) {
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
                    .addHeader("Authorization", generateJWT(params))
                    .build()
                chain.proceed(newRequest)
            }).addInterceptor(httpInterceptor())
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS).build()
        retrofitBuiler.client(okHttpClient)
    }

    private class RetryInterceptor: Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val request: Request = chain.request()
            var response: Response = chain.proceed(request)
            var tryCount = 0
            val maxLimit = 2

            while(!response.isSuccessful && tryCount < maxLimit){
                response.close()
                Log.d("TAG", "요청실패 - $tryCount")
                tryCount++
                response = chain.proceed(request)
            }
            return response
        }
    }
    private fun httpInterceptor(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return interceptor
    }
    companion object {
        @RequiresApi(Build.VERSION_CODES.N)
        fun generateJWT(params: Map<String, String>? = null): String {
            val algorithm: Algorithm = Algorithm.HMAC256(SECRET_KEY)
            val jwtToken = with(JWT.create()) {
                withClaim("access_key", ACCESS_KEY)
                withClaim("nonce", System.currentTimeMillis())//UUID.randomUUID().toString()
                withClaim("expiresIn","10s")
                //.withExpiresAt(Date(System.currentTimeMillis() + 30*1000))
                if (params != null) {
                    val paramStr = getQueryString(params)

                    val md = MessageDigest.getInstance("SHA-512")
                    md.update(paramStr.toByteArray(Charsets.UTF_8))
                    val hashByteArray = String.format("%0128x", BigInteger(1, md.digest()))
                    //val hashByteArray = bytesToHex(md.digest())
                    withClaim("query_hash", hashByteArray)
                    withClaim("query_hash_alg", "SHA512")
                }
                sign(algorithm)
            }
            return "Bearer $jwtToken"
        }
    }
}