package com.im.app.coinwatcher

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnKeyListener
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.im.app.coinwatcher.JWT.GeneratorJWT.JWTGenerator.generateJWT
import com.im.app.coinwatcher.common.SoundSearcher
import com.im.app.coinwatcher.common.getGsonList
import com.im.app.coinwatcher.common.responseUpbitAPI
import com.im.app.coinwatcher.databinding.FragmentCoinListBinding
import com.im.app.coinwatcher.json_data.MarketAll
import com.im.app.coinwatcher.json_data.MarketTicker
import com.im.app.coinwatcher.okhttp_retrofit.RetrofitOkHttpManagerUpbit
import kotlinx.coroutines.*
import retrofit2.await
import java.util.*
import kotlin.collections.HashMap


class CoinListFragment: Fragment() {

    private lateinit var binding: FragmentCoinListBinding
    private lateinit var kwrMarketList: MutableList<MarketAll>
    private lateinit var marketList: MutableList<MarketTicker>
    private var kwrMap = HashMap<String, MarketAll>()
    private var filteredKwrMap = HashMap<String, MarketAll>()
    private var flag = true

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCoinListBinding.inflate(inflater, container, false)
        with(binding.searchCoin){
            this.doAfterTextChanged {
                if(this.text.toString().trim().isNotEmpty()){
                    kwrMap.forEach{
                        if(SoundSearcher.matchString("""${it.value.korean_name}${it.value.english_name}""", this.text.toString().trim()))
                            filteredKwrMap[it.key] = it.value
                        /*else if(SoundSearcher.matchString(it.value.english_name, binding.searchCoin.text.toString())){
                            filteredKwrMap[it.key] = it.value
                        }*/
                    }
                    recyclerViewUpdate()
                }
            }
        }

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CoroutineScope(Dispatchers.IO).launch {
            initMarketList()
            while(flag){
                requestCoinList()
                delay(5000L)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.N)
    private suspend fun requestCoinList(){
        val tmpList = mutableListOf<String>()
        kwrMap.forEach { (key, _) -> tmpList.add(key) }
        val kwrMarketStr = tmpList.joinToString(
            separator = ","
        )
        val rest = RetrofitOkHttpManagerUpbit(generateJWT()).restService
        val responseBody = responseUpbitAPI(rest.requestMarketsTicker(kwrMarketStr))
        marketList = getGsonList(responseBody, MarketTicker::class.java)
            .sortedByDescending { MarketTicker -> MarketTicker.acc_trade_price_24h } as MutableList<MarketTicker>
        recyclerViewUpdate()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private suspend fun initMarketList(){
        val rest = RetrofitOkHttpManagerUpbit(generateJWT()).restService
        val responseStr = responseUpbitAPI(rest.requestMarketAll())
        getGsonList(responseStr, MarketAll::class.java)
            .filter { marketAll -> marketAll.market.contains("KRW") }//마켓 목록중 원화만 취급
            .forEach{
                kwrMap[it.market] = it
            }
    }

    private fun marketMapping(market: String): String{
        return filteredKwrMap[market]?.let {
           """${it.korean_name}${it.english_name}"""
        } ?: ""
    }

    private fun recyclerViewUpdate(){
        CoroutineScope(Dispatchers.Main).launch {
            with(binding.marketRV){
                if(adapter == null){
                    val manager = LinearLayoutManager(activity as Activity, LinearLayoutManager.VERTICAL, false)
                    val deco = MarketItemDecoration(requireContext(), 5, 8, 5, 8)
                    addItemDecoration(deco)
                    layoutManager = manager
                    adapter = CoinListFragmentAdapter(marketList, kwrMap)
                }
                else{
                    val recyclerViewState = layoutManager!!.onSaveInstanceState()
                    adapter = if(binding.searchCoin.text!!.isNotEmpty()){
                        val tmpList = marketList.filter {
                            SoundSearcher.matchString(marketMapping(it.market), binding.searchCoin.text.toString())
                        }.toMutableList()

                        CoinListFragmentAdapter(tmpList, kwrMap)
                    } else
                        CoinListFragmentAdapter(marketList, kwrMap)


                    layoutManager!!.onRestoreInstanceState(recyclerViewState)

                }
            }
        }
    }

    companion object{
        fun getInstance() = CoinListFragment()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        flag = false
    }
}