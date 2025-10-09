package com.example.videosaver.firebase

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import com.example.videosaver.MyApplication
import com.example.videosaver.utils.Common
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsLogger @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
) {
    var userProperties = UserProperties(
        currentMode = "",
        currentScreen = "",
        connectionType = "",
        totalValue = "",
    )

    fun logScreenGo(screenName: String, prevScreenName: String, prevScreenDuration: Long) {
        MyApplication.screenName = screenName
        val bundle = Bundle().apply {
            putString("screen_name", screenName)
            putString("prev_screen_name", prevScreenName)
            putLong("prev_screen_duration", prevScreenDuration)
        }
        firebaseAnalytics.logEvent("screen_go", bundle)
    }

    fun logScreenExit(prevScreenName: String, prevScreenDuration: Long) {
        val bundle = Bundle().apply {
            putString("prev_screen_name", prevScreenName)
            putLong("prev_screen_duration", prevScreenDuration)
        }
        firebaseAnalytics.logEvent("screen_exit", bundle)
    }

    fun logMainFunction(
        prevScreenName: String,
        buttonCount: Long,
    ) {
        val bundle = Bundle().apply {
            putString("prev_screen_name", prevScreenName)
            putLong("button_count", buttonCount)
        }
        firebaseAnalytics.logEvent("main_function", bundle)
    }

    fun logButtonClick(
        buttonName: String,
        screenName: String,
    ) {
        val bundle = Bundle().apply {
            putString("button_name", buttonName)
            putString("screen_name", screenName)
        }
        firebaseAnalytics.logEvent("button_click", bundle)
    }


    fun logOnBoardingAction(
        isSkip: Int
    ) {
        val bundle = Bundle().apply {
            putInt("is_skip", isSkip)
        }
        firebaseAnalytics.logEvent("onboarding_action", bundle)
    }

    fun logLoadingStart(
        placement: String,
    ) {
        val bundle = Bundle().apply {
            putString("placement", placement)
        }

        if(placement.isEmpty()) {
            firebaseAnalytics.logEvent("loading_start", bundle)
        }

    }

    fun logLoadingFinish(
        placement: String,
        isLoad: Int,
        loadingTime: Long,
    ) {
        val bundle = Bundle().apply {
            putString("placement", placement)
            putInt("is_load", isLoad)
            putLong("load_time", loadingTime)
        }

        if(loadingTime > 60000) {
            firebaseAnalytics.logEvent("loading_finish", bundle)
        }

    }

//    fun logAdRequest(
//        formatType: String,
//        placement: String,
//        adapterClassName: String,
//        isLoad: Int,
//        loadingTime: Long
//    ) {
//        val adPlatform = BannerAds.getNetworkName(adapterClassName, true)
//        val adNetwork = BannerAds.getNetworkName(adapterClassName)
//
//        val bundle = Bundle().apply {
//            putString("ad_format", formatType)
//            putString("ad_platform", adPlatform)
//            putString("ad_network", adNetwork)
//            putString("placement", placement)
//            putInt("is_load", isLoad)
//            putLong("load_time", loadingTime)
//        }
//        firebaseAnalytics.logEvent("ad_request", bundle)
//    }

//    fun logAdImpression(
//        formatType: String,
//        context: Activity,
//        adapterClassName: String,
//        isLoad: Int,
//        value: Double
//    ) {
//        println("logAdImpression: $formatType -- $adapterClassName")
//        val placement = context::class.java.simpleName
//        val adPlatform = BannerAds.getNetworkName(adapterClassName, true)
//        val adNetwork = BannerAds.getNetworkName(adapterClassName)
//        val bundle = Bundle().apply {
//            putString("ad_format", formatType)
//            putString("ad_platform", adPlatform)
//            putString("ad_network", adNetwork)
//            putString("placement", placement)
//            putInt("is_load", isLoad)
//            putDouble("value", value)
//        }
//
//        val prevTotal = Common.getAllPreviousClickValue(context)
//        val result = prevTotal + value
//        Common.setAllPreviousClickValue(context, result)
//
//
//        Log.d("AnalyticsLogger", "logAdImpression: ")
//        firebaseAnalytics.logEvent("ad_impression", bundle)
//    }

//    fun logAdComplete(
//        formatType: String,
//        placement: String,
//        adapterClassName: String,
//        endType: String,
//        duration: Long
//    ) {
//        val adPlatform = BannerAds.getNetworkName(adapterClassName, true)
//        val adNetwork = BannerAds.getNetworkName(adapterClassName)
//
//        val bundle = Bundle().apply {
//            putString("ad_format", formatType)
//            putString("ad_platform", adPlatform)
//            putString("ad_network", adNetwork)
//            putString("placement", placement)
//            putString("end_type", endType)
//            putLong("ad_duration", duration)
//        }
//        firebaseAnalytics.logEvent("ad_complete", bundle)
//    }

    fun getConnectionType(context: Context): String {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return "offline"
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return "offline"

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "mobile_data "
            else -> "unknown"
        }
    }

    fun updateUserProperties(
        context: Context,
        currentScreen: String,
        mode: Int
    ) {
        val newUserProperties = UserProperties(
            currentScreen = currentScreen,
            currentMode = getCurrentMode(mode),
            connectionType = getConnectionType(context),
            totalValue = Common.getAllPreviousClickValue(context).toString()
        )

        if (userProperties != newUserProperties) {
            userProperties = newUserProperties
            applyUserPropertiesToFirebase(userProperties)
        }
    }

    private fun applyUserPropertiesToFirebase(userProperties: UserProperties) {
        firebaseAnalytics.setUserProperty("current_mode", userProperties.currentMode)
        firebaseAnalytics.setUserProperty("current_screen", userProperties.currentScreen)
        firebaseAnalytics.setUserProperty("connection_type", userProperties.connectionType)
        firebaseAnalytics.setUserProperty("ad_ltv_n", userProperties.totalValue)
    }

    fun getCurrentMode(mode: Int) = when (mode) {
        0 -> "ringtone"
        1 -> "wallpaper"
        2 -> "callscreen"
        else -> "no_mode"
    }

    fun getCurrentModeByScreen(input: String): Int {
        return when {
            input.contains("ringtone", true) -> 0
            input.contains("wallpaper", true) -> 1
            input.contains("callscreen", true) -> 2
            else -> -1
        }
    }
}

data class UserProperties(
    var currentScreen: String = "",
    var currentMode: String = "",
    var connectionType: String = "",
    var totalValue : String = ""
)
