package com.example.videosaver.advance.ui.component.binding

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.videosaver.advance.data.local.model.LocalVideo
import com.example.videosaver.advance.data.local.model.Proxy
import com.example.videosaver.advance.data.local.model.Suggestion
import com.example.videosaver.advance.data.local.room.entity.HistoryItem
import com.example.videosaver.advance.data.local.room.entity.PageInfo
import com.example.videosaver.advance.data.local.room.entity.ProgressInfo
import com.example.videosaver.advance.data.local.room.entity.VideoInfo
import com.example.videosaver.advance.ui.component.adapter.BookmarksAdapter
import com.example.videosaver.advance.ui.component.adapter.HistoryAdapter
import com.example.videosaver.advance.ui.component.adapter.HistorySearchAdapter
import com.example.videosaver.advance.ui.component.adapter.ProgressAdapter
import com.example.videosaver.advance.ui.component.adapter.ProxiesAdapter
import com.example.videosaver.advance.ui.component.adapter.SuggestionAdapter
import com.example.videosaver.advance.ui.component.adapter.TopPageAdapter
import com.example.videosaver.advance.ui.component.adapter.VideoAdapter
import com.example.videosaver.advance.ui.component.adapter.VideoInfoAdapter
import com.example.videosaver.advance.ui.component.adapter.WebTabsAdapter
import com.example.videosaver.advance.ui.webtab.WebTab

object RecyclerViewBinding {
    @BindingAdapter("app:items")
    @JvmStatic
    fun RecyclerView.setWebTabs(tabs: List<WebTab>) {
        with(adapter as WebTabsAdapter?) {
            this?.let { setData(tabs) }
        }
    }

    @BindingAdapter("app:items")
    @JvmStatic
    fun RecyclerView.setTopPages(items: List<PageInfo>) {
        with(adapter as TopPageAdapter?) {
            this?.let { setData(items) }
        }
    }

    @BindingAdapter("app:items")
    @JvmStatic
    fun RecyclerView.setSuggestions(items: List<Suggestion>) {
        with(adapter as SuggestionAdapter?) {
            this?.let { setData(items) }
        }
    }

    @BindingAdapter("app:items")
    @JvmStatic
    fun RecyclerView.setProgressInfos(items: List<ProgressInfo>) {
        with(adapter as ProgressAdapter?) {
            this?.let { setData(items) }
        }
    }

    @BindingAdapter("app:items")
    @JvmStatic
    fun RecyclerView.setProxiesList(items: List<Proxy>) {
        with(adapter as ProxiesAdapter?) {
            this?.let { setData(items) }
        }
    }

    @BindingAdapter("app:items")
    @JvmStatic
    fun RecyclerView.setVideoInfos(items: List<LocalVideo>) {
        with(adapter as VideoAdapter?) {
            this?.let { setData(items) }
        }
    }

    @BindingAdapter("app:items")
    @JvmStatic
    fun RecyclerView.historyItems(items: List<HistoryItem>) {

        if (adapter is HistoryAdapter?) {
            with(adapter as HistoryAdapter?) {
                this?.let { setData(items) }
            }
        }
        if (adapter is HistorySearchAdapter?) {
            with(adapter as HistorySearchAdapter?) {
                this?.let { setData(items) }
            }
        }
    }

    @BindingAdapter("app:items")
    @JvmStatic
    fun RecyclerView.setDetectedVideoInfos(items: List<VideoInfo>) {
        with(adapter as VideoInfoAdapter?) {
            this?.let { setData(items) }
        }
    }

    @BindingAdapter("app:items")
    @JvmStatic
    fun RecyclerView.setDetectedVideoInfosSet(items: Set<VideoInfo>) {
        with(adapter as VideoInfoAdapter?) {
            this?.let { setData(items.toList()) }
        }
    }

    @BindingAdapter("app:items")
    @JvmStatic
    fun RecyclerView.setBookmarks(items: MutableList<PageInfo>) {
        with(adapter as BookmarksAdapter?) {
            this?.let { setData(items) }
        }
    }
}