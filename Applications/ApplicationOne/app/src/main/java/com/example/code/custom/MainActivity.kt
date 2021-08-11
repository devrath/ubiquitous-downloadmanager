package com.example.code.custom

import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import com.example.code.custom.Constants.imageURL
import com.example.code.custom.downloadManager.DownloadTask
import com.example.code.databinding.ActivityMainBinding
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
        }
    }

    private fun clearFiles() {
       File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()).deleteDirectoryFiles()
    }

}