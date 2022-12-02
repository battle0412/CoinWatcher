package com.im.app.coinwatcher

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.android.material.R
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.im.app.coinwatcher.JWT.GeneratorJWT
import com.im.app.coinwatcher.common.*
import com.im.app.coinwatcher.databinding.FragmentAutoBuyingBinding
import com.im.app.coinwatcher.json_data.MarketAll
import com.im.app.coinwatcher.json_data.ReqOrder
import com.im.app.coinwatcher.okhttp_retrofit.RetrofitOkHttpManagerUpbit
import kotlinx.android.synthetic.main.fragment_auto_buying.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class AutoBuyingFragment: Fragment() {
    private lateinit var binding: FragmentAutoBuyingBinding
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAutoBuyingBinding.inflate(inflater, container, false)
        /*
        with(binding){
            autoTradingSwitching.setOnCheckedChangeListener { _, isChecked ->
                if(checkCondition()){
                    toastMessage("자동매수 시작")
                    autoTradingSwitching.isChecked = isChecked
                        *//*
                        서비스 시작
                        val map = mutableMapOf<String, String>()
                        map["market"] = "KRW-BTC"
                        map["side"] = "bid"
                        map["volume"] = ""
                        map["price"] = "5000"
                        map["ord_type"] = "price"
                        map["identifier"] = ""
                        val requestBody = Gson().toJson(map).toString()//(JSONObject(queryString)).toString()
                            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
                        val rest = RetrofitOkHttpManagerUpbit(GeneratorJWT.generateJWT(map)).restService
                        val requestOrders = UpbitAPIService(rest.requestOrders(requestBody))
                            .responseUpbitAPI()*//*
                }
                else {
                    autoTradingSwitching.isChecked = false
                }
            }
        }*/

        return binding.root
    }

    /*private fun checkCondition(): Boolean{
        var isValid: Boolean
        with(binding){
            isValid = marketItems.selectedItem.toString().isNotEmpty()
                    //업비트 최소 주문금액 5천원 이상
                    && (numberFormat(buyPrice.text.toString()).isNotEmpty() 
                        && numberFormat(buyPrice.text.toString()).toFloat() > 5000F)
                    //주문근액이 (가용자산 - 수수료)보다 작아야 함. 업비트 일반 매매 수수료는 0.05% -> 0.9995까지 가능
                    && (numberFormat(myAsset.text.toString()).toFloat() 
                        > numberFormat(buyPrice.text.toString()).toFloat() * 0.99)
        }
        toastMessage("조건을 확인해 주세요")
        return isValid
    }*/

    private fun numberFormat(input: String): String{
       return input.replace(",", "")
    }

    companion object{
        fun getInstance() = AutoBuyingFragment()
    }
}