package com.example.videosaver.advance.di.module.activity

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.example.videosaver.advance.di.module.FragmentScoped
import com.example.videosaver.screen.player.VideoPlayerFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class VideoPlayerModule {

    @OptIn(UnstableApi::class)
    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun bindVideoPlayerFragment(): VideoPlayerFragment
}