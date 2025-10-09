package com.example.videosaver.advance.data.local

import com.example.videosaver.advance.data.local.room.dao.VideoDao
import com.example.videosaver.advance.data.local.room.entity.VideoInfo
import com.example.videosaver.advance.data.repository.VideoRepository
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoLocalDataSource @Inject constructor(
    private val videoDao: VideoDao
) : VideoRepository {

    override fun getVideoInfo(
        url: Request,
        isM3u8OrMpd: Boolean,
        isAudioCheck: Boolean
    ): VideoInfo? {
        return videoDao.getVideoById(url.url.toString()).toSingle().blockingGet()
    }

    override fun saveVideoInfo(videoInfo: VideoInfo) {
        videoDao.insertVideo(videoInfo)
    }

}