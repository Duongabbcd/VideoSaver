package com.example.videosaver.screen.player

import android.net.Uri
import androidx.databinding.ObservableField
import com.example.videosaver.base.BaseViewModel
import com.example.videosaver.utils.SingleLiveEvent
import javax.inject.Inject

class VideoPlayerViewModel @Inject constructor() : BaseViewModel() {

    val videoName = ObservableField("")
    val videoUrl = ObservableField(Uri.EMPTY)
    val videoHeaders = ObservableField(emptyMap<String, String>())

    val stopPlayerEvent = SingleLiveEvent<Void?>()

    override fun start() {
    }

    override fun stop() {
        stopPlayerEvent.call()
    }
}