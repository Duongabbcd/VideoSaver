package com.example.videosaver.advance.ui.webtab

import android.util.Patterns
import com.example.videosaver.advance.ui.browser.BrowserViewModel
import kotlin.text.format
import kotlin.text.isNotEmpty
import kotlin.text.startsWith

class WebTabFactory {
    companion object {
        fun createWebTabFromInput(input: String): WebTab {
            if (input.isNotEmpty()) {
                return if (input.startsWith("http://") || input.startsWith("https://")) {
                    WebTab(input, null, null, emptyMap())
                } else if (Patterns.WEB_URL.matcher(input).matches()) {
                    WebTab("https://$input", null, null, emptyMap())
                } else {
                    WebTab(
                        String.format(BrowserViewModel.SEARCH_URL, input),
                        null,
                        null,
                        emptyMap())
                }
            }

            return WebTab.HOME_TAB
        }
    }
}

