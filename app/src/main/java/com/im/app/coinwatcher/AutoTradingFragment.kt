package com.im.app.coinwatcher

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.im.app.coinwatcher.JWT.GeneratorJWT.Companion.generateJWT
import com.im.app.coinwatcher.common.*
import com.im.app.coinwatcher.databinding.FragmentAutoTradingBinding
import com.im.app.coinwatcher.json_data.Accounts
import com.im.app.coinwatcher.json_data.MarketAll
import com.im.app.coinwatcher.okhttp_retrofit.RetrofitOkHttpManagerUpbit
import kotlinx.android.synthetic.main.fragment_auto_buying.*
import kotlinx.coroutines.*

class AutoTradingFragment: Fragment() {
    private lateinit var binding: FragmentAutoTradingBinding
    private val coroutineMain = CoroutineScope(Dispatchers.Main)
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAutoTradingBinding.inflate(inflater, container, false)

        if(savedInstanceState == null){
            with(childFragmentManager.beginTransaction()){
                add(R.id.autoTradingTabContent, AutoTradingTabFragment.newInstance())
                    .commit()
            }
        }
        with(binding){
            autoTradingSwitching.isChecked = arguments?.getBoolean("autoTrading") ?: false
            setTextEditEnabled(!autoTradingSwitching.isChecked)

            autoTradingSwitching.setOnCheckedChangeListener { _, isChecked ->
                val intent = Intent(requireContext(), AutoTradingService::class.java)
                if(isChecked){
                    coroutineMain.launch {
                        setTextEditEnabled(false)
                        toastMessage("자동매수 시작")
                    }
                    val sharedPreferences = SharedPreferenceManager.getAutoTradingPreference(requireContext())
                    intent.putExtra("autoTrading", true)
                    intent.putExtra("market", marketItems.selectedItem.toString())
                    intent.putExtra("unit", unitItems.selectedItem.toString())
                    sharedPreferences.edit().putString("market", marketItems.selectedItem.toString()).apply()
                    sharedPreferences.edit().putString("unit", unitItems.selectedItem.toString()).apply()

                    intent.putExtra("rsiLess" , sharedPreferences.getString("rsiLess", ""))
                    intent.putExtra("buySlowK" , sharedPreferences.getString("buySlowK", ""))
                    intent.putExtra("buySlowD" , sharedPreferences.getString("buySlowD", ""))
                    intent.putExtra("priceLess" , sharedPreferences.getString("priceLess", ""))
                    intent.putExtra("buyPrice" , sharedPreferences.getString("buyPrice", ""))

                    intent.putExtra("rsiMore" , sharedPreferences.getString("rsiMore", ""))
                    intent.putExtra("sellSlowK" , sharedPreferences.getString("sellSlowK", ""))
                    intent.putExtra("sellSlowD" , sharedPreferences.getString("sellSlowD", ""))
                    intent.putExtra("priceMore" , sharedPreferences.getString("priceMore", ""))
                    intent.putExtra("volume" , sharedPreferences.getString("volume", ""))
                    requireActivity().startService(intent)

                }
                else {
                    requireActivity().stopService(intent)
                    setTextEditEnabled(true)
                }
            }
        }

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CoroutineScope(Dispatchers.IO).launch {
            with(binding){
                val rest = RetrofitOkHttpManagerUpbit(generateJWT()).restService
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
                val sharedPreferences = SharedPreferenceManager.getAutoTradingPreference(requireContext())
                val marketPosition = adapter.getPosition(sharedPreferences.getString("market", "KRW-BTC"))
                val unitPosition = adapter2.getPosition(sharedPreferences.getString("unit", "1분"))
                withContext(Dispatchers.Main){
                    marketItems.adapter = adapter
                    marketItems.setSelection(marketPosition)
                    unitItems.adapter = adapter2
                    unitItems.setSelection(unitPosition)
                }
                val marketItem = marketItems.selectedItem.toString()
                val unitItem = unitItems.selectedItem.toString()
                if(marketItem.isNotEmpty()
                    && unitItem.isNotEmpty()){

                    val candles = getCandles(marketItem, unitItem)
                    val stochasticList = stochasticFastSlow(candles)
                    val rsi = calculateRSI(candles)

                    val rest3 = RetrofitOkHttpManagerUpbit(generateJWT()).restService
                    val responseStr3 = responseUpbitAPI(rest3.requestAccounts())
                    val accounts = getGsonList(responseStr3, Accounts::class.java)
                    CoroutineScope(Dispatchers.Main).launch {
                        /*
                        보여줄떄는 주석
                        priceTV.setText(decimalFormat(candles[0].trade_price, 0))
                        rsiTV.setText(decimalFormat(calculateRSI(candles), 2))
                        stochasticSlowKTV.setText(decimalFormat(stochasticList[0]))
                        stochasticSlowDTV.setText(decimalFormat(stochasticList[1]))

                        accounts.forEach {
                            when (it.currency) {
                                "KRW" -> {
                                    myAssetTV.setText(decimalFormat(it.balance.toFloat()))
                                    myLockedTV.setText(decimalFormat(it.locked.toFloat()))
                                }
                                marketItem.split("-")[1] ->
                                    quantityTV.setText(decimalFormat(it.balance.toFloat(), false))
                            }
                        }*/
                        priceTV.setText("123,456")
                        rsiTV.setText(decimalFormat(rsi, 2))
                        stochasticSlowKTV.setText(decimalFormat(stochasticList[0]))
                        stochasticSlowDTV.setText(decimalFormat(stochasticList[1]))

                        accounts.forEach {
                            when (it.currency) {
                                "KRW" -> {
                                    myAssetTV.setText("1,000.0")
                                    myLockedTV.setText("10,000")
                                }
                                marketItem.split("-")[1] ->
                                    volumeTV.setText(decimalFormat(it.balance.toFloat(), false))
                            }
                        }
                    }
                }
            }
        }
    }

 /*   override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isChecked", binding.autoTradingSwitching.isChecked)
        outState.putBundle("autoTrading", arguments)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let { switchChecked = it.getBoolean("isChecked") }
    }*/

    private fun setTextEditEnabled(enabled: Boolean){
        with(binding){
            marketItems.isEnabled = enabled
            unitItems.isEnabled = enabled
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
}