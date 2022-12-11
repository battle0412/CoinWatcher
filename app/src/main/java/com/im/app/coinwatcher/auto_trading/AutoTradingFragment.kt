package com.im.app.coinwatcher.auto_trading

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.im.app.coinwatcher.R
import com.im.app.coinwatcher.common.*
import com.im.app.coinwatcher.databinding.FragmentAutoTradingBinding
import com.im.app.coinwatcher.json_data.Accounts
import com.im.app.coinwatcher.json_data.Candles
import com.im.app.coinwatcher.json_data.MarketAll
import com.im.app.coinwatcher.model.factory.UpbitViewModel
import com.im.app.coinwatcher.model.factory.UpbitViewModelFactory
import com.im.app.coinwatcher.okhttp_retrofit.RetrofitOkHttpManagerUpbit
import com.im.app.coinwatcher.repository.UpbitRepository
import kotlinx.android.synthetic.main.fragment_auto_buying.*
import kotlinx.android.synthetic.main.fragment_auto_trading.*
import kotlinx.android.synthetic.main.fragment_my_assets.*
import kotlinx.coroutines.*

class AutoTradingFragment: Fragment() {
    private lateinit var binding: FragmentAutoTradingBinding
    private lateinit var viewModel: UpbitViewModel
    private var flag = true
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAutoTradingBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(
            this, UpbitViewModelFactory(
                UpbitRepository(RetrofitOkHttpManagerUpbit().restService)
            )
        )[UpbitViewModel::class.java]
        viewModel.accounts.observe(viewLifecycleOwner){
            setMyAsset(it)
        }
        viewModel.candles.observe(viewLifecycleOwner){
            setTradingIndicator(it)
            viewModel.getAccountsFromViewModel()
        }
        initSpinner()
        if(savedInstanceState == null){
            with(childFragmentManager.beginTransaction()){
                add(R.id.autoTradingTabContent, AutoTradingTabFragment.newInstance())
                    .commit()
            }
        }
        with(binding){
            autoTradingSwitching.isChecked = arguments?.getBoolean("autoTrading") ?: false
            setTextEditEnabled(!autoTradingSwitching.isChecked)
            val sharedPreferences = SharedPreferenceManager.getAutoTradingPreference(requireContext())
            autoTradingSwitching.setOnCheckedChangeListener { _, isChecked ->
                if(isChecked){
                    if(!checkCondition()) {
                        CoroutineScope(Dispatchers.Main).launch {
                            autoTradingSwitching.isChecked = false
                            toastMessage("주문금액을 확인하세요")
                        }
                        return@setOnCheckedChangeListener
                    } else {

                        requireActivity().startService(setPreferenceToIntent())
                    }
                }
                else {
                    val intent = Intent(requireContext(), AutoTradingService::class.java)
                    requireActivity().stopService(intent)
                    setTextEditEnabled(true)
                }
            }
            marketItems.setOnItemClickListener { _, _, _, _ ->
                val unitText = marketItems.text.toString()
                sharedPreferences.edit().putString("market",unitText ).apply()
                requestViewModel()
            }
            unitItems.setOnItemClickListener { _, _, _, _ ->
                val unitText = unitItems.text.toString()
                sharedPreferences.edit().putString("unit",unitText ).apply()
                requestViewModel()
            }
        }
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CoroutineScope(Dispatchers.IO).launch{
            while (flag){
                delay(5000L)
                requestViewModel()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun setTradingIndicator(candles: MutableList<Candles>){
        with(binding){
            val marketItem = marketItems.text.toString()
            val unitItem = unitItems.text.toString()
            if(marketItem.isNotEmpty()
                && unitItem.isNotEmpty()){

                val stochasticList = stochasticFastSlow(candles)
                val rsi = calculateRSI(candles)
                CoroutineScope(Dispatchers.Main).launch {
                    priceTV.setText(decimalFormat(candles[0].trade_price, 0))
                    rsiTV.setText(decimalFormat(rsi, 2))
                    stochasticSlowKTV.setText(decimalFormat(stochasticList[0]))
                    stochasticSlowDTV.setText(decimalFormat(stochasticList[1]))
                }
            }
        }
    }
    private fun setMyAsset(accounts: MutableList<Accounts>){
        with(binding){
            val marketItem = marketItems.text.toString()
            val unitItem = unitItems.text.toString()
            if(marketItem.isNotEmpty()
                && unitItem.isNotEmpty()){
                CoroutineScope(Dispatchers.Main).launch {
                    myAssetTV.setText("0")
                    myLockedTV.setText("0")
                    volumeTV.setText("0")
                    accounts.forEach {
                        when (it.currency) {
                            "KRW" -> {
                                //myAssetTV.setText(decimalFormat(it.balance.toFloat()))
                                myAssetTV.setText("0")
                                myLockedTV.setText(decimalFormat(it.locked.toFloat()))
                            }
                            marketItem.split("-")[1] ->
                                volumeTV.setText(decimalFormat(it.balance.toFloat(), false))
                        }
                    }
                    /*priceTV.setText("123,456")
                    rsiTV.setText(decimalFormat(rsi, 2))
                    stochasticSlowKTV.setText(decimalFormat(stochasticList[0]))
                    stochasticSlowDTV.setText(decimalFormat(stochasticList[1]))*/

                    /*accounts.forEach {
                        when (it.currency) {
                            "KRW" -> {
                                *//*myAssetTV.setText("1,000.0")
                                myLockedTV.setText("10,000")*//*
                                myAssetTV.setText(decimalFormat(it.balance.toFloat(), 0))
                                myLockedTV.setText(decimalFormat(it.locked.toFloat(), 0))
                            }
                            marketItem.split("-")[1] ->
                                volumeTV.setText(decimalFormat(it.balance.toFloat(), false))
                        }
                    }*/
                }
            }
        }
    }

    private fun setTextEditEnabled(enabled: Boolean){
        with(binding){
            marketItems.isEnabled = enabled
            unitItems.isEnabled = enabled
        }
    }

    private fun checkCondition(): Boolean{
        val sharedPreferences = SharedPreferenceManager.getAutoTradingPreference(requireContext())
        val buyPrice = sharedPreferences.getString("buyPrice", "") ?: ""
        val volume = sharedPreferences.getString("volume", "") ?: ""
        val conditionCheck = (buyPrice.isNotEmpty() &&
                    (buyPrice.toFloat() < 5000F
                    || buyPrice.toFloat() > my_assetTV.text.toString().toFloat())
                ) //매수금액이 비어있지 않고 5천보다 작거나 보유자산보다 크면 true
                || (volume.isNotEmpty()
                    && (volume.toFloat() > volumeTV.text.toString().toFloat())
                ) //매도량이 비어있지 않고 보유량보다 크면 true
                || (buyPrice.isEmpty() && volume.isEmpty()) //매매 금액이 비어있다면 true
        if(conditionCheck){
            return false
        }
        return true
    }


    private fun setPreferenceToIntent(): Intent {
        val sharedPreferences = SharedPreferenceManager.getAutoTradingPreference(requireContext())
        val intent = Intent(requireContext(), AutoTradingService::class.java)
        with(binding){
            intent.putExtra("autoTrading", true)
            intent.putExtra("market", marketItems.text.toString())
            intent.putExtra("unit", unitItems.text.toString())
            sharedPreferences.edit().putString("market", marketItems.text.toString()).apply()
            sharedPreferences.edit().putString("unit", unitItems.text.toString()).apply()

            intent.putExtra("rsiLess" , sharedPreferences.getString("rsiLess", "") ?: "")
            intent.putExtra("buySlowK" , sharedPreferences.getString("buySlowK", "") ?: "")
            intent.putExtra("buySlowD" , sharedPreferences.getString("buySlowD", "") ?: "")
            intent.putExtra("priceLess" , sharedPreferences.getString("priceLess", "") ?: "")
            intent.putExtra("buyPrice" , sharedPreferences.getString("buyPrice", "") ?: "")

            intent.putExtra("rsiMore" , sharedPreferences.getString("rsiMore", "") ?: "")
            intent.putExtra("sellSlowK" , sharedPreferences.getString("sellSlowK", "") ?: "")
            intent.putExtra("sellSlowD" , sharedPreferences.getString("sellSlowD", "") ?: "")
            intent.putExtra("priceMore" , sharedPreferences.getString("priceMore", "") ?: "")
            intent.putExtra("volume" , sharedPreferences.getString("volume", "") ?: "")
        }
        return intent
    }

    private fun requestViewModel(){
        with(binding){
            viewModel.getCandlesFromViewModel(unitItems.text.toString(), marketItems.text.toString(), 200)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initSpinner(){
        CoroutineScope(Dispatchers.IO).launch {
            with(binding){
                val rest = RetrofitOkHttpManagerUpbit().restService
                val responseStr = responseSyncUpbitAPI(rest.requestMarketAll())
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
                val sharedPreferences = SharedPreferenceManager.getAutoTradingPreference(requireContext())
                withContext(Dispatchers.Main) {
                    marketItems.setText(sharedPreferences.getString("market", "KRW-BTC"))
                    unitItems.setText(sharedPreferences.getString("unit", "1분"))
                    marketItems.setAdapter(adapter)
                    unitItems.setAdapter(adapter2)
                }
                requestViewModel()
            }
        }
    }

    companion object{
        fun newInstance() = AutoTradingFragment()
        fun newInstance(intent: Intent): AutoTradingFragment {
            val fragment = AutoTradingFragment()
            fragment.arguments = intent.extras
            return fragment
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        flag = false
    }
}