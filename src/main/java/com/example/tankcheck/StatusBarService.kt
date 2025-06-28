package com.example.tankcheck

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import android.graphics.Color
import android.os.Build
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class StatusBarService : Service() {
    private val database = Firebase.database
    private val statusRef = database.getReference("containerStatus")
    private var valueEventListener: ValueEventListener? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Leggi lo stato iniziale
        val isFull = intent?.getBooleanExtra("IS_FULL", false) ?: false
        updateNotification(isFull)

        // Aggiungi listener per aggiornamenti in tempo reale
        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(Boolean::class.java)?.let { status ->
                    updateNotification(status)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Gestisci l'errore
            }
        }
        statusRef.addValueEventListener(valueEventListener!!)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        valueEventListener?.let {
            statusRef.removeEventListener(it)
        }
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(1, notification)
        }
    }
}