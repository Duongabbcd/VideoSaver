package com.example.videosaver.advance.di.module

import android.app.Application
import android.content.Context
import com.example.videosaver.MyApplication
import com.example.videosaver.utils.advance.scheduler.BaseSchedulers
import com.example.videosaver.utils.advance.scheduler.BaseSchedulersImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class  AppModule {

//    @Binds
//    abstract  fun bindApplication(application: MyApplication): Application

    @Singleton
    @Binds
    abstract  fun bindBaseSchedulers(baseSchedulers: BaseSchedulersImpl): BaseSchedulers


    //
//    @Binds
//    @ApplicationContext
//     fun bindApplicationContext(application: DLApplication): Context


//    @ContributesAndroidInjector
//     fun contributesNotificationReceiver(): NotificationReceiver
}