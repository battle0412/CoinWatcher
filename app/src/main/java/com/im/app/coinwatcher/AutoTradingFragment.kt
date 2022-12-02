package com.im.app.coinwatcher

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.im.app.coinwatcher.JWT.GeneratorJWT
import com.im.app.coinwatcher.common.getGsonList
import com.im.app.coinwatcher.common.responseUpbitAPI
import com.im.app.coinwatcher.databinding.FragmentAutoTradingBinding
import com.im.app.coinwatcher.json_data.MarketAll
import com.im.app.coinwatcher.okhttp_retrofit.RetrofitOkHttpManagerUpbit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.im.app.coinwatcher.common.getUnits

class AutoTradingFragment: Fragment() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentAutoTradingBinding.inflate(inflater, container, false)

        if(savedInstanceState == null){
            with(childFragmentManager.beginTransaction()){
                add(R.id.autoTradingTabContent, AutoTradingTabFragment.getInstance())
                    .commit()
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val rest = RetrofitOkHttpManagerUpbit(GeneratorJWT.generateJWT()).restService
            val responseStr = responseUpbitAPI(rest.requestMarketAll())
            val kwrArray = ArrayList<String>()
            getGsonList(responseStr, MarketAll::class.java)
                .filter { marketAll -> marketAll.market.contains("KRW") }//마켓 목록중 원화만 취급
                .forEach {
                    kwrArray.add(it.market)
                }
            val adapter = ArrayAdapter(
                requireContext(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                kwrArray
            )
            val adapter2 = ArrayAdapter(
                requireContext(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                getUnits()
            )
            CoroutineScope(Dispatchers.Main).launch {
                binding.marketItems.adapter = adapter
                binding.unitItems.adapter = adapter2
            }
        }
        return binding.root
    }

    companion object{
        fun getInstance() = AutoTradingFragment()
    }
}