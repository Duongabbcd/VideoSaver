package com.example.videosaver.advance.ui.component

import androidx.annotation.OptIn
import androidx.databinding.BindingAdapter
import androidx.media3.common.util.UnstableApi
import com.example.videosaver.advance.ui.browser.BrowserFragment
import com.example.videosaver.advance.ui.browser.CustomViewPager2
import com.example.videosaver.advance.ui.webtab.WebTab

object CustomViewPager2Binding {

    @OptIn(UnstableApi::class)
    @BindingAdapter("app:items")
    @JvmStatic
    fun CustomViewPager2.setWebItems(currentItems: List<WebTab>?) {
        with(adapter as BrowserFragment.TabsFragmentStateAdapter?) {
            this?.setRoutes(currentItems ?: emptyList())
        }
    }

    @BindingAdapter("app:offScreenPageLimit")
    @JvmStatic
    fun CustomViewPager2.setOffScreenPageLimit(pageLimit: Int) {
        offscreenPageLimit = pageLimit
    }

    @BindingAdapter("app:currentItem")
    @JvmStatic
    fun CustomViewPager2.setCurrentItem(currentItemPosition: Int) {
        currentItem = currentItemPosition
    }
}
