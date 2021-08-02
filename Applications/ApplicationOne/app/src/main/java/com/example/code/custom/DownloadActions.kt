package com.example.code.custom

import android.content.ContentValues
import android.content.Context
import android.net.Uri

object DownloadActions {

    private const val downloadUri = "content://downloads/my_downloads"

    fun pauseDownload(context: Context, downloadModel: DownloadModel): Boolean {
        var updatedRow = 0
        val contentValues = ContentValues()
        contentValues.put("control", 1)
        try {
            updatedRow = context.contentResolver.update(
                Uri.parse(downloadUri),
                contentValues,
                "title=?",
                arrayOf(downloadModel.getTitle())
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
                arrayOf(downloadModel.getTitle())
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0 < updatedRow
    }

}