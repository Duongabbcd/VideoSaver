package com.example.videosaver.utils.advance.util

import android.util.Log
import com.example.videosaver.MyApplication


class AppLogger {

    companion object {

        private const val TAG = MyApplication.DEBUG_TAG

        fun d(message: String) {
            Log.d(TAG, message)
        }

        fun i(message: String) {
            Log.i(TAG, message)
        }

        fun w(message: String) {
            Log.w(TAG, message)
        }

        fun e(message: String) {
            Log.e(TAG, message)
        }
    }
}