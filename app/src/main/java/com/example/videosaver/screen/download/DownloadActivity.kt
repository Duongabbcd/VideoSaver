package com.example.videosaver.screen.download

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.example.videosaver.base.BaseActivity
import com.example.videosaver.databinding.ActivityDownloadBinding
import com.example.videosaver.viewmodel.VideoViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.videosaver.screen.download.adapter.VideoSolutionAdapter
import com.example.videosaver.screen.player.PlayerActivity
import com.google.gson.Gson
import java.net.URL

@AndroidEntryPoint
class DownloadActivity : BaseActivity<ActivityDownloadBinding>(ActivityDownloadBinding::inflate) {
    private val downloadedURL by lazy {
        intent.getStringExtra("downloadedURL") ?: ""
    }
    private val viewModel: VideoViewModel by viewModels()

    private val videoSolutionAdapter by lazy {
        VideoSolutionAdapter { videoSolution ->
            Log.d(TAG, "videoSolutionAdapter: $videoSolution")
            startActivity(Intent(this@DownloadActivity, PlayerActivity::class.java).apply {
                putExtra("playerURL", Gson().toJson(videoSolution))
            })
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.fetchRawJson(downloadedURL)
        println("downloadedURL: $downloadedURL")

        binding.apply {
            allVideoSolutions.adapter = videoSolutionAdapter
            allVideoSolutions.layoutManager = LinearLayoutManager(this@DownloadActivity,
                LinearLayoutManager.VERTICAL, false)

            if (downloadedURL.isNotEmpty()) {
                val receivedURL = URL(downloadedURL)
                urlInfo.text = downloadedURL

                viewModel.videoItem.observe(this@DownloadActivity) { videoItem ->
                    // display it somewhere (TextView) or log
                    Log.d(TAG, "videoItem: $videoItem")
                    binding.urlInfo.text = videoItem.original_url
                    binding.videoTitle.text = videoItem.fulltitle
                }
                 viewModel.loading.observe(this@DownloadActivity) { isVisible ->
                     progressBarBottomSheet.isVisible = isVisible
                     allVideoSolutions.isVisible = !isVisible
                }

                viewModel.error.observe(this@DownloadActivity) { err ->
                    if (err != null) {
                        Toast.makeText(this@DownloadActivity, err, Toast.LENGTH_SHORT).show()
                    }
                }
                viewModel.allVideSolution.observe(this@DownloadActivity) { data ->
                   videoSolutionAdapter.submitList(data)
                }

            }

        }
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