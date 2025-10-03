package com.example.videosaver.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.videosaver.remote.ApifyService
import com.example.videosaver.remote.model.scraper.VideoItem
import com.example.videosaver.remote.model.scraper.VideoSolution
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoViewModel @Inject constructor(
    private val apifyService: ApifyService
) : ViewModel() {

    private val _rawJson = MutableLiveData<String>()
    val rawJson: LiveData<String> = _rawJson

    private val _videoItem = MutableLiveData<VideoItem>()
    val videoItem: LiveData<VideoItem>  = _videoItem

    private val _allVideSolution = MutableLiveData<List<VideoSolution>>()
    val allVideSolution : LiveData<List<VideoSolution>> = _allVideSolution

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading


    fun fetchRawJson(url: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val response = apifyService.extractVideo(mapOf("url" to url))
                val videoItem = response.first()
                _videoItem.value = videoItem
                _allVideSolution.value = videoItem.formats
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