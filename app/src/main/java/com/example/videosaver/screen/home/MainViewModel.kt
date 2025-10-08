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

    // Pair<format, url>
    val selectedFormatTitle = ObservableField<Pair<String, String>?>()

    val currentOriginal = ObservableField<String>()

    val downloadVideoEvent = SingleLiveEvent<VideoInfo>()
    val openDownloadedVideoEvent = SingleLiveEvent<String>()
    val openNavDrawerEvent = SingleLiveEvent<Unit?>()

    // Use LiveData instead of ObservableField
    private val _bookmarksList = MutableLiveData<List<PageInfo>>(emptyList())
    val bookmarksList: LiveData<List<PageInfo>> = _bookmarksList

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
            // Read current bookmarks or start fresh
            val current = _bookmarksList.value?.toMutableList() ?: mutableListOf()
            val faviconBytes = FaviconUtils.bitmapToBytes(favicon)
            val newBookmark = PageInfo(
                link = url,
                order = current.size,
                name = name,
                favicon = faviconBytes
            )
            current.add(newBookmark)
            // Reassign order so it's consistent
            current.forEachIndexed { idx, pi ->
                pi.order = idx
            }
            // Persist and update LiveData
            topPagesRepository.replaceBookmarksWith(current)
            _bookmarksList.postValue(current)  // or `.value = current` on main thread
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

            pages?.let {
                _bookmarksList.postValue(it)
            }

            try {
                topPagesRepository.updateLocalStorageFavicons().collect { pageInfo ->
                    // When a favicon is updated, update that item in LiveData list
                    val current = _bookmarksList.value?.toMutableList() ?: mutableListOf()
                    val idx = current.indexOfFirst { it.link == pageInfo.link }
                    if (idx != -1) {
                        current[idx] = pageInfo  // replace the updated item
                    } else {
                        current.add(pageInfo)
                    }
                    // Re-index orders
                    current.forEachIndexed { index, pi ->
                        pi.order = index
                    }
                    _bookmarksList.postValue(current)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun updateBookmarks(bookmarks: List<PageInfo>) {
        viewModelScope.launch(executorMoverSingle) {
            val updated = bookmarks.mapIndexed { index, pi ->
                pi.order = index
                pi
            }
            topPagesRepository.replaceBookmarksWith(updated)
            _bookmarksList.postValue(updated)
        }
    }
}
