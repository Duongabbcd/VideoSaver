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

    fun convertIntoFileSize(input : Long) : String {
        try {
            val unit = 1024
            if (input < unit) return "$input B"
            val exp = (Math.log(input.toDouble()) / Math.log(input.toDouble())).toInt()
            val pre = "KMGTPE"[exp - 1]
            return String.format("%.1f %sB", input / Math.pow(input.toDouble(), exp.toDouble()), pre)
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