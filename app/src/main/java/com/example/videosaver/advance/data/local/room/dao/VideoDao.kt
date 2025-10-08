package com.example.videosaver.advance.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.videosaver.advance.data.local.room.entity.VideoInfo
import io.reactivex.rxjava3.core.Maybe

@Dao
interface VideoDao {

    @Query("SELECT * FROM VideoInfo WHERE originalUrl = :url")
    fun getVideoById(url: String): Maybe<VideoInfo>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insertVideo(videoInfo: VideoInfo)
}