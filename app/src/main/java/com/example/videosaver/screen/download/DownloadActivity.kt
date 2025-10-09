package com.example.videosaver.screen.download

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.example.videosaver.base.BaseActivity
import com.example.videosaver.databinding.ActivityDownloadBinding
import com.example.videosaver.viewmodel.video.VideoViewModel
 
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.videosaver.R
import com.example.videosaver.remote.model.scraper.VideoSolution
import com.example.videosaver.screen.download.adapter.VideoSolutionAdapter
import com.example.videosaver.screen.home.player.PlayerActivity
import com.example.videosaver.screen.home.player.PlayerActivity.Companion.returnedFromSettings
import com.example.videosaver.utils.Common
import com.example.videosaver.utils.Common.gone
import com.example.videosaver.utils.Common.visible
import com.example.videosaver.utils.PermissionUtils
import com.example.videosaver.utils.VideoSupporter
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

 
class DownloadActivity : BaseActivity<ActivityDownloadBinding>(ActivityDownloadBinding::inflate) {
    private val downloadedURL by lazy {
        intent.getStringExtra("downloadedURL") ?: ""
    }
    private val viewModel: VideoViewModel by viewModels()
    
    private var videoSolution = VideoSolution.VIDEO_SOLUTION_DEFAULT
    private var videoName = ""

    private var player: ExoPlayer? = null
    private var hasInitialized = false
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    private val videoSolutionAdapter by lazy {
        VideoSolutionAdapter { currentVideoItem, currentVideoName ->
            Log.d(TAG, "videoSolutionAdapter: $currentVideoItem")
            videoSolution = currentVideoItem
            videoName = currentVideoName
           downloadMediaFiles()
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.fetchRawJson(downloadedURL)
        println("downloadedURL: $downloadedURL")

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            handlePermissionResult(permissions)
        }
        
        binding.apply {
            allVideoSolutions.adapter = videoSolutionAdapter
            allVideoSolutions.layoutManager = LinearLayoutManager(this@DownloadActivity,
                LinearLayoutManager.VERTICAL, false)

            if(downloadedURL.contains("instagram", true) || downloadedURL.contains("facebook", true)) {
                initializePlayer(downloadedURL)
                playerView.visible()
            } else {
                playerView.gone()
            }


            if (downloadedURL.isNotEmpty()) {
                val receivedURL = URL(downloadedURL)
                urlInfo.text = downloadedURL

                viewModel.videoItem.observe(this@DownloadActivity) { videoItem ->
                    // display it somewhere (TextView) or log
                    Log.d(TAG, "videoItem: $videoItem")
                    binding.videoTitle.text = videoItem.title
                    videoSolutionAdapter.getVideoName(videoItem.title)
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
                   videoSolutionAdapter.submitList(data.filter { it.hasVideo == true })
                }

            }

        }
    }

    private fun initializePlayer(url: String) {
        println("initializePlayer: $url")
        hasInitialized = true
        binding.apply {
            playerView.visibility = PlayerView.VISIBLE

            player = ExoPlayer.Builder(this@DownloadActivity).build().also { exoPlayer ->
                playerView.player = exoPlayer

                val mediaItem = MediaItem.fromUri(Uri.parse(url))
                exoPlayer.setMediaItem(mediaItem)
                // Loop one track indefinitely
                exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
                // Playback error listener
                exoPlayer.addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        Toast.makeText(
                            this@DownloadActivity,
                            "Playback error: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })

                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
            }
        }

    }

    private fun downloadMediaFiles() {

        val missingPermissions = PermissionUtils.getMissingMediaPermissions(this@DownloadActivity)

        if (missingPermissions.isEmpty()) {
            // All permissions granted
            actuallyDownloadMediaFile()
        } else {
            // Request the missing permissions using launcher
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
            return
        }

    }

    private fun handlePermissionResult(permissions: Map<String, Boolean>) {
        val prefs = getSharedPreferences("permissions_download_prefs", MODE_PRIVATE)
        val allGranted = permissions.values.all { it }

        if (allGranted) {
            // Reset denial count
            prefs.edit().putInt("permission_denied_count", 0).apply()
            actuallyDownloadMediaFile()

        } else {

            // Increment denial count
            val currentCount = prefs.getInt("permission_denied_count", 0) + 1
            prefs.edit().putInt("permission_denied_count", currentCount).apply()
            println("currentCount: $currentCount")
            if (currentCount > 2) {
                // Denied more than twice, show settings dialog
                showGoToSettingsDialog()
            } else {
                Toast.makeText(
                    this,
                    resources.getString(R.string.permission_denied),
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    }

    private fun actuallyDownloadMediaFile() {
        Log.d(TAG,"actuallyDownloadMediaFile: $videoSolution")
        if(videoSolution.url.isEmpty() || videoName.isEmpty()) {
            return
        }

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DownloadActivity, "Start downloading", Toast.LENGTH_SHORT).show()
                }
                val uri = VideoSupporter.downloadMedia(this@DownloadActivity, videoSolution.url , videoName,isVideo = true)
                uri?.let {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@DownloadActivity, "Download successfully", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showGoToSettingsDialog() {
        Common.showDialogGoToSetting(this@DownloadActivity) { result ->
            if (result) {
                returnedFromSettings = true
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", packageName, null)
                startActivity(intent)
            } else {
                Toast.makeText(
                    this,
                    resources.getString(R.string.permission_denied),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    private fun releasePlayer() {
        player?.release()
        player = null
        hasInitialized = false
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