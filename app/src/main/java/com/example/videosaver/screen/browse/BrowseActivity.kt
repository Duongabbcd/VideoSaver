package com.example.videosaver.screen.browse

import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import com.example.videosaver.base.BaseActivity
import com.example.videosaver.databinding.ActivityBrowseBinding
import com.example.videosaver.screen.download.DownloadActivity
import com.example.videosaver.screen.home.subscreen.HomeFragment.Companion.checkForInternet
import com.google.android.material.snackbar.Snackbar

class BrowseActivity : BaseActivity<ActivityBrowseBinding>(ActivityBrowseBinding::inflate) {

    private val urlNew by lazy {
        intent.getStringExtra("receivedURL") ?: ""
    }

    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private var originalSystemUiVisibility = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (urlNew.isEmpty()) {
            Toast.makeText(this, "Invalid URL", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.apply {
            homePage.setOnClickListener {
                finish()
            }
            topSearchBar.setText(urlNew)
            setupWebView()
            webView.loadUrl(formatUrl(urlNew))

            refreshBtn.setOnClickListener {
                webView.loadUrl(formatUrl(urlNew))
            }

            downloadBtn.setOnClickListener {
                startActivity(Intent(this@BrowseActivity, DownloadActivity::class.java).apply {
                    putExtra("downloadedURL",   urlNew)
                })
            }

            binding.topSearchBar.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {

                    val query = binding.topSearchBar.text.toString().trim()

                    if (query.isNotEmpty()) {
                        if (checkForInternet(this@BrowseActivity)) {
                            startActivity(Intent(this@BrowseActivity, BrowseActivity::class.java).apply {
                                putExtra("receivedURL",   query)
                            })
                        } else {
                            Snackbar.make(binding.root, "Internet Not Connected 😃", 3000).show()
                        }
                    }

                    true // consume the action
                } else {
                    false
                }
            }
        }
    }

    private fun setupWebView() = binding.webView.apply {
        settings.apply {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            domStorageEnabled = true
            databaseEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            mediaPlaybackRequiresUserGesture = false
            loadsImagesAutomatically = true
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false


            userAgentString = if(urlNew.contains("facebook", true)) {
                DESKTOP_URGENT
            } else {
                MOBILE_URGENT
            }


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
        }

        // For navigation and error handling
        webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding.progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.progressBar.visibility = View.GONE
            }

            override fun onReceivedError(
                view: WebView?, request: WebResourceRequest?, error: WebResourceError?
            ) {
                Log.e("WebViewError", "Error loading page: ${error?.description}")
//                Toast.makeText(context, "Failed to load page", Toast.LENGTH_SHORT).show()
            }
        }

        // Enable fullscreen video support
        webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (customView != null) {
                    callback?.onCustomViewHidden()
                    return
                }

                customView = view
                customViewCallback = callback
                originalSystemUiVisibility = window.decorView.systemUiVisibility

                (window.decorView as FrameLayout).addView(
                    view,
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                )

                window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            }

            override fun onHideCustomView() {
                (window.decorView as FrameLayout).removeView(customView)
                customView = null
                window.decorView.systemUiVisibility = originalSystemUiVisibility
                customViewCallback?.onCustomViewHidden()
            }
        }
    }



    override fun onBackPressed() {
        when {
            customView != null -> binding.webView.webChromeClient?.onHideCustomView()
            binding.webView.canGoBack() -> binding.webView.goBack()
            else -> super.onBackPressed()
        }
    }

    companion object {
        val DESKTOP_URGENT =  "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.5993.90 Safari/537.36"
        val MOBILE_URGENT =   "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.5993.90 Mobile Safari/537.36"

        fun formatUrl(url: String): String {
            return when {
                URLUtil.isValidUrl(url) -> url
                url.contains(".com", ignoreCase = true) -> "https://$url"
                else -> "https://www.google.com/search?q=$url"
            }
        }
    }
}
