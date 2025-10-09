package com.example.videosaver.advance.di.module.activity

import android.app.Activity
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.example.videosaver.advance.di.module.ActivityScoped
import com.example.videosaver.advance.di.module.FragmentScoped
import com.example.videosaver.advance.ui.browser.BrowserFragment
import com.example.videosaver.advance.ui.browser.detectedVideos.DetectedVideosTabFragment
import com.example.videosaver.advance.ui.browser.hometab.BrowserHomeFragment
import com.example.videosaver.advance.ui.browser.webtab.WebTabFragment
import com.example.videosaver.screen.home.MainActivity
import com.example.videosaver.utils.advance.fragment.FragmentFactory
import com.example.videosaver.utils.advance.fragment.FragmentFactoryImpl
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MainModule {

    @OptIn(UnstableApi::class)
    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun bindBrowserFragment(): BrowserFragment

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun bindWebTabFragment(): WebTabFragment

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun bindDetectedVideosFragment(): DetectedVideosTabFragment

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun bindBrowserHomeFragment(): BrowserHomeFragment

    @ActivityScoped
    @Binds
    abstract fun bindMainActivity(mainActivity: MainActivity): Activity

    @ActivityScoped
    @Binds
    abstract fun bindFragmentFactory(fragmentFactory: FragmentFactoryImpl): FragmentFactory
}