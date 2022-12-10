package com.im.app.coinwatcher.settings

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.im.app.coinwatcher.common.*
import com.im.app.coinwatcher.databinding.KeySettingDialogBinding

class KeySettingActivity: AppCompatActivity() {
    private lateinit var binding: KeySettingDialogBinding
    @SuppressLint("CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = KeySettingDialogBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }
        with(binding){
            saveButton.setOnClickListener {
                val sharedPreference = getSharedPreferences("GZASettings", Context.MODE_PRIVATE)
                with(sharedPreference.edit()){
                    IS_NIGHT = false
                    ACCESS_KEY = accesskeyED.text.toString()
                    SECRET_KEY = secretkeyED.text.toString()
                    this.putBoolean("IS_NIGHT", false)
                    this.putString("ACCESS_KEY", accesskeyED.text.toString())
                    this.putString("SECRET_KEY", secretkeyED.text.toString())
                    this.apply()
                    setResult(RESULT_OK)
                }
                /*if(accesskeyED.text.toString().isNotEmpty()
                    && secretkeyED.text.toString().isNotEmpty()){
                    with(SharedPreferenceManager.getSettingsPreference(this@KeySettingActivity)){
                        IS_NIGHT = false
                        IS_RECEIVE_ALARM = true
                        ACCESS_KEY = accesskeyED.text.toString()
                        SECRET_KEY = secretkeyED.text.toString()
                        this.edit().putBoolean("IS_NIGHT", false)
                        this.edit().putBoolean("IS_RECEIVE_ALARM", true)
                        this.edit().putString("ACCESS_KEY", accesskeyED.text.toString())
                        this.edit().putString("SECRET_KEY", secretkeyED.text.toString())
                        this.edit().commit()
                        val test = this.getString("ACCESS_KEY", "")
                        val test2 = this.getString("SECRET_KEY", "")
                        val test3 = this.getBoolean("IS_NIGHT", true)
                        val test4 = this.getBoolean("IS_RECEIVE_ALARM", false)
                        setResult(RESULT_OK)
                    }
                }*/
                finish()
            }
            cancelButton.setOnClickListener {
                setResult(RESULT_CANCELED)
                finish()
            }
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        setResult(RESULT_CANCELED)
    }
}