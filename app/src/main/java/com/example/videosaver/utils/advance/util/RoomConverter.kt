package com.example.videosaver.utils.advance.util

import androidx.room.TypeConverter
import com.example.videosaver.advance.data.local.room.entity.VideoInfo
import com.google.gson.Gson
import kotlin.jvm.java

class RoomConverter {

    @TypeConverter
    fun convertJsonToVideo(json: String): VideoInfo {
        return Gson().fromJson(json, VideoInfo::class.java)
    }

    @TypeConverter
    fun convertListVideosToJson(video: VideoInfo): String {
        return Gson().toJson(video)
    }
}