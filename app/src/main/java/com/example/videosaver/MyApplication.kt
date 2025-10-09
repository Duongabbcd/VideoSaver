package com.example.videosaver

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.media3.exoplayer.ExoPlayer
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.videosaver.advance.di.component.DaggerAppComponent
import com.example.videosaver.firebase.AnalyticsLogger
import com.example.videosaver.utils.Common
import com.example.videosaver.utils.advance.util.AppLogger
import com.example.videosaver.utils.advance.util.ContextUtils
import com.example.videosaver.utils.advance.util.FileUtil
import com.example.videosaver.utils.advance.util.SharedPrefHelper
import com.example.videosaver.utils.advance.util.downloaders.generic_downloaders.DaggerWorkerFactory
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject


class MyApplication() : DaggerApplication(), Application.ActivityLifecycleCallbacks{
    var currentActivity: Activity? = null
        private set

    lateinit var exoPlayer: ExoPlayer
        private set

    private lateinit var androidInjector: AndroidInjector<out DaggerApplication>
    @Inject
    lateinit var analyticsLogger: AnalyticsLogger

    @Inject
    lateinit var workerFactory: DaggerWorkerFactory

    @Inject
    lateinit var sharedPrefHelper: SharedPrefHelper

    @Inject
    lateinit var fileUtil: FileUtil

    private var activityReferences = 0
    private var isActivityChangingConfigurations = false
    private var isInBackground = true
    private var foregroundHandled = false // ✅ NEW

    private val activityStartTimes = mutableMapOf<String, Long>()




    override fun onCreate() {
        super.onCreate()
        setupForDownloading()

        instance = this // ✅ Fix: Set instance here
        FirebaseApp.initializeApp(this)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        exoPlayer = ExoPlayer.Builder(this).build()

        registerActivityLifecycleCallbacks(this)


    }

    private fun setupForDownloading() {
        ContextUtils.initApplicationContext(applicationContext)

        initializeFileUtils()

        val file: File = fileUtil.folderDir
        val ctx = applicationContext

//        WorkManager.initialize(
//            ctx,
//            Configuration.Builder()
//                .setWorkerFactory(workerFactory).build()
//        )

        RxJavaPlugins.setErrorHandler { error: Throwable? ->
            AppLogger.e("RxJavaError unhandled $error")
        }

        CoroutineScope(Dispatchers.Default).launch {
            if (!file.exists()) {
                file.mkdirs()
            }

            initializeYoutubeDl()
            updateYoutubeDL()
        }
    }

    private fun initializeFileUtils() {
        val isExternal = sharedPrefHelper.getIsExternalUse()
        val isAppDir = sharedPrefHelper.getIsAppDirUse()

        FileUtil.IS_EXTERNAL_STORAGE_USE = isExternal
        FileUtil.IS_APP_DATA_DIR_USE = isAppDir
        FileUtil.INITIIALIZED = true
    }

    private fun initializeYoutubeDl() {
        try {
            YoutubeDL.getInstance().init(applicationContext)
            FFmpeg.getInstance().init(applicationContext)
        } catch (e: YoutubeDLException) {
            AppLogger.e("failed to initialize youtubedl-android $e")
        }
    }

    private fun updateYoutubeDL() {
        try {
            val status = YoutubeDL.getInstance()
                .updateYoutubeDL(applicationContext, YoutubeDL.UpdateChannel.MASTER)
            AppLogger.d("UPDATE_STATUS MASTER: $status")
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        androidInjector = DaggerAppComponent.builder().application(this).build()
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =  androidInjector

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
        const val DEBUG_TAG: String = "VideoSaver"

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
