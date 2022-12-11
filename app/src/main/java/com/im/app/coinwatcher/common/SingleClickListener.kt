package com.im.app.coinwatcher.common

import android.os.SystemClock
import android.view.View

abstract class SingleClickListener: View.OnClickListener {
    private val MIN_CLICK_INTERVAL = 3000L
    private var lastClick = 0L
    abstract fun onSingleClick(v: View?)
    override fun onClick(v: View?) {
        val curClickTime = SystemClock.uptimeMillis()
        val elapsedTime = curClickTime - lastClick //경과시간
        lastClick = elapsedTime
        if(elapsedTime > MIN_CLICK_INTERVAL)
            onSingleClick(v)
    }
}