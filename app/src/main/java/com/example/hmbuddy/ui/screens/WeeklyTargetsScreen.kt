package com.example.hmbuddy.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.hmbuddy.viewmodel.TargetViewModel

@Composable
fun WeeklyTargetsScreen(
    targetViewModel: TargetViewModel,
    onTargetsSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTarget by targetViewModel.weeklyTarget.collectAsState()

    var zone2Minutes by remember { mutableStateOf("") }
    var zone2Seconds by remember { mutableStateOf("") }
    var tempoMinutes by remember { mutableStateOf("") }
    var tempoSeconds by remember { mutableStateOf("") }
    var weeklyDuration by remember { mutableStateOf("") }

    LaunchedEffect(currentTarget) {
        currentTarget?.let { target ->
            val z2Mins = target.zone2PaceSecondsPerKm / 60
            val z2Secs = target.zone2PaceSecondsPerKm % 60
            zone2Minutes = z2Mins.toString()
            zone2Seconds = z2Secs.toString().padStart(2, '0')

            val tMins = target.tempoPaceSecondsPerKm / 60
            val tSecs = target.tempoPaceSecondsPerKm % 60
            tempoMinutes = tMins.toString()
            tempoSeconds = tSecs.toString().padStart(2, '0')

            weeklyDuration = target.weeklyDurationMinutes.toString()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Weekly Targets",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Zone 2 Pace Target (min/km)",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        PaceInput(
            minutes = zone2Minutes,
            seconds = zone2Seconds,
            onMinutesChange = { zone2Minutes = it },
            onSecondsChange = { zone2Seconds = it }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Tempo Pace Target (min/km)",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        PaceInput(
            minutes = tempoMinutes,
            seconds = tempoSeconds,
            onMinutesChange = { tempoMinutes = it },
            onSecondsChange = { tempoSeconds = it }
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = weeklyDuration,
            onValueChange = { weeklyDuration = it.filter { c -> c.isDigit() } },
            label = { Text("Weekly Duration Target (minutes)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val z2PaceSecs = (zone2Minutes.toIntOrNull() ?: 0) * 60 + (zone2Seconds.toIntOrNull() ?: 0)
                val tempoPaceSecs = (tempoMinutes.toIntOrNull() ?: 0) * 60 + (tempoSeconds.toIntOrNull() ?: 0)
                val duration = weeklyDuration.toIntOrNull() ?: 0

                if (z2PaceSecs > 0 && tempoPaceSecs > 0 && duration > 0) {
                    targetViewModel.saveWeeklyTarget(z2PaceSecs, tempoPaceSecs, duration)
                    onTargetsSaved()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = zone2Minutes.isNotBlank() && tempoMinutes.isNotBlank() && weeklyDuration.isNotBlank()
        ) {
            Text("Save Targets")
        }
    }
}

@Composable
private fun PaceInput(
    minutes: String,
    seconds: String,
    onMinutesChange: (String) -> Unit,
    onSecondsChange: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = minutes,
            onValueChange = { onMinutesChange(it.filter { c -> c.isDigit() }.take(2)) },
            label = { Text("Min") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(":", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.width(8.dp))

        OutlinedTextField(
            value = seconds,
            onValueChange = { onSecondsChange(it.filter { c -> c.isDigit() }.take(2)) },
            label = { Text("Sec") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
    }
}
