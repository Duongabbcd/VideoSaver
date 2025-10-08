package com.example.videosaver.advance.ui.browser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.ServiceWorkerClient
import android.webkit.ServiceWorkerController
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.videosaver.advance.BrowserServicesProvider
import com.example.videosaver.advance.ui.browser.detectedVideos.GlobalVideoDetectionModel
import com.example.videosaver.advance.ui.browser.hometab.BrowserHomeFragment
import com.example.videosaver.advance.ui.browser.webtab.WebTabFragment
import com.example.videosaver.advance.ui.setting.SettingsViewModel
import com.example.videosaver.advance.ui.webtab.WebTab
import com.example.videosaver.base.BaseFragment
import com.example.videosaver.databinding.FragmentBrowseBinding
import com.example.videosaver.screen.home.MainViewModel
import com.example.videosaver.utils.SingleLiveEvent
import com.example.videosaver.utils.advance.proxy_utils.OkHttpProxyClient
import com.example.videosaver.utils.advance.util.AppLogger
import com.example.videosaver.utils.advance.util.ContentType
import com.example.videosaver.utils.advance.util.CookieUtils
import com.example.videosaver.utils.advance.util.VideoUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject



const val HOME_TAB_INDEX = 0

const val TAB_INDEX_KEY = "TAB_INDEX_KEY"

class BrowserFragment : BaseFragment<FragmentBrowseBinding>(FragmentBrowseBinding::inflate), BrowserServicesProvider {
    private val settingsModel : SettingsViewModel by viewModels()
    private val browserViewModel : BrowserViewModel by viewModels()
    private val mainViewModel : MainViewModel by viewModels()

