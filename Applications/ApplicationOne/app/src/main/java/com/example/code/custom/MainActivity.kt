package com.example.code.custom

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.code.R
import com.example.code.custom.Constants.completedState
import com.example.code.custom.Constants.downloadingState
import com.example.code.custom.Constants.imageURL
import com.example.code.custom.Constants.pauseState
import com.example.code.custom.Constants.resumeState
import com.example.code.custom.DownloadData.downloadedData
import com.example.code.custom.DownloadUtils.bytesIntoHumanReadable
import com.example.code.custom.DownloadUtils.getStatusMessage
import com.example.code.custom.DownloadUtils.pauseDownload
import com.example.code.custom.DownloadUtils.resumeDownload
import com.example.code.custom.ProgressNotification.cancelProgressNotification
import com.example.code.custom.ProgressNotification.updateProgressNotification
import com.example.code.databinding.ActivityMainBinding
import java.io.File
import java.util.*

@SuppressLint("Range")
class MainActivity : AppCompatActivity() {

    var downloadPath = ""

    private lateinit var binding: ActivityMainBinding

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
            .plus(File.separator)
            .plus("Devrath")
            .plus(File.separator)
    }

    private fun downloadStatusTaskViaCoroutine(id: Long, downloadModel: DownloadModel) {
        lifecycleScope.executeAsyncTask(onPreExecute = {
            // ...
        }, doInBackground = {
            downloadFileProcess(id,downloadModel)
            "Result" // send data to "onPostExecute"
        }, onPostExecute = {
            // ... here "it" is a data returned from "doInBackground"
        })
    }

    private fun downloadFileProcess(downloadId: Long, downloadModel: DownloadModel) {
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        var downloading = true
        while (downloading) {
            DownloadManager.Query().apply {
                setFilterById(downloadId)
                downloadManager.query(this).apply {
                    if(downloadModel.isCancelled){
                        close()
                        cancelProgressNotification(this@MainActivity)
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
                            if(isCancelled){
                                close()
                            }else{
                                updateProgressNotification(this@MainActivity,100,progress,fileSizeDownloaded)
                            }
                        }

                        publishProgress(progress.toString(), bytesDownloaded.toString(), status,downloadModel)
                        if(progress==100){
                            cancelProgressNotification(this@MainActivity)
                        }
                        close()
                    }
                }
            }

        }
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

            binding.apply {
                // Update the UI
                fileTitle.text = downloadedData.title
                fileStatus.text = downloadedData.status
                fileProgress.progress = downloadedData.progress.toInt()
                fileSize.text = "Downloaded : ".plus(downloadedData.file_size)
            }
        }
    }

    private fun downloadFile(url: String) {
        val filename = URLUtil.guessFileName(url, null, null)
        val file = File(downloadPath, filename)
        var request: DownloadManager.Request? = null

        request = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
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
        val downloadEnquId = downloadManager.enqueue(request)

        downloadedData = DownloadModel().apply {
            status = downloadingState
            title = filename
            file_size = "0"
            progress = "0"
            isIs_paused = false
            downloadId = downloadEnquId
            file_path = ""
        }

        downloadStatusTaskViaCoroutine(downloadedData.downloadId,downloadedData)
    }


    private var onComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            val comp = changeItemWithStatus(completedState)
            if (comp) {
                DownloadManager.Query().apply {
                    setFilterById(id)
                    val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                    downloadManager.query(DownloadManager.Query().setFilterById(id)).apply {
                        moveToFirst()
                        getString(getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)).apply {
                            setChangeItemFilePath(this, id)
                        }
                    }
                }
            }
        }
    }

    fun changeItemWithStatus(message: String): Boolean {
        val comp = true
        downloadedData.status = message
        return comp
    }

    fun setChangeItemFilePath(path: String, id: Long) {
        downloadedData.file_path = path
    }

}