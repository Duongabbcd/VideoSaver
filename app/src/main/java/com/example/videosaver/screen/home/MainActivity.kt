package com.example.videosaver.screen.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Window
import android.webkit.URLUtil
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.videosaver.R
import com.example.videosaver.advance.ui.proxies.ProxiesViewModel
import com.example.videosaver.advance.ui.setting.SettingsViewModel
import com.example.videosaver.base.BaseActivity2
import com.example.videosaver.databinding.ActivityMainBinding
import com.example.videosaver.remote.model.Bookmark
import com.example.videosaver.remote.model.Tab
import com.google.android.material.textview.MaterialTextView
import com.google.gson.GsonBuilder
import com.example.videosaver.screen.download.DownloadActivity
import com.example.videosaver.screen.home.adapter.MainAdapter
import com.example.videosaver.screen.home.subscreen.DisplayURL
import com.example.videosaver.utils.advance.fragment.FragmentFactory
import com.example.videosaver.utils.advance.scheduler.BaseSchedulers
import com.example.videosaver.utils.advance.util.SharedPrefHelper
import com.example.videosaver.utils.advance.util.downloaders.youtubedl_downloader.YoutubeDlDownloaderWorker
import com.google.gson.reflect.TypeToken
import javax.inject.Inject


class MainActivity : BaseActivity2(){
    @Inject
    lateinit var fragmentFactory: FragmentFactory

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var baseSchedulers: BaseSchedulers

    @Inject
    lateinit var sharedPrefHelper: SharedPrefHelper

    lateinit var mainViewModel: MainViewModel

    lateinit var proxiesViewModel: ProxiesViewModel

    lateinit var settingsViewModel: SettingsViewModel

    private lateinit var dataBinding: ActivityMainBinding

