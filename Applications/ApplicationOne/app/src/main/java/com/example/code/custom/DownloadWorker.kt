package com.example.code.custom

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import com.example.code.custom.Constants.DOWNLOAD_STATUS_COMPLETED
import com.example.code.custom.DownloadData.downloadedData
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class DownloadWorker (var ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result = coroutineScope {

        var status = ""

        async {
            status = downloadFileProcess(downloadedData.downloadId, downloadedData)
        }.await()

        if(status == DOWNLOAD_STATUS_COMPLETED){
            Result.success()
        }

        Result.retry()
    }

    @SuppressLint("Range")
    private fun downloadFileProcess(downloadId: Long, downloadModel: DownloadModel): String {
        val downloadManager = ctx.getSystemService(AppCompatActivity.DOWNLOAD_SERVICE) as DownloadManager
        var downloading = true
        while (downloading) {
            DownloadManager.Query().apply {
                setFilterById(downloadId)
                downloadManager.query(this).apply {
                    if(downloadModel.isCancelled){
                        close()
                        ProgressNotification.cancelProgressNotification(ctx)
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
                            val fileSizeDownloaded =
                                DownloadUtils.bytesIntoHumanReadable(bytesDownloaded.toLong())
                            ProgressNotification.updateProgressNotification(
                                context = ctx,
                                max = 100, progress = progress,
                                fileSizeDownloaded = fileSizeDownloaded,
                                isDownloadPaused = isPaused
                            )
                        }

                        publishProgress(progress.toString(), bytesDownloaded.toString(), status, downloadModel)
                        if(progress==100){
                            ProgressNotification.cancelProgressNotification(ctx)
                        }
                        close()
                    }
                }
            }

        }

        return downloadModel.status
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