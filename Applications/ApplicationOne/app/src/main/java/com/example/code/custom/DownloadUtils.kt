package com.example.code.custom

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri

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
            DownloadManager.STATUS_FAILED -> "Failed"
            DownloadManager.STATUS_PAUSED -> "Paused"
            DownloadManager.STATUS_RUNNING -> "Running"
            DownloadManager.STATUS_SUCCESSFUL -> "Completed"
            DownloadManager.STATUS_PENDING -> "Pending"
            else -> "Unknown"
        }
        return msg
    }

    fun pauseDownload(context: Context, downloadModel: DownloadModel): Boolean {
        var updatedRow = 0
        val contentValues = ContentValues()
        contentValues.put("control", 1)
        try {
            updatedRow = context.contentResolver.update(
                Uri.parse(downloadUri),
                contentValues,
                "title=?",
                arrayOf(downloadModel.title)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0 < updatedRow
    }

    fun resumeDownload(context: Context, downloadModel: DownloadModel): Boolean {
        var updatedRow = 0
        val contentValues = ContentValues()
        contentValues.put("control", 0)
        try {
            updatedRow = context.contentResolver.update(
                Uri.parse(downloadUri),
                contentValues,
                "title=?",
                arrayOf(downloadModel.title)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0 < updatedRow
    }

}