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
 
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

   // if you're using Hilt
   class BrowserHomeViewModel @Inject constructor(
       private val okHttpClient: OkHttpProxyClient,
       private val baseSchedulers: BaseSchedulers,
   ) :
       BaseViewModel() {
       val isSearchInputFocused = ObservableBoolean(false)
       val searchTextInput = ObservableField("")
       val listSuggestions: ObservableField<MutableList<Suggestion>> = ObservableField(mutableListOf())

       lateinit var homePublishSubject: PublishSubject<String>

       private var suggestionJob: Job? = null

       override fun start() {
           homePublishSubject = PublishSubject.create()
       }

       override fun stop() {

       }

       fun changeSearchFocus(isFocus: Boolean) {
           this.isSearchInputFocused.set(isFocus)
       }

       fun showSuggestions() {
           if (suggestionJob != null && suggestionJob?.isActive == true) {
               suggestionJob?.cancel()
           }
           suggestionJob = viewModelScope.launch(Dispatchers.IO) {
               try {
                   withContext(this.coroutineContext) {
                       val list = getListSuggestions().blockingFirst()
                       if (list.size > 50) {
                           listSuggestions.set(list.subList(0, 50).toMutableList())
                       } else {
                           listSuggestions.set(list.toMutableList())
                       }
                   }
               } catch (e: Throwable) {
                   e.printStackTrace()
               }
           }
       }

       private fun getListSuggestions(): Flowable<List<Suggestion>> {
           return Flowable.combineLatest(
               homePublishSubject.debounce(300, TimeUnit.MILLISECONDS)
                   .toFlowable(BackpressureStrategy.LATEST), SuggestionsUtils.getSuggestions(
                   okHttpClient.getProxyOkHttpClient(), searchTextInput.get() ?: ""
               )
           ) { _, suggestions ->
               val listSuggestions = mutableListOf<Suggestion>()
               listSuggestions.addAll(suggestions)
               listSuggestions.toList()
           }.onErrorReturn {
               emptyList()
           }.take(1).observeOn(baseSchedulers.single)
               .subscribeOn(baseSchedulers.computation)
       }
   }