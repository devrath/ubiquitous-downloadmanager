package com.example.code.custom.downloadManager

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.code.R
import com.example.code.custom.Constants.CHANNEL_6_ID
import com.example.code.custom.Constants.FILTER_DOWNLOAD_PAUSE
import com.example.code.custom.Constants.FILTER_DOWNLOAD_RESUME

class ProgressNotification( var context: Context, var max: Int,
                           var fileSizeDownloaded: String,
                           var pendingIntent: PendingIntent, var progress: Int,
                           var isPaused : Boolean) {

    private val progressMax = 100

    fun updateProgressNotificationWorkManager(): NotificationCompat.Builder {
        val notification = prepareCancelableNotification(context,pendingIntent,isPaused)
        val messageToDisplay = fileSizeDownloaded.plus(" ").plus(context.getString(R.string.str_downloaded))
        notification.setContentText(messageToDisplay)
                .setProgress(max, progress, false)
                .setOngoing(false)
        return notification
    }

    private fun prepareCancelableNotification(
        context: Context,
        pendingIntent: PendingIntent,
        isPaused: Boolean
    ): NotificationCompat.Builder {
        val actionPause = PendingIntent.getBroadcast(context, 0, Intent(FILTER_DOWNLOAD_PAUSE), PendingIntent.FLAG_UPDATE_CURRENT)
        val actionResume = PendingIntent.getBroadcast(context, 0, Intent(FILTER_DOWNLOAD_RESUME), PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Builder(context, CHANNEL_6_ID).apply {
            setSmallIcon(R.drawable.ic_pokemon)
            setContentTitle(context.getString(R.string.initiate_download))
            setContentText(context.getString(R.string.str_downloading))
            priority = NotificationCompat.PRIORITY_LOW
            setOngoing(true)
            setAutoCancel(false)
            setOnlyAlertOnce(true)
            // Cancel - action
            addAction(R.drawable.ic_action, context.getString(R.string.str_cancel), pendingIntent)
            when {
                // Toggling between pause and resume
                isPaused -> addAction(R.drawable.ic_action, context.getString(R.string.str_resume), actionResume)
                else -> addAction(R.drawable.ic_action, context.getString(R.string.str_pause), actionPause)
            }
            setProgress(progressMax, 0, true)
        }
    }


}