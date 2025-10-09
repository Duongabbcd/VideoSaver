package com.example.videosaver.viewmodel.video

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.videosaver.remote.ApifyService
import com.example.videosaver.remote.model.scraper.VideoItem
import com.example.videosaver.remote.model.scraper.VideoItem.Companion.VIDEO_DEFAULT
import com.example.videosaver.remote.model.scraper.VideoSolution
 
import kotlinx.coroutines.launch
import javax.inject.Inject

 
class VideoViewModel @Inject constructor(
//    private val apifyService: ApifyService
) : ViewModel() {
    private val _videoItem = MutableLiveData<VideoItem>()
    val videoItem: LiveData<VideoItem>  = _videoItem

    private val _tiktokVideoItem = MutableLiveData<VideoItem>()
    val tiktokVideoItem: LiveData<VideoItem>  = _tiktokVideoItem

    private val _allVideSolution = MutableLiveData<List<VideoSolution>>()
    val allVideSolution : LiveData<List<VideoSolution>> = _allVideSolution

    private val _allAudioSolution = MutableLiveData<List<VideoSolution>>()
    val allAudioSolution : LiveData<List<VideoSolution>> = _allAudioSolution

    private val _allMediaFiles = MutableLiveData<List<MediaFile>>()
    val allMediaFiles : LiveData<List<MediaFile>> = _allMediaFiles

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading


    fun queryMediaFiles(context: Context) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val resolver = context.contentResolver
                val mediaList = mutableListOf<MediaFile>()

                // Use a unified collection URI depending on Android version
                val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Files.getContentUri("external")
                }

                // We’ll query for both video and audio MIME types
                val projection = arrayOf(
                    MediaStore.MediaColumns._ID,
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    MediaStore.MediaColumns.MIME_TYPE,
                    MediaStore.MediaColumns.RELATIVE_PATH
                )

                val selection = ("${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ? OR " +
                        "${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?")

                val selectionArgs = arrayOf(
                    "%${Environment.DIRECTORY_MOVIES}/VideoSaver%",
                    "%${Environment.DIRECTORY_MUSIC}/VideoSaver%"
                )

                val sortOrder = "${MediaStore.MediaColumns.DATE_ADDED} DESC"

                resolver.query(collection, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                    val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                    val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val name = cursor.getString(nameColumn)
                        val mime = cursor.getString(mimeColumn)

                        val isVideo = mime.startsWith("video")
                        val uri = if (isVideo) {
                            ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                        } else {
                            ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                        }

                        val mediaFile = MediaFile(uri = uri.toString(), name = name, isVideo = isVideo)
                        mediaList.add(mediaFile)
                    }
                }

                _allMediaFiles.value = mediaList
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }


    fun fetchRawJson(url: String) {
        _loading.value = true
        var allVideoSolutions =mutableListOf<VideoSolution>()
        var allAudioSolutions =mutableListOf<VideoSolution>()
        var allMediaSolutions =mutableListOf<VideoSolution>()
        viewModelScope.launch {
            try {
                val response = listOf(VIDEO_DEFAULT)
                val videoItem = response
                _videoItem.value = videoItem.first()
                allVideoSolutions.addAll(videoItem.first().availableFormats.filter {
                    it.hasVideo && !it.hasAudio
                })

                allVideoSolutions = allVideoSolutions
                    .distinctBy { it.resolution }
                    .toMutableList()


                allAudioSolutions.add(videoItem.first().availableFormats.filter {
                   it.hasAudio
                }.last())

                allMediaSolutions.addAll(videoItem.first().availableFormats.filter {
                   it.hasAudio && it.hasVideo && !it.quality.contains("unknown", true)
                })

                allMediaSolutions = allMediaSolutions
                    .distinctBy { it.resolution }
                    .toMutableList()

                _allAudioSolution.value = allAudioSolutions
                if(allMediaSolutions.isNotEmpty()) {
                    _allVideSolution.value = allMediaSolutions
                } else {
                    _allVideSolution.value = allVideoSolutions
                }
                _error.value = null
            } catch (e: Exception) {
                Log.e("VideoViewModel", "Failed to fetch video data", e)
                _error.value = "Failed: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

}

data class MediaFile(
    val uri:  String,
    val name: String,
    val isVideo: Boolean
)