package com.example.code.custom

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.code.custom.ProgressNotification.updateProgressNotificationWorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class DownloadWorkerTwo (var context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result = coroutineScope {
        var status = ""
        withContext(Dispatchers.Default) {
            status = downloadFileProcess(DownloadData.downloadedData.downloadId, DownloadData.downloadedData)
        }
        when (status) {
            Constants.DOWNLOAD_STATUS_COMPLETED -> Result.success()
            else -> Result.retry()
        }
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

                        publishProgress(progress.toString(), bytesDownloaded.toString(), status, downloadModel)
                        close()
                    }
                }
            }

        }

        return downloadModel.status
    }

    private fun createForegroundInfo(context : Context, progress : Int, fileSizeDownloaded : String): ForegroundInfo {
        val intent = WorkManager.getInstance(applicationContext).createCancelPendingIntent(id)
        val notification = updateProgressNotificationWorkManager(context = context,max = 100,
                fileSizeDownloaded =fileSizeDownloaded, pendingIntent = intent, progress=progress)
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
            if (!status
                            .equals(Constants.pauseState, ignoreCase = true) && !status
                            .equals(Constants.resumeState, ignoreCase = true)) {
                downloadModel.status = status
            }
        }
    }
}