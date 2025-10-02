package com.example.videosaver.remote.model.scraper

import com.google.gson.annotations.SerializedName

data class HttpHeadersX(
    @SerializedName("Accept")
    val accept: String,

    @SerializedName("Accept-Language")
    val acceptLanguage: String,

    @SerializedName("Sec-Fetch-Mode")
    val secFetchMode: String,

    @SerializedName("User-Agent")
    val userAgent: String
)