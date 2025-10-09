package com.example.videosaver.advance.di.module

import android.app.Application
import android.content.Context
import com.example.videosaver.MyApplication
import com.example.videosaver.advance.di.qualifier.ApplicationContext
import com.example.videosaver.utils.advance.scheduler.BaseSchedulers
import com.example.videosaver.utils.advance.scheduler.BaseSchedulersImpl
import com.example.videosaver.utils.advance.util.downloaders.NotificationReceiver
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import javax.inject.Singleton

@Module
abstract class  AppModule {

    @Binds
    @ApplicationContext
    abstract fun bindApplicationContext(application: MyApplication): Context

    @Binds
    abstract fun bindApplication(application: MyApplication): Application

    @Singleton
    @Binds
    abstract fun bindBaseSchedulers(baseSchedulers: BaseSchedulersImpl): BaseSchedulers

    @ContributesAndroidInjector
    abstract fun contributesNotificationReceiver(): NotificationReceiver
}