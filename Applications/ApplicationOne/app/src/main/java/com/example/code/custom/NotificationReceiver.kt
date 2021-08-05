package com.example.code.custom

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.example.code.custom.Constants.CHANNEL_6_ID
import com.example.code.custom.Constants.DOWNLOAD_ID

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val message = intent.getStringExtra("toastMessage")
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        NotificationManagerCompat.from(context).cancel(null, DOWNLOAD_ID);
    }
}