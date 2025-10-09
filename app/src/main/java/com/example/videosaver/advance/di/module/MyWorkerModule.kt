package com.example.videosaver.advance.di.module

import androidx.work.WorkerFactory
import com.example.videosaver.advance.data.repository.ProgressRepository
import com.example.videosaver.utils.advance.proxy_utils.CustomProxyController
import com.example.videosaver.utils.advance.proxy_utils.OkHttpProxyClient
import com.example.videosaver.utils.advance.util.FileUtil
import com.example.videosaver.utils.advance.util.NotificationsHelper
import com.example.videosaver.utils.advance.util.SharedPrefHelper
import com.example.videosaver.utils.advance.util.downloaders.generic_downloaders.DaggerWorkerFactory
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class MyWorkerModule {
    @Provides
    @Singleton
    fun workerFactory(
        progressRepository: ProgressRepository,
        fileUtil: FileUtil,
        notificationsHelper: NotificationsHelper,
        proxyController: CustomProxyController,
        okHttpProxyClient: OkHttpProxyClient,
        sharedPrefHelper: SharedPrefHelper
    ): WorkerFactory {
        return DaggerWorkerFactory(
            progressRepository,
            fileUtil,
            notificationsHelper,
            proxyController,
            okHttpProxyClient,
            sharedPrefHelper
        )
    }
}
