package com.example.code.custom

import android.app.DownloadManager
import android.database.Cursor
import android.util.Log
import com.google.gson.Gson
import javax.inject.Inject

open class DownloadManagerCursorStatus @Inject constructor(
    val gson: Gson
) {

    companion object {
        const val TAG = "DOWNLOAD_ASSETS_TAG"

        // ---- > ERROR STATE CODES
        const val ERROR_CANNOT_RESUME = "ERROR_CANNOT_RESUME"
        const val ERROR_DEVICE_NOT_FOUND = "ERROR_DEVICE_NOT_FOUND"
        const val ERROR_FILE_ALREADY_EXISTS = "ERROR_FILE_ALREADY_EXISTS"
        const val ERROR_FILE_ERROR = "ERROR_FILE_ERROR"
        const val ERROR_HTTP_DATA_ERROR = "ERROR_HTTP_DATA_ERROR"
        const val ERROR_INSUFFICIENT_SPACE = "ERROR_INSUFFICIENT_SPACE"
        const val ERROR_TOO_MANY_REDIRECTS = "ERROR_TOO_MANY_REDIRECTS"
        const val ERROR_UNHANDLED_HTTP_CODE = "ERROR_UNHANDLED_HTTP_CODE"
        const val ERROR_UNKNOWN = "ERROR_UNKNOWN"
        const val FORBIDDEN_ERROR = "FORBIDDEN_ERROR"


        // ---- > ERROR STATE CODES

        // ---- > PAUSED STATE CODES
        const val PAUSED_QUEUED_FOR_WIFI = "PAUSED_QUEUED_FOR_WIFI"
        const val PAUSED_UNKNOWN = "PAUSED_UNKNOWN"
        const val PAUSED_WAITING_FOR_NETWORK = "PAUSED_WAITING_FOR_NETWORK"
        const val PAUSED_WAITING_TO_RETRY = "PAUSED_WAITING_TO_RETRY"
        // ---- > PAUSED STATE CODES

        // ---- > OTHER STATE CODES
        const val STATUS_PENDING = "STATUS_PENDING"
        const val STATUS_RUNNING = "STATUS_RUNNING"
        const val STATUS_SUCCESSFUL = "STATUS_SUCCESSFUL"
        // ---- > OTHER STATE CODES

        const val STATUS_CODE_403 = 403

        const val ERROR_CAUSE_DOWNLOAD_MANAGER = "ERROR_CAUSE_DOWNLOAD_MANAGER -- "
        const val WITH_ERROR_CODE_STR = " with error code -> "
        const val WITH_PAUSED_CODE_STR = " with paused code -> "

    }

    fun checkDownloadStatus(cursor: Cursor): DownloadManagerStatus {

        val columnIndex: Int = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
        val status: Int = cursor.getInt(columnIndex)
        val columnReason: Int = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
        val reason: Int = cursor.getInt(columnReason)

        return checkCondition(status, reason)

    }

    private fun checkCondition(status: Int, reason: Int): DownloadManagerStatus {
        var failedReason = ""
        var isFailed = false
        var pausedReason = ""
        var isPaused = false
        var pendingReason = ""
        var isPending = false
        var runningReason = ""
        var isRunning = false
        var successfulReason = ""
        var isSuccessful = false

        when (status) {
            DownloadManager.STATUS_FAILED -> {
                isFailed = true
                when (reason) {
                    DownloadManager.ERROR_CANNOT_RESUME -> failedReason =
                        "$ERROR_CANNOT_RESUME$WITH_ERROR_CODE_STR$reason"
                    DownloadManager.ERROR_DEVICE_NOT_FOUND -> failedReason =
                        "$ERROR_DEVICE_NOT_FOUND$WITH_ERROR_CODE_STR$reason"
                    DownloadManager.ERROR_FILE_ALREADY_EXISTS -> failedReason =
                        "$ERROR_FILE_ALREADY_EXISTS$WITH_ERROR_CODE_STR$reason"
                    DownloadManager.ERROR_FILE_ERROR -> failedReason =
                        "$ERROR_FILE_ERROR$WITH_ERROR_CODE_STR$reason"
                    DownloadManager.ERROR_HTTP_DATA_ERROR -> failedReason =
                        "$ERROR_HTTP_DATA_ERROR$WITH_ERROR_CODE_STR$reason"
                    DownloadManager.ERROR_INSUFFICIENT_SPACE -> failedReason =
                        "$ERROR_INSUFFICIENT_SPACE$WITH_ERROR_CODE_STR$reason"
                    DownloadManager.ERROR_TOO_MANY_REDIRECTS -> failedReason =
                        "$ERROR_TOO_MANY_REDIRECTS$WITH_ERROR_CODE_STR$reason"
                    DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> failedReason =
                        "$ERROR_UNHANDLED_HTTP_CODE$WITH_ERROR_CODE_STR$reason"
                    DownloadManager.ERROR_UNKNOWN -> failedReason =
                        "$ERROR_UNKNOWN$WITH_ERROR_CODE_STR$reason"
                    STATUS_CODE_403 -> failedReason = "$FORBIDDEN_ERROR$WITH_ERROR_CODE_STR$reason"
                    else -> failedReason =
                        "$ERROR_CAUSE_DOWNLOAD_MANAGER$WITH_ERROR_CODE_STR$reason"
                }
            }
            DownloadManager.STATUS_PAUSED -> {
                isPaused = true
                when (reason) {
                    DownloadManager.PAUSED_QUEUED_FOR_WIFI -> pausedReason =
                        "$PAUSED_QUEUED_FOR_WIFI$WITH_PAUSED_CODE_STR$reason"
                    DownloadManager.PAUSED_UNKNOWN -> pausedReason =
                        "$PAUSED_UNKNOWN$WITH_PAUSED_CODE_STR$reason"
                    DownloadManager.PAUSED_WAITING_FOR_NETWORK -> pausedReason =
                        "$PAUSED_WAITING_FOR_NETWORK$WITH_PAUSED_CODE_STR$reason"
                    DownloadManager.PAUSED_WAITING_TO_RETRY -> pausedReason =
                        "$PAUSED_WAITING_TO_RETRY$WITH_PAUSED_CODE_STR$reason"
                }
            }
            DownloadManager.STATUS_PENDING -> {
                pendingReason = STATUS_PENDING
                isPending = true
            }
            DownloadManager.STATUS_RUNNING -> {
                runningReason = STATUS_RUNNING
                isRunning = true
            }
            DownloadManager.STATUS_SUCCESSFUL -> {
                successfulReason = STATUS_SUCCESSFUL
                isSuccessful = true
            }
        }

        val currentStatus = DownloadManagerStatus(
            isFailed, isPaused, isPending, isRunning, isSuccessful,
            failedReason, pausedReason, pendingReason, runningReason, successfulReason
        )

        // Print Status to console
        Log.d(TAG, "<---------------------------------------------------------------->")
        Log.d(TAG, gson.toJson(currentStatus))
        Log.d(TAG, "<---------------------------------------------------------------->")

        return currentStatus
    }
}