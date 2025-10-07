package com.example.videosaver.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.example.videosaver.remote.process.DownloadItem
import com.example.videosaver.remote.process.DownloadStatus
import io.microshow.rxffmpeg.RxFFmpegInvoke
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer


object VideoSupporter {


    suspend fun downloadMedia(
        context: Context,
        mediaUrl: String,
        inputFileName: String,
        isVideo: Boolean = false,
        headers: Map<String, String> = emptyMap() // <-- new parameter
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            val isM3u8 = mediaUrl.endsWith(".m3u8", ignoreCase = true)
            val mimeType = if (isVideo) "video/mp4" else "audio/mpeg"
            val fileExtension = if (isVideo) ".mp4" else ".mp3"
            val relativePath = if (isVideo)
                "${Environment.DIRECTORY_MOVIES}/VideoSaver"
            else
                "${Environment.DIRECTORY_MUSIC}/VideoSaver"

            val fileName = "$inputFileName$fileExtension"

            // 1️⃣ Report starting download
            DownloadRepository.addDownload(
                DownloadItem(
                    url = mediaUrl,
                    fileName = fileName,
                    progress = 0,
                    status = DownloadStatus.Downloading
                )
            )

            // 2️⃣ Insert into MediaStore
            val uri = insertMediaFile(context, fileName, mimeType, isVideo, relativePath)
                ?: return@withContext null

            if (isM3u8) {
                // HLS -> MP4 conversion via FFmpeg in cacheDir
                val tempFile = File(context.cacheDir, fileName)
                if (tempFile.exists()) tempFile.delete()

                val cmd = arrayOf("-i", mediaUrl, "-c", "copy", tempFile.absolutePath)

                val listener = object : RxFFmpegInvoke.IFFmpegListener {
                    override fun onFinish() {
                        Log.d("RxFFmpeg", "FFmpeg finished")
                    }

                    override fun onProgress(progress: Int, progressTime: Long) {
                        Log.d("RxFFmpeg", "$progress: $progressTime")
                    }

                    override fun onCancel() {
                        Log.d("RxFFmpeg", "FFmpeg cancelled")
                    }

                    override fun onError(message: String?) {
                        Log.d("RxFFmpeg", "FFmpeg error: $message")
                    }

                }


                val result = RxFFmpegInvoke.getInstance().runCommand(cmd, listener)

                if (result != 0) {
                    DownloadRepository.updateStatus(mediaUrl, DownloadStatus.Failed)
                    Log.e("VideoSaver", "FFmpeg conversion failed, code: $result")
                    return@withContext null
                }

                // Copy tempFile to MediaStore
                resolver.openOutputStream(uri, "rw")?.use { out ->
                    tempFile.inputStream().use { input -> input.copyTo(out) }
                }
                tempFile.delete()
                DownloadRepository.updateStatus(mediaUrl, DownloadStatus.Completed)
                Log.d("VideoSaver", "HLS converted and saved: $uri")
                return@withContext uri
            } else {
                val cleanUrl = mediaUrl.replace("&amp;", "&")

                val client = OkHttpClient()
                val requestBuilder = Request.Builder().url(cleanUrl)

                // Add all headers from the map first
                for ((key, value) in headers) {
                    requestBuilder.header(key, value)
                }
                // Add default headers only if they aren't already present
                if (!headers.containsKey("User-Agent")) {
                    requestBuilder.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                }
                if (!headers.containsKey("Referer")) {
                    requestBuilder.header("Referer", "https://www.tiktok.com/")
                }
                if (!headers.containsKey("Origin")) {
                    requestBuilder.header("Origin", "https://www.tiktok.com")
                }
                if (!headers.containsKey("Accept-Language")) {
                    requestBuilder.header("Accept-Language", "en-US,en;q=0.9")
                }
                if (!headers.containsKey("Accept")) {
                    requestBuilder.header("Accept", "*/*")
                }

                val request = requestBuilder.build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e("VideoSaver", "HTTP failed: ${response.code}")
                        DownloadRepository.updateStatus(mediaUrl, DownloadStatus.Failed)
                        return@withContext null
                    }

                    val totalBytes = response.body?.contentLength() ?: -1L
                    val inputStream = response.body?.byteStream() ?: return@withContext null

                    resolver.openFileDescriptor(uri, "rw")?.use { pfd ->
                        FileOutputStream(pfd.fileDescriptor).channel.use { channel ->
                            val buffer = ByteArray(8 * 1024)
                            var bytesRead: Int
                            var currentBytes = 0L
                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                channel.write(ByteBuffer.wrap(buffer, 0, bytesRead))
                                currentBytes += bytesRead
                                val progress =
                                    if (totalBytes > 0) ((currentBytes * 100) / totalBytes).toInt() else 0
                                DownloadRepository.updateProgress(
                                    mediaUrl,
                                    progress,
                                    currentBytes,
                                    totalBytes
                                )
                            }
                        }
                    }

                    DownloadRepository.updateStatus(mediaUrl, DownloadStatus.Completed)
                    Log.d("VideoSaver", "File downloaded: $uri")
                    return@withContext uri
                }
            }
            } catch (e: Exception) {
                DownloadRepository.updateStatus(mediaUrl, DownloadStatus.Failed)
                Log.e("VideoSaver", "Download failed: ${e.message}", e)
                return@withContext null
            }

        }


        private fun insertMediaFile(
            context: Context,
            fileName: String,
            mimeType: String,
            isVideo: Boolean,
            relativePath: String
        ): Uri? {
            val collection: Uri
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            }

            // For API >= 29 (Android 10), use scoped storage.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                collection = if (isVideo) {
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                }

                // Scoped storage: use RELATIVE_PATH
                contentValues.put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    relativePath
                )

            } else {
                // For API < 29, use legacy storage.
                collection = if (isVideo) {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else {
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                // Legacy: absolute path using DATA
                val dir = if (isVideo) {
                    File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                        "VideoSaver"
                    )
                } else {
                    File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                        "VideoSaver"
                    )
                }

                // Ensure the directory exists
                if (!dir.exists()) {
                    val created = dir.mkdirs()
                    Log.d("VideoSaver", "Directory created: $created at ${dir.absolutePath}")
                }

                val file = File(dir, fileName)
                if (file.exists()) {
                    val deleted = file.delete()
                    Log.d(
                        "VideoSaver",
                        "Existing file deleted: $deleted at ${file.absolutePath}"
                    )
                }

                // Remove any existing media record (legacy storage)
                val rowsDeleted = context.contentResolver.delete(
                    collection,
                    "${MediaStore.MediaColumns.DATA} = ?",
                    arrayOf(file.absolutePath)
                )
                Log.d("VideoSaver", "MediaStore record deleted: $rowsDeleted rows")

                contentValues.put(MediaStore.MediaColumns.DATA, file.absolutePath)
            }

            // Insert the media item into the appropriate collection (Images or Videos)
            Log.d("VideoSaver", "Inserting into $collection with values: $contentValues")
            return context.contentResolver.insert(collection, contentValues)
        }

}