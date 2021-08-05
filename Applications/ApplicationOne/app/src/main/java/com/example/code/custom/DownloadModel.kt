package com.example.code.custom

class DownloadModel {
    var downloadId: Long = 0
    var title: String = ""
    var filePath: String = ""
    var progress: String = ""
    var status: String = ""
    var fileSize: String = ""
    var isPaused = false
    var isCancelled = false
}