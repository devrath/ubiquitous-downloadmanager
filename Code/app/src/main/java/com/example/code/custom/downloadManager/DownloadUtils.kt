package com.example.code.custom.downloadManager

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.NotificationManager
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.os.Environment
import com.example.code.custom.Constants.DOWNLOAD_STATUS_COMPLETED
import com.example.code.custom.Constants.DOWNLOAD_STATUS_FAILED
import com.example.code.custom.Constants.DOWNLOAD_STATUS_PAUSED
import com.example.code.custom.Constants.DOWNLOAD_STATUS_PENDING
import com.example.code.custom.Constants.DOWNLOAD_STATUS_RUNNING
import java.io.File

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

    /**
     *  Get the path where the file needs to be downloaded
     **/
    fun getDownloadPath(context : Context): String {
        return context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()
    }
    /**
     *  Get the URI location of the file downloaded
     **/
    fun getFilePath(context : Context, fileName : String): String {
        return when {
            fileName.isEmpty() -> ""
            else -> context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString().plus("/").plus(fileName)
        }
    }
    /**
     * Check if internal storage is available
     */
    fun isInternalStorageAvailable(context : Context): Boolean {
        return File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()).exists()
    }

    /**
     * Get the notification manager instance
     */
    fun getNotificationManager(context: Context?): NotificationManager? {
        if(isNotificationChannelValid()){
            context?.let {
                // Create the notification manager
                return it.getSystemService(NotificationManager::class.java)
            }
        }
        return null
    }

    private fun isNotificationChannelValid(): Boolean {
        // Notification channel is present only in Oreo and higher
        // Notification channel is not present in lower API levels
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }

    fun isDownloadManagerEqualOrAboveNougat(): Boolean {
        // certain features of download manager is available for Nougat and higher
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    }
}