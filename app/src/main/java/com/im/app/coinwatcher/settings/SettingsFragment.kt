package com.im.app.coinwatcher.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.im.app.coinwatcher.common.*
import com.im.app.coinwatcher.databinding.FragmentSettingsBinding

class SettingsFragment: Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    @SuppressLint("CommitPrefEdits")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        with(binding){
            darkSwitching.isChecked = IS_NIGHT
            val sharedPreferences = SharedPreferenceManager.getSettingsPreference(requireContext())
            darkSwitching.setOnCheckedChangeListener { _, isChecked ->
                IS_NIGHT = isChecked
                sharedPreferences.edit().putBoolean("IS_NIGHT", isChecked).apply()
                if(IS_NIGHT)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                else
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            accessKeySave.setOnClickListener(object: SingleClickListener(){
                override fun onSingleClick(v: View?) {
                    if(accesskeyED.text.toString().trim().isEmpty())
                        toastMessage("ACCESS_KEY를 입력하세요")
                    else{
                        sharedPreferences.edit().putString("ACCESS_KEY", accesskeyED.text.toString())
                            .apply()
                        ACCESS_KEY = accesskeyED.text.toString()
                        toastMessage("저장 성공")
                    }
                }
            })
            secretKeySave.setOnClickListener(object: SingleClickListener(){
                override fun onSingleClick(v: View?) {
                    if(secretkeyED.text.toString().trim().isEmpty())
                        toastMessage("SECRET_KEY를 입력하세요")
                    else {
                        sharedPreferences.edit().putString("SECRET_KEY", secretkeyED.text.toString())
                            .apply()
                        SECRET_KEY = secretkeyED.text.toString()
                        toastMessage("저장 성공")
                    }
                }
            })
        }
        return binding.root
    }

    companion object{
        fun newInstance() = SettingsFragment()
    }
}