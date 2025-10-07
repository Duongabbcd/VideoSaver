package com.example.videosaver.viewmodel.process

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.videosaver.remote.process.DownloadStatus
import com.example.videosaver.utils.DownloadRepository
import com.example.videosaver.utils.VideoSupporter.downloadMedia
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DownloadViewModel : ViewModel() {

    // Expose downloads as StateFlow or LiveData for the UI
    val downloads = DownloadRepository.downloadsFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun pauseDownload(url: String) {
        DownloadRepository.downloadsFlow.value.find { it.url == url }?.job?.cancel()
        DownloadRepository.updateStatus(url, DownloadStatus.Paused)
    }

    fun resumeDownload(context: Context, url: String, fileName: String, isVideo: Boolean) {
        val item = DownloadRepository.downloadsFlow.value.find { it.url == url } ?: return
        if (item.status != DownloadStatus.Paused) return

        val job = viewModelScope.launch {
            DownloadRepository.updateStatus(url, DownloadStatus.Downloading)
            downloadMedia(context, url, fileName, isVideo)
        }
        DownloadRepository.setJob(url, job)
    }

    fun cancelDownload(url: String) {
        DownloadRepository.cancelDownload(url)
    }

}
