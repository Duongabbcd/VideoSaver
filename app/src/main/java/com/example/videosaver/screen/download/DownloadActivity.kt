package com.example.videosaver.screen.download

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.util.JsonReader
import android.util.JsonToken
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.example.videosaver.base.BaseActivity
import com.example.videosaver.databinding.ActivityDownloadBinding
import com.example.videosaver.screen.browse.BrowseActivity
import com.example.videosaver.screen.browse.BrowseActivity.Companion.DESKTOP_URGENT
import com.example.videosaver.screen.browse.BrowseActivity.Companion.MOBILE_URGENT
import com.example.videosaver.utils.ResolutionDetail
import com.example.videosaver.utils.VideoSupporter.getVideoResolutionsFromPageSource
import com.example.videosaver.viewmodel.VideoViewModel
import com.maxrave.kotlinyoutubeextractor.YTExtractor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.StringReader
import androidx.activity.viewModels
import androidx.core.view.isVisible
import java.net.URL

@AndroidEntryPoint
class DownloadActivity : BaseActivity<ActivityDownloadBinding>(ActivityDownloadBinding::inflate) {
    private val downloadedURL by lazy {
        intent.getStringExtra("downloadedURL") ?: ""
    }
    private val viewModel: VideoViewModel by viewModels()

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        println("downloadedURL: $downloadedURL")

        binding.apply {
            if (downloadedURL.isNotEmpty()) {
                val receivedURL = URL(downloadedURL)

                viewModel.rawJson.observe(this@DownloadActivity) { jsonString ->
                    // display it somewhere (TextView) or log
                    Log.d(TAG, "jsonString: $jsonString")
                    binding.urlInfo.text = jsonString
                }
                 viewModel.loading.observe(this@DownloadActivity) { isVisible ->
                     progressBarBottomSheet.isVisible = isVisible
                }

                viewModel.error.observe(this@DownloadActivity) { err ->
                    if (err != null) {
                        Toast.makeText(this@DownloadActivity, err, Toast.LENGTH_SHORT).show()
                    }
                }

            }

        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchRawJson(downloadedURL)
    }

    companion object {
        private val TAG = DownloadActivity::class.java.simpleName
    }

}



data class VideoQualityItem(
    val resolution: String,
    val bitrate: Int,
    val estimatedSizeMB : Double
)