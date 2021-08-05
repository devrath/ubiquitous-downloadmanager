package com.example.code.custom.recievers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.code.R
import com.example.code.custom.Constants
import com.example.code.custom.Constants.FILTER_DOWNLOAD_CANCEL
import com.example.code.custom.Constants.FILTER_DOWNLOAD_PAUSE
import com.example.code.custom.Constants.FILTER_DOWNLOAD_RESUME
import com.example.code.custom.DownloadData.downloadedData
import com.example.code.custom.DownloadUtils
import com.example.code.custom.DownloadUtils.togglePauseResumeDownload
import com.example.code.custom.ProgressNotification.cancelProgressNotification

class DownloadReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        intent.action?.apply {
            when {
                equals(FILTER_DOWNLOAD_PAUSE) -> {
                    downloadedData.isPaused = true
                    downloadedData.status = Constants.pauseState
                    togglePauseResumeDownload(context = context,pauseDownload = true)
                }
                equals(FILTER_DOWNLOAD_RESUME) -> {
                    downloadedData.isPaused = false
                    downloadedData.status = Constants.resumeState
                    togglePauseResumeDownload(context = context,pauseDownload = false)
                }
                equals(FILTER_DOWNLOAD_CANCEL) -> cancelProgressNotification(context)
            }
        }

    }

}