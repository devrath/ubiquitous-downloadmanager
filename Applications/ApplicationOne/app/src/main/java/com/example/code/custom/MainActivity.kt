package com.example.code.custom

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.webkit.URLUtil
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import com.example.code.R
import com.example.code.custom.Constants.FILTER_DOWNLOAD_CANCEL
import com.example.code.custom.Constants.FILTER_DOWNLOAD_COMPLETE
import com.example.code.custom.Constants.FILTER_DOWNLOAD_PAUSE
import com.example.code.custom.Constants.FILTER_DOWNLOAD_RESUME
import com.example.code.custom.Constants.downloadingState
import com.example.code.custom.Constants.imageURL
import com.example.code.custom.data.DownloadData.downloadedData
import com.example.code.custom.utils.NotificationChannelApiLevel.isDownloadManagerEqualOrAbove
import com.example.code.custom.data.DownloadModel
import com.example.code.custom.reciever.DownloadReceiver
import com.example.code.custom.workers.DownloadWorker
import com.example.code.databinding.ActivityMainBinding
import java.io.File
import java.util.*

@SuppressLint("Range")
class MainActivity : AppCompatActivity() {

    var downloadPath = ""

    private lateinit var binding: ActivityMainBinding

    var receiver = DownloadReceiver()

    val workManager = WorkManager.getInstance(this@MainActivity)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setOnClickListener()
        setFilePath()
    }

    private fun setOnClickListener() {
        binding.apply {
            initiateDownloadId.setOnClickListener { downloadFile(imageURL) }
        }
    }

    private fun setFilePath() {
        downloadPath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()
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
            fileSize = "0"
            progress = "0"
            isPaused = false
            downloadId = downloadEnqueueId
            filePath = ""
        }

        initWorkManager()
    }

    private fun initWorkManager() {
        workManager.beginUniqueWork("ordering_work", ExistingWorkPolicy.REPLACE,
                        OneTimeWorkRequestBuilder<DownloadWorker>()
                        .addTag("DownloadWorker")
                        .setConstraints(Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED).build())
                        .build())
                        .enqueue()
    }


}