package com.im.app.coinwatcher

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.im.app.coinwatcher.JWT.GeneratorJWT.JWTGenerator.generateJWT
import com.im.app.coinwatcher.common.getGsonList
import com.im.app.coinwatcher.common.responseUpbitAPI
import com.im.app.coinwatcher.databinding.FragmentCoinListBinding
import com.im.app.coinwatcher.json_data.MarketAll
import com.im.app.coinwatcher.json_data.MarketTicker
import com.im.app.coinwatcher.okhttp_retrofit.RetrofitOkHttpManagerUpbit
import kotlinx.coroutines.*


class CoinListFragment: Fragment() {

    private lateinit var binding: FragmentCoinListBinding
    private var flag = true
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCoinListBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CoroutineScope(Dispatchers.IO).launch {
            while(flag){
                requestCoinList()
                delay(5000L)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.N)
    private suspend fun requestCoinList(){
        val manager = LinearLayoutManager(activity as Activity, LinearLayoutManager.VERTICAL, false)
        val deco = MarketItemDecoration(requireContext(), 5, 8, 5, 8)
        var kwrMarketStr = ""
        val rest = RetrofitOkHttpManagerUpbit(generateJWT()).restService
        val responseBody = responseUpbitAPI(rest.requestMarketAll())
        val kwrMarketList = getGsonList(responseBody, MarketAll::class.java)
            .filter { marketAll -> marketAll.market.contains("KRW") } as MutableList<MarketAll> //마켓 목록중 원화만 취급
        kwrMarketList.forEachIndexed { index, marketAll ->
            if(index > 0) kwrMarketStr += ","
            kwrMarketStr += marketAll.market
        }
        val rest2 = RetrofitOkHttpManagerUpbit(generateJWT()).restService
        val responseBody2 = responseUpbitAPI(rest2.requestMarketsTicker(kwrMarketStr))
        val marketList = getGsonList(responseBody2, MarketTicker::class.java)
            .sortedByDescending { MarketTicker -> MarketTicker.acc_trade_price_24h } as MutableList<MarketTicker>
        CoroutineScope(Dispatchers.Main).launch {
            with(binding.marketRV){
                if(adapter == null){
                    addItemDecoration(deco)
                    layoutManager = manager
                    adapter = CoinListFragmentAdapter(marketList, activity as Activity, kwrMarketList)
                }
                else{
                    adapter!!.notifyDataSetChanged()
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