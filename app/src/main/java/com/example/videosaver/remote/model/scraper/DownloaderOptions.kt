package com.example.videosaver.remote.model.scraper

import com.google.gson.annotations.SerializedName

data class DownloaderOptions(
    @SerializedName("http_chunk_size")
    var httpChunkSize: Int = 0
)