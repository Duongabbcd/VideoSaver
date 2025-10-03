package com.example.videosaver.remote

import com.example.videosaver.remote.model.scraper.ScraperVideo
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface ApifyService {
    @POST("v2/acts/clear_aisle~my-actor/run-sync-get-dataset-items")
    suspend fun extractVideo(
        @Body body: Map<String, String>
    ): ScraperVideo
}

data class VideoFormat(
    val format_id: String?,
    val quality: String?,
    val url: String?,
    val ext: String?,
    val filesize: Long?
)

