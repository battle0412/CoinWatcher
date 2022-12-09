package com.im.app.coinwatcher

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
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
                CoroutineScope(Dispatchers.IO).launch {
                    setTradingIndicator()
                }
            }
            unitItems.setOnItemClickListener { _, _, _, _ ->
                val unitText = unitItems.text.toString()
                sharedPreferences.edit().putString("unit",unitText ).apply()
                CoroutineScope(Dispatchers.IO).launch {
                    setTradingIndicator()
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
                withContext(Dispatchers.Main){
                    marketItems.setText(sharedPreferences.getString("market", "KRW-BTC"))
                    unitItems.setText(sharedPreferences.getString("unit", "1분"))
                    marketItems.setAdapter(adapter)
                    unitItems.setAdapter(adapter2)
                }
                setTradingIndicator()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private suspend fun setTradingIndicator(){
        with(binding){
            val marketItem = marketItems.text.toString()
            val unitItem = unitItems.text.toString()
            if(marketItem.isNotEmpty()
                && unitItem.isNotEmpty()){

                val candles = getCandles(marketItem, unitItem)
                val stochasticList = stochasticFastSlow(candles)
                val rsi = calculateRSI(candles)

                val rest = RetrofitOkHttpManagerUpbit(generateJWT()).restService
                val responseStr = responseSyncUpbitAPI(rest.requestAccounts())
                val accounts = getGsonList(responseStr, Accounts::class.java)
                CoroutineScope(Dispatchers.Main).launch {
                    //보여줄떄는 주석
                    priceTV.setText(decimalFormat(candles[0].trade_price, 0))
                    rsiTV.setText(decimalFormat(rsi, 2))
                    stochasticSlowKTV.setText(decimalFormat(stochasticList[0]))
                    stochasticSlowDTV.setText(decimalFormat(stochasticList[1]))

                    accounts.forEach {
                        when (it.currency) {
                            "KRW" -> {
                                myAssetTV.setText(decimalFormat(it.balance.toFloat()))
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

                    accounts.forEach {
                        when (it.currency) {
                            "KRW" -> {
                                /*myAssetTV.setText("1,000.0")
                                myLockedTV.setText("10,000")*/
                                myAssetTV.setText(decimalFormat(it.balance.toFloat(), 0))
                                myAssetTV.setText(decimalFormat(it.locked.toFloat(), 0))
                            }
                            marketItem.split("-")[1] ->
                                volumeTV.setText(decimalFormat(it.balance.toFloat(), false))
                        }
                    }
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
        val conditionCheck = (buyPrice.isNotEmpty() && buyPrice.toFloat() < 5000F)
                || (volume.isNotEmpty() && volume.toFloat() > 0F)
                || (buyPrice.isEmpty() && volume.isEmpty())
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

    companion object{
        fun newInstance() = AutoTradingFragment()
        fun newInstance(intent: Intent): AutoTradingFragment {
            val fragment = AutoTradingFragment()
            fragment.arguments = intent.extras
            return fragment
        }
    }
}