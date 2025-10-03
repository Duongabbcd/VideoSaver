package com.example.videosaver.screen.player

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.videosaver.remote.model.scraper.VideoSolution
import com.google.gson.Gson
import kotlin.apply


class PlayerActivity : BaseActivity<ActivityPlayerBinding>(ActivityPlayerBinding::inflate) {
    private lateinit var videoSolution: VideoSolution
    private val videoUrl by lazy {
        intent.getStringExtra("playerURL") ?: ""
    }
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

        videoSolution = Gson().fromJson(videoUrl, VideoSolution::class.java)

        binding.apply {
            if(videoUrl.isNotEmpty()) {
                playButton.setOnClickListener {
                    if (!hasInitialized) {
                        initializePlayer(videoUrl)
                    } else {
                        player?.playWhenReady = true
                    }
                }

                downloadButton.setOnClickListener {
                   downloadMediaFiles()
                }

                iconBack.setOnClickListener {
                    finish()
                }
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

    private fun downloadMediaFiles() {

        val missingPermissions = PermissionUtils.getMissingMediaPermissions(this@PlayerActivity)

        if (missingPermissions.isEmpty()) {
            // All permissions granted
            actuallyDownloadMediaFile()
        } else {
            // Request the missing permissions using launcher
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
            return
        }

    }


    private fun initializePlayer(url: String) {
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