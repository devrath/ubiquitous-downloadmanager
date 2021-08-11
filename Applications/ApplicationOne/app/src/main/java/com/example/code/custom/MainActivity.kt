package com.example.code.custom

import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import com.example.code.custom.Constants.imageURL
import com.example.code.custom.application.MyApp.DownloadData.downloadedData
import com.example.code.custom.downloadManager.DownloadTask
import com.example.code.custom.downloadManager.DownloadUtils.getFilePath
import com.example.code.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        binding.apply {
            initiateDownloadId.setOnClickListener {
                DownloadTask(this@MainActivity, imageURL).initiateDownload()
            }
            delFilesId.setOnClickListener {
                clearFiles()
            }
            chkIfFileExistsId.setOnClickListener {
                checkIfFileIsDownloaded()
            }
        }
    }

    /**
     * Check if the file is downloaded
     */
    private fun checkIfFileIsDownloaded() {
        val doesFileExist =
            File(getFilePath(this@MainActivity, downloadedData.title)).exists()
        when {
            doesFileExist -> showToast("File exists")
            else -> showToast("File does not exists")
        }
    }

    /**
     * Clear the downloads
     */
    private fun clearFiles() {
        lifecycleScope.launch(Dispatchers.IO) {
            File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()).deleteDirectoryFiles()
            withContext(Dispatchers.Main){
                showToast("Downloads cleared !!")
            }
        }
    }

}