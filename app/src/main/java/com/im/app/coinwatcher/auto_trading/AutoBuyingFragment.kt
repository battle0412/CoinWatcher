package com.im.app.coinwatcher.auto_trading

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.im.app.coinwatcher.common.*
import com.im.app.coinwatcher.databinding.FragmentAutoBuyingBinding
import com.im.app.coinwatcher.model.factory.UpbitViewModel
import com.im.app.coinwatcher.model.factory.UpbitViewModelFactory
import com.im.app.coinwatcher.okhttp_retrofit.RetrofitOkHttpManagerUpbit
import com.im.app.coinwatcher.repository.UpbitRepository
import kotlinx.android.synthetic.main.fragment_auto_buying.view.*
import kotlinx.android.synthetic.main.fragment_auto_trading.*
import kotlin.math.round

class AutoBuyingFragment: Fragment() {
    private lateinit var binding: FragmentAutoBuyingBinding
    private var bundle = Bundle()
    private var myAsset = "0"
    @SuppressLint("CommitPrefEdits", "SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAutoBuyingBinding.inflate(inflater, container, false)

        if(savedInstanceState != null){
            with(binding){
                rsiLess.setText(bundle.getString("rsiLess"))
                stochasticSlowKLess.setText(bundle.getString("buySlowK"))
                stochasticSlowDLess.setText(bundle.getString("buySlowD"))
                priceLess.setText(bundle.getString("priceLess"))
                buyPrice.setText(bundle.getString("buyPrice"))
            }
        }

        val viewModel = ViewModelProvider(
            this, UpbitViewModelFactory(
                UpbitRepository(RetrofitOkHttpManagerUpbit().restService)
            )
        )[UpbitViewModel::class.java]
        viewModel.accounts.observe(viewLifecycleOwner){
            it.forEach { accounts -> if(accounts.currency == "KRW") myAsset += accounts.balance.toFloat() }
        }
        viewModel.getAccountsFromViewModel()
        val sharedPreferences = SharedPreferenceManager.getAutoTradingPreference(requireContext())
        with(binding){
            rsiLess.setText(sharedPreferences.getString("rsiLess", ""))
            stochasticSlowKLess.setText(sharedPreferences.getString("buySlowK", ""))
            stochasticSlowDLess.setText(sharedPreferences.getString("buySlowD", ""))
            priceLess.setText(sharedPreferences.getString("priceLess", ""))
            buyPrice.setText(sharedPreferences.getString("buyPrice", ""))
            buyPricePer.adapter = ArrayAdapter(requireContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, getBuyPricePerList())
            rsiLess.doAfterTextChanged {
                sharedPreferences.edit().putString("rsiLess", rsiLess.text.toString()).apply()
            }
            stochasticSlowKLess.doAfterTextChanged {
                sharedPreferences.edit().putString("buySlowK", stochasticSlowKLess.text.toString()).apply()
            }
            stochasticSlowDLess.doAfterTextChanged {
                sharedPreferences.edit().putString("buySlowD", stochasticSlowDLess.text.toString()).apply()
            }
            priceLess.doAfterTextChanged {
                sharedPreferences.edit().putString("priceLess", priceLess.text.toString()).apply()
            }
            buyPrice.doAfterTextChanged {
                sharedPreferences.edit().putString("buyPrice", buyPrice.text.toString()).apply()
            }
            buyPricePer.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    with(buyPricePer.selectedItem.toString()) {
                        if (this != "선택") {
                            val percent = when (this) {
                                "100%" -> 0.9994F
                                else -> decimalFormat(
                                    this.replace("%", "").toFloat() / 100,
                                    3
                                ).toFloat()
                            }
                            val myAsset = myAsset.replace(",", "").ifEmpty { "0" }.toFloat()
                            buyPrice.setText((myAsset * percent).toInt().toString())
                        }
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
            addCalculateClickEvent(rsiLessPlus ,rsiLess, "+")
            addCalculateClickEvent(rsiLessMinus, rsiLess, "-")
            addCalculateClickEvent(stochasticSlowKLessPlus, stochasticSlowKLess, "+")
            addCalculateClickEvent(stochasticSlowKLessMinus, stochasticSlowKLess, "-")
            addCalculateClickEvent(stochasticSlowDLessPlus, stochasticSlowDLess, "+")
            addCalculateClickEvent(stochasticSlowDLessMinus, stochasticSlowDLess, "-")
        }
        return binding.root
    }

    private fun addCalculateClickEvent(eventTextView: TextView, editText: EditText, calcType: String){
        eventTextView.setOnClickListener {
            var curValue =
                if(editText.text.toString().trim().isEmpty())
                    0F
                else
                    editText.text.toString().toFloat()
            when(calcType){
                "+" -> if(curValue + 1 < 100F) curValue += 1 else curValue = 100F
                "-" -> if(curValue - 1 > 0F) curValue -= 1 else curValue = 0F
            }
            editText.setText((round(curValue * 100) / 100).toString())
        }
    }

    private fun getBuyPricePerList(): ArrayList<String> {
        val array = arrayListOf<String>()
        array.add("선택")
        array.add("100%")
        array.add("70%")
        array.add("50%")
        array.add("25%")
        array.add("10%")
        return array
    }

    companion object{
        fun newInstance() = AutoBuyingFragment()
    }
}