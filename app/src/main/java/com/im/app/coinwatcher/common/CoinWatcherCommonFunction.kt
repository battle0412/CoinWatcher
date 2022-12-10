package com.im.app.coinwatcher.common

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.im.app.coinwatcher.JWT.GeneratorJWT
import com.im.app.coinwatcher.json_data.Candles
import com.im.app.coinwatcher.okhttp_retrofit.RetrofitOkHttpManagerUpbit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.await
import java.lang.reflect.Type
import java.text.DecimalFormat
import kotlin.math.abs

fun toastMessage(message: String){
    Toast.makeText(CoinWatcherApplication.getAppInstance(), message, Toast.LENGTH_SHORT).show()
}

/**
 * HashMap 형태로 받은 key = value 형태를
 * ?key1=value1&key2=value2 ... 형태로
 */
fun getQueryString(parameters: HashMap<String, String>): String {
    val queryElements = mutableListOf<String>()
    parameters.forEach { (key, value) -> queryElements.add("$key=$value") }

    return queryElements.joinToString(
        separator = "&",
        //prefix = "?"
    )
}
fun getQueryString(parameters: Map<String, String>): String {
    val queryElements = mutableListOf<String>()
    parameters.forEach { (key, value) -> queryElements.add("$key=$value") }

    return queryElements.joinToString(
        separator = "&",
        //prefix = "?"
    )
}

/**
 * 제이슨 배열을 특정 클래스 배열로 리턴
 */
fun <T> getGsonList(jsonArrayStr: String, classType: Class<T>): MutableList<T> {
    val typeOfT: Type = TypeToken.getParameterized(MutableList::class.java, classType).type
    return Gson().fromJson(jsonArrayStr, typeOfT)
}

/**
 * 제이슨 배열을 특정 클래스로 리턴
 */
fun <T> getGsonData(jsonArrayStr: String, classType: Class<T>): T {
    return Gson().fromJson(jsonArrayStr, classType)
}

/**
 * 업비트 응답 함수
 * 
 */
/*suspend fun responseSyncUpbitAPI(call: Call<ResponseBody>): String {
    val resultStr =
        withContext(Dispatchers.IO) {
            val response = call.await()
            response.let {
                response.string()
            }
        }
    return resultStr
}*/

class UpbitAPIService(private val call: Call<ResponseBody>) {
    fun responseSyncUpbitAPI(): String {
        var resultStr = ""
        call.enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    try {
                        resultStr = response.body()!!.string()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    resultStr = response.errorBody()!!.string()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }
        })
        return resultStr
    }
}



/**
 * 업비트 market 소수점 자리수 표시방법
 * input: 변환할 숫자
 * isPercent: 소수점 여부
 * 퍼센트 -> 항상 소수점 2자리
 * 1~100 -> 소수점 2자리
 * 1이하  -> 소수점 4자리
 * 그외, 정수 -> 소수점 없음
 */
fun decimalFormat(input: Number, isPercent: Boolean = false): String {
    val dFormat = if(isPercent) DecimalFormat("#,##0.00")
    else {
        if(input::javaClass == Int.Companion::class.java)
            DecimalFormat("#,##0")
        else{
            when(input.toFloat()){
                in 1f .. 100f -> DecimalFormat("#,##0.00")
                in 0f .. 1f -> DecimalFormat("0.00##")
                else -> DecimalFormat("#,##0")
            }
        }
    }
    return dFormat.format(input)
}
/**
 * input: 변환할 숫자
 * numberOfDecimalPlace: 소수점 자리수 최대 4
 */
fun decimalFormat(input: Number, numberOfDecimalPlace: Int): String {
    val dFormat = when(numberOfDecimalPlace){
        0 -> DecimalFormat("#,##0")
        1 -> DecimalFormat("#,##0.0")
        2 -> DecimalFormat("#,##0.00")
        3 -> DecimalFormat("#,##0.000")
        4 -> DecimalFormat("#,##0.0000")
        else -> DecimalFormat("#,##0")
    }
    return dFormat.format(input)
}

//초성 검색 ------------
object SoundSearcher {
    private const val HANGUL_BEGIN_UNICODE = 44032 // 가
        .toChar()
    private const val HANGUL_LAST_UNICODE = 55203 // 힣
        .toChar()
    private const val HANGUL_BASE_UNIT = 588 //각자음 마다 가지는 글자수
        .toChar()