    private lateinit var videoDetectionModel: GlobalVideoDetectionModel
    @Inject
    lateinit var okHttpProxyClient: OkHttpProxyClient

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var tabsAdapter: TabsFragmentStateAdapter

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        AppLogger.d("Permissions for writing isGranted: $isGranted")
    }

    private val serviceWorkerClient = object : ServiceWorkerClient() {
        override fun shouldInterceptRequest(request: WebResourceRequest): WebResourceResponse? {
            val url = request.url.toString()

            val isM3u8Check = settingsModel.isCheckIfEveryRequestOnM3u8.get()
            val isMp4Check = settingsModel.getIsCheckEveryRequestOnMp4Video().get()
            val isCheckOnAudio = settingsModel.isCheckOnAudio.get()

            if (isM3u8Check || isMp4Check) {
                val requestWithCookies = request.let { resourceRequest ->
                    try {
                        CookieUtils.webRequestToHttpWithCookies(
                            resourceRequest
                        )
                    } catch (_: Throwable) {
                        null
                    }
                }

                val contentType = VideoUtils.getContentTypeByUrl(
                    url, requestWithCookies?.headers, okHttpProxyClient
                )

                if (contentType == ContentType.MPD || contentType == ContentType.M3U8 || url.contains(
                        ".m3u8"
                    ) || url.contains(
                        ".mpd"
                    ) || url.contains(".txt")
                ) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        if (requestWithCookies != null && isM3u8Check) {
                            videoDetectionModel.verifyLinkStatus(requestWithCookies, "", true)
                        }
                    }
                } else if (contentType == ContentType.VIDEO && isMp4Check || contentType == ContentType.AUDIO && isCheckOnAudio) {
                    videoDetectionModel.checkRegularVideoOrAudio(requestWithCookies, isCheckOnAudio, isMp4Check)
                }
            }

            return super.shouldInterceptRequest(request)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val swController = ServiceWorkerController.getInstance()
        swController.setServiceWorkerClient(serviceWorkerClient)
        swController.serviceWorkerWebSettings.allowContentAccess = true

        videoDetectionModel =
            ViewModelProvider(this, viewModelFactory)[GlobalVideoDetectionModel::class.java]
        tabsAdapter = TabsFragmentStateAdapter(emptyList())
        binding.apply {
            viewPager.adapter = tabsAdapter
            viewPager.setSwipeThreshold(500)
            viewPager.isUserInputEnabled = false
            viewPager.setOnGoThroughListener(onGoThroughListener)

            browserViewModel.start()
            handlePressWebTabEvent()
            handleOpenTabEvent()
            handleCloseWebTabEventEvent()
            handleUpdateWebTabEventEvent()
        }
    }

    override fun shareWebLink() {

    }

    override fun bookmarkCurrentUrl() {

    }


    inner class TabsFragmentStateAdapter(private var webTabsRoutes: List<WebTab>) :
        FragmentStateAdapter(this) {
        fun setRoutes(newRoutes: List<WebTab>) {
            webTabsRoutes = newRoutes

            notifyDataSetChanged()
        }

        override fun getItemCount(): Int = webTabsRoutes.size

        override fun getItemId(position: Int): Long {
            return webTabsRoutes[position].id.hashCode().toLong()
        }

        override fun containsItem(itemId: Long): Boolean {
            val webTab = webTabsRoutes.find { it.id.hashCode().toLong() == itemId }
            return webTab != null
        }

        override fun createFragment(position: Int): Fragment {
            if (position == HOME_TAB_INDEX) {
                return createHomeTabFragment()
            }

            return createTabFragment(position)
        }
    }

    private fun createHomeTabFragment(): Fragment {
        return BrowserHomeFragment.newInstance()
    }

    private fun createTabFragment(index: Int): Fragment {
        val fragment = WebTabFragment.newInstance().apply {
            val args = Bundle().apply {
                putInt(TAB_INDEX_KEY, index)
            }
            arguments = args
        }

        return fragment
    }

    override fun getOpenTabEvent(): SingleLiveEvent<WebTab> {
        return browserViewModel.openPageEvent
    }

    override fun getCloseTabEvent(): SingleLiveEvent<WebTab> {
        return browserViewModel.closePageEvent

    }

    override fun getUpdateTabEvent(): SingleLiveEvent<WebTab> {
        return browserViewModel.updateWebTabEvent
    }

    override fun getTabsListChangeEvent(): ObservableField<List<WebTab>> {
        return browserViewModel.tabs
    }

    override fun getPageTab(position: Int): WebTab {
        val list = browserViewModel.tabs.get() ?: listOf(WebTab.HOME_TAB)
        if (position in list.indices) {
            return list[position]
        }
        return WebTab("error", "error")
    }


    override fun getWorkerM3u8MpdEvent(): MutableLiveData<DownloadButtonState> {
        return browserViewModel.workerM3u8MpdEvent    }

    override fun getWorkerMP4Event(): MutableLiveData<DownloadButtonState> {
        return browserViewModel.workerMP4Event
    }

    override fun getCurrentTabIndex(): ObservableInt {
        return browserViewModel.currentTab

    }

    private fun handlePressWebTabEvent() {
        browserViewModel.selectWebTabEvent.observe(viewLifecycleOwner) { webTab ->
            val index = browserViewModel.tabs.get()?.indexOf(webTab) ?: 0
            browserViewModel.currentTab.set(index.coerceAtLeast(0))
        }
    }

    // TODO: Show dialog with variants: "Open in New Tab", "Load in Current Tab", "Block", "Don't show again"
    private fun handleOpenTabEvent() {
        browserViewModel.openPageEvent.observe(viewLifecycleOwner) { webTab ->
            val newList = browserViewModel.tabs.get()?.plus(webTab) ?: emptyList()
            browserViewModel.tabs.set(newList)
            val index = newList.indexOf(webTab)
            browserViewModel.currentTab.set(index.coerceAtLeast(0))
        }
    }

    private fun handleCloseWebTabEventEvent() {
        browserViewModel.closePageEvent.observe(viewLifecycleOwner) { webTab ->
            val tabs =
                browserViewModel.tabs.get()?.toMutableList() ?: mutableListOf(WebTab.HOME_TAB)
            val tabToClose = tabs.find { it.id == webTab.id }
            val index = tabs.indexOf(tabToClose)
            if (index in tabs.indices && index != HOME_TAB_INDEX) {
                tabs.removeAt(index)
            }

            if (browserViewModel.currentTab.get() == index) {
                val newIndex = (index - 1).coerceAtLeast(0)
                browserViewModel.currentTab.set(newIndex)
            }

            browserViewModel.tabs.set(tabs)
        }
    }

    private fun handleUpdateWebTabEventEvent() {
        browserViewModel.updateWebTabEvent.observe(viewLifecycleOwner) { webTab ->
            val tabs = browserViewModel.tabs.get()?.toMutableList()
            val tabToUpdate = tabs?.find { it.id == webTab.id }
            val updateIndex = tabs?.indexOf(tabToUpdate)

            if (updateIndex != null && updateIndex in tabs.indices) {
                tabs[updateIndex] = webTab
            }

            browserViewModel.tabs.set(tabs ?: emptyList())
        }
    }


    private val onGoThroughListener = object : OnGoThroughListener {
        override fun onRightGoThrough() {
            val currentTabIndex = browserViewModel.currentTab.get()
            if (currentTabIndex == 0) {
                mainViewModel.currentItem.set((mainViewModel.currentItem.get() ?: 0) + 1)
            }
        }
    }


    companion object {
        fun newInstance() = BrowserFragment()
        var DESKTOP_USER_AGENT =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36"

        // TODO different agents for different androids
        var MOBILE_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 12; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Mobile Safari/537.36"
    }


}