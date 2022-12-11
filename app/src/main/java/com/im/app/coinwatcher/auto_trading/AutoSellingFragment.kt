package com.im.app.coinwatcher.auto_trading

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.im.app.coinwatcher.common.SharedPreferenceManager
import com.im.app.coinwatcher.common.SingleClickListener
import com.im.app.coinwatcher.databinding.FragmentAutoSellingBinding
import kotlinx.android.synthetic.main.fragment_auto_selling.*
import kotlin.math.round

class AutoSellingFragment: Fragment() {
    private lateinit var binding: FragmentAutoSellingBinding
    private var bundle = Bundle()
    //private var isEnabled = true
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

        val sharedPreferences = SharedPreferenceManager.getAutoTradingPreference(requireContext())
        with(binding){
            rsiMore.setText(sharedPreferences.getString("rsiMore", ""))
            stochasticSlowKMore.setText(sharedPreferences.getString("sellSlowK", ""))
            stochasticSlowDMore.setText(sharedPreferences.getString("sellSlowD", ""))
            priceMore.setText(sharedPreferences.getString("priceMore", ""))
            volume.setText(sharedPreferences.getString("volume", ""))


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
            addCalculateClickEvent(rsiMorePlus ,rsiMore, "+")
            addCalculateClickEvent(rsiMoreMinus, rsiMore, "-")
            addCalculateClickEvent(stochasticSlowKMorePlus, stochasticSlowKMore, "+")
            addCalculateClickEvent(stochasticSlowKMoreMinus, stochasticSlowKMore, "-")
            addCalculateClickEvent(stochasticSlowDMorePlus, stochasticSlowDMore, "+")
            addCalculateClickEvent(stochasticSlowDMoreMinus, stochasticSlowDMore, "-")
        }
        return binding.root
    }

   /* private fun setTextEditEnabled(enabled: Boolean) {
        with(binding){
            rsiMore.isEnabled = enabled
            stochasticSlowKMore.isEnabled = enabled
            stochasticSlowDMore.isEnabled = enabled
            priceMore.isEnabled = enabled
            volume.isEnabled = enabled
        }
    }*/
/*
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle("sellingBundle", bundle)
        outState.putBoolean("textEditEnabled", binding.rsiMore.isEnabled)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if(savedInstanceState != null) {
            bundle = savedInstanceState.getBundle("sellingBundle")!!
            //isEnabled = savedInstanceState.getBoolean("textEditEnabled")
        }
    }*/
   private fun addCalculateClickEvent(eventTextView: TextView, editText: EditText, calcType: String){
       eventTextView.setOnClickListener(object: SingleClickListener(){
           override fun onSingleClick(v: View?) {
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
       })
   }
    companion object{
        fun newInstance() = AutoSellingFragment()
    }
}