package com.example.videosaver.remote.model.scraper

data class VideoSolution1(
    var abr: Double = 0.0,
    var acodec: String = "",
    var aspect_ratio: Double = 0.0,
    var asr: Int = 0,
    var audio_ext: String = "",
    var container: String = "",
    var downloader_options: DownloaderOptions,
    var dynamic_range: String = "",
    var ext: String = "",
    var filesize: String = "",
    var filesize_approx: String = "",
    var format: String = "",
    var format_id: String = "",
    var format_note: String = "",
    var fps: String = "",
    var height: Int = 0,
    var http_headers: HttpHeadersX,
    var is_dash_periods: Boolean,
    var language: String = "",
    var manifest_stream_number: Int = 0,
    var manifest_url: String = "",
    var protocol: String = "",
    var quality: Int = 0,
    var resolution: String = "",
    var tbr: Double = 0.0,
    var url: String = "",
    var vbr: Double = 0.0,
    var vcodec: String = "",
    var video_ext: String = "",
    var width: Int = 0
)

data class VideoSolution(
    var quality : String = "",
    var formatNote : String = "",
    var resolution : String = "",
    var fps: String ="",
    var size : Int = 0,
    var url : String = "",
    var hasAudio : Boolean = false,
    var hasVideo : Boolean = false,
) {
    companion object {
         val VIDEO_SOLUTION_DEFAULT = VideoSolution(
            "", "", "", "", 0, "", false, false
        )
    }
}