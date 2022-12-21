package com.im.app.coinwatcher

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.im.app.coinwatcher.adapter.CoinListFragmentAdapter
import com.im.app.coinwatcher.adapter.MarketItemDecoration
import com.im.app.coinwatcher.common.*
import com.im.app.coinwatcher.databinding.FragmentCoinListBinding
import com.im.app.coinwatcher.json_data.MarketAll
import com.im.app.coinwatcher.json_data.MarketTicker
import com.im.app.coinwatcher.model.factory.UpbitViewModel
import com.im.app.coinwatcher.model.factory.UpbitViewModelFactory
import com.im.app.coinwatcher.okhttp_retrofit.RetrofitOkHttpManagerUpbit
import com.im.app.coinwatcher.repository.UpbitRepository
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.HashMap


class CoinListFragment: Fragment() {
    private lateinit var binding: FragmentCoinListBinding
    private var marketList = mutableListOf<MarketTicker>()
    private var kwrMap = HashMap<String, MarketAll>()
    private var filteredKwrMap = HashMap<String, MarketAll>()
    private var flag = true
    private lateinit var viewModel: UpbitViewModel
    private var marketType = "KRW"

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCoinListBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(
            this, UpbitViewModelFactory(
                UpbitRepository(RetrofitOkHttpManagerUpbit().restService)
            )
        )[UpbitViewModel::class.java]
        with(viewModel){
            marketTicker.observe(viewLifecycleOwner){
                this@CoinListFragment.marketList = it
                recyclerViewUpdate()
            }
            marketList.observe(viewLifecycleOwner){
                it.filter { marketAll -> marketAll.market.contains("KRW") }//마켓 목록중 원화만 취급
                    .forEach{ marketAll ->
                        kwrMap[marketAll.market] = marketAll
                    }
            }
            errorMessage.observe(viewLifecycleOwner){
                CoroutineScope(Dispatchers.Main).launch {
                    toastMessage(it.error.message)
                }
            }
        }
        with(binding){
            this.kwrList.setOnClickListener {
                if(marketType == "KRW")
                    return@setOnClickListener
                this.kwrList.typeface = Typeface.DEFAULT_BOLD
                this.watchList.typeface = Typeface.DEFAULT
                marketType = "KRW"

                recyclerViewUpdate()
            }
            this.watchList.setOnClickListener {
                if(marketType == "관심")
                    return@setOnClickListener
                this.watchList.typeface = Typeface.DEFAULT_BOLD
                this.kwrList.typeface = Typeface.DEFAULT
                marketType = "관심"

                recyclerViewUpdate()
            }
        }
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CoroutineScope(Dispatchers.IO).launch {
            initMarketList()
            delay(1000L)
            while(flag){
                if(binding.searchCoin.text!!.isNotEmpty())
                    requestMarketTicker(filteredKwrMap)
                else
                    requestMarketTicker(kwrMap)
                delay(5000L)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        with(binding.searchCoin){
            this.doAfterTextChanged {
                filteredKwrMap.clear()
                if(this.text.toString().trim().isNotEmpty()){
                    kwrMap.forEach{
                        if(SoundSearcher.matchString(
                                """${it.value.market.split("-")[1]}${it.value.korean_name}${it.value.english_name}"""
                                , this.text.toString().trim()))
                            filteredKwrMap[it.key] = it.value
                    }
                }
                recyclerViewUpdate()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.N)
    private fun requestMarketTicker(kwrMap: HashMap<String, MarketAll>){
        val tmpList = mutableListOf<String>()
        kwrMap.forEach { (key, _) -> tmpList.add(key) }
        val kwrMarketStr = tmpList.joinToString(
            separator = ","
        )
        if(kwrMarketStr.isNotEmpty())
            viewModel.getMarketTickerFromViewModel(kwrMarketStr)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initMarketList(){
        viewModel.getAllMarketsFromViewModel()
        binding.kwrList.typeface = Typeface.DEFAULT_BOLD
        /*val rest = RetrofitOkHttpManagerUpbit().restService
        val responseStr = responseSyncUpbitAPI(rest.requestMarketAll())
        getGsonList(responseStr, MarketAll::class.java)
            .filter { marketAll -> marketAll.market.contains("KRW") }//마켓 목록중 원화만 취급
            .forEach{
                kwrMap[it.market] = it
            }*/
    }

    fun recyclerViewUpdate(){
        CoroutineScope(Dispatchers.Main).launch {
            with(binding.marketRV){
                val watchList = SharedPreferenceManager.getWatchListPreference(requireContext())
                    .getStringSet("WatchList", mutableSetOf())

                var orderedMarketTicker = if(marketType == "관심"){
                    marketList.filter { watchList!!.contains(it.market) }.sortedByDescending {
                            MarketTicker -> MarketTicker.acc_trade_price_24h
                    }
                } else {
                    marketList.sortedByDescending {
                            MarketTicker -> MarketTicker.acc_trade_price_24h
                    }
                }
                orderedMarketTicker = if(orderedMarketTicker.isNotEmpty())
                        orderedMarketTicker as MutableList<MarketTicker>
                    else
                        mutableListOf()
                /*val orderedMarketTicker = marketList.sortedByDescending {
                        MarketTicker -> MarketTicker.acc_trade_price_24h
                } as MutableList<MarketTicker>*/
                if(adapter == null){
                    val manager = LinearLayoutManager(activity as Activity, LinearLayoutManager.VERTICAL, false)
                    val deco = MarketItemDecoration(requireContext(), 5, 8, 5, 8)
                    addItemDecoration(deco)
                    layoutManager = manager
                    adapter = CoinListFragmentAdapter(orderedMarketTicker, kwrMap, this@CoinListFragment)
                }
                else{
                    val recyclerViewState = layoutManager!!.onSaveInstanceState()
                    adapter = if(binding.searchCoin.text!!.isNotEmpty()){
                        val tmpList = orderedMarketTicker.filter {
                            filteredKwrMap[it.market]?.let { true } ?: false
                            //SoundSearcher.matchString(marketMapping(it.market), binding.searchCoin.text.toString())
                        }.toMutableList()

                        CoinListFragmentAdapter(tmpList, kwrMap, this@CoinListFragment)
                    } else
                        CoinListFragmentAdapter(orderedMarketTicker, kwrMap, this@CoinListFragment)

                    layoutManager!!.onRestoreInstanceState(recyclerViewState)
                }
            }
        }
    }


    companion object{
        fun newInstance() = CoinListFragment()
    }

    override fun onDestroy() {
        super.onDestroy()
        flag = false
    }
}