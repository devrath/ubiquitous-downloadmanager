package com.example.code.custom

import android.app.DownloadManager

object Constants {

    // ---> Channel constants
    const val CHANNEL_ID = "channel_id"
    const val CHANNEL_NAME = "Channel_name"
    const val CHANNEL_DESCRIPTION = "Channel Description"

    // ---> Download file endpoints
    const val imageURL = "http://speedtest.ftp.otenet.gr/files/test10Mb.db"
    const val pauseState = "PAUSE"
    const val resumeState = "RESUME"
    const val downloadingState = "Downloading"
    const val completedState = "Completed"

    const val FILTER_DOWNLOAD_PAUSE = "com.example.code.DOWNLOAD_PAUSE"
    const val FILTER_DOWNLOAD_RESUME = "com.example.code.DOWNLOAD_RESUME"
    const val FILTER_DOWNLOAD_CANCEL = "com.example.code.DOWNLOAD_CANCEL"
    const val FILTER_DOWNLOAD_COMPLETE = DownloadManager.ACTION_DOWNLOAD_COMPLETE

    const val DOWNLOAD_STATUS_FAILED = "Failed"
    const val DOWNLOAD_STATUS_PAUSED = "Paused"
    const val DOWNLOAD_STATUS_RUNNING = "Running"
    const val DOWNLOAD_STATUS_COMPLETED = "Completed"
    const val DOWNLOAD_STATUS_PENDING = "Pending"

}