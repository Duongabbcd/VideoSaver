package com.example.videosaver.screen.home

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.webkit.URLUtil
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.Observable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.videosaver.R
import com.example.videosaver.advance.ui.proxies.ProxiesViewModel
import com.example.videosaver.advance.ui.setting.SettingsViewModel
import com.example.videosaver.base.BaseActivity
import com.example.videosaver.databinding.ActivityMainBinding
import com.example.videosaver.databinding.BookmarkDialogBinding
import com.example.videosaver.remote.model.Bookmark
import com.example.videosaver.remote.model.Tab
import com.example.videosaver.screen.home.subscreen.advance.BrowseFragment
import com.example.videosaver.screen.home.subscreen.HomeFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.example.videosaver.databinding.MoreFeaturesBinding
import com.example.videosaver.databinding.TabsViewBinding
import com.example.videosaver.screen.browse.BrowseActivity
import com.example.videosaver.screen.download.DownloadActivity
import com.example.videosaver.screen.home.adapter.MainAdapter
import com.example.videosaver.screen.home.adapter.TabAdapter
import com.example.videosaver.screen.home.subscreen.DisplayURL
import com.example.videosaver.screen.home.subscreen.process.ProgressFragment
import com.example.videosaver.screen.home.subscreen.saved.SavedFragment
import com.example.videosaver.utils.advance.fragment.FragmentFactory
import com.example.videosaver.utils.advance.util.SharedPrefHelper
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlin.text.get
import kotlin.text.set

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate){
    private lateinit var mainAdapter: MainAdapter
     val mainViewModel: MainViewModel by viewModels()
     val proxiesViewModel: ProxiesViewModel by viewModels()
     val settingsViewModel: SettingsViewModel by viewModels()
    @Inject
    lateinit var fragmentFactory: FragmentFactory

    @Inject
    lateinit var sharedPrefHelper: SharedPrefHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        openFragment(HomeFragment.Companion.newInstance())
        binding.lifecycleOwner = this  // so LiveData updates propagate to the UI
        mainAdapter = MainAdapter(supportFragmentManager, lifecycle, fragmentFactory)
        binding.apply {
            selectedTab = 0

            getAllBookmarks()
            
//            tabsList.add(Tab("Home", HomeFragment()))
            binding.viewPager.adapter = mainAdapter
            binding.viewPager.isUserInputEnabled = false
            binding.viewPager.registerOnPageChangeCallback(onPageChangeListener)
            myPager = binding.viewPager
            mainViewModel.offScreenPageLimit.observe(this@MainActivity) { limit ->
                Log.d("MainActivity", "Setting offscreenPageLimit to $limit")
                val validLimit = if (limit == null || limit == 0) ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT else limit
                binding.viewPager.offscreenPageLimit = validLimit
            }


            binding.bottomController.setOnItemSelectedListener { menuItem ->
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
            mainViewModel.start()
        }
    }

//    private fun displayScreen() {
//       when(selectedTab) {
//            0-> {
//                openFragment(HomeFragment.Companion.newInstance())
//            }
//          1-> {
//                openFragment(ProgressFragment.Companion.newInstance())
//            }
//          2-> {
//                openFragment(SavedFragment.Companion.newInstance())
//            }
//
//           else -> {
//               openFragment(HomeFragment.Companion.newInstance())
//           }
//       }
//    }


    override fun onResume() {
        super.onResume()
        Handler(Looper.getMainLooper()).postDelayed({
            checkClipboardForUrl()
        }, 300)  // 300ms delay often helps clipboard availability

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

            val childrenCount = binding.frameLayout.childCount
            if (childrenCount > 0) {
                supportFragmentManager.popBackStack()
            }
            if (postion > 0) {
                //previous: true
                binding.viewPager.isUserInputEnabled = false
            } else {
                binding.viewPager.isUserInputEnabled = false
            }

            mainViewModel.currentItem.set(postion)
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    override fun onDestroy() {
        mainViewModel.stop()
        super.onDestroy()
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

//    private fun handleScreenOrientationSettingsInit() {
//
//        requestedOrientation = if (settingsViewModel.isLockPortrait.get()) {
//            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//        } else {
//            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
//        }
//    }
//
//    private fun handleScreenOrientationSettingChange() {
//
//        settingsViewModel.isLockPortrait.addOnPropertyChangedCallback(object :
//            Observable.OnPropertyChangedCallback() {
//            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
//                val isLock = settingsViewModel.isLockPortrait.get()
//
//                requestedOrientation = if (isLock) {
//                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//                } else {
//                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
//                }
//            }
//        })
//    }




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