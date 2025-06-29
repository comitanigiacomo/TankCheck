package com.example.tankcheck

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.Calendar

class DailyResetReceiver : BroadcastReceiver() {
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun onReceive(context: Context, intent: Intent?) {
        val database = Firebase.database
        val statusRef = database.getReference("containerStatus")
        statusRef.setValue(true)

        val serviceIntent = Intent(context, StatusBarService::class.java).apply {
            putExtra("IS_FULL", true)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val newIntent = Intent(context, DailyResetReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, newIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 3)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }
}