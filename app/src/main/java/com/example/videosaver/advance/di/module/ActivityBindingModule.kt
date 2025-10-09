package com.example.videosaver.advance.di.module

import com.example.videosaver.advance.di.module.activity.MainModule
import com.example.videosaver.advance.di.module.activity.VideoPlayerModule
import com.example.videosaver.screen.home.MainActivity
import com.example.videosaver.screen.player.VideoPlayerActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class ActivityBindingModule {

//    @ActivityScoped
//    @ContributesAndroidInjector
//    internal abstract fun bindSplashActivity(): SplashActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = [MainModule::class])
    internal abstract fun bindMainActivity(): MainActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = [VideoPlayerModule::class])
    internal abstract fun bindVideoPlayerActivity(): VideoPlayerActivity
}