    private lateinit var mainAdapter: MainAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)

        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)


        mainViewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
        proxiesViewModel = ViewModelProvider(this, viewModelFactory)[ProxiesViewModel::class.java]
        settingsViewModel = ViewModelProvider(this, viewModelFactory)[SettingsViewModel::class.java]

        mainAdapter = MainAdapter(supportFragmentManager, lifecycle, fragmentFactory)

        dataBinding.viewPager.isUserInputEnabled = false
        dataBinding.viewPager.adapter = mainAdapter
        dataBinding.viewPager.registerOnPageChangeCallback(onPageChangeListener)
        dataBinding.bottomController.setOnItemSelectedListener { menuItem ->
            val isBrowser = mainViewModel.currentItem.get() == 0
            var goingToBrowser = false
            when (menuItem.itemId) {
                R.id.tab_browser -> {
                    mainViewModel.currentItem.set(0)
                    goingToBrowser = true
                }

                R.id.tab_progress -> mainViewModel.currentItem.set(1)
                R.id.tab_video -> mainViewModel.currentItem.set(2)
                else -> mainViewModel.currentItem.set(3)
            }

            if (isBrowser && goingToBrowser && mainViewModel.isBrowserCurrent.get()) {
                mainViewModel.openNavDrawerEvent.call()
            }
            return@setOnItemSelectedListener true
        }
        dataBinding.viewModel = mainViewModel

        grantPermissions()
        proxiesViewModel.start()
        settingsViewModel.start()
        mainViewModel.start()

        if (intent.action == Intent.ACTION_VIEW) {
            val videoUrl = intent.dataString
            if (videoUrl != null) {
                mainViewModel.openedUrl.set(videoUrl)
            }
        }

        if (intent.action == Intent.ACTION_SEND) {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (sharedText != null) {
                mainViewModel.openedText.set(sharedText)
            }
        }

        handleScreenOrientationSettingChange()
        handleScreenOrientationSettingsInit()

        onNewIntent(intent)
    }

    override fun onResume() {
        super.onResume()
//        Handler(Looper.getMainLooper()).postDelayed({
//            checkClipboardForUrl()
//        }, 300)

    }

    private fun checkClipboardForUrl() {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboard.primaryClip
        println("clipData: ${clipData == null} and ${clipData?.itemCount}")
        if (clipData != null && clipData.itemCount > 0) {
            val copiedText = clipData.getItemAt(0).coerceToText(this).toString()

            if (URLUtil.isValidUrl(copiedText)) {

                if(copiedText.contains("facebook", true) || copiedText.contains("tiktok", true)) {
                    startActivity(Intent(this@MainActivity, DownloadActivity::class.java).apply {
                        putExtra("downloadedURL", copiedText)
                    })
                } else {
                    val fragment = supportFragmentManager.findFragmentById(R.id.frameLayout)
                    if (fragment is DisplayURL) {
                        fragment.onReceivedURL(copiedText)

                    }
                }

                // Clear clipboard after showing it
                // Clear content
                 clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    fun isBookmarked(url: String): Int {
        bookmarkList.forEachIndexed { index, bookmark ->
            if (bookmark.url == url) return index
        }
        return -1
    }

    fun saveBookmarks() {
        //for storing bookmarks data using shared preferences
        val editor = getSharedPreferences("BOOKMARKS", MODE_PRIVATE).edit()

        val data = GsonBuilder().create().toJson(bookmarkList)
        editor.putString("bookmarkList", data)

        editor.apply()
    }

    private fun getAllBookmarks() {
        //for getting bookmarks data using shared preferences from storage
      bookmarkList = ArrayList()
        val editor = getSharedPreferences("BOOKMARKS", MODE_PRIVATE)
        val data = editor.getString("bookmarkList", null)

        if (data != null) {
            val list: java.util.ArrayList<Bookmark> = GsonBuilder().create()
                .fromJson(data, object : TypeToken<java.util.ArrayList<Bookmark>>() {}.type)
           bookmarkList.addAll(list)
            bookmarkList.addAll(defaultList)
        } else {
            // add default bookmarks
            bookmarkList.addAll(defaultList)
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.getBooleanExtra(
                YoutubeDlDownloaderWorker.IS_FINISHED_DOWNLOAD_ACTION_KEY,
                false
            ) == true
        ) {
            if (intent.getBooleanExtra(
                    YoutubeDlDownloaderWorker.IS_FINISHED_DOWNLOAD_ACTION_ERROR_KEY,
                    false
                )
            ) {
                dataBinding.viewPager.currentItem = 1
            } else {
                dataBinding.viewPager.currentItem = 2
            }

            if (intent.hasExtra(YoutubeDlDownloaderWorker.DOWNLOAD_FILENAME_KEY)) {
                val downloadFileName =
                    intent.getStringExtra(YoutubeDlDownloaderWorker.DOWNLOAD_FILENAME_KEY)
                        .toString()

                Handler(Looper.getMainLooper()).postDelayed({
                    mainViewModel.openDownloadedVideoEvent.value = downloadFileName
                }, 1000)
            }
        } else {
            if (intent?.hasExtra(YoutubeDlDownloaderWorker.IS_FINISHED_DOWNLOAD_ACTION_KEY) == true) {
                dataBinding.viewPager.currentItem = 1
            } else {
                dataBinding.viewPager.currentItem = 0
            }
        }
    }

    private fun grantPermissions() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.POST_NOTIFICATIONS
                    ),
                    0
                )
            }
        }
    }

    private val onPageChangeListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrollStateChanged(p0: Int) {
        }

        override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
        }

        override fun onPageSelected(postion: Int) {
            if (postion == 0) {
                // Если без этого, дровер отркрываетс когда не надо
                Handler(Looper.getMainLooper()).postDelayed({
                    mainViewModel.isBrowserCurrent.set(true)
                }, 1000)
            } else {
                mainViewModel.isBrowserCurrent.set(false)
            }

            val childrenCount = dataBinding.frameLayout.childCount
            if (childrenCount > 0) {
                supportFragmentManager.popBackStack()
            }
            if (postion > 0) {
                dataBinding.viewPager.isUserInputEnabled = true
            } else {
                dataBinding.viewPager.isUserInputEnabled = false
            }

            mainViewModel.currentItem.set(postion)
        }
    }

    override fun onDestroy() {
        mainViewModel.stop()
        super.onDestroy()
    }

    private fun handleScreenOrientationSettingsInit() {
        // INIT
        requestedOrientation = if (settingsViewModel.isLockPortrait.get()) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    private fun handleScreenOrientationSettingChange() {
        // CHANGES HANDLING
        settingsViewModel.isLockPortrait.addOnPropertyChangedCallback(object :
            Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                val isLock = settingsViewModel.isLockPortrait.get()

                requestedOrientation = if (isLock) {
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
            }
        })
    }



    companion object {
        private val TAG = MainActivity::class.java.simpleName
        var selectedTab = 0
        private val defaultList = listOf<Bookmark>(
//            Bookmark("Google", "https://www.google.com", null, R.drawable.icon_google),
//            Bookmark("Youtube", "https://youtube.com", null, R.drawable.icon_youtube),
            Bookmark("Facebook", "https://www.facebook.com/", null, R.drawable.icon_facebook),
            Bookmark("Instagram", "https://www.instagram.com/", null, R.drawable.icon_instagram),
            Bookmark("TikTok", "https://www.tiktok.com/", null, R.drawable.icon_tiktok),
            Bookmark("Twitter (X)", "https://x.com", null, R.drawable.icon_twitter),
            Bookmark("Dailymotion", "https://www.dailymotion.com/", null, R.drawable.icon_dailymotion),
            Bookmark("Pinterest", "https://www.pinterest.com/", null, R.drawable.icon_pinterest)
        )

        var isChangeTheme = false
        var tabsList: java.util.ArrayList<Tab> = ArrayList()
        private var isFullscreen: Boolean = true
        var isDesktopSite: Boolean = false
        var bookmarkList: java.util.ArrayList<Bookmark> = ArrayList()
        var bookmarkIndex: Int = -1
        lateinit var myPager: ViewPager2
        lateinit var tabsBtn: MaterialTextView
    }
}