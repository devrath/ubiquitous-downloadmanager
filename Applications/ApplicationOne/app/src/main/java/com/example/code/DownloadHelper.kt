package com.example.code

import android.app.DownloadManager
import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.example.code.Constants.downloadID
import com.example.code.Constants.url
import java.io.File
import java.util.*

class DownloadHelper(var context: Context) {

     fun beginDownload() {
        var downloadIdentifier = downloadID

        val file = createNewFile(context)

        val request = DownloadManager.Request(Uri.parse(url))
        request.apply {
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN) // Visibility of the download Notification
            setDestinationUri(Uri.fromFile(file)) // Uri of the destination file
            //setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, outputFileName);
            setTitle(file.name) // Title of the Download Notification
            setDescription("Downloading") // Description of the Download Notification
            // setRequiresCharging(false)// Set if charging is required to begin the download
            setAllowedOverMetered(true) // Set if download is allowed on Mobile network
            setAllowedOverRoaming(true) // Set if download is allowed on roaming network
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        }

        var downloadManager: DownloadManager? = null

        downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadIdentifier = downloadManager.enqueue(request) // enqueue puts the download request in the queue.

        // using query method
        var finishDownload = false
        var progress: Int

        while (!finishDownload) {
            val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadIdentifier))
            if (cursor.moveToFirst()) {
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                when (status) {
                    DownloadManager.STATUS_FAILED -> {
                        finishDownload = true
                    }
                    DownloadManager.STATUS_PAUSED -> {
                    }
                    DownloadManager.STATUS_PENDING -> {
                    }
                    DownloadManager.STATUS_RUNNING -> {
                        val total = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        if (total >= 0) {
                            val downloaded = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                            progress = (downloaded * 100L / total).toInt()
                            // if you use downloadmanger in async task, here you can use like this to display progress.
                            // Don't forget to do the division in long to get more digits rather than double.
                            //  publishProgress((int) ((downloaded * 100L) / total));
                        }
                    }
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        progress = 100
                        // if you use aysnc task
                        // publishProgress(100);
                        finishDownload = true
                        Toast.makeText(context, "Download Completed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun createNewFile(context: Context): File {

       /* val directory = File(Environment.getExternalStorageDirectory().toString() + File.separator + "MPL")
        directory.mkdirs()*/

        var fileName = url.substring(url.lastIndexOf('/') + 1)
        fileName = fileName.substring(0, 1).toUpperCase(Locale.ROOT) + fileName.substring(1)

        //val dir: File = context.filesDir
        //val file = File(directory, fileName)
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/MPL/", fileName)

        try {
            Log.d(TAG, "The file path = " + file.absolutePath)
            file.createNewFile()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return file
    }
}