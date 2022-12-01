package com.im.app.coinwatcher.common

import android.content.Context
import android.content.SharedPreferences

object SharedPreferenceManager {
    private lateinit var sharedPreference: SharedPreferences
    fun getPreference(context: Context): SharedPreferences{
        sharedPreference = context.getSharedPreferences("CoinWatcherSettings", Context.MODE_PRIVATE)
        return sharedPreference
    }
}