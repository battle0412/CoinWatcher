package com.im.app.coinwatcher.chart_marker

data class CandleMarker(
    var shadowH: String, //고가
    var shadowL: String, //저가
    var open: String,    //시가
    var close: String,   //종가
    var dateTime: String,//일자 *일 **:**(KST)
    var volume: String   //거래량
)

data class BarMarker(
    var dateTime: String,//일자 *일 **:**(KST)
    var volume: String   //거래량
)