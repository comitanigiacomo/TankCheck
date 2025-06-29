package com.example.tankcheck

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresPermission
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.Calendar

class StatusBarService : Service() {
    private val database = Firebase.database
    private val statusRef = database.getReference("containerStatus")
    private var valueEventListener: ValueEventListener? = null
    private lateinit var alarmManager: AlarmManager
    private lateinit var alarmIntent: PendingIntent

    override fun onBind(intent: Intent?): IBinder? = null

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun onCreate() {
        super.onCreate()
        setupDailyReset()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val isFull = intent?.getBooleanExtra("IS_FULL", false) ?: false
        updateNotification(isFull)

        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(Boolean::class.java)?.let { status ->
                    updateNotification(status)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }
        statusRef.addValueEventListener(valueEventListener!!)

        return START_STICKY
    }

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    private fun setupDailyReset() {
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, DailyResetReceiver::class.java)
        alarmIntent = PendingIntent.getBroadcast(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 3)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)

            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            alarmIntent
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        valueEventListener?.let {
            statusRef.removeEventListener(it)
        }
        alarmManager.cancel(alarmIntent)
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
    }
}