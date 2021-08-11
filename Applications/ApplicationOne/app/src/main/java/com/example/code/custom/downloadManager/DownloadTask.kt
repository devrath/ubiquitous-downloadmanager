package com.example.code.custom.downloadManager

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.webkit.URLUtil
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import com.example.code.R
import com.example.code.custom.Constants
import com.example.code.custom.application.MyApp.DownloadData.downloadedData
import com.example.code.custom.data.DownloadModel
import com.example.code.custom.downloadManager.UtilDownloadPath.isInternalStorageAvailable
import com.example.code.custom.showToast
import com.example.code.custom.workers.DownloadWorker
import java.io.File

class DownloadTask(var context: Context,var url : String) {

    private var downloadPath = ""
    private val workManager = WorkManager.getInstance(context)

    fun initiateDownload() {
        if(isInternalStorageAvailable(context)){
            downloadPath = UtilDownloadPath.getDownloadPath(context)
            initDownloadManager(url)
            initWorkManager()
        }else{
            context.showToast("Internal Storage is not available")
        }
    }

    /**
     * Here we prepare the download request
     * @param url :-> Param which is needed for the download manager to download the apk
     */
    private fun initDownloadManager(url: String) {
        val filename = URLUtil.guessFileName(url, null, null)
        val file = File(downloadPath, filename)
        var request: DownloadManager.Request? = null

        request = when {
            NotificationChannelApiLevel.isDownloadManagerEqualOrAboveNougat() -> DownloadManager.Request(Uri.parse(url)).apply {
                setTitle(filename)
                setDescription(context.getString(R.string.str_desc_downloading))
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                setDestinationUri(Uri.fromFile(file))
                setRequiresCharging(false)
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }
            else -> DownloadManager.Request(Uri.parse(url)).apply {
                setTitle(filename)
                setDescription(context.getString(R.string.str_desc_downloading))
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                setDestinationUri(Uri.fromFile(file))
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }
        }
        val downloadManager = context.getSystemService(AppCompatActivity.DOWNLOAD_SERVICE) as DownloadManager
        val downloadEnqueueId = downloadManager.enqueue(request)

        downloadedData = DownloadModel().apply {
            status = Constants.downloadingState
            title = filename
            fileSize = "0"
            progress = "0"
            isPaused = false
            downloadId = downloadEnqueueId
            filePath = ""
        }
    }

    /**
     * Here we initialize the work manager and specify the conditions for the work manager
     */
    private fun initWorkManager() {
        workManager.beginUniqueWork("download_work", ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<DownloadWorker>()
                .addTag("DownloadWorker")
                .setConstraints(Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build())
                .enqueue()
    }


}