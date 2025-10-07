package com.example.videosaver.remote.process

import kotlinx.coroutines.Job

data class DownloadItem(
    val url: String,
    val fileName: String,
    var progress: Int = 0,
    var status: DownloadStatus = DownloadStatus.Pending,
    var job: Job? = null,  // keep track of the coroutine job to cancel/pause/resume
    var downloadedBytes: Long = 0L, // to keep track for resuming
    var totalBytes: Long = 0L // total file size
)


enum class DownloadStatus {
    Pending, Downloading, Paused, Completed, Failed, Canceled
}