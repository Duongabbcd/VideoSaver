package com.example.videosaver.advance.ui.browser

import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import com.example.videosaver.advance.data.local.room.entity.VideoInfo
import com.example.videosaver.advance.ui.setting.SettingsViewModel
import com.example.videosaver.advance.ui.webtab.WebTab
import com.example.videosaver.base.BaseViewModel
import com.example.videosaver.utils.SingleLiveEvent
 
import javax.inject.Inject

 
class BrowserViewModel @Inject constructor() : BaseViewModel() {
    companion object {
        const val SEARCH_URL = "https://duckduckgo.com/?t=ffab&q=%s"

        var instance: BrowserViewModel? = null
    }

    var settingsModel: SettingsViewModel? = null

    val openPageEvent = SingleLiveEvent<WebTab>()

    val closePageEvent = SingleLiveEvent<WebTab>()

    val selectWebTabEvent = SingleLiveEvent<WebTab>()

    val updateWebTabEvent = SingleLiveEvent<WebTab>()

    val workerM3u8MpdEvent = MutableLiveData<DownloadButtonState>()

    val workerMP4Event = MutableLiveData<DownloadButtonState>()

    val progress = ObservableInt(0)

    val tabs = ObservableField(listOf(WebTab.HOME_TAB))

    val currentTab = ObservableInt(HOME_TAB_INDEX)

    override fun start() {
        instance = this
    }

    override fun stop() {
        instance = null
    }
}

abstract class DownloadButtonState

class DownloadButtonStateLoading : DownloadButtonState()

class DownloadButtonStateCanDownload(val info: VideoInfo?) : DownloadButtonState()
class DownloadButtonStateCanNotDownload : DownloadButtonState()