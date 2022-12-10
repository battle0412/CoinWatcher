package com.im.app.coinwatcher

import android.annotation.SuppressLint
import android.app.Activity
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
import com.im.app.coinwatcher.common.SoundSearcher
import com.im.app.coinwatcher.common.getGsonList
import com.im.app.coinwatcher.common.responseSyncUpbitAPI
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
    private lateinit var marketList: MutableList<MarketTicker>
    private var kwrMap = HashMap<String, MarketAll>()
    private var filteredKwrMap = HashMap<String, MarketAll>()
    private var flag = true
    private lateinit var viewModel: UpbitViewModel

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
                    requestMarketTicker(filteredKwrMap)
                    //recyclerViewUpdate()
                }
            }
        }
        viewModel.marketTicker.observe(viewLifecycleOwner){
            with(binding.marketRV){
                val orderedMarketTicker = it.sortedByDescending {
                        MarketTicker -> MarketTicker.acc_trade_price_24h
                } as MutableList<MarketTicker>

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
                            SoundSearcher.matchString(marketMapping(it.market), binding.searchCoin.text.toString())
                        }.toMutableList()

                        CoinListFragmentAdapter(tmpList, kwrMap, this@CoinListFragment)
                    } else
                        CoinListFragmentAdapter(orderedMarketTicker, kwrMap, this@CoinListFragment)
                    layoutManager!!.onRestoreInstanceState(recyclerViewState)
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
                if(binding.searchCoin.text!!.isNotEmpty())
                    requestMarketTicker(filteredKwrMap)
                else
                    requestMarketTicker(kwrMap)
                delay(5000L)
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
        //val rest = RetrofitOkHttpManagerUpbit(GeneratorJWT.generateJWT()).restService
        viewModel.getMarketTickerFromViewModel(kwrMarketStr)
    /*    marketList = getGsonList(responseBody, MarketTicker::class.java)
            .sortedByDescending { MarketTicker -> MarketTicker.acc_trade_price_24h } as MutableList<MarketTicker>*/
        //recyclerViewUpdate()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private suspend fun initMarketList(){
        val rest = RetrofitOkHttpManagerUpbit().restService
        val responseStr = responseSyncUpbitAPI(rest.requestMarketAll())
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
                    adapter = CoinListFragmentAdapter(marketList, kwrMap, this@CoinListFragment)
                }
                else{
                    val recyclerViewState = layoutManager!!.onSaveInstanceState()
                    adapter = if(binding.searchCoin.text!!.isNotEmpty()){
                        val tmpList = marketList.filter {
                            SoundSearcher.matchString(marketMapping(it.market), binding.searchCoin.text.toString())
                        }.toMutableList()

                        CoinListFragmentAdapter(tmpList, kwrMap, this@CoinListFragment)
                    } else
                        CoinListFragmentAdapter(marketList, kwrMap, this@CoinListFragment)


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