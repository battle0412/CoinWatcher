package com.im.app.coinwatcher

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.im.app.coinwatcher.common.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
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

        //없으면 DB인스턴스 생성 후 테이블 생성
        CoroutineScope(Dispatchers.IO).launch {
            if(Arrays.binarySearch(databaseList(), DATABASE_NAME) < 0){
                openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null).also {
                    it.setLocale(Locale.getDefault())
                    SQLiteManager.getDBInstance(this@SplashActivity).onCreate(it)
                }
            }
        }
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