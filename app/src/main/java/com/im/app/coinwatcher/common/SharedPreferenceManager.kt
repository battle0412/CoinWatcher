package com.im.app.coinwatcher.common

import android.content.Context
import android.content.SharedPreferences

object SharedPreferenceManager {
    private lateinit var sharedPreference: SharedPreferences
    fun getSettingsPreference(context: Context): SharedPreferences{
        sharedPreference = context.getSharedPreferences("GZASettings", Context.MODE_PRIVATE)
        return sharedPreference
    }
    fun getAutoTradingPreference(context: Context): SharedPreferences{
        sharedPreference = context.getSharedPreferences("GZAutoTrading", Context.MODE_PRIVATE)
        return sharedPreference
    }
    fun getWatchListPreference(context: Context): SharedPreferences{
        sharedPreference = context.getSharedPreferences("GZAWatchList", Context.MODE_PRIVATE)
        return sharedPreference
    }
}