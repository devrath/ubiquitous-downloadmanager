package com.example.code.custom

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.code.R
import com.example.code.custom.Constants.CHANNEL_6_ID
import com.example.code.custom.Constants.DOWNLOAD_ID
import com.example.notification.utils.NotificationManager

object ProgressNotification {

    const val progressMax = 100

    fun progressInNotification(activity: Context) {
        activity.let {
            NotificationManager.getNotificationManager(activity)?.apply { notify(DOWNLOAD_ID,  prepareNotification(it).build()) }
        }
    }

    private fun prepareNotification(context: Context): NotificationCompat.Builder {

        val broadcastIntent = Intent(context, NotificationReceiver::class.java)
        val actionIntent = PendingIntent.getBroadcast(context, 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Builder(context, CHANNEL_6_ID)
                .setSmallIcon(R.drawable.ic_pokemon)
                .setContentTitle("Download")
                .setContentText("Download in progress")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                // Add the action click behavior
                .addAction(R.drawable.ic_action, context.getString(R.string.str_cancel), actionIntent)
                .setProgress(progressMax, 0, true)
    }

    private fun updateProgressNotification(context: Context) {
        val notification = prepareNotification(context)

        Thread {
            SystemClock.sleep(2000)
            var progress = 0
            while (progress <= progressMax) {
                SystemClock.sleep(1000)
                progress += 20
            }
            notification.setContentText("Download finished")
                    .setProgress(0, 0, false)
                    .setOngoing(false)

            NotificationManager.getNotificationManager(context)?.apply { notify(DOWNLOAD_ID, notification.build()) }

        }.start()
    }

    fun updateProgressNotificationTwo(context: Context,max: Int,progress:Int) {
        val notification = prepareNotification(context)
        notification.setContentText("Download finished")
                .setProgress(max, progress, false)
                .setOngoing(false)
        NotificationManager.getNotificationManager(context)?.apply { notify(DOWNLOAD_ID, notification.build()) }
    }

    fun cancelProgressNotification(context: Context) {
        NotificationManagerCompat.from(context).cancel(null, DOWNLOAD_ID);
    }
}