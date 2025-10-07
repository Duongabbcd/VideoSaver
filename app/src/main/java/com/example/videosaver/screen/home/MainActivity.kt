package com.example.videosaver.screen.home

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.webkit.URLUtil
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.videosaver.R
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
import com.example.videosaver.screen.home.adapter.TabAdapter
import com.example.videosaver.screen.home.subscreen.DisplayURL
import com.example.videosaver.screen.home.subscreen.process.ProgressFragment
import com.example.videosaver.screen.home.subscreen.saved.SavedFragment
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openFragment(HomeFragment.Companion.newInstance())

        binding.apply {
            selectedTab = 0
            getAllBookmarks()
            
            tabsList.add(Tab("Home", HomeFragment()))
            binding.viewPager.adapter = TabsAdapter(supportFragmentManager, lifecycle)
            binding.viewPager.isUserInputEnabled = false
            myPager = binding.viewPager
            tabsBtn = binding.homeTab
            
            initializeView()


            binding.bottomController.setOnNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.navigation_home -> {
                        selectedTab = 0
                        displayScreen()
                    }

                    R.id.navigation_request -> {
                        selectedTab = 1
                        displayScreen()
                    }

                    R.id.navigation_settings -> {
                        selectedTab = 2
                        displayScreen()
                    }

                    else -> {
                        selectedTab = 0
                        displayScreen()
                    }
                }
                true
            }

        }
    }

    private fun displayScreen() {
       when(selectedTab) {
            0-> {
                openFragment(HomeFragment.Companion.newInstance())
            }
          1-> {
                openFragment(ProgressFragment.Companion.newInstance())
            }
          2-> {
                openFragment(SavedFragment.Companion.newInstance())
            }

           else -> {
               openFragment(HomeFragment.Companion.newInstance())
           }
       }
    }

    private fun initializeView() {

        binding.homeTab.setOnClickListener {
            val viewTabs = layoutInflater.inflate(R.layout.tabs_view, binding.root, false)
            val bindingTabs = TabsViewBinding.bind(viewTabs)

            val dialogTabs =
                MaterialAlertDialogBuilder(this, R.style.roundCornerDialog).setView(viewTabs)
                    .setTitle("Select Tab")
                    .setPositiveButton("Home") { self, _ ->
                        openFragment(HomeFragment.Companion.newInstance())
                        self.dismiss()
                    }
                    .setNeutralButton("Google") { self, _ ->
                        startActivity(Intent(this@MainActivity, BrowseActivity::class.java).apply {
                            putExtra("receivedURL", "https://www.google.com")
                        })
                        self.dismiss()
                    }
                    .create()

            bindingTabs.tabsRV.setHasFixedSize(true)
            bindingTabs.tabsRV.layoutManager = LinearLayoutManager(this)
            bindingTabs.tabsRV.adapter = TabAdapter(dialogTabs)

            dialogTabs.show()

            val pBtn = dialogTabs.getButton(AlertDialog.BUTTON_POSITIVE)
            val nBtn = dialogTabs.getButton(AlertDialog.BUTTON_NEUTRAL)

            pBtn.isAllCaps = false
            nBtn.isAllCaps = false

            pBtn.setTextColor(Color.BLACK)
            nBtn.setTextColor(Color.BLACK)

            pBtn.setCompoundDrawablesWithIntrinsicBounds(
                ResourcesCompat.getDrawable(resources, R.drawable.ic_home, theme), null, null, null
            )
            nBtn.setCompoundDrawablesWithIntrinsicBounds(
                ResourcesCompat.getDrawable(resources, R.drawable.ic_add, theme), null, null, null
            )
        }

        binding.settingBtn.setOnClickListener {

            var frag: BrowseFragment? = null
            try {
                frag =   tabsList[binding.viewPager.currentItem].fragment as BrowseFragment
            } catch (_: Exception) {
            }

            val view = layoutInflater.inflate(R.layout.more_features, binding.root, false)
            val dialogBinding = MoreFeaturesBinding.bind(view)

            val dialog = MaterialAlertDialogBuilder(this).setView(view).create()

            dialog.window?.apply {
                attributes.gravity = Gravity.BOTTOM
                attributes.y = 50
                setBackgroundDrawable(ColorDrawable(0xFFFFFFFF.toInt()))
            }
            dialog.show()

            if (  isFullscreen) {
                dialogBinding.fullscreenBtn.apply {
                    setIconTintResource(R.color.cool_blue)
                    setTextColor(ContextCompat.getColor( this@MainActivity, R.color.cool_blue))
                }
            }

            frag?.let {
                  bookmarkIndex = isBookmarked(it.binding.webView.url!!)
                if (  bookmarkIndex != -1) {

                    dialogBinding.bookmarkBtn.apply {
                        setIconTintResource(R.color.cool_blue)
                        setTextColor(ContextCompat.getColor( this@MainActivity, R.color.cool_blue))
                    }
                }
            }

            if (  isDesktopSite) {
                dialogBinding.desktopBtn.apply {
                    setIconTintResource(R.color.cool_blue)
                    setTextColor(ContextCompat.getColor( this@MainActivity, R.color.cool_blue))
                }
            }



            dialogBinding.backBtn.setOnClickListener {
                onBackPressed()
            }

            dialogBinding.forwardBtn.setOnClickListener {
                frag?.apply {
                    if (binding.webView.canGoForward())
                        binding.webView.goForward()
                }
            }

            dialogBinding.saveBtn.setOnClickListener {
                dialog.dismiss()
//                if (frag != null)
//                    saveAsPdf(web = frag.binding.webView)
//                else Snackbar.make(binding.root, "First Open A WebPage\uD83D\uDE03", 3000).show()
            }

            dialogBinding.fullscreenBtn.setOnClickListener {
                it as MaterialButton
                  isFullscreen = if (  isFullscreen) {
                    changeFullscreen(enable = false)
                    it.setIconTintResource(R.color.black)
                    it.setTextColor(ContextCompat.getColor(this, R.color.black))
                    false
                } else {
                    changeFullscreen(enable = true)
                    it.setIconTintResource(R.color.cool_blue)
                    it.setTextColor(ContextCompat.getColor(this, R.color.cool_blue))
                    true
                }
            }

            dialogBinding.desktopBtn.setOnClickListener {
                it as MaterialButton

                frag?.binding?.webView?.apply {
                      isDesktopSite = if (  isDesktopSite) {
                        settings.userAgentString = null
                        it.setIconTintResource(R.color.black)
                        it.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.black))
                        false
                    } else {
                        settings.userAgentString =
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:99.0) Gecko/20100101 Firefox/99.0"
                        settings.useWideViewPort = true
                        evaluateJavascript(
                            "document.querySelector('meta[name=\"viewport\"]').setAttribute('content'," +
                                    " 'width=1024px, initial-scale=' + (document.documentElement.clientWidth / 1024));",
                            null
                        )
                        it.setIconTintResource(R.color.cool_blue)
                        it.setTextColor(
                            ContextCompat.getColor(
                                 this@MainActivity,
                                R.color.cool_blue
                            )
                        )
                        true
                    }
                    reload()
                    dialog.dismiss()
                }

            }

            dialogBinding.bookmarkBtn.setOnClickListener {
                frag?.let {
                    if (  bookmarkIndex == -1) {
                        val viewB =
                            layoutInflater.inflate(R.layout.bookmark_dialog, binding.root, false)
                        val bBinding = BookmarkDialogBinding.bind(viewB)
                        val dialogB = MaterialAlertDialogBuilder(this)
                            .setTitle("Add Bookmark")
                            .setMessage("Url:${it.binding.webView.url}")
                            .setPositiveButton("Add") { self, _ ->
                                try {
                                    val array = ByteArrayOutputStream()
                                    it.webIcon?.compress(Bitmap.CompressFormat.PNG, 100, array)
                                      bookmarkList.add(
                                        Bookmark(
                                            name = bBinding.bookmarkTitle.text.toString(),
                                            url = it.binding.webView.url!!,
                                            array.toByteArray()
                                        )
                                    )
                                } catch (e: Exception) {
                                      bookmarkList.add(
                                        Bookmark(
                                            name = bBinding.bookmarkTitle.text.toString(),
                                            url = it.binding.webView.url!!
                                        )
                                    )
                                }
                                self.dismiss()
                            }
                            .setNegativeButton("Cancel") { self, _ -> self.dismiss() }
                            .setView(viewB).create()
                        dialogB.show()
                        bBinding.bookmarkTitle.setText(it.binding.webView.title)
                    } else {
                        val dialogB = MaterialAlertDialogBuilder(this)
                            .setTitle("Remove Bookmark")
                            .setMessage("Url:${it.binding.webView.url}")
                            .setPositiveButton("Remove") { self, _ ->
                                  bookmarkList.removeAt(
                                      bookmarkIndex
                                )
                                self.dismiss()
                            }
                            .setNegativeButton("Cancel") { self, _ -> self.dismiss() }
                            .create()
                        dialogB.show()
                    }
                }

                dialog.dismiss()
            }
        }

    }

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


    @SuppressLint("NotifyDataSetChanged")
    override fun onBackPressed() {
//        var frag: BrowseFragment? = null
//        try {
//            frag = tabsList[binding.viewPager.currentItem].fragment as BrowseFragment
//        } catch (_: Exception) {
//        }
//
//        when {
//            frag?.binding?.webView?.canGoBack() == true -> frag.binding.webView.goBack()
//            binding.viewPager.currentItem != 0 -> {
//                tabsList.removeAt(binding.viewPager.currentItem)
//                binding.viewPager.adapter?.notifyDataSetChanged()
//                binding.viewPager.currentItem = tabsList.size - 1
//                binding.homeTab.text = "${tabsList.size - 1}"
//            }
//
//            else -> super.onBackPressed()
//        }
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



    private inner class TabsAdapter(fa: FragmentManager, lc: Lifecycle) :
        FragmentStateAdapter(fa, lc) {
        override fun getItemCount(): Int = tabsList.size

        override fun createFragment(position: Int): Fragment = tabsList[position].fragment
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