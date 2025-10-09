package com.example.videosaver.utils.advance.util

import android.content.Context
import android.webkit.CookieManager
import android.widget.Toast
import javax.inject.Inject
import com.example.videosaver.R


//@OpenForTesting
class SystemUtil @Inject constructor() {

    fun clearCookies(context: Context?) {
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
        context?.let {
            Toast.makeText(it, it.getString(R.string.cookies_cleared), Toast.LENGTH_SHORT)
                .show()
        }
    }
}