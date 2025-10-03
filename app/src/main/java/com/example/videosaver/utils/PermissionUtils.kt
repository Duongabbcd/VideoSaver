package com.example.videosaver.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object PermissionUtils {
    fun getMissingMediaPermissions(context: Context): List<String> {
        val missing = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val mediaPermissions = listOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
            )

            mediaPermissions.forEach { permission ->
                if (!isPermissionGranted(context, permission)) {
                    missing.add(permission)
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isPermissionGranted(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                missing.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                if (!isPermissionGranted(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    missing.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
        }

        return missing
    }

    private fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}