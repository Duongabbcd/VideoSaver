package com.example.videosaver.advance

import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import com.example.videosaver.advance.ui.browser.DownloadButtonState
import com.example.videosaver.advance.ui.webtab.WebTab
import com.example.videosaver.utils.SingleLiveEvent


interface BrowserServicesProvider : TabManagerProvider, PageTabProvider,
//    HistoryProvider,
    WorkerEventProvider, CurrentTabIndexProvider

interface TabManagerProvider {
    fun getOpenTabEvent(): SingleLiveEvent<WebTab>

    fun getCloseTabEvent(): SingleLiveEvent<WebTab>

    fun getUpdateTabEvent(): SingleLiveEvent<WebTab>

    fun getTabsListChangeEvent(): ObservableField<List<WebTab>>
}

interface PageTabProvider {
    fun getPageTab(position: Int): WebTab
}

//interface HistoryProvider {
//    fun getHistoryVModel(): HistoryViewModel
//}

interface WorkerEventProvider {
    fun getWorkerM3u8MpdEvent(): MutableLiveData<DownloadButtonState>

    fun getWorkerMP4Event(): MutableLiveData<DownloadButtonState>
}

interface CurrentTabIndexProvider {
    fun getCurrentTabIndex(): ObservableInt
}

interface BrowserListener {
    fun onBrowserMenuClicked()

    fun onBrowserReloadClicked()

    fun onTabCloseClicked()

    fun onBrowserStopClicked()

    fun onBrowserBackClicked()

    fun onBrowserForwardClicked()
}