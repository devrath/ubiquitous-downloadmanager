package com.example.code.custom.workers

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.IntentFilter
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.code.custom.Constants
import com.example.code.custom.DownloadManagerCursorStatus
import com.example.code.custom.application.MyApp
import com.example.code.custom.application.MyApp.DownloadData.downloadedData
import com.example.code.custom.data.DownloadModel
import com.example.code.custom.reciever.DownloadReceiver
import com.example.code.custom.downloadManager.DownloadUtils
import com.example.code.custom.downloadManager.ProgressNotification
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class DownloadWorker (var context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {

    var receiver = DownloadReceiver()

    override suspend fun doWork(): Result = coroutineScope {
        registerReciever()
        var status = ""
        withContext(Dispatchers.Default) {
            status = downloadFileProcess(downloadedData.downloadId, MyApp.DownloadData.downloadedData)
        }
        when (status) {
            Constants.DOWNLOAD_STATUS_FAILED -> {

                context.unregisterReceiver(receiver)
                Result.failure()
            }

            Constants.DOWNLOAD_STATUS_COMPLETED -> {
                context.unregisterReceiver(receiver)
                Result.success()
            }
            else -> Result.retry()
        }
    }

    private fun registerReciever() {
        context.registerReceiver(receiver, IntentFilter().apply {
            addAction(Constants.FILTER_DOWNLOAD_PAUSE)
            addAction(Constants.FILTER_DOWNLOAD_RESUME)
            addAction(Constants.FILTER_DOWNLOAD_CANCEL)
            addAction(Constants.FILTER_DOWNLOAD_COMPLETE)
        })
    }

    @SuppressLint("Range")
    private suspend fun downloadFileProcess(downloadId: Long, downloadModel: DownloadModel): String {
        val downloadManager = context.getSystemService(AppCompatActivity.DOWNLOAD_SERVICE) as DownloadManager
        var downloading = true
        while (downloading) {
            DownloadManager.Query().apply {
                setFilterById(downloadId)
                downloadManager.query(this).apply {
                    if(downloadModel.isCancelled){
                        close()
                        downloading = false
                    }else{
                        moveToFirst()
                        val bytesDownloaded = getInt(getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                        val totalSize = getInt(getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        if (getInt(getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                            downloading = false
                        }
                        val progress = (bytesDownloaded * 100L / totalSize).toInt()
                        val status = DownloadUtils.getStatusMessage(this)

                        downloadModel.apply {
                            val fileSizeDownloaded = DownloadUtils.bytesIntoHumanReadable(bytesDownloaded.toLong())
                            val foregroundInfo = createForegroundInfo(context = context, progress = progress, fileSizeDownloaded = fileSizeDownloaded)
                            coroutineScope {
                                setForeground(foregroundInfo)
                            }
                        }

                        when (getInt(getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                            DownloadManager.STATUS_FAILED -> {
                                val causeOfFailure = DownloadManagerCursorStatus(Gson()).checkDownloadStatus(this)
                                Log.d("tag", causeOfFailure.failedReason)
                            }
                        }
                        publishProgress(progress.toString(), bytesDownloaded.toString(), status, downloadModel)
                        close()
                    }
                }
            }

        }

        return downloadModel.status
    }

    private fun createForegroundInfo(context : Context, progress : Int, fileSizeDownloaded : String): ForegroundInfo {
        val isPaused = downloadedData.isPaused
        val intent = WorkManager.getInstance(applicationContext).createCancelPendingIntent(id)
        val notification = ProgressNotification(context = context,max = 100, fileSizeDownloaded = fileSizeDownloaded,
                            pendingIntent = intent, progress=progress,isPaused=isPaused).updateDownloadNotification()
        return ForegroundInfo(1, notification.build())
    }

    private  fun publishProgress(
            publishProgress: String,
            bytesDownloaded: String,
            status: String,
            downloadModel: DownloadModel
    ) {
        downloadModel.apply {
            fileSize = DownloadUtils.bytesIntoHumanReadable(bytesDownloaded.toLong())
            progress = publishProgress
            downloadModel.status = status
        }
    }
}