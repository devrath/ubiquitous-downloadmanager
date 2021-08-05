package com.example.code.custom

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.code.R
import com.example.code.custom.Constants.CHANNEL_6_ID
import com.example.code.custom.Constants.DOWNLOAD_CANCEL
import com.example.code.custom.Constants.DOWNLOAD_ID
import com.example.code.custom.Constants.DOWNLOAD_PAUSE
import com.example.code.custom.DownloadData.downloadedData

object ProgressNotification {

    const val progressMax = 100

    private fun prepareNotification(context: Context): NotificationCompat.Builder {

        val cancelIntent = Intent(context, NotificationCancelReceiver::class.java)
        cancelIntent.putExtra(DOWNLOAD_CANCEL, true)
        val actionCancel = PendingIntent.getBroadcast(context, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val pauseIntent = Intent(context, NotificationPauseReceiver::class.java)
        cancelIntent.putExtra(DOWNLOAD_PAUSE, true)
        val actionPause = PendingIntent.getBroadcast(context, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)


        return NotificationCompat.Builder(context, CHANNEL_6_ID)
                .setSmallIcon(R.drawable.ic_pokemon)
                .setContentTitle(context.getString(R.string.initiate_download))
                .setContentText(context.getString(R.string.str_downloading))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                // Add the action click behavior
                .addAction(R.drawable.ic_action, context.getString(R.string.str_cancel), actionCancel)
                .addAction(R.drawable.ic_action, context.getString(R.string.str_pause), actionPause)
                .setProgress(progressMax, 0, true)
    }

    fun updateProgressNotification(context: Context, max: Int, progress: Int, fileSizeDownloaded: String) {
        val notification = prepareNotification(context)
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
}