package com.im.app.coinwatcher

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.im.app.coinwatcher.common.*
import com.im.app.coinwatcher.settings.KeySettingActivity
import com.im.app.coinwatcher.sqlite.SQLiteManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

@SuppressLint("CustomSplashScreen")
class SplashActivity: AppCompatActivity() {
    @SuppressLint("CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val sharedPreferences = SharedPreferenceManager.getSettingsPreference(this@SplashActivity)
        val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result: ActivityResult ->
            if(result.resultCode == RESULT_OK){
                IS_NIGHT = sharedPreferences.getBoolean("IS_NIGHT", false)
                ACCESS_KEY = sharedPreferences.getString("ACCESS_KEY", "").toString()
                SECRET_KEY = sharedPreferences.getString("SECRET_KEY", "").toString()
                val mainIntent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(mainIntent)
                finish()
            } else {
                finish()
            }
        }

        //없으면 DB인스턴스 생성 후 테이블 생성
        CoroutineScope(Dispatchers.IO).launch {
            delay(2000L)

            if(Arrays.binarySearch(databaseList(), DATABASE_NAME) < 0){
                openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null).also {
                    it.setLocale(Locale.getDefault())
                    SQLiteManager.getDBInstance(this@SplashActivity).onCreate(it)
                }
            }

            if(sharedPreferences.getString("ACCESS_KEY", "") == ""
                || sharedPreferences.getString("SECRET_KEY", "") == ""){
                val keySettingIntent = Intent(this@SplashActivity, KeySettingActivity::class.java)
                startForResult.launch(keySettingIntent)
            } else {
                IS_NIGHT = sharedPreferences.getBoolean("IS_NIGHT", false)
                ACCESS_KEY = sharedPreferences.getString("ACCESS_KEY", "").toString()
                SECRET_KEY = sharedPreferences.getString("SECRET_KEY", "").toString()

                val mainIntent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(mainIntent)
                finish()
            }
        }
    }
}