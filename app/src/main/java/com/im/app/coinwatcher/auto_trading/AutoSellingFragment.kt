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
import androidx.appcompat.R
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.im.app.coinwatcher.common.SharedPreferenceManager
import com.im.app.coinwatcher.common.SingleClickListener
import com.im.app.coinwatcher.common.decimalFormat
import com.im.app.coinwatcher.databinding.FragmentAutoSellingBinding
import com.im.app.coinwatcher.model.factory.UpbitViewModel
import com.im.app.coinwatcher.model.factory.UpbitViewModelFactory
import com.im.app.coinwatcher.okhttp_retrofit.RetrofitOkHttpManagerUpbit
import com.im.app.coinwatcher.repository.UpbitRepository
import kotlinx.android.synthetic.main.fragment_auto_selling.*
import kotlin.math.round

class AutoSellingFragment: Fragment() {
    private lateinit var binding: FragmentAutoSellingBinding
    private var bundle = Bundle()
    private var myVolume = "0"
    //private var isEnabled = true
    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("CommitPrefEdits")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAutoSellingBinding.inflate(inflater, container, false)

        if(savedInstanceState != null){
            //setTextEditEnabled(isEnabled)
            with(binding){
                rsiMore.setText(bundle.getString("rsiMore"))
                stochasticSlowKMore.setText(bundle.getString("sellSlowK"))
                stochasticSlowDMore.setText(bundle.getString("sellSlowD"))
                priceMore.setText(bundle.getString("priceMore"))
                volume.setText(bundle.getString("volume"))
            }
        }
        val viewModel = ViewModelProvider(
            this, UpbitViewModelFactory(
                UpbitRepository(RetrofitOkHttpManagerUpbit().restService)
            )
        )[UpbitViewModel::class.java]
        val sharedPreferences = SharedPreferenceManager.getAutoTradingPreference(requireContext())
        viewModel.accounts.observe(viewLifecycleOwner){
            val market = sharedPreferences.getString("market", "")
            it.forEach { accounts -> if(accounts.currency == market!!.split("-")[1]) myVolume += accounts.balance.toFloat() }
        }
        viewModel.getAccountsFromViewModel()
        with(binding){
            rsiMore.setText(sharedPreferences.getString("rsiMore", ""))
            stochasticSlowKMore.setText(sharedPreferences.getString("sellSlowK", ""))
            stochasticSlowDMore.setText(sharedPreferences.getString("sellSlowD", ""))
            priceMore.setText(sharedPreferences.getString("priceMore", ""))
            volume.setText(sharedPreferences.getString("volume", ""))
            volumePer.adapter = ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, getBuyPricePerList())

            rsiMore.doAfterTextChanged {
                sharedPreferences.edit().putString("rsiMore", rsiMore.text.toString()).apply()
            }
            stochasticSlowKMore.doAfterTextChanged {
                sharedPreferences.edit().putString("sellSlowK", stochasticSlowKMore.text.toString()).apply()
            }
            stochasticSlowDMore.doAfterTextChanged {
                sharedPreferences.edit().putString("sellSlowD", stochasticSlowDMore.text.toString()).apply()
            }
            priceMore.doAfterTextChanged {
                sharedPreferences.edit().putString("priceMore", priceMore.text.toString()).apply()
            }
            volume.doAfterTextChanged {
                sharedPreferences.edit().putString("volume", volume.text.toString()).apply()
            }
            volumePer.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    with(volumePer.selectedItem.toString()) {
                        if (this != "선택") {
                            val percent = decimalFormat(
                                    this.replace("%", "").toFloat() / 100,
                                    3
                                ).toFloat()
                            val myVolume = myVolume.replace(",", "").ifEmpty { "0" }.toFloat()
                            volume.setText(decimalFormat(myVolume * percent, 8))
                        }
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
            addCalculateClickEvent(rsiMorePlus ,rsiMore, "+")
            addCalculateClickEvent(rsiMoreMinus, rsiMore, "-")
            addCalculateClickEvent(stochasticSlowKMorePlus, stochasticSlowKMore, "+")
            addCalculateClickEvent(stochasticSlowKMoreMinus, stochasticSlowKMore, "-")
            addCalculateClickEvent(stochasticSlowDMorePlus, stochasticSlowDMore, "+")
            addCalculateClickEvent(stochasticSlowDMoreMinus, stochasticSlowDMore, "-")
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
        fun newInstance() = AutoSellingFragment()
    }
}