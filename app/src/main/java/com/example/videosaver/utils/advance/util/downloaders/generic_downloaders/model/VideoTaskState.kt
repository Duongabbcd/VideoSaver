package com.example.videosaver.utils.advance.util.downloaders.generic_downloaders.model

object VideoTaskState {

    val DEFAULT = 0
    val PENDING = -1
    val PREPARE = 1
    val START = 2
    val DOWNLOADING = 3
    val PROXYREADY = 4
    val SUCCESS = 5
    val ERROR = 6
    val PAUSE = 7
    val ENOSPC = 8
    val CANCELED = 9
}