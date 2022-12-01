package com.im.app.coinwatcher

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
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
            alarmSwitching.isChecked = IS_RECEIVE_ALARM
            darkSwitching.isChecked = IS_NIGHT
            val sharedPreferences = SharedPreferenceManager.getPreference(requireContext())

            alarmSwitching.setOnCheckedChangeListener { _, isChecked ->
                sharedPreferences.edit().putBoolean("IS_RECEIVE_ALARM", isChecked).apply()
                IS_RECEIVE_ALARM = isChecked
            }
            darkSwitching.setOnCheckedChangeListener { _, isChecked ->
                IS_NIGHT = isChecked
                sharedPreferences.edit().putBoolean("IS_NIGHT", isChecked).apply()
                if(IS_NIGHT)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                else
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            accessKeySave.setOnClickListener {
                sharedPreferences.edit().putString("ACCESS_KEY", accesskeyED.text.toString()).apply()
                ACCESS_KEY = accesskeyED.text.toString()
                toastMessage("저장 성공")
            }
            secretKeySave.setOnClickListener {
                sharedPreferences.edit().putString("SECRET_KEY", secretkeyED.text.toString()).apply()
                SECRET_KEY = secretkeyED.text.toString()
                toastMessage("저장 성공")
            }
        }
        return binding.root
    }

    companion object{
        fun getInstance() = SettingsFragment()
    }
}