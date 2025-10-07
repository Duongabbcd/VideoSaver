package com.example.videosaver.utils

import com.example.videosaver.remote.process.DownloadItem
import com.example.videosaver.remote.process.DownloadStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object DownloadRepository {

    private val _downloads = MutableStateFlow<List<DownloadItem>>(emptyList())
    val downloadsFlow = _downloads.asStateFlow()

    fun addDownload(item: DownloadItem) {
        _downloads.value = _downloads.value + item
    }

    fun updateProgress(url: String, progress: Int, downloadedBytes: Long, totalBytes: Long) {
        _downloads.value = _downloads.value.map {
            if (it.url == url) {
                it.copy(progress = progress, downloadedBytes = downloadedBytes, totalBytes = totalBytes)
            } else it
        }
    }

    fun updateStatus(url: String, status: DownloadStatus) {
        _downloads.value = _downloads.value.map {
            if (it.url == url) it.copy(status = status) else it
        }
    }

    fun setJob(url: String, job: Job) {
        _downloads.value = _downloads.value.map {
            if (it.url == url) it.copy(job = job) else it
        }
    }

    fun cancelDownload(url: String) {
        _downloads.value.find { it.url == url }?.job?.cancel()
        updateStatus(url, DownloadStatus.Canceled)
        // Optionally remove from list or keep with canceled status
    }
}
