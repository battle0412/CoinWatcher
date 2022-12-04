package com.im.app.coinwatcher.common

const val BASE_URL = """https://api.upbit.com/"""
var ACCESS_KEY = """"""
var SECRET_KEY = """"""
var IS_NIGHT = false
var IS_RECEIVE_ALARM = true

const val RSI_DAY_CNT = 14 //14일 기준

//스토캐스틱 슬로우 값 3개
const val STOCHASTIC_N = 10
const val STOCHASTIC_M = 5
const val STOCHASTIC_T = 5
//백그라운드 서비스 Notify Channel
const val CHANNEL_ID = "AutoTrading"
const val CHANNEL_NAME = "자동매매"