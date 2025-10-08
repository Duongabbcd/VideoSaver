package com.example.videosaver.advance.ui.browser.hometab

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.videosaver.advance.data.local.model.Suggestion
import com.example.videosaver.base.BaseViewModel
import com.example.videosaver.utils.advance.proxy_utils.OkHttpProxyClient
import com.example.videosaver.utils.advance.scheduler.BaseSchedulers
import com.example.videosaver.utils.advance.util.SuggestionsUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel  // if you're using Hilt
class BrowserHomeViewModel @Inject constructor(
    private val okHttpClient: OkHttpProxyClient,
    private val baseSchedulers: BaseSchedulers
) : ViewModel() {

    private val _isSearchInputFocused = MutableLiveData(false)
    val isSearchInputFocused: LiveData<Boolean> = _isSearchInputFocused

    private val _searchTextInput = MutableLiveData("")
    val searchTextInput: LiveData<String> = _searchTextInput

    private val _listSuggestions = MutableLiveData<List<Suggestion>>(emptyList())
    val listSuggestions: LiveData<List<Suggestion>> = _listSuggestions

     lateinit var homePublishSubject: PublishSubject<String>
    private var suggestionJob: Job? = null

    fun start() {
        homePublishSubject = PublishSubject.create()
    }

    fun stop() {
        // cleanup if needed
        suggestionJob?.cancel()
    }

    fun changeSearchFocus(isFocus: Boolean) {
        _isSearchInputFocused.value = isFocus
    }

    fun showSuggestions() {
        suggestionJob?.cancel()
        suggestionJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val list = getListSuggestions()
                    .blockingFirst()   // or better approach without blocking
                val trimmed = if (list.size > 50) list.subList(0, 50) else list
                _listSuggestions.postValue(trimmed.toList())
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun onSearchTextChanged(input: String) {
        _searchTextInput.value = input
        if (!(input.startsWith("http://") || input.startsWith("https://"))) {
            showSuggestions()
        }
        homePublishSubject.onNext(input)
    }

    private fun getListSuggestions(): Flowable<List<Suggestion>> {
        return Flowable.combineLatest(
            homePublishSubject.debounce(300, TimeUnit.MILLISECONDS)
                .toFlowable(BackpressureStrategy.LATEST),
            SuggestionsUtils.getSuggestions(
                okHttpClient.getProxyOkHttpClient(),
                _searchTextInput.value ?: ""
            )
        ) { _, suggestions ->
            suggestions.toList()
        }
            .onErrorReturn { emptyList() }
            .take(1)
            .observeOn(baseSchedulers.single)
            .subscribeOn(baseSchedulers.computation)
    }
}
