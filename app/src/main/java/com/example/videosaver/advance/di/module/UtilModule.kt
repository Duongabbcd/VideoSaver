package com.example.videosaver.advance.di.module

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import com.example.videosaver.MyApplication
import com.example.videosaver.utils.advance.util.AppUtil
import com.example.videosaver.utils.advance.util.FileUtil
import com.example.videosaver.utils.advance.util.IntentUtil
import com.example.videosaver.utils.advance.util.NotificationsHelper
import com.example.videosaver.utils.advance.util.SharedPrefHelper
import com.example.videosaver.utils.advance.util.SystemUtil
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class UtilModule {

    @Singleton
    @Provides
    fun bindDownloadManager(application: Application): DownloadManager =
        application.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    @Singleton
    @Provides
    fun bindFileUtil() = FileUtil()

    @Singleton
    @Provides
    fun bindSystemUtil() = SystemUtil()

    @Singleton
    @Provides
    fun bindIntentUtil(fileUtil: FileUtil) = IntentUtil(fileUtil)

    @Singleton
    @Provides
    fun bindAppUtil() = AppUtil()

    @Singleton
    @Provides
    fun provideNotificationsHelper(application: MyApplication): NotificationsHelper {
        return NotificationsHelper(application.applicationContext)
    }

    @Singleton
    @Provides
    fun provideSharedPrefHelper(dlApplication: MyApplication, appUtil: AppUtil): SharedPrefHelper {
        return SharedPrefHelper(dlApplication.applicationContext, appUtil)
    }
}