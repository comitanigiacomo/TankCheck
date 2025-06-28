package com.example.tankcheck

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tankcheck.ui.theme.TankCheckTheme

class MainActivity : ComponentActivity() {
    private var currentStatus by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()
        startService(Intent(this, StatusBarService::class.java))

        enableEdgeToEdge()

        if (intent?.action != Intent.ACTION_MAIN) {
            showStatusChangeUI()
        } else {
            setContent {
                TankCheckTheme {
                    AppContent(
                        modifier = Modifier.fillMaxSize(),
                        onStatusChange = { newStatus ->
                            currentStatus = newStatus
                            updateServiceStatus(newStatus)
                        }
                    )
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "container_channel",
                "Stato Contenitore",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Mostra lo stato del contenitore"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                enableVibration(true)
            }
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    private fun showStatusChangeUI() {
        setContent {
            TankCheckTheme {
                StatusChangeScreen(
                    onStatusChange = { newStatus ->
                        currentStatus = newStatus
                        updateServiceStatus(newStatus)
                    }
                )
            }
        }
    }

    private fun updateServiceStatus(isFull: Boolean) {
        val intent = Intent(this, StatusBarService::class.java).apply {
            putExtra("IS_FULL", isFull)
        }
        startService(intent)
    }
}

@Composable
fun AppContent(
    modifier: Modifier = Modifier,
    onStatusChange: (Boolean) -> Unit = {}
) {
    var isFull by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(
                if (isFull) R.drawable.baseline_air_24
                else R.drawable.baseline_air_24
            ),
            contentDescription = "Stato contenitore",
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                isFull = !isFull
                onStatusChange(isFull)
            }
        ) {
            Text(if (isFull) "Segna come VUOTO" else "Segna come PIENO")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Stato attuale: ${if (isFull) "PIENO" else "VUOTO"}",
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
fun StatusChangeScreen(
    onStatusChange: (Boolean) -> Unit
) {
    var isFull by remember { mutableStateOf(false) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(
                    if (isFull) R.drawable.baseline_air_24
                    else R.drawable.baseline_air_24
                ),
                contentDescription = "Contenitore",
                modifier = Modifier.size(96.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    isFull = !isFull
                    onStatusChange(isFull)
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = if (isFull) "CONTENITORE PIENO - Clicca per vuotare"
                    else "CONTENITORE VUOTO - Clicca per riempire",
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    TankCheckTheme {
        AppContent(modifier = Modifier.fillMaxSize())
    }
}

@Preview(showBackground = true)
@Composable
fun StatusChangePreview() {
    TankCheckTheme {
        StatusChangeScreen(onStatusChange = {})
    }
}