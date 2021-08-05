package com.example.code.custom

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.code.R
import com.example.code.custom.Constants.FILTER_DOWNLOAD_CANCEL
import com.example.code.custom.Constants.FILTER_DOWNLOAD_PAUSE
import com.example.code.custom.Constants.FILTER_DOWNLOAD_RESUME
import com.example.code.custom.Constants.completedState
import com.example.code.custom.Constants.downloadingState
import com.example.code.custom.Constants.imageURL
import com.example.code.custom.Constants.pauseState
import com.example.code.custom.Constants.resumeState
import com.example.code.custom.DownloadData.downloadedData
import com.example.code.custom.DownloadUtils.bytesIntoHumanReadable
import com.example.code.custom.DownloadUtils.getStatusMessage
import com.example.code.custom.NotificationChannelApiLevel.isDownloadManagerEqualOrAbove
import com.example.code.custom.ProgressNotification.cancelProgressNotification
import com.example.code.custom.ProgressNotification.updateProgressNotification
import com.example.code.custom.recievers.DownloadReceiver
import com.example.code.databinding.ActivityMainBinding
import java.io.File
import java.util.*

@SuppressLint("Range")
class MainActivity : AppCompatActivity() {

    var downloadPath = ""

    private lateinit var binding: ActivityMainBinding

    var receiver = DownloadReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setOnClickListener()
        setFilePath()
        registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onComplete)
    }

    private fun setOnClickListener() {
        binding.apply {
            initiateDownloadId.setOnClickListener { downloadFile(imageURL) }
        }
    }

    private fun setFilePath() {
        downloadPath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()
    }

    private fun downloadStatusTaskViaCoroutine(id: Long, downloadModel: DownloadModel) {
        lifecycleScope.executeAsyncTask(onPreExecute = {
            // ...
        }, doInBackground = {
            downloadFileProcess(id, downloadModel)
        }, onPostExecute = {
            // ... here "it" is a data returned from "doInBackground"
            Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show()
        })
    }

    private fun downloadFileProcess(downloadId: Long, downloadModel: DownloadModel): String {
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        var downloading = true
        while (downloading) {
            DownloadManager.Query().apply {
                setFilterById(downloadId)
                downloadManager.query(this).apply {
                    if(downloadModel.isCancelled){
                        close()
                        cancelProgressNotification(this@MainActivity)
                        downloading = false
                    }else{
                        moveToFirst()
                        val bytesDownloaded = getInt(getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                        val totalSize = getInt(getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        if (getInt(getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                            downloading = false
                        }
                        val progress = (bytesDownloaded * 100L / totalSize).toInt()
                        val status = getStatusMessage(this)

                        downloadModel.apply {
                            val fileSizeDownloaded = bytesIntoHumanReadable(bytesDownloaded.toLong())
                            updateProgressNotification(
                                    context = this@MainActivity,
                                    max = 100, progress = progress,
                                    fileSizeDownloaded = fileSizeDownloaded,
                                    isDownloadPaused = isPaused
                            )
                        }

                        publishProgress(progress.toString(), bytesDownloaded.toString(), status, downloadModel)
                        if(progress==100){
                            cancelProgressNotification(this@MainActivity)
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
        runOnUiThread {
            downloadModel.apply {
                file_size = bytesIntoHumanReadable(bytesDownloaded.toLong())
                progress = publishProgress
                if (!status
                        .equals(pauseState, ignoreCase = true) && !status
                        .equals(resumeState, ignoreCase = true)) {
                    downloadModel.status = status
                }
            }

        }
    }

    private fun downloadFile(url: String) {
        val filename = URLUtil.guessFileName(url, null, null)
        val file = File(downloadPath, filename)
        var request: DownloadManager.Request? = null

        request = if (isDownloadManagerEqualOrAbove()) {
            DownloadManager.Request(Uri.parse(url)).apply {
                setTitle(filename)
                setDescription(getString(R.string.str_desc_downloading))
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                setDestinationUri(Uri.fromFile(file))
                setRequiresCharging(false)
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }
        } else {
            DownloadManager.Request(Uri.parse(url)).apply {
                setTitle(filename)
                setDescription(getString(R.string.str_desc_downloading))
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                setDestinationUri(Uri.fromFile(file))
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }
        }
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val downloadEnqueueId = downloadManager.enqueue(request)

        downloadedData = DownloadModel().apply {
            status = downloadingState
            title = filename
            file_size = "0"
            progress = "0"
            isPaused = false
            downloadId = downloadEnqueueId
            file_path = ""
        }

        downloadStatusTaskViaCoroutine(downloadedData.downloadId, downloadedData)
    }


    private var onComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            downloadedData.status = completedState

            DownloadManager.Query().apply {
                setFilterById(id)
                val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                downloadManager.query(DownloadManager.Query().setFilterById(id)).apply {
                    moveToFirst()
                    getString(getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)).apply {
                        downloadedData.file_path = this
                    }
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        registerReceiver(receiver, IntentFilter().apply {
            addAction(FILTER_DOWNLOAD_PAUSE)
            addAction(FILTER_DOWNLOAD_RESUME)
            addAction(FILTER_DOWNLOAD_CANCEL)
        })
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
    }



}