package com.example.code.custom

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.webkit.URLUtil
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.code.R
import com.example.code.custom.DisplayFileSize.bytesIntoHumanReadable
import com.example.code.custom.DownloadActions.pauseDownload
import com.example.code.custom.DownloadActions.resumeDownload
import java.io.File
import java.util.*

@SuppressLint("Range")
class MainActivity : AppCompatActivity() {
    var downloadPath = ""
    var filename = "test.db"
    var downloadModels: MutableList<DownloadModel> = ArrayList()
    var rootView: View? = null
    var pause_resume: Button? = null
    var file_status: TextView? = null
    var file_title: TextView? = null
    var file_size: TextView? = null
    var file_progress: ProgressBar? = null
    var initiateDownloadId: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViews()
        setOnClickListener()
        setFilePath()
        registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    private fun setOnClickListener() {
        pause_resume!!.setOnClickListener { v: View? -> togglePauseResume() }
        initiateDownloadId!!.setOnClickListener { v: View? -> downloadFile(imageURL) }
    }

    private fun findViews() {
        rootView = findViewById(R.id.downloadWidgetId)
        pause_resume = findViewById(R.id.pause_resume)
        file_status = findViewById(R.id.file_status)
        file_title = findViewById(R.id.file_title)
        file_progress = findViewById(R.id.file_progress)
        file_size = findViewById(R.id.file_size)
        initiateDownloadId = findViewById(R.id.initiateDownloadId)
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
                query.setFilterById(downloadId.toLong())
                val cursor = downloadManager.query(query)
                cursor.moveToFirst()
                val bytes_downloaded =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                val total_size =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    downloading = false
                }
                val progress = (bytes_downloaded * 100L / total_size).toInt()
                val status = getStatusMessage(cursor)
                publishProgress(progress.toString(), bytes_downloaded.toString(), status)
                cursor.close()
            }
        }

        override fun onProgressUpdate(vararg values: String) {
            super.onProgressUpdate(*values)
            downloadModel.setFile_size(bytesIntoHumanReadable(values[1].toLong()))
            downloadModel.setProgress(values[0])
            if (!downloadModel.getStatus()
                    .equals("PAUSE", ignoreCase = true) && !downloadModel.getStatus()
                    .equals("RESUME", ignoreCase = true)
            ) {
                downloadModel.setStatus(values[2])
            }
            file_title!!.text = downloadObject.getTitle()
            file_status!!.text = downloadObject.getStatus()
            file_progress!!.progress = downloadObject.getProgress().toInt()
            file_size!!.text = "Downloaded : " + downloadObject.getFile_size()
        }


    }

    private fun getStatusMessage(cursor: Cursor): String {
        var msg = "-"
        msg = when (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
            DownloadManager.STATUS_FAILED -> "Failed"
            DownloadManager.STATUS_PAUSED -> "Paused"
            DownloadManager.STATUS_RUNNING -> "Running"
            DownloadManager.STATUS_SUCCESSFUL -> "Completed"
            DownloadManager.STATUS_PENDING -> "Pending"
            else -> "Unknown"
        }
        return msg
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
        val downloadModel = DownloadModel()
        downloadModel.setId(nextId.toLong())
        downloadModel.setStatus("Downloading")
        downloadModel.setTitle(filename)
        downloadModel.setFile_size("0")
        downloadModel.setProgress("0")
        downloadModel.isIs_paused = false
        downloadModel.setDownloadId(downloadId)
        downloadModel.setFile_path("")
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
        if (downloadObject.isIs_paused) {
            downloadModel.isIs_paused = false
            pause_resume!!.text = "PAUSE"
            downloadModel.setStatus("RESUME")
            file_status!!.text = "Running"
            if (!resumeDownload(this, downloadModel)) {
                Toast.makeText(this, "Failed to Resume", Toast.LENGTH_SHORT).show()
            }
        } else {
            downloadModel.isIs_paused = true
            pause_resume!!.text = "RESUME"
            downloadModel.setStatus("PAUSE")
            file_status!!.text = "PAUSE"
            if (!pauseDownload(this, downloadModel)) {
                Toast.makeText(this, "Failed to Pause", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val downloadObject: DownloadModel
        private get() = downloadModels[0]

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onComplete)
    }

    var onComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            val comp = ChangeItemWithStatus("Completed")
            if (comp) {
                val query = DownloadManager.Query()
                query.setFilterById(id)
                val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                val cursor = downloadManager.query(DownloadManager.Query().setFilterById(id))
                cursor.moveToFirst()
                val downloaded_path =
                    cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                setChangeItemFilePath(downloaded_path, id)
            }
        }
    }

    fun ChangeItemWithStatus(message: String?): Boolean {
        val comp = true
        downloadModels[0].setStatus(message)
        return comp
    }

    fun setChangeItemFilePath(path: String?, id: Long) {
        downloadModels[0].setFile_path(path)
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 101
        const val imageURL = "http://speedtest.ftp.otenet.gr/files/test10Mb.db"
    }
}