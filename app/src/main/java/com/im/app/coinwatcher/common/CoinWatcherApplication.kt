package com.im.app.coinwatcher.common

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.pm.ActivityInfo
import android.os.Bundle

/**
 * 앱 런처 아이콘을 터치하면 처음 실행되는 코드
 * App Scope 모든 코틀린 클래스 파일에서 호출할 수 있는 코드
 * manifests에 android:name=".common.CoinWatcherApplication" 추가
 * 앱 실행시 가장먼저 실행
 */
class CoinWatcherApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        appInstance = this
        settingScreenPortrait()
    }
    companion object{
        private lateinit var appInstance: CoinWatcherApplication
        fun getAppInstance() = appInstance
    }
    fun settingScreenPortrait(){
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks{
            @SuppressLint("SourceLockedOrientationActivity")
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            override fun onActivityStarted(activity: Activity) {
            }

            override fun onActivityResumed(activity: Activity) {
            }

            override fun onActivityPaused(activity: Activity) {
            }

            override fun onActivityStopped(activity: Activity) {
            }

            override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {
            }

            override fun onActivityDestroyed(activity: Activity) {
            }
        })
    }
}