    //자음
    private val INITIAL_SOUND = charArrayOf(
        'ㄱ',
        'ㄲ',
        'ㄴ',
        'ㄷ',
        'ㄸ',
        'ㄹ',
        'ㅁ',
        'ㅂ',
        'ㅃ',
        'ㅅ',
        'ㅆ',
        'ㅇ',
        'ㅈ',
        'ㅉ',
        'ㅊ',
        'ㅋ',
        'ㅌ',
        'ㅍ',
        'ㅎ'
    )

    /**
     * 해당 문자가 INITIAL_SOUND인지 검사.
     * @param searchar
     * @return
     */
    private fun isInitialSound(searchar: Char): Boolean {
        for (c in INITIAL_SOUND) {
            if (c == searchar) {
                return true
            }
        }
        return false
    }

    /**
     * 해당 문자의 자음을 얻는다.
     *
     * @param c 검사할 문자
     * @return
     */
    private fun getInitialSound(c: Char): Char {
        val hanBegin = c - HANGUL_BEGIN_UNICODE
        val index = hanBegin / HANGUL_BASE_UNIT.code
        return INITIAL_SOUND[index]
    }

    /**
     * 해당 문자가 한글인지 검사
     * @param c 문자 하나
     * @return
     */
    private fun isHangul(c: Char): Boolean {
        return c in HANGUL_BEGIN_UNICODE..HANGUL_LAST_UNICODE
    }

    /** * 검색을 한다. 초성 검색 완벽 지원함.
     * @param value : 검색 대상 ex> 초성검색합니다
     * @param search : 검색어 ex> ㅅ검ㅅ합ㄴ
     * @return 매칭 되는거 찾으면 true 못찾으면 false.
     */
    fun matchString(value: String, search: String): Boolean {
        var t = 0
        val seof = value.length - search.length
        val slen = search.length
        if (seof < 0) return false //검색어가 더 길면 false를 리턴한다.
        for (i in 0..seof) {
            t = 0
            while (t < slen) {
                if (isInitialSound(search[t]) && isHangul(value[i + t])) {
                    //만약 현재 char이 초성이고 value가 한글이면
                    if (getInitialSound(value[i + t]) == search[t]) //각각의 초성끼리 같은지 비교한다
                        t++ else break
                } else if(!isHangul(value[i + t])) {
                        if (value[i + t].lowercaseChar() == search[t].lowercaseChar()) //그냥 같은지 비교한다.
                            t++ else break
                } else {
                    //char이 초성이 아니라면
                    if (value[i + t] == search[t]) //그냥 같은지 비교한다.
                        t++ else break
                }
            }
            if (t == slen) return true //모두 일치한 결과를 찾으면 true를 리턴한다.
        }
        return false //일치하는 것을 찾지 못했으면 false를 리턴한다.
    }
}
//초성 검색 끝 ------------
fun getUnits(): ArrayList<String>{
    val unitArr = ArrayList<String>()
    unitArr.add("1분")
    unitArr.add("3분")
    unitArr.add("5분")
    unitArr.add("10분")
    unitArr.add("15분")
    unitArr.add("30분")
    unitArr.add("60분")
    unitArr.add("240분")
    unitArr.add("일")
    unitArr.add("주")
    unitArr.add("월")
    return unitArr
}

fun unitMapping(unitStr: String?): String{
    return when(unitStr){
        "1분" -> "1"
        "3분" -> "3"
        "5분" -> "5"
        "10분" -> "10"
        "15분" -> "15"
        "30분" -> "30"
        "60분" -> "60"
        "240분" -> "240"
        "일" -> "days"
        "주" -> "weeks"
        "월" -> "months"
        else -> "30"
    }
}

