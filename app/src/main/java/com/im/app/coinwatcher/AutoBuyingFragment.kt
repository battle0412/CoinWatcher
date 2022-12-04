package com.im.app.coinwatcher

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.im.app.coinwatcher.common.*
import com.im.app.coinwatcher.databinding.FragmentAutoBuyingBinding
import kotlinx.android.synthetic.main.fragment_auto_buying.view.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class AutoBuyingFragment: Fragment() {
    private lateinit var binding: FragmentAutoBuyingBinding
    private var bundle = Bundle()
    @SuppressLint("CommitPrefEdits")
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
        val sharedPreferences = SharedPreferenceManager.getAutoTradingPreference(requireContext())
        with(binding){
            rsiLess.setText(sharedPreferences.getString("rsiLess", ""))
            stochasticSlowKLess.setText(sharedPreferences.getString("buySlowK", ""))
            stochasticSlowDLess.setText(sharedPreferences.getString("buySlowD", ""))
            priceLess.setText(sharedPreferences.getString("priceLess", ""))
            buyPrice.setText(sharedPreferences.getString("buyPrice", ""))

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
        }
        return binding.root
    }

 /*   override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle("sellingBundle", bundle)
        outState.putBoolean("textEditEnabled", binding.rsiLess.isEnabled)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if(savedInstanceState != null){
            bundle = savedInstanceState.getBundle("sellingBundle")!!
            //isEnabled = savedInstanceState.getBoolean("textEditEnabled")
        }
    }*/

    /*private fun setTextEditEnabled(enabled: Boolean){
        with(binding){
            rsiLess.isEnabled = enabled
            stochasticSlowKLess.isEnabled = enabled
            stochasticSlowDLess.isEnabled = enabled
            priceLess.isEnabled = enabled
            buyPrice.isEnabled = enabled
        }
        arguments = bundle
    }*/

    companion object{
        fun newInstance() = AutoBuyingFragment()
    }
}