package com.example.code.custom

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

class MyService : Service() {

    companion object {
        private var downloadReciever: DownloadReceiver? = null
    }

    lateinit var context : Context

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onCreate() {
        context = this;
        registerScreenOffReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int  {
        scope.launch {
            startProgress()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private suspend fun startProgress() = coroutineScope {
        var status = ""
        withContext(Dispatchers.Default) {
            status = downloadFileProcess(
                DownloadData.downloadedData.downloadId,
                DownloadData.downloadedData
            )
        }

        if(status == Constants.DOWNLOAD_STATUS_COMPLETED){
            stopSelf()
        }
    }

    override fun onDestroy() {
        unregisterReceiver(downloadReciever)
        downloadReciever = null
        job.cancel()
    }

    private fun registerScreenOffReceiver() {
        downloadReciever = DownloadReceiver()

        registerReceiver(downloadReciever, IntentFilter().apply {
            addAction(Constants.FILTER_DOWNLOAD_PAUSE)
            addAction(Constants.FILTER_DOWNLOAD_RESUME)
            addAction(Constants.FILTER_DOWNLOAD_CANCEL)
            addAction(Constants.FILTER_DOWNLOAD_COMPLETE)
        })
    }

    override fun onBind(arg0: Intent): IBinder? {
        return null
    }


    @SuppressLint("Range")
    private fun downloadFileProcess(downloadId: Long, downloadModel: DownloadModel): String {
        val downloadManager = context.getSystemService(AppCompatActivity.DOWNLOAD_SERVICE) as DownloadManager
        var downloading = true
        while (downloading) {
            DownloadManager.Query().apply {
                setFilterById(downloadId)
                downloadManager.query(this).apply {
                    if(downloadModel.isCancelled){
                        close()
                        ProgressNotification.cancelProgressNotification(context)
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
                                context = context,
                                max = 100, progress = progress,
                                fileSizeDownloaded = fileSizeDownloaded,
                                isDownloadPaused = isPaused
                            )
                        }

                        publishProgress(progress.toString(), bytesDownloaded.toString(), status, downloadModel)
                        if(progress==100){
                            ProgressNotification.cancelProgressNotification(context)
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