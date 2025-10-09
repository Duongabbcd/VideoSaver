package com.example.videosaver.advance.data.remote

import com.example.videosaver.advance.data.local.room.entity.VideoInfo
import com.example.videosaver.advance.data.remote.service.VideoService
import com.example.videosaver.advance.data.repository.VideoRepository
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoRemoteDataSource @Inject constructor(
    private val videoService: VideoService
) : VideoRepository {

    override fun getVideoInfo(
        url: Request,
        isM3u8OrMpd: Boolean,
        isAudioCheck: Boolean
    ): VideoInfo? {
        return videoService.getVideoInfo(url, isM3u8OrMpd, isAudioCheck)?.videoInfo
    }

    override fun saveVideoInfo(videoInfo: VideoInfo) {
    }
}