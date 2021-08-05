package com.example.code.custom

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.code.R
import com.example.code.custom.Constants.DOWNLOAD_CANCEL
import com.example.code.custom.Constants.DOWNLOAD_PAUSE
import com.example.code.custom.Constants.DOWNLOAD_RESUME
import com.example.code.custom.Constants.pauseState
import com.example.code.custom.Constants.resumeState
import com.example.code.custom.ProgressNotification.cancelProgressNotification

class NotificationCancelReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        cancelProgressNotification(context)
    }
}