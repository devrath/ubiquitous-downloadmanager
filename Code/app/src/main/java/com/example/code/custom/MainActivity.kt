package com.example.code.custom

import android.os.Bundle
import android.os.Environment
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import com.example.code.custom.Constants.imageURL
import com.example.code.custom.application.MyApp.DownloadData.downloadedData
import com.example.code.custom.downloadManager.DownloadTask
import com.example.code.custom.downloadManager.DownloadUtils.getFilePath
import com.example.code.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setOnClickListeners()
        setObservers()
    }

    private fun setOnClickListeners() {
        binding.apply {
            initiateDownloadId.setOnClickListener {
                DownloadTask(this@MainActivity, imageURL).initiateDownload()
            }
            delFilesId.setOnClickListener {
                viewModel.clearFiles(this@MainActivity)
            }
            chkIfFileExistsId.setOnClickListener {
                viewModel.checkIfFileExists(this@MainActivity)
            }
        }
    }

    private fun setObservers() {
        viewModel.clearFiles.observe(this, {
            showToast("Downloads cleared !!")
        })
        viewModel.checkIfFileExists.observe(this, {
            when {
                it -> showToast("File exists")
                else -> showToast("File does not exists")
            }
        })
    }

}