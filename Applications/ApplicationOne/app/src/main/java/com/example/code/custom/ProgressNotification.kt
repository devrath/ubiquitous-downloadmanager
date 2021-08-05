package com.example.code.custom

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.code.R
import com.example.code.custom.Constants.CHANNEL_6_ID
import com.example.code.custom.Constants.DOWNLOAD_ID
import com.example.code.custom.Constants.FILTER_DOWNLOAD_CANCEL
import com.example.code.custom.Constants.FILTER_DOWNLOAD_PAUSE
import com.example.code.custom.Constants.FILTER_DOWNLOAD_RESUME
import com.example.code.custom.DownloadData.downloadedData

object ProgressNotification {

    private const val progressMax = 100

    fun updateProgressNotification(context: Context, max: Int, progress: Int, fileSizeDownloaded: String, isDownloadPaused: Boolean) {
        val notification = prepareNotification(context,isDownloadPaused)
        val messageToDisplay = fileSizeDownloaded.plus(" ").plus(context.getString(R.string.str_downloaded))
        notification.setContentText(messageToDisplay)
                .setProgress(max, progress, false)
                .setOngoing(false)
        NotificationManager.getNotificationManager(context)?.apply { notify(DOWNLOAD_ID, notification.build()) }
    }

    fun cancelProgressNotification(context: Context) {
        NotificationManagerCompat.from(context).cancel(null, DOWNLOAD_ID);
        downloadedData.isCancelled = true
    }

    private fun prepareNotification(context: Context, isDownloadPaused: Boolean): NotificationCompat.Builder {

        val actionCancel = PendingIntent.getBroadcast(context, 0, Intent(FILTER_DOWNLOAD_CANCEL), PendingIntent.FLAG_UPDATE_CURRENT)
        val actionPause = PendingIntent.getBroadcast(context, 0, Intent(FILTER_DOWNLOAD_PAUSE), PendingIntent.FLAG_UPDATE_CURRENT)
        val actionResume = PendingIntent.getBroadcast(context, 0, Intent(FILTER_DOWNLOAD_RESUME), PendingIntent.FLAG_UPDATE_CURRENT)
        // Paused
        if(isDownloadPaused) {
            return NotificationCompat.Builder(context, CHANNEL_6_ID).apply {
                setSmallIcon(R.drawable.ic_pokemon)
                setContentTitle(context.getString(R.string.initiate_download))
                setContentText(context.getString(R.string.str_downloading))
                priority = NotificationCompat.PRIORITY_LOW
                setOngoing(true)
                setAutoCancel(false)
                setOnlyAlertOnce(true)
                // Add the action click behavior
                addAction(R.drawable.ic_action, context.getString(R.string.str_cancel), actionCancel)
                addAction(R.drawable.ic_action, context.getString(R.string.str_resume), actionResume)
                setProgress(progressMax, 0, true)
            }
        }else{
            return NotificationCompat.Builder(context, CHANNEL_6_ID).apply {
                setSmallIcon(R.drawable.ic_pokemon)
                setContentTitle(context.getString(R.string.initiate_download))
                setContentText(context.getString(R.string.str_downloading))
                priority = NotificationCompat.PRIORITY_LOW
                setOngoing(true)
                setAutoCancel(false)
                setOnlyAlertOnce(true)
                // Add the action click behavior
                addAction(R.drawable.ic_action, context.getString(R.string.str_cancel), actionCancel)
                addAction(R.drawable.ic_action, context.getString(R.string.str_pause), actionPause)
                setProgress(progressMax, 0, true)
            }
        }
    }

}