package com.im.app.coinwatcher.auto_trading

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getSystemService
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
    private var kwrArray = arrayListOf<String>()
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
        with(viewModel){
            accounts.observe(viewLifecycleOwner){
                setMyAsset(it)
            }
            candles.observe(viewLifecycleOwner){
                setTradingIndicator(it)
                viewModel.getAccountsFromViewModel()
            }
            marketList.observe(viewLifecycleOwner){
                it.filter { marketAll -> marketAll.market.contains("KRW") }//?????? ????????? ????????? ??????
                    .forEach { kwr ->
                        kwrArray.add(kwr.market)
                    }
            }
            errorMessage.observe(viewLifecycleOwner){
                CoroutineScope(Dispatchers.Main).launch{
                    toastMessage(it.error.message)
                }
            }
        }

        initSpinner()
        if(savedInstanceState == null){
            with(childFragmentManager.beginTransaction()){
                add(R.id.autoTradingTabContent, AutoTradingTabFragment.newInstance())
                    .commit()
            }
        }
        with(binding){
            //??????????????? ????????? ?????? ?????? ??????
            autoTradingSwitching.isChecked =
                arguments?.getBoolean("autoTrading") ?: false
                        || isMyServiceRunning(AutoTradingService::class.java)
            setTextEditEnabled(!autoTradingSwitching.isChecked)
            val sharedPreferences = SharedPreferenceManager.getAutoTradingPreference(requireContext())
            autoTradingSwitching.setOnCheckedChangeListener { _, isChecked ->
                if(isChecked){
                    if(!checkCondition()) {
                        CoroutineScope(Dispatchers.Main).launch {
                            autoTradingSwitching.isChecked = false
                            toastMessage("??????????????? ???????????????")
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
                                myAssetTV.setText(decimalFormat(it.balance.toFloat()))
                                //myAssetTV.setText("10000")
                                myLockedTV.setText(decimalFormat(it.locked.toFloat()))
                            }
                            marketItem.split("-")[1] ->
                                volumeTV.setText(decimalFormat(it.balance.toFloat(), 8))
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
        val conditionCheck = (buyPrice.isNotEmpty() &&
                    (buyPrice.toFloat() < 5000F //????????? ?????? ???????????? 5?????? ????????? 0.05% -> 0.0005
                    || buyPrice.toFloat() > my_assetTV.text.toString().replace(",", "").toFloat() * 0.9994)
                ) //??????????????? ???????????? ?????? 5????????? ????????? ?????????????????? ?????? true
                || (volume.isNotEmpty()
                    && (volume.toFloat() > volumeTV.text.toString().toFloat())
                ) //???????????? ???????????? ?????? ??????????????? ?????? true
                || (buyPrice.isEmpty() && volume.isEmpty()) //?????? ????????? ??????????????? true
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
                /*val rest = RetrofitOkHttpManagerUpbit().restService
                val responseStr = responseSyncUpbitAPI(rest.requestMarketAll())
                val kwrArray = ArrayList<String>()
                getGsonList(responseStr, MarketAll::class.java)
                    .filter { marketAll -> marketAll.market.contains("KRW") }//?????? ????????? ????????? ??????
                    .forEach {
                        kwrArray.add(it.market)
                    }*/
                viewModel.getAllMarketsFromViewModel()
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
                    myAssetTV.setText("0")
                    myLockedTV.setText("0")
                    volumeTV.setText("0")
                    marketItems.setText(sharedPreferences.getString("market", "KRW-BTC"))
                    unitItems.setText(sharedPreferences.getString("unit", "1???"))
                    marketItems.setAdapter(adapter)
                    unitItems.setAdapter(adapter2)
                }
                requestViewModel()
            }
        }
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        manager?.let {
            for (service in it.getRunningServices(Int.MAX_VALUE)) {
                if (serviceClass.name == service.service.className) {
                    return true
                }
            }
        }
        return false
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