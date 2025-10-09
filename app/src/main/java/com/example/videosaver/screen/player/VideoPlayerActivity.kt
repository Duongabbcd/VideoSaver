package com.example.videosaver.screen.player

import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.example.videosaver.base.BaseActivity
import com.example.videosaver.R
import com.example.videosaver.advance.ext.addFragment
import com.example.videosaver.base.BaseActivity2


class VideoPlayerActivity : BaseActivity2() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_player_video)

        intent.extras?.let { addFragment(R.id.content_frame, it, ::VideoPlayerFragment) }
    }
}