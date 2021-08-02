package com.example.code.custom

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.code.custom.DisplayFileSize.bytesIntoHumanReadable
import com.example.code.custom.DisplayFileSize.getStatusMessage
import com.example.code.custom.DisplayFileSize.pauseDownload
import com.example.code.custom.DisplayFileSize.resumeDownload
import com.example.code.databinding.ActivityMainBinding
import java.io.File
import java.util.*

@SuppressLint("Range")
class MainActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 101
        const val imageURL = "http://speedtest.ftp.otenet.gr/files/test10Mb.db"
    }

    var downloadPath = ""
    var filename = "test.db"
    var downloadModels: MutableList<DownloadModel> = ArrayList()


    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setOnClickListener()
        setFilePath()
        registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
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

    inner class DownloadStatusTask(var downloadModel: DownloadModel) : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg strings: String): String? {
            downloadFileProcess(strings[0])
            return null
        }

        private fun downloadFileProcess(downloadId: String) {
            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            var downloading = true
            while (downloading) {
                val query = DownloadManager.Query()


                DownloadManager.Query().apply {
                    setFilterById(downloadId.toLong())
                    downloadManager.query(this).apply {
                        moveToFirst()
                        val bytesDownloaded = getInt(getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                        val totalSize = getInt(getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        if (getInt(getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                            downloading = false
                        }
                        val progress = (bytesDownloaded * 100L / totalSize).toInt()
                        val status = getStatusMessage(this)
                        publishProgress(progress.toString(), bytesDownloaded.toString(), status)
                        close()
                    }
                }

            }
        }


        override fun onProgressUpdate(vararg values: String) {
            super.onProgressUpdate(*values)

            downloadModel.apply {
                setFile_size(bytesIntoHumanReadable(values[1].toLong()))
                setProgress(values[0])
                if (!getStatus()
                        .equals("PAUSE", ignoreCase = true) && !getStatus()
                        .equals("RESUME", ignoreCase = true)) {
                    downloadModel.setStatus(values[2])
                }

            }

            binding.apply {
                fileTitle.text = downloadObject.getTitle()
                fileStatus.text = downloadObject.getStatus()
                fileProgress.progress = downloadObject.getProgress().toInt()
                fileSize.text = "Downloaded : " + downloadObject.getFile_size()
            }

        }


    }

    private fun downloadFile(url: String) {
        val filename = URLUtil.guessFileName(url, null, null)
        val file = File(downloadPath, filename)
        var request: DownloadManager.Request? = null
        request = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            DownloadManager.Request(Uri.parse(url))
                .setTitle(filename)
                .setDescription("Downloading")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationUri(Uri.fromFile(file))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
        } else {
            DownloadManager.Request(Uri.parse(url))
                .setTitle(filename)
                .setDescription("Downloading")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationUri(Uri.fromFile(file))
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
        }
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)
        val nextId = 1
        val downloadModel = DownloadModel().apply {
            setId(nextId.toLong())
            setStatus("Downloading")
            setTitle(filename)
            setFile_size("0")
            setProgress("0")
            isIs_paused = false
            setDownloadId(downloadId)
            setFile_path("")
        }


        downloadModels.add(downloadModel)
        val downloadStatusTask = DownloadStatusTask(downloadModel)
        runTask(downloadStatusTask, "" + downloadId)
    }

    fun runTask(downloadStatusTask: DownloadStatusTask, id: String?) {
        try {
            downloadStatusTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun togglePauseResume() {
        val downloadModel = downloadObject

        downloadObject.apply {
            if (isIs_paused) {
                isIs_paused = false
                binding.pauseResume.text = "PAUSE"
                setStatus("RESUME")
                binding.fileStatus.text = "Running"
                if (!resumeDownload(this@MainActivity, downloadModel)) {
                    Toast.makeText(this@MainActivity, "Failed to Resume", Toast.LENGTH_SHORT).show()
                }
            } else {
                isIs_paused = true
                binding.pauseResume.text = "RESUME"
                setStatus("PAUSE")
                binding.fileStatus.text = "PAUSE"
                if (!pauseDownload(this@MainActivity, downloadModel)) {
                    Toast.makeText(this@MainActivity, "Failed to Pause", Toast.LENGTH_SHORT).show()
                }
            }
        }


    }

    private val downloadObject: DownloadModel
        get() = downloadModels[0]

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onComplete)
    }

    private var onComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            val comp = changeItemWithStatus("Completed")
            if (comp) {
                val query = DownloadManager.Query()
                query.setFilterById(id)
                val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                val cursor = downloadManager.query(DownloadManager.Query().setFilterById(id))
                cursor.moveToFirst()
                val downloaded_path = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                setChangeItemFilePath(downloaded_path, id)
            }
        }
    }

    fun changeItemWithStatus(message: String?): Boolean {
        val comp = true
        downloadModels[0].setStatus(message)
        return comp
    }

    fun setChangeItemFilePath(path: String?, id: Long) {
        downloadModels[0].setFile_path(path)
    }

}