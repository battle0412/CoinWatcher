package com.im.app.coinwatcher.common

import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.await
import java.lang.reflect.Type
import java.text.DecimalFormat

fun toastMessage(message: String){
    Toast.makeText(CoinWatcherApplication.getAppInstance(), message, Toast.LENGTH_SHORT).show()
}

// HashMap 형태로 받은 key = value 형태를
// ?key1=value1&key2=value2 ... 형태로
fun getQueryString(parameters: HashMap<String, String>): String {
    val queryElements = mutableListOf<String>()
    parameters.forEach { (key, value) -> queryElements.add("$key=$value") }

    return queryElements.joinToString(
        separator = "&",
        prefix = "?"
    )
}


fun <T> getGsonList(jsonArrayStr: String, classType: Class<T>): MutableList<T> {
    val typeOfT: Type = TypeToken.getParameterized(MutableList::class.java, classType).type
    return Gson().fromJson(jsonArrayStr, typeOfT)
}

fun <T> getGsonData(jsonArrayStr: String, classType: Class<T>): T {
    return Gson().fromJson(jsonArrayStr, classType)
}

suspend fun responseUpbitAPI(call: Call<ResponseBody>): String {
    val resultStr =
        withContext(Dispatchers.IO) {
            val response = call.await()
            response.let {
                response.string()
            }
        }
    return resultStr
}
//decimal: 소수점 여부
fun decimalFormat(input: Number, isPercent: Boolean): String {
    val dFormat = if(isPercent) DecimalFormat("#,##0.00")
    else {
        when(input.toFloat()){
            in 1f .. 100f -> DecimalFormat("#,##0.00")
            in 0f .. 1f -> DecimalFormat("0.00##")
            else -> DecimalFormat("#,##0")
        }
    }
    return dFormat.format(input)
}