fun calculateRSI(minuteCandles: MutableList<Candles>): Double {
    var averageUp = 0.0
    var averageDown = 0.0
    val upList = mutableListOf<Double>()
    val downList = mutableListOf<Double>()
    var diff: Double
    for(i in 1 until minuteCandles.size){
        diff = minuteCandles[i-1].trade_price - minuteCandles[i].trade_price
        if(i > minuteCandles.size - RSI_DAY_CNT){
            when(diff >= 0.0){
                true -> averageUp += diff
                else -> averageDown += diff
            }
        } else {
            when(diff >= 0.0){
                true -> {
                    upList.add(diff)
                    downList.add(0.0)
                }
                else -> {
                    upList.add(0.0)
                    downList.add(abs(diff))
                }
            }
        }
    }
    averageDown = abs(averageDown)
    averageUp /= RSI_DAY_CNT
    averageDown /= RSI_DAY_CNT
    val resultAU = exponentialMovingAverage(averageUp, upList, upList.size - 1)
    val resultAD = exponentialMovingAverage(averageDown, downList, downList.size - 1)

    return (resultAU / resultAD) / (1 + (resultAU / resultAD)) * 100;
}
//지수평균이동
tailrec fun exponentialMovingAverage(average: Double, lst:MutableList<Double>, listCount: Int): Double{
    return if(listCount < 0) average
    else exponentialMovingAverage(
        (average * (RSI_DAY_CNT - 1) + lst[listCount]) / RSI_DAY_CNT
        , lst
        , listCount - 1
    )
}

fun stochasticFastSlow(minuteCandles: MutableList<Candles>): MutableList<Double>{
    var currentPrice = 0.0
    var maxPrice = 0.0
    var minPrice = 0.0
    val fastkList = mutableListOf<Double>()
    val fastdList = mutableListOf<Double>()
    val slowkList = mutableListOf<Double>()
    val slowdList = mutableListOf<Double>()
    val slowList = mutableListOf<Double>()
    for(i in 0 until minuteCandles.size){
        if(minuteCandles.size - i < STOCHASTIC_N) break

        currentPrice = 0.0
        maxPrice = 0.0
        minPrice = 0.0

        for(j in 0 until STOCHASTIC_N){
            //종가가 아닌 최고가 최저가 사용해야함
            if(currentPrice == 0.0)
                currentPrice = minuteCandles[i + j].trade_price;
            if(maxPrice == 0.0)
                maxPrice = minuteCandles[i + j].high_price;
            if(minPrice == 0.0)
                minPrice = minuteCandles[i + j].low_price;
            if(maxPrice < minuteCandles[i + j].high_price)
                maxPrice = minuteCandles[i + j].high_price;
            if(minPrice > minuteCandles[i + j].low_price)
                minPrice = minuteCandles[i + j].low_price;
        }
        fastkList.add((currentPrice - minPrice) / (maxPrice - minPrice) * 100)
    }
    //슬로캐스틱 fast d = 슬로캐스틱 slow k
    slowList.add(movingAverage(fastkList[fastkList.size - 1], fastkList, fastkList.size - 1, fastdList))
    fastdList.reverse()
    slowList.add(movingAverage(fastdList[fastdList.size - 1], fastdList, fastdList.size - 1))
    //slowkList.reverse()
    return slowList
}

//이동평균
tailrec fun movingAverage(average: Double, lst: MutableList<Double>, listCount: Int, resultList: MutableList<Double>): Double{
    var ma = 0.0

    return if(listCount < STOCHASTIC_M - 1) average
    else {
        for(i in 0 until STOCHASTIC_M){
            ma += lst[listCount - i]
        }
        resultList.add(ma / STOCHASTIC_M)
        movingAverage(
            ma / STOCHASTIC_M, lst, listCount - 1, resultList
        )
    }
}

tailrec fun movingAverage(average: Double, lst: MutableList<Double>, listCount: Int): Double{
    var ma = 0.0

    return if(listCount < STOCHASTIC_M - 1) average
    else {
        for(i in 0 until STOCHASTIC_M){
            ma += lst[listCount - i]
        }
        movingAverage(
            ma / STOCHASTIC_M, lst, listCount - 1
        )
    }
}

fun numberFormat(input: String): String{
    return input.replace(",", "")
}

@RequiresApi(Build.VERSION_CODES.N)
suspend fun getCandles(marketItem: String, unitItem: String): MutableList<Candles>{
    val rest = RetrofitOkHttpManagerUpbit().restService

    val resultList = withContext(Dispatchers.IO){
        if (unitItem == "일" || unitItem == "주" || unitItem == "월")
            rest.requestCandles(unitMapping(unitItem), marketItem, 2).body()!!
        else
            rest.requestMinuteCandles(
                unitMapping(unitItem).toInt(),
                marketItem,
                2
            ).body()!!
    }
    return resultList
}

suspend fun responseSyncUpbitAPI(call: Call<ResponseBody>): String {
    val resultStr = withContext(Dispatchers.IO){
        val response = call.execute()
        if(response.isSuccessful)
            response.body()!!.string()
        else
            response.errorBody()!!.string()
    }
    return resultStr
}
