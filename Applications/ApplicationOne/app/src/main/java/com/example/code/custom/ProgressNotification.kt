package com.example.code.custom

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.example.code.R
import com.example.code.custom.Constants.CHANNEL_6_ID
import com.example.notification.utils.NotificationManager

object ProgressNotification {
    private fun progressInNotification(activity: Context, title: String, message: String) {
        activity?.let {

            /*
             * Broadcast Receiver:  As a intent in action click
             * **** This is the intent triggered when we initiate a action */
            val broadcastIntent = Intent(it, NotificationReceiver::class.java)
            broadcastIntent.putExtra("toastMessage", message)
            /*
             * DESCRIPTION: Pending Intent is just a wrapper around the Intent used to have a action to be initiated in future
             * PARAMETERS:
             * *********** Context: From the launching screen
             * *********** RequestCode: Used as a reference so pending intent can be cancelled in future
             * *********** Intent: Used to launch the destination
             * *********** Flag: This is used to define what happens when our Intent is recreated, since the intent remains same, we can add zero
             * */
            val actionIntent = PendingIntent.getBroadcast(it, 0, broadcastIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            )

            val progressMax = 100
            val notification: NotificationCompat.Builder =
                    NotificationCompat.Builder(it, CHANNEL_6_ID)
                            .setSmallIcon(R.drawable.ic_pokemon)
                            .setContentTitle("Download")
                            .setContentText("Download in progress")
                            .setPriority(NotificationCompat.PRIORITY_LOW)
                            .setOngoing(true)
                            .setOnlyAlertOnce(true)
                            // Add the action click behavior
                            .addAction(R.drawable.ic_action, "Toast", actionIntent)
                            .setProgress(progressMax, 0, true)

            NotificationManager.getNotificationManager(activity)?.apply { notify(2, notification.build()) }

            Thread {
                SystemClock.sleep(2000)
                var progress = 0
                while (progress <= progressMax) {

                    /*notification.setProgress(progressMax, progress, false);
                            notificationManager.notify(2, notification.build());*/SystemClock.sleep(
                            1000
                    )
                    progress += 20
                }
                notification.setContentText("Download finished")
                        .setProgress(0, 0, false)
                        .setOngoing(false)

                NotificationManager.getNotificationManager(activity)?.apply { notify(2, notification.build()) }

            }.start()
        }
    }
}