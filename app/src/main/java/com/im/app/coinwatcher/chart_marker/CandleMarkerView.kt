package com.im.app.coinwatcher.chart_marker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.im.app.coinwatcher.R

@SuppressLint("ViewConstructor")
class CandleMarkerView(context: Context?, layoutResource: Int) :
    MarkerView(context, layoutResource) {

    //marker 위치 조정(왼쪽 상단)
    /*verride fun draw(canvas: Canvas?) {
        super.draw(canvas)
    }*/

    override fun draw(canvas: Canvas, posX: Float, posY: Float) {
        if(posX > (canvas.width / 2.0))
            super.draw(canvas, canvas.width * 0.05f, canvas.height * 0.03f)
        else
            super.draw(canvas, canvas.width - this.width.toFloat() - canvas.width * 0.2f, canvas.height * 0.03f)
    }

    @SuppressLint("SetTextI18n")
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        val mc = e?.data as CandleMarker

        findViewById<TextView>(R.id.dateTime_Text).text =
            """${mc.dateTime.substring(8, 10)}일 ${mc.dateTime.substring(11, 16)}(KST)"""
        findViewById<TextView>(R.id.open_Value).text = mc.open
        findViewById<TextView>(R.id.shadowH_Value).text = mc.shadowH
        findViewById<TextView>(R.id.shadowL_Value).text = mc.shadowL
        findViewById<TextView>(R.id.close_Value).text = mc.close
        findViewById<TextView>(R.id.volumeTotal_Value).text = mc.volume

        super.refreshContent(e, highlight)
    }
}