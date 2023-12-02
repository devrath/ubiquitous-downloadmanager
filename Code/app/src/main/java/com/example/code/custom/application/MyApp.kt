package com.example.code.custom.application

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.code.custom.Constants.CHANNEL_DESCRIPTION
import com.example.code.custom.Constants.CHANNEL_ID
import com.example.code.custom.Constants.CHANNEL_NAME
import com.example.code.custom.data.DownloadModel
import com.example.code.custom.downloadManager.DownloadUtils.getNotificationManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp : Application() {

    object DownloadData {
        var downloadedData = DownloadModel()
    }

    override fun onCreate() {
        super.onCreate()
        // Set the notification channel
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // Set up the channels
            val channel = NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW
            ).apply {
                // Set properties that applies to all the notifications in this channel
                description = CHANNEL_DESCRIPTION
            }

            // Apply the channel using the notification manager
            getNotificationManager(this)?.apply {
                channel.apply { createNotificationChannel(this) }
            }

        }
    }

}
