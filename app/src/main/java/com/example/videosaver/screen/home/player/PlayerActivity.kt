package com.example.videosaver.screen.home.player

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.videosaver.base.BaseActivity
import com.example.videosaver.databinding.ActivityPlayerBinding
import com.example.videosaver.utils.Common
import com.example.videosaver.utils.PermissionUtils
import com.example.videosaver.R
import com.example.videosaver.remote.model.scraper.VideoItem
import com.example.videosaver.remote.model.scraper.VideoSolution
import com.example.videosaver.screen.home.MainActivity
import com.example.videosaver.utils.VideoSupporter
import com.example.videosaver.viewmodel.video.MediaFile
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.apply


class PlayerActivity : BaseActivity<ActivityPlayerBinding>(ActivityPlayerBinding::inflate) {
    private lateinit var videoSolution: VideoSolution
    private var videoInDevice: MediaFile? = null // ✅ make nullable instead of lateinit
    private val videoUrl by lazy {
        intent.getStringExtra("playerURL") ?: ""
    }
    private val videoName by lazy {
        intent.getStringExtra("videoName") ?: ""
    }

    private val downloadedVideo by lazy {
     intent.getStringExtra("mediaFile") ?: ""
    }

    private var playedURL = ""

    private var player: ExoPlayer? = null
    private var hasInitialized = false

  private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 🔐 Register here
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            handlePermissionResult(permissions)
        }

        binding.apply {
            videoSolution = Gson().fromJson(videoUrl, VideoSolution::class.java)
            videoInDevice = try {
                Gson().fromJson(downloadedVideo, MediaFile::class.java)
            } catch (e: Exception) {
                null
            }

            videoInDevice?.let { media ->
                if (media.name.isNotEmpty()) {
                    initializePlayer(media.uri)
                    return@apply
                }
            }

            playedURL = videoSolution.url

            playButton.setOnClickListener {
                if (!hasInitialized) {
                    initializePlayer(playedURL)
                } else {
                    player?.playWhenReady = true
                }
            }

//            downloadButton.setOnClickListener {
//                downloadMediaFiles()
//            }

            iconBack.setOnClickListener {
                finish()
            }

            iconHome.setOnClickListener {
                startActivity(Intent(this@PlayerActivity, MainActivity::class.java))
            }
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
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PlayerActivity, "Start downloading", Toast.LENGTH_SHORT).show()
                }
                val uri = VideoSupporter.downloadMedia(this@PlayerActivity,playedURL , videoName,isVideo = true)
                uri?.let {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@PlayerActivity, "Download successfully", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showGoToSettingsDialog() {
        Common.showDialogGoToSetting(this@PlayerActivity) { result ->
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



    private fun initializePlayer(url: String) {
        println("initializePlayer: $url")
        hasInitialized = true
        binding.apply {
            playerView.visibility = PlayerView.VISIBLE

            player = ExoPlayer.Builder(this@PlayerActivity).build().also { exoPlayer ->
                playerView.player = exoPlayer

                val mediaItem = MediaItem.fromUri(Uri.parse(url))
                exoPlayer.setMediaItem(mediaItem)
                // Loop one track indefinitely
                exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
                // Playback error listener
                exoPlayer.addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        Toast.makeText(
                            this@PlayerActivity,
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

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    private fun releasePlayer() {
        player?.release()
        player = null
        hasInitialized = false
    }

    override fun onResume() {
        super.onResume()

    }

    companion object {
        var returnedFromSettings = false
        private val TAG = PlayerActivity::class.java.simpleName
    }
}