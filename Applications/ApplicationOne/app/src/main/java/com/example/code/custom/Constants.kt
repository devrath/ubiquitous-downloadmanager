package com.example.code.custom

import android.app.DownloadManager

object Constants {
    const val DOWNLOAD_ID = 6
    const val CHANNEL_6_ID = "channel6"
    const val CHANNEL_6_NAME = "Channel 6"
    const val CHANNEL_6_DESCRIPTION = "This is Channel 6"

    const val DOWNLOAD_CANCEL = "DOWNLOAD_CANCEL"
    const val DOWNLOAD_PAUSE = "DOWNLOAD_PAUSE"
    const val DOWNLOAD_RESUME = "DOWNLOAD_RESUME"

    const val imageURL = "http://speedtest.ftp.otenet.gr/files/test10Mb.db"
    //const val imageURL = "https://mpl-dev-builds.s3.ap-south-1.amazonaws.com/2021-08-05/MPL_com.mpl.androidapp_debug_development_qa_1.0.134_development_qa_20210805_08_57_1000134_134_20210805_08_57_sealed_zip.apk"
    const val pauseState = "PAUSE"
    const val resumeState = "RESUME"
    const val downloadingState = "Downloading"
    const val completedState = "Completed"



    const val FILTER_DOWNLOAD_PAUSE = "com.example.code.DOWNLOAD_PAUSE"
    const val FILTER_DOWNLOAD_RESUME = "com.example.code.DOWNLOAD_RESUME"
    const val FILTER_DOWNLOAD_CANCEL = "com.example.code.DOWNLOAD_CANCEL"
    const val FILTER_DOWNLOAD_COMPLETE = DownloadManager.ACTION_DOWNLOAD_COMPLETE


    private const val UNIQUE_WORK_NAME = "UNIQUE_WORK_NAME"



    const val DOWNLOAD_STATUS_FAILED = "Failed"
    const val DOWNLOAD_STATUS_PAUSED = "Paused"
    const val DOWNLOAD_STATUS_RUNNING = "Running"
    const val DOWNLOAD_STATUS_COMPLETED = "Completed"
    const val DOWNLOAD_STATUS_PENDING = "Pending"


}