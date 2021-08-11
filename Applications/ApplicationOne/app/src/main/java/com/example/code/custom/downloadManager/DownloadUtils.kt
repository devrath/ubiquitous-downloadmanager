package com.example.code.custom.downloadManager

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.database.Cursor
import com.example.code.custom.Constants.DOWNLOAD_STATUS_COMPLETED
import com.example.code.custom.Constants.DOWNLOAD_STATUS_FAILED
import com.example.code.custom.Constants.DOWNLOAD_STATUS_PAUSED
import com.example.code.custom.Constants.DOWNLOAD_STATUS_PENDING
import com.example.code.custom.Constants.DOWNLOAD_STATUS_RUNNING

@SuppressLint("Range")
object DownloadUtils {

    /**
     * Based on the bytes here we calculate bytes in terms of KB. MB, GB, TB which has to be displayed to user
     * @param bytes : -> Bytes that are currently downloaded
     */
    fun bytesIntoHumanReadable(bytes: Long): String {
        val kilobyte: Long = 1024
        val megabyte = kilobyte * 1024
        val gigabyte = megabyte * 1024
        val terabyte = gigabyte * 1024
        return when {
            bytes in 0 until kilobyte -> "$bytes B"
            bytes in kilobyte until megabyte -> (bytes / kilobyte).toString() + " KB"
            bytes in megabyte until gigabyte -> (bytes / megabyte).toString() + " MB"
            bytes in gigabyte until terabyte -> (bytes / gigabyte).toString() + " GB"
            bytes >= terabyte -> (bytes / terabyte).toString() + " TB"
            else -> "$bytes Bytes"
        }
    }

    /**
     * By checking the state of the cursor from the download manager we determine the state of the file in process of download
     * @param cursor : -> Cursor that points to the current state of the download
     */
    fun getStatusMessage(cursor: Cursor): String {
        var msg = "-"
        msg = when (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
            DownloadManager.STATUS_FAILED -> DOWNLOAD_STATUS_FAILED
            DownloadManager.STATUS_PAUSED -> DOWNLOAD_STATUS_PAUSED
            DownloadManager.STATUS_RUNNING -> DOWNLOAD_STATUS_RUNNING
            DownloadManager.STATUS_SUCCESSFUL -> DOWNLOAD_STATUS_COMPLETED
            DownloadManager.STATUS_PENDING -> DOWNLOAD_STATUS_PENDING
            else -> "Unknown"
        }
        return msg
    }

}