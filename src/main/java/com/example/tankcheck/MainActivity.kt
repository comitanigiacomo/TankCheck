package com.example.tankcheck

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var statusRef: com.google.firebase.database.DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            Log.e("CRASH", "Crash dell'app", e)
            finish()
        }

        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        database = Firebase.database
        statusRef = database.getReference("containerStatus")

        createNotificationChannel()
        startService(Intent(this, StatusBarService::class.java))

        enableEdgeToEdge()

        signInAnonymously()
    }

    private fun signInAnonymously() {
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("AUTH", "signInAnonymously:success")
                    proceedAfterAuth()
                } else {
                    Log.w("AUTH", "signInAnonymously:failure", task.exception)
                }
            }
    }

    private fun proceedAfterAuth() {
        if (intent?.action != Intent.ACTION_MAIN) {
            showStatusChangeUI()
        } else {
            setContent {
                TankCheckTheme {
                    AppContent(
                        modifier = Modifier.fillMaxSize(),
                        onStatusChange = { newStatus ->
                            updateFirebaseStatus(newStatus)
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
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Mostra lo stato del contenitore"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                enableVibration(false)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun showStatusChangeUI() {
        setContent {
            TankCheckTheme {
                StatusChangeScreen(
                    onStatusChange = { newStatus ->
                        updateFirebaseStatus(newStatus)
                    }
                )
            }
        }
    }

    private fun updateFirebaseStatus(isFull: Boolean) {
        try {
            statusRef.setValue(isFull)
                .addOnSuccessListener {
                    updateServiceStatus(isFull)
                }
                .addOnFailureListener { e ->
                    Log.e("FIREBASE", "Errore scrittura", e)
                }
        } catch (e: Exception) {
            Log.e("FIREBASE", "Errore inizializzazione", e)
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
    val database = Firebase.database
    val statusRef = database.getReference("containerStatus")

    var isFull by remember { mutableStateOf(false) }
    var isAuthenticated by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        Firebase.auth.addAuthStateListener { auth ->
            isAuthenticated = auth.currentUser != null
        }
    }

    DisposableEffect(isAuthenticated) {
        if (!isAuthenticated) {
            return@DisposableEffect onDispose {}
        }

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(Boolean::class.java)?.let { status ->
                    isFull = status
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FIREBASE", "Database error", error.toException())
            }
        }

        statusRef.addValueEventListener(valueEventListener)

        onDispose {
            statusRef.removeEventListener(valueEventListener)
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!isAuthenticated) {
            Text("Autenticazione in corso...")
        } else {
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
                    onStatusChange(!isFull)
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
}

@Composable
fun StatusChangeScreen(
    onStatusChange: (Boolean) -> Unit
) {
    val database = Firebase.database
    val statusRef = database.getReference("containerStatus")

    var isFull by remember { mutableStateOf(false) }
    var isAuthenticated by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        Firebase.auth.addAuthStateListener { auth ->
            isAuthenticated = auth.currentUser != null
        }
    }


    DisposableEffect(isAuthenticated) {
        if (!isAuthenticated) {
            return@DisposableEffect onDispose {}
        }

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(Boolean::class.java)?.let { status ->
                    isFull = status
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FIREBASE", "Database error", error.toException())
            }
        }

        statusRef.addValueEventListener(valueEventListener)

        onDispose {
            statusRef.removeEventListener(valueEventListener)
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isAuthenticated) {
                Text("Autenticazione in corso...")
            } else {
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
                        onStatusChange(!isFull)
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