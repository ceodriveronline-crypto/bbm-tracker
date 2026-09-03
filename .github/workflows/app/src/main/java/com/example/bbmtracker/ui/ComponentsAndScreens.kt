package com.example.bbmtracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bbmtracker.data.PeriodSummary

@Composable
fun MetricCard(title: String, value: String, unit: String, highlightColor: Color = MaterialTheme.colorScheme.primary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = highlightColor)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = unit, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun DashboardScreen(
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    onResetDaily: () -> Unit
) {
    var isTracking by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Pelacak BBM", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            IconButton(onClick = { showResetDialog = true }) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset Sesi Harian", tint = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        MetricCard(title = "Jarak Sesi Ini", value = "0.0", unit = "KM")
        Spacer(modifier = Modifier.height(12.dp))
        MetricCard(title = "Estimasi BBM Terpakai", value = "0.00", unit = "Liter")

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (isTracking) onStopTracking() else onStartTracking()
                isTracking = !isTracking
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isTracking) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text(if (isTracking) "BERHENTI" else "MULAI PERJALANAN", fontSize = 18.sp)
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Sesi Harian?") },
            text = { Text("Jarak dan konsumsi BBM sesi aktif akan direset kembali ke 0.") },
            confirmButton = {
                TextButton(onClick = {
                    showResetDialog = false
                    onResetDaily()
                }) { Text("Reset", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Batal") }
            }
        )
    }
}

@Composable
fun ReportScreen(dailySummary: PeriodSummary, weeklySummary: PeriodSummary) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val currentSummary = if (selectedTab == 0) dailySummary else weeklySummary

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Laporan Operasional", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Hari Ini") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("7 Hari Terakhir") })
        }

        Spacer(modifier = Modifier.height(24.dp))
        MetricCard(title = "Total Jarak Tempuh", value = String.format("%.2f", (currentSummary.totalDistanceMeters ?: 0.0) / 1000.0), unit = "KM")
        Spacer(modifier = Modifier.height(12.dp))
        MetricCard(title = "BBM Terpakai", value = String.format("%.3f", currentSummary.totalFuelLiters ?: 0.0), unit = "Liter")
    }
}
