package com.example.videosaver.screen.home

import android.graphics.Bitmap
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.videosaver.advance.BrowserServicesProvider
import com.example.videosaver.advance.data.repository.TopPagesRepository
import com.example.videosaver.advance.data.local.room.entity.VideoInfo
import com.example.videosaver.advance.data.local.room.entity.PageInfo
import com.example.videosaver.base.BaseViewModel
import com.example.videosaver.utils.SingleLiveEvent
import com.example.videosaver.utils.advance.util.FaviconUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.collections.indexOfFirst
import kotlin.collections.plus
import kotlin.collections.toMutableList

@HiltViewModel
class MainViewModel @Inject constructor(
    private val topPagesRepository: TopPagesRepository,
) : BaseViewModel() {

    var browserServicesProvider: BrowserServicesProvider? = null

    val openedUrl = ObservableField<String?>()

    val openedText = ObservableField<String?>()

    val isBrowserCurrent = ObservableBoolean(false)

    val currentItem = ObservableField<Int>()

    private val _offScreenPageLimit = MutableLiveData(3)
    val offScreenPageLimit: LiveData<Int> = _offScreenPageLimit

    // pair - format:url
    val selectedFormatTitle = ObservableField<Pair<String, String>?>()

    val currentOriginal = ObservableField<String>()

    val downloadVideoEvent = SingleLiveEvent<VideoInfo>()

    val openDownloadedVideoEvent = SingleLiveEvent<String>()

    val openNavDrawerEvent = SingleLiveEvent<Unit?>()

    var bookmarksList: ObservableField<MutableList<PageInfo>> = ObservableField(mutableListOf())

    private val executorSingle = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    private val executorMoverSingle = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    override fun start() {
        updateTopPages()
    }

    override fun stop() {
        executorSingle.cancel()
        executorMoverSingle.cancel()
    }

    fun bookmark(url: String, name: String, favicon: Bitmap?) {
        viewModelScope.launch(executorMoverSingle) {
            var bookmarks = topPagesRepository.getTopPages().toMutableList()
            val faviconBytes = FaviconUtils.bitmapToBytes(favicon)
            val newBookmark = PageInfo(
                link = url, order = bookmarks.size, name = name, favicon = faviconBytes
            )
            bookmarks.add(newBookmark)
            bookmarks = bookmarks.mapIndexed { index, pageInfo ->
                pageInfo.order = index
                pageInfo
            }.toMutableList()
            bookmarksList.set(bookmarks)
            topPagesRepository.replaceBookmarksWith(bookmarks)
        }
    }

    private fun updateTopPages() {
        viewModelScope.launch(executorSingle) {
            val pages = try {
                topPagesRepository.getTopPages()
            } catch (e: Throwable) {
                e.printStackTrace()
                null
            }

            if (!pages.isNullOrEmpty()) {
                bookmarksList.set(pages.toMutableList())
            }

            try {
                topPagesRepository.updateLocalStorageFavicons().collect { pageInfo ->
                    val bookmrks = bookmarksList.get()
                    val index = bookmrks?.indexOfFirst { it.link == pageInfo.link }
                    if (index != null && index != -1) {
                        bookmrks[index] = pageInfo
                        bookmarksList.set(bookmrks.toMutableList())
                    } else {
                        bookmarksList.set((bookmarksList.get()?.plus(pageInfo))?.toMutableList())
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun updateBookmarks(bookmarks: List<PageInfo>) {
        viewModelScope.launch(executorMoverSingle) {
            val updatedBookMarks = bookmarks.mapIndexed { index, value ->
                value.order = index
                value
            }
            topPagesRepository.replaceBookmarksWith(updatedBookMarks)
            bookmarksList.set(bookmarks.toMutableList())
        }
    }
}
