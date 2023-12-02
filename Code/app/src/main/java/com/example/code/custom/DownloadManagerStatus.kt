package com.example.code.custom

data class DownloadManagerStatus(
    val isFailed: Boolean = false,
    val isPaused: Boolean = false,
    val isPending: Boolean = false,
    val isRunning: Boolean = false,
    val isSuccessful: Boolean = false,
    var failedReason: String = "",
    var pausedReason: String = "",
    var pendingReason: String = "",
    var runningReason: String = "",
    var successfulReason: String = ""
)