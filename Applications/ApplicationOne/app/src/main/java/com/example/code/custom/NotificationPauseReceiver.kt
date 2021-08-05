package com.example.code.custom

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.code.R
import com.example.code.custom.DownloadData.downloadedData

class NotificationPauseReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        downloadedData.isIs_paused = true
        downloadedData.status = Constants.pauseState

        if (!DownloadUtils.pauseDownload(context)) {
            Toast.makeText(context, context.getString(R.string.str_failed_to_pause), Toast.LENGTH_SHORT).show()
        }
    }
}