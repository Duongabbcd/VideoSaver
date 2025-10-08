package com.example.videosaver.advance.ui.component

import androidx.annotation.OptIn
import androidx.databinding.BindingAdapter
import androidx.media3.common.util.UnstableApi
import androidx.viewpager2.widget.ViewPager2

object ViewPager2Binding {

//    @OptIn(UnstableApi::class)
//    @BindingAdapter("app:items")
//    @JvmStatic
//    fun ViewPager2.setWebItems(currentItems: List<WebTab>?) {
//        with(adapter as BrowserFragment.TabsFragmentStateAdapter?) {
//            this?.setRoutes(currentItems ?: emptyList())
//        }
//    }

    @BindingAdapter("offScreenPageLimit")
    @JvmStatic
    fun ViewPager2.setOffScreenPageLimit(pageLimit: Int?) {
        val validLimit = if(pageLimit == null ||pageLimit == 0) ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT else pageLimit
        offscreenPageLimit = validLimit
    }
}
