package com.example.videosaver.utils.advance.fragment

import androidx.fragment.app.Fragment
import com.example.videosaver.advance.ui.browser.BrowserFragment
import com.example.videosaver.advance.ui.browser.detectedVideos.DetectedVideosTabFragment
import com.example.videosaver.advance.ui.browser.hometab.BrowserHomeFragment
import com.example.videosaver.advance.ui.browser.webtab.WebTabFragment
import javax.inject.Inject

interface FragmentFactory {
    fun createBrowserFragment(): Fragment
    fun createProgressFragment(): Fragment
    fun createVideoFragment(): Fragment
    fun createSettingsFragment(): Fragment
    fun createHistoryFragment(): Fragment

    fun createBrowserHomeFragment(): Fragment

    fun createWebTabFragment(): Fragment

    fun createDetectedVideosTabFragment(): Fragment
}

class FragmentFactoryImpl @Inject constructor() : FragmentFactory {
    override fun createBrowserFragment() = BrowserFragment.newInstance()

    override fun createProgressFragment() = BrowserFragment.newInstance()

    override fun createVideoFragment() = BrowserFragment.newInstance()

    override fun createSettingsFragment() = BrowserFragment.newInstance()

    override fun createHistoryFragment() = BrowserFragment.newInstance()

    override fun createBrowserHomeFragment() = BrowserHomeFragment.newInstance()

    override fun createWebTabFragment() = WebTabFragment.newInstance()

    override fun createDetectedVideosTabFragment() = DetectedVideosTabFragment.newInstance()
}