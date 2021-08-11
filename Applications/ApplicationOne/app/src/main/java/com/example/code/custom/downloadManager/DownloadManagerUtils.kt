package com.example.code.custom.downloadManager

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import com.example.code.custom.Constants
import com.example.code.custom.application.MyApp
import com.example.code.custom.data.DownloadModel

@SuppressLint("Range")
class DownloadManagerUtils(var context: Context,var data: DownloadModel) {

    private val downloadUri = "content://downloads/my_downloads"

    fun downloadManagerComplete(id : Long) {
        DownloadManager.Query().apply {
            setFilterById(id)
            val downloadManager = context.getSystemService(AppCompatActivity.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.query(DownloadManager.Query().setFilterById(id)).apply {
                moveToFirst()
                getString(getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)).apply {
                    data.filePath = this
                    data.status = Constants.completedState
                }
            }
        }
    }

    fun togglePauseResumeDownload(pauseDownload:Boolean): Boolean {
        var updatedRow = 0
        val contentValues = ContentValues()
        when {
            pauseDownload -> {
                data.isPaused = true
                data.status = Constants.pauseState
                contentValues.put("control", 1)
            }
            else -> {
                data.isPaused = false
                data.status = Constants.resumeState
                contentValues.put("control", 0)
            }
        }
        try {
            updatedRow = context.contentResolver.update(
                Uri.parse(downloadUri),
                contentValues,
                "title=?",
                arrayOf(MyApp.DownloadData.downloadedData.title)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0 < updatedRow
    }

}