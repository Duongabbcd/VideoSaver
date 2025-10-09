package com.example.videosaver.utils.advance.util.downloaders.generic_downloaders

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.videosaver.advance.data.repository.ProgressRepository
import com.example.videosaver.utils.advance.proxy_utils.CustomProxyController
import com.example.videosaver.utils.advance.proxy_utils.OkHttpProxyClient
import com.example.videosaver.utils.advance.util.FileUtil
import com.example.videosaver.utils.advance.util.NotificationsHelper
import com.example.videosaver.utils.advance.util.SharedPrefHelper
import com.example.videosaver.utils.advance.util.downloaders.generic_downloaders.workers.GenericDownloadWorker
import com.example.videosaver.utils.advance.util.downloaders.generic_downloaders.workers.GenericDownloadWorkerWrapper
import javax.inject.Inject

class DaggerWorkerFactory @Inject constructor(
    private val progress: ProgressRepository,
    private val fileUtil: FileUtil,
    private val notificationsHelper: NotificationsHelper,
    private val proxyController: CustomProxyController,
    private val okHttpProxyClient: OkHttpProxyClient,
    private val sharedPrefHelper: SharedPrefHelper
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context, workerClassName: String, workerParameters: WorkerParameters
    ): CoroutineWorker? {

        val workerKlass =
            Class.forName(workerClassName).asSubclass(GenericDownloadWorker::class.java)
        val constructor =
            workerKlass.getDeclaredConstructor(Context::class.java, WorkerParameters::class.java)
        val instance = constructor.newInstance(appContext, workerParameters)

        when (instance) {
            is GenericDownloadWorkerWrapper -> {
                instance.sharedPrefHelper = sharedPrefHelper
                instance.progressRepository = progress
                instance.fileUtil = fileUtil
                instance.notificationsHelper = notificationsHelper
                instance.proxyController = proxyController
                instance.proxyOkHttpClient = okHttpProxyClient
            }
        }

        return instance
    }
}