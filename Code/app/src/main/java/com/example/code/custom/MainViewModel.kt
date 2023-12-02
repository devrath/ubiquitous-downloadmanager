package com.example.code.custom

import android.content.Context
import android.os.Environment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.example.code.custom.application.MyApp
import com.example.code.custom.downloadManager.DownloadUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MainViewModel
@Inject constructor()   : ViewModel() {

    var clearFiles: MutableLiveData<Unit> = MutableLiveData()
    var checkIfFileExists: MutableLiveData<Boolean> = MutableLiveData()

    /**
     * Clear the downloads
     */
    fun clearFiles(@ApplicationContext context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()).deleteDirectoryFiles()
            withContext(Dispatchers.Main){
                clearFiles.value = Unit
            }
        }
    }

    /**
     * Check if the downloaded file exists
     */
    fun checkIfFileExists(@ApplicationContext context: Context){
        checkIfFileExists.value = File(DownloadUtils.getFilePath(context, MyApp.DownloadData.downloadedData.title)).exists()
    }

}