package com.example.videosaver.advance.ui.proxies

import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.example.videosaver.advance.data.local.model.Proxy
import com.example.videosaver.base.BaseViewModel
import com.example.videosaver.utils.advance.proxy_utils.CustomProxyController
import com.example.videosaver.utils.advance.scheduler.BaseSchedulers
import com.example.videosaver.utils.advance.util.SharedPrefHelper
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProxiesViewModel @Inject constructor(
    private val proxyController: CustomProxyController,
    private val baseSchedulers: BaseSchedulers,
    private val sharedPrefHelper: SharedPrefHelper
) : BaseViewModel() {
    val currentProxy = ObservableField(Proxy.noProxy())

    val userProxy = ObservableField(Proxy.noProxy())

    val proxiesList: ObservableField<MutableList<Proxy>> = ObservableField(mutableListOf())

    val isProxyOn = ObservableField(false)

    private var compositeDisposable = CompositeDisposable()

    override fun start() {
        if (compositeDisposable.size() >= 1) {
            compositeDisposable.dispose()
            compositeDisposable = CompositeDisposable()
        }

        fetchProxies()
        viewModelScope.launch(Dispatchers.IO) {
            userProxy.set(sharedPrefHelper.getUserProxy())
            currentProxy.set(proxyController.getCurrentSavedProxy())
            isProxyOn.set(proxyController.isProxyOn())
        }
    }

    private fun fetchProxies() {
        val disposable = proxyController.fetchUserProxy().subscribeOn(baseSchedulers.io)
            .observeOn(baseSchedulers.computation).subscribe {
                proxiesList.set(listOf(it).toMutableList())
            }
        compositeDisposable.add(disposable)
    }

    override fun stop() {
        compositeDisposable.clear()
    }


    fun setProxy(proxy: Proxy) {
        proxyController.setCurrentProxy(proxy)
        currentProxy.set(proxy)
        isProxyOn.set(true)

        refreshList()
    }

    fun turnOffProxy() {
        proxyController.setIsProxyOn(false)
        isProxyOn.set(false)
    }

    fun turnOnProxy() {
        proxyController.setIsProxyOn(true)
        isProxyOn.set(true)
    }

    private fun refreshList() {
        val refreshed = proxiesList.get()?.toMutableList()
        proxiesList.set(refreshed)
    }

    fun setUserProxy(userProxy: Proxy) {
        viewModelScope.launch(Dispatchers.IO) {
            this@ProxiesViewModel.userProxy.set(userProxy)
            sharedPrefHelper.saveUserProxy(userProxy)
            proxyController.setCurrentProxy(userProxy)
        }
    }
}