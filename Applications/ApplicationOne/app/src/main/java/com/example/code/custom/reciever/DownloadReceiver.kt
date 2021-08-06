package com.example.code.custom.reciever

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.example.code.custom.Constants
import com.example.code.custom.Constants.FILTER_DOWNLOAD_CANCEL
import com.example.code.custom.Constants.FILTER_DOWNLOAD_COMPLETE
import com.example.code.custom.Constants.FILTER_DOWNLOAD_PAUSE
import com.example.code.custom.Constants.FILTER_DOWNLOAD_RESUME
import com.example.code.custom.data.DownloadData.downloadedData
import com.example.code.custom.utils.DownloadUtils.togglePauseResumeDownload
import com.example.code.custom.utils.ProgressNotification.cancelProgressNotification

class DownloadReceiver : BroadcastReceiver() {

    @SuppressLint("Range")
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
                equals(FILTER_DOWNLOAD_COMPLETE) -> {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

                    downloadedData.status = Constants.completedState

                    DownloadManager.Query().apply {
                        setFilterById(id)
                        val downloadManager = context.getSystemService(AppCompatActivity.DOWNLOAD_SERVICE) as DownloadManager
                        downloadManager.query(DownloadManager.Query().setFilterById(id)).apply {
                            moveToFirst()
                            getString(getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)).apply {
                                downloadedData.filePath = this
                            }
                        }
                    }
                }
                equals(FILTER_DOWNLOAD_CANCEL) -> {
                    cancelProgressNotification(context)
                }
            }
        }

    }

}