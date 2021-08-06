package com.example.code.custom.utils

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.example.code.custom.Constants.DOWNLOAD_STATUS_COMPLETED
import com.example.code.custom.Constants.DOWNLOAD_STATUS_FAILED
import com.example.code.custom.Constants.DOWNLOAD_STATUS_PAUSED
import com.example.code.custom.Constants.DOWNLOAD_STATUS_PENDING
import com.example.code.custom.Constants.DOWNLOAD_STATUS_RUNNING
import com.example.code.custom.data.DownloadData.downloadedData

@SuppressLint("Range")
object DownloadUtils {

    private const val downloadUri = "content://downloads/my_downloads"

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

    fun togglePauseResumeDownload(context: Context,pauseDownload:Boolean): Boolean {
        var updatedRow = 0
        val contentValues = ContentValues()
        when {
            pauseDownload -> contentValues.put("control", 1)
            else -> contentValues.put("control", 0)
        }
        try {
            updatedRow = context.contentResolver.update(
                    Uri.parse(downloadUri),
                    contentValues,
                    "title=?",
                    arrayOf(downloadedData.title)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0 < updatedRow
    }

}