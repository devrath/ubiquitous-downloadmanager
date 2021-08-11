package com.example.code.custom.downloadManager

import android.os.Build

object NotificationChannelApiLevel {

    fun isNotificationChannelValid(): Boolean {
        // Notification channel is present only in Oreo and higher
        // Notification channel is not present in lower API levels
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }

    fun isDownloadManagerEqualOrAboveNougat(): Boolean {
        // certain features of download manager is available for Nougat and higher
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    }

}