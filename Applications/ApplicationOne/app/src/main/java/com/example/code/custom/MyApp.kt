package com.example.code.custom

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.code.custom.Constants.CHANNEL_6_DESCRIPTION
import com.example.code.custom.Constants.CHANNEL_6_ID
import com.example.code.custom.Constants.CHANNEL_6_NAME
import com.example.code.custom.NotificationManager.getNotificationManager

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Set up the channels
            val channel6 = NotificationChannel(
                    CHANNEL_6_ID, CHANNEL_6_NAME,
                    NotificationManager.IMPORTANCE_LOW
            ).apply {
                // Set properties that applies to all the notifications in this channel
                description = CHANNEL_6_DESCRIPTION
            }

            getNotificationManager(this)?.apply {
                channel6.apply { createNotificationChannel(this) }
            }

        }
    }

}
