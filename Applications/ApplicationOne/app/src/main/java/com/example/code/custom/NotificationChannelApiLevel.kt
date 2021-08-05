package com.example.notification.utils

import android.os.Build

object NotificationChannelApiLevel {

    fun isNotificationChannelValid(): Boolean {
        // Notification channel is present only in Oreo and higher
        // Notification channel is not present in lower API levels
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }
}