package com.example.code.custom.downloadManager

import android.content.Context
import android.os.Environment
import java.io.File

object UtilDownloadPath {
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
}