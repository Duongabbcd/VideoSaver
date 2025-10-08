package com.example.videosaver

import android.app.Activity
import android.app.Application
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.media3.exoplayer.ExoPlayer
import com.example.videosaver.firebase.AnalyticsLogger
import com.example.videosaver.utils.Common
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application(), Application.ActivityLifecycleCallbacks{
    var currentActivity: Activity? = null
        private set

    lateinit var exoPlayer: ExoPlayer
        private set

    @Inject
    lateinit var analyticsLogger: AnalyticsLogger

    private var activityReferences = 0
    private var isActivityChangingConfigurations = false
    private var isInBackground = true
    private var foregroundHandled = false // ✅ NEW

    private val activityStartTimes = mutableMapOf<String, Long>()

    override fun onCreate() {
        super.onCreate()
        instance = this // ✅ Fix: Set instance here
        FirebaseApp.initializeApp(this)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        exoPlayer = ExoPlayer.Builder(this).build()

        registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        val lang = Common.getPreLanguage(this)
        println("onActivityCreated: $lang")
        Common.setLocale(this@MyApplication, lang)
    }

    override fun onActivityResumed(activity: Activity) {
        val name = activity::class.java.simpleName
        activityStartTimes[name] = System.currentTimeMillis()

        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }

    override fun onActivityStarted(activity: Activity) {
        activityReferences++
        if (activityReferences == 1 && !isActivityChangingConfigurations) {
            // App enters foreground
            onAppForegrounded()
        }
    }

    override fun onActivityStopped(activity: Activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations
        activityReferences--
        if (activityReferences == 0 && !isActivityChangingConfigurations) {
            // App enters background
            onAppBackgrounded()
        }
    }

    fun onAppForegrounded() {
        if (foregroundHandled) return // ✅ Prevent duplicate triggers
        foregroundHandled = true

        for (listener in appStateListeners) {
            listener.onAppReturnedToForeground()
        }
    }

    fun onAppBackgrounded() {
        foregroundHandled = false // ✅ Reset on background
        for (listener in appStateListeners) {
            listener.onAppWentToBackground()
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        //do nothing
    }

    override fun onActivityDestroyed(activity: Activity) {
        val startTime = activityStartTimes.remove(screenName)

        if (startTime != null) {
            val mode = analyticsLogger.getCurrentModeByScreen(screenName)
            analyticsLogger.updateUserProperties(this, screenName, mode)

            val durationMs = System.currentTimeMillis() - startTime
            Log.d("onActivityDestroyed", "$screenName was active for ${durationMs}ms")
            // Optionally, send to Firebase:
            analyticsLogger.logScreenExit(screenName, durationMs)
        }
    }

    private val appStateListeners = mutableListOf<AppStateListener>()

    fun registerAppStateListener(listener: AppStateListener) {
        if (!appStateListeners.contains(listener)) {
            appStateListeners.add(listener)
        }
    }

    fun unregisterAppStateListener(listener: AppStateListener) {
        appStateListeners.remove(listener)
    }

    companion object {
        var screenName = ""
        const val DEBUG_TAG: String = "YOUTUBE_DL_DEBUG_TAG"

        private var instance: MyApplication? = null

        lateinit var mFirebaseAnalytics: FirebaseAnalytics


        fun getInstance(): MyApplication {
            return instance!!
        }

        @JvmStatic
        fun initROAS(revenue: Long, currency: String) {
            try {
                val sharedPref = PreferenceManager.getDefaultSharedPreferences(instance)
                val editor: SharedPreferences.Editor = sharedPref.edit()
                val currentImpressionRevenue = revenue / 1000000
                // make sure to divide by 10^6
                val previousTroasCache: Float = sharedPref.getFloat(
                    "TroasCache",
                    0F
                ) //Use App Local storage to store cache of tROAS
                val currentTroasCache = (previousTroasCache + currentImpressionRevenue).toFloat()
                //check whether to trigger  tROAS event
                if (currentTroasCache >= 0.01) {
                    logTroasFirebaseAdRevenueEvent(currentTroasCache, currency)
                    editor.putFloat("TroasCache", 0f) //reset TroasCache
                } else {
                    editor.putFloat("TroasCache", currentTroasCache)
                }
                editor.apply()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

        private fun logTroasFirebaseAdRevenueEvent(tRoasCache: Float, currency: String) {
            try {
                val bundle = Bundle()
                bundle.putDouble(
                    FirebaseAnalytics.Param.VALUE,
                    tRoasCache.toDouble()
                ) //(Required)tROAS event must include Double Value
                bundle.putString(
                    FirebaseAnalytics.Param.CURRENCY,
                    currency
                ) //put in the correct currency
                mFirebaseAnalytics.logEvent("Daily_Ads_Revenue", bundle)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }


        @JvmStatic
        fun trackingEvent(event: String) {
            try {
                val params = Bundle()
                mFirebaseAnalytics.logEvent(event, params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

interface AppStateListener {
    fun onAppReturnedToForeground()
    fun onAppWentToBackground()
}
