package com.im.app.coinwatcher

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.im.app.coinwatcher.common.*
import com.im.app.coinwatcher.json_data.Order
import com.im.app.coinwatcher.json_data.Trade
import com.im.app.coinwatcher.json_data.TradeOrder
import java.util.*

class SQLiteManager(context: Context): SQLiteOpenHelper(
    context
    , DATABASE_NAME
    , null
    , CURRENT_DB_VERSION
    ) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.let {
            it.execSQL(CREATE_TRADING_HD_TABLE)
            it.execSQL(CREATE_TRADING_DT_TABLE)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.let {
            it.execSQL("DROP TABLE IF EXISTS $TABLE_TRADING_HD")
            it.execSQL("DROP TABLE IF EXISTS $TABLE_TRADING_DT")
        }
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    fun insertTrading(mDatabase: SQLiteDatabase, values: ContentValues, tableName: String): Int {
        var result = -1
        mDatabase.beginTransaction()
        result = mDatabase.insertOrThrow(tableName, null, values).toInt()
        mDatabase.setTransactionSuccessful()
        mDatabase.endTransaction()
        return result
    }

    @SuppressLint("Recycle")
    fun selectTrading(mDatabase: SQLiteDatabase): MutableList<TradeOrder>{
        mDatabase.setLocale(Locale.getDefault())
        val cursor: Cursor = mDatabase.rawQuery("SELECT * FROM $TABLE_TRADING_DT ORDER BY created_at DESC", null)
        val resultData = mutableListOf<TradeOrder>()
        cursor.moveToFirst()
        while(!cursor.isAfterLast){
            val tradeOrder = TradeOrder(
                cursor.getString(0)?.toString() ?: "",
                cursor.getString(1)?.toString() ?: "",
                cursor.getString(2)?.toString() ?: "",
                cursor.getString(3)?.toString() ?: "",
                cursor.getString(4)?.toString() ?: "",
                cursor.getString(5)?.toString() ?: "",
                cursor.getString(6)?.toString() ?: "",
                cursor.getString(7)?.toString() ?: "",
                cursor.getString(8)?.toString() ?: "",
                cursor.getString(9)?.toString() ?: "",
                cursor.getString(10)?.toString() ?: "",
                cursor.getString(11)?.toString() ?: "",
                cursor.getString(12)?.toString() ?: "",
                cursor.getString(13)?.toString() ?: "",
                cursor.getString(14)?.toString() ?: "",
                cursor.getString(15)?.toString() ?: "",
                cursor.getString(16)?.toString() ?: "",
                cursor.getString(17)?.toString() ?: "",
                cursor.getString(18)?.toString() ?: "",
                cursor.getString(19)?.toString() ?: "",
                cursor.getString(20)?.toString() ?: "",
                cursor.getString(21)?.toString() ?: ""
            )
            resultData.add(tradeOrder)
            cursor.moveToNext()
        }
        return resultData
    }
    /*
    fun deleteTrading(whereClause: String, values: Array<String>, tableName: String): Int {
        var result = -1
        writableDatabase.use {
            it.beginTransaction()
            result = it.delete(tableName, whereClause, values)
            it.setTransactionSuccessful()
            it.endTransaction()
        }
        return result
    }*/

    companion object {
        private lateinit var dbInstance: SQLiteManager
        fun getDBInstance(context: Context): SQLiteManager{
            if(!this::dbInstance.isInitialized){
                dbInstance = SQLiteManager(context)
            }
            return dbInstance
        }
    }
}