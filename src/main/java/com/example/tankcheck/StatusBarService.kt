package com.example.tankcheck

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import android.graphics.Color

class StatusBarService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val isFull = intent?.getBooleanExtra("IS_FULL", false) ?: false
        updateNotification(isFull)
        return START_STICKY
    }

    @SuppressLint("ForegroundServiceType")
    private fun updateNotification(isFull: Boolean) {
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, "container_channel")
            .setSmallIcon(R.drawable.baseline_air_24)
            .setColor(if (isFull) Color.RED else Color.GREEN)
            .setContentTitle("Stato Contenitore")
            .setContentText(if (isFull) "🔴 PIENO" else "🟢 VUOTO")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()

        startForeground(1, notification)

        startForeground(1, notification)
    }
}