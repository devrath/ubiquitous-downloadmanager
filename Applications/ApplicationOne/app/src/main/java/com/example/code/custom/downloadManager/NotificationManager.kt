package com.example.code.custom.downloadManager

import android.app.NotificationManager
import android.content.Context

object NotificationManager {
    fun getNotificationManager(context: Context?): NotificationManager? {
        if(NotificationChannelApiLevel.isNotificationChannelValid()){
            context?.let {
                // Create the notification manager
                return it.getSystemService(NotificationManager::class.java)
            }
        }
        return null
    }
}