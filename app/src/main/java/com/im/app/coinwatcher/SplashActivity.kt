package com.im.app.coinwatcher

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.im.app.coinwatcher.common.*
import kotlinx.coroutines.delay
import kotlin.concurrent.thread

@SuppressLint("CustomSplashScreen")
class SplashActivity: AppCompatActivity() {
    @SuppressLint("CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*val test = SharedPreferenceManager.getSettingsPreference(this).edit()
        test.remove("ACCESS_KEY")
        test.remove("SECRET_KEY")
        test.apply()*/


        with(SharedPreferenceManager.getSettingsPreference(this)){
            if(this.getString("ACCESS_KEY", "") == ""
                || this.getString("SECRET_KEY", "") == ""){
                 val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                    result: ActivityResult ->
                    if(result.resultCode == RESULT_OK){
                        IS_NIGHT = this.getBoolean("IS_NIGHT", false)
                        IS_RECEIVE_ALARM = this.getBoolean("IS_RECEIVE_ALARM", true)
                        ACCESS_KEY = this.getString("ACCESS_KEY", "").toString()
                        SECRET_KEY = this.getString("SECRET_KEY", "").toString()
                        val mainIntent = Intent(this@SplashActivity, MainActivity::class.java)
                        startActivity(mainIntent)
                        finish()
                    } else {
                        finish()
                    }
                }
                val keySettingIntent = Intent(this@SplashActivity, KeySettingActivity::class.java)
                startForResult.launch(keySettingIntent)
            } else {
                IS_NIGHT = this.getBoolean("IS_NIGHT", false)
                IS_RECEIVE_ALARM = this.getBoolean("IS_RECEIVE_ALARM", true)
                ACCESS_KEY = this.getString("ACCESS_KEY", "").toString()
                SECRET_KEY = this.getString("SECRET_KEY", "").toString()

                val mainIntent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(mainIntent)
                finish()
            }
        }
    }
}