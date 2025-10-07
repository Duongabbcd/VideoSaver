package com.example.videosaver.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

object Utils {
    fun Context.hideKeyBoard(editText: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    fun convertIntoFileSize(bytes : Long) : String {
        try {
            if (bytes == 0L) return "0 B"

            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            var size = bytes.toDouble()
            var unitIndex = 0

            while (size >= 1024 && unitIndex < units.lastIndex) {
                size /= 1024
                unitIndex++
            }

            return String.format("%.1f %s", size, units[unitIndex])
        } catch (e: Exception) {
            e.printStackTrace()
            return "Unknown bytes"
        }

    }

    suspend fun getFileSize(url: String): Long = withContext(Dispatchers.IO) {
        println("getFileSize: $url")
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .head()
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                response.header("Content-Length")?.toLongOrNull() ?: 0L
            } else {
                0L
            }
        }
    }

}