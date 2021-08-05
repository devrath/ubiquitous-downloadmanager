package com.example.code.custom

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.code.custom.ProgressNotification.cancelProgressNotification

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        cancelProgressNotification(context)
    }
}