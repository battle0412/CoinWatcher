package com.im.app.coinwatcher

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.im.app.coinwatcher.auto_trading.AutoTradingFragment
import com.im.app.coinwatcher.common.IS_NIGHT
import com.im.app.coinwatcher.common.SingleClickListener
import com.im.app.coinwatcher.history.FundingHistoryFragment
import com.im.app.coinwatcher.settings.SettingsFragment


/**
 * 파일이름 예쁘게 Fragment_Member_Insert
 * text를 코드에 하드 코딩 x
 * common에 preference 추가 singleton으로
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(IS_NIGHT)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        findViewById<TextView>(R.id.gza_logo).setOnClickListener(object: SingleClickListener() {
            override fun onSingleClick(v: View?) {
                with(supportFragmentManager.beginTransaction()){
                    replace(R.id.container, CoinListFragment.newInstance())
                    commit()
                }
            }
        })

        if(intent.extras != null){
            with(supportFragmentManager.beginTransaction()){
                add(R.id.container, AutoTradingFragment.newInstance(intent))
                commit()
            }
        } else {
            if(savedInstanceState == null){
                with(supportFragmentManager.beginTransaction()){
                    add(R.id.container, CoinListFragment.newInstance())
                    commit()
                }
            }
        }


/*

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        drawerLayout = findViewById(R.id.mainActivity)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer
        )
        toggle.syncState()
        val navigationView = findViewById<NavigationView>(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener { menuItem: MenuItem ->
            menuItem.isChecked = true
            drawerLayout.closeDrawers()
            true
        }
*/
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            val instance = when(item.itemId){
                R.id.coinList -> {
                    CoinListFragment.newInstance()
                }
                R.id.autoTrading -> {
                    AutoTradingFragment.newInstance()
                }
                R.id.fundingHistory -> {
                    FundingHistoryFragment.newInstance()
                }
                R.id.settings -> {
                    SettingsFragment.newInstance()
                }
                else -> throw IllegalStateException("하단 메뉴바 목록 오류 item.itemId: ${item.itemId}")
            }
            with(supportFragmentManager.beginTransaction()){
                replace(R.id.container, instance)
                commit()
            }
            true
        }
    }
}