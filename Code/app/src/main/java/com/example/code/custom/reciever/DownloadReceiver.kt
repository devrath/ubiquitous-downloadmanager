package com.example.code.custom.reciever

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.code.custom.Constants
import com.example.code.custom.Constants.FILTER_DOWNLOAD_COMPLETE
import com.example.code.custom.Constants.FILTER_DOWNLOAD_PAUSE
import com.example.code.custom.Constants.FILTER_DOWNLOAD_RESUME
import com.example.code.custom.application.MyApp.DownloadData.downloadedData
import com.example.code.custom.downloadManager.DownloadManagerUtils

class DownloadReceiver : BroadcastReceiver() {

    @SuppressLint("Range")
    override fun onReceive(context: Context, intent: Intent) {

        intent.action?.apply {
            when {
                equals(FILTER_DOWNLOAD_PAUSE) -> {
                    downloadedData.isPaused = true
                    downloadedData.status = Constants.PAUSE_STATE
                    DownloadManagerUtils(context = context,data=downloadedData).togglePauseResumeDownload(pauseDownload = true)
                }
                equals(FILTER_DOWNLOAD_RESUME) -> {
                    downloadedData.isPaused = false
                    downloadedData.status = Constants.RESUME_STATE
                    DownloadManagerUtils(context = context,data=downloadedData).togglePauseResumeDownload(pauseDownload = false)
                }
                equals(FILTER_DOWNLOAD_COMPLETE) -> {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    DownloadManagerUtils(context = context,data=downloadedData).downloadManagerComplete(id)
                }
            }
        }

    }

}