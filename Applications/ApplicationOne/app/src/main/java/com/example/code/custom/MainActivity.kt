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
import com.example.code.custom.DownloadUtils.bytesIntoHumanReadable
import com.example.code.custom.DownloadUtils.getStatusMessage
import com.example.code.custom.DownloadUtils.pauseDownload
import com.example.code.custom.DownloadUtils.resumeDownload
import com.example.code.custom.ProgressNotification.cancelProgressNotification
import com.example.code.custom.ProgressNotification.progressInNotification
import com.example.code.custom.ProgressNotification.updateProgressNotificationTwo
import com.example.code.databinding.ActivityMainBinding
import java.io.File
import java.util.*

@SuppressLint("Range")
class MainActivity : AppCompatActivity() {

    companion object {
        const val imageURL = "http://speedtest.ftp.otenet.gr/files/test10Mb.db"
        const val pauseState = "PAUSE"
        const val resumeState = "RESUME"
        const val downloadingState = "Downloading"
        const val completedState = "Completed"
    }

    var downloadPath = ""
    var filename = "test.db"
    var downloadData : DownloadModel = DownloadModel()


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
            pauseResume.setOnClickListener { togglePauseResume() }
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
                    moveToFirst()
                    val bytesDownloaded = getInt(getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    val totalSize = getInt(getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    if (getInt(getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                        downloading = false
                    }
                    val progress = (bytesDownloaded * 100L / totalSize).toInt()
                    val status = getStatusMessage(this)
                    updateProgressNotificationTwo(this@MainActivity,100,progress)
                    publishProgress(progress.toString(), bytesDownloaded.toString(), status,downloadModel)
                    if(progress==100){ cancelProgressNotification(this@MainActivity) }
                    close()
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
                fileTitle.text = downloadData.title
                fileStatus.text = downloadData.status
                fileProgress.progress = downloadData.progress.toInt()
                fileSize.text = "Downloaded : ".plus(downloadData.file_size)
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
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                setDestinationUri(Uri.fromFile(file))
                setRequiresCharging(false)
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }
        } else {
            DownloadManager.Request(Uri.parse(url)).apply {
                setTitle(filename)
                setDescription(getString(R.string.str_desc_downloading))
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                setDestinationUri(Uri.fromFile(file))
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }
        }
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val downloadEnquId = downloadManager.enqueue(request)
        val nextId = 1

        downloadData = DownloadModel().apply {
            id = nextId.toLong()
            status = downloadingState
            title = filename
            file_size = "0"
            progress = "0"
            isIs_paused = false
            downloadId = downloadEnquId
            file_path = ""
        }

        downloadStatusTaskViaCoroutine(downloadData.downloadId,downloadData)
    }




    private fun togglePauseResume() {
        val downloadModel = downloadData

        downloadData.apply {
            if (isIs_paused) {
                // Set the states
                isIs_paused = false
                status = resumeState
                // Update the UI
                binding.pauseResume.text = getString(R.string.str_pause)
                binding.fileStatus.text = getString(R.string.str_running)
                // Notify the download manager
                if (!resumeDownload(this@MainActivity, downloadModel)) {
                    Toast.makeText(this@MainActivity, getString(R.string.str_failed_to_resume), Toast.LENGTH_SHORT).show()
                }
            } else {
                // Set the states
                isIs_paused = true
                status = pauseState
                // Update the UI
                binding.pauseResume.text = getString(R.string.str_resume)
                binding.fileStatus.text = getString(R.string.str_pause)
                // Notify the download manager
                if (!pauseDownload(this@MainActivity, downloadModel)) {
                    Toast.makeText(this@MainActivity, getString(R.string.str_failed_to_pause), Toast.LENGTH_SHORT).show()
                }
            }
        }


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
        downloadData.status = message
        return comp
    }

    fun setChangeItemFilePath(path: String, id: Long) {
        downloadData.file_path = path
    }

}