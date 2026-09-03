package com.example.bbmtracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.bbmtracker.data.*
import com.example.bbmtracker.service.LocationService
import com.example.bbmtracker.ui.*

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()

        val database = AppDatabase.getInstance(applicationContext)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainScreen(database = database)
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isNotEmpty()) {
            requestPermissionLauncher.launch(notGranted.toTypedArray())
        }
    }
}

@Composable
fun MainScreen(database: AppDatabase) {
    var selectedScreen by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    val startOfToday = remember { DateUtils.getStartOfToday() }
    val startOfWeek = remember { DateUtils.getStartOfWeek() }
    val currentTimestamp = remember { DateUtils.getCurrentTimestamp() }

    val dailySummary by database.reportDao().getSummaryBetween(startOfToday, currentTimestamp)
        .collectAsState(initial = PeriodSummary(0.0, 0.0, 0.0, 0))

    val weeklySummary by database.reportDao().getSummaryBetween(startOfWeek, currentTimestamp)
        .collectAsState(initial = PeriodSummary(0.0, 0.0, 0.0, 0))

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedScreen == 0,
                    onClick = { selectedScreen = 0 },
                    label = { Text("Dashboard") },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") }
                )
                NavigationBarItem(
                    selected = selectedScreen == 1,
                    onClick = { selectedScreen = 1 },
                    label = { Text("Laporan") },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Laporan") }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (selectedScreen) {
                0 -> DashboardScreen(
                    onStartTracking = {
                        val intent = Intent(context, LocationService::class.java).apply { action = "ACTION_START" }
                        context.startService(intent)
                    },
                    onStopTracking = {
                        val intent = Intent(context, LocationService::class.java).apply { action = "ACTION_STOP" }
                        context.startService(intent)
                    },
                    onResetDaily = { }
                )
                1 -> ReportScreen(dailySummary = dailySummary, weeklySummary = weeklySummary)
            }
        }
    }
}
