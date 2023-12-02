package com.example.code.custom

import android.app.DownloadManager

object Constants {

    // ---> Download file endpoints
    const val imageURL = "http://speedtest.ftp.otenet.gr/files/test10Mb.db"

    const val DownloadWorkerName = "download_work"

    // ---> Notification channel constants
    const val CHANNEL_ID = "channel_id"
    const val CHANNEL_NAME = "Channel_name"
    const val CHANNEL_DESCRIPTION = "Channel Description"
    // ---> Notification channel constants

    // ---> Broadcast receiver intent filter constants
    const val FILTER_DOWNLOAD_PAUSE = "com.example.code.DOWNLOAD_PAUSE"
    const val FILTER_DOWNLOAD_RESUME = "com.example.code.DOWNLOAD_RESUME"
    const val FILTER_DOWNLOAD_CANCEL = "com.example.code.DOWNLOAD_CANCEL"
    const val FILTER_DOWNLOAD_COMPLETE = DownloadManager.ACTION_DOWNLOAD_COMPLETE
    // ---> Broadcast receiver intent filter constants

    // -----> Don't change the case and spelling of these constants
    const val DOWNLOAD_STATUS_FAILED = "Failed"
    const val DOWNLOAD_STATUS_PAUSED = "Paused"
    const val DOWNLOAD_STATUS_RUNNING = "Running"
    const val DOWNLOAD_STATUS_COMPLETED = "Completed"
    const val DOWNLOAD_STATUS_PENDING = "Pending"
    // -----> Don't change the case and spelling of these constants

    // -----> Constants to track in model data
    const val PAUSE_STATE = "PAUSE"
    const val RESUME_STATE = "RESUME"
    const val DOWNLOADING_STATE = "DOWNLOADING"
    const val COMPLETED_STATE = "COMPLETED"
    // -----> Constants to track in model data

}