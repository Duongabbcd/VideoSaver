package com.example.videosaver.remote.model

data class Bookmark(val name: String, val url: String, var image: ByteArray? = null, var imagePath: Int? = null)
