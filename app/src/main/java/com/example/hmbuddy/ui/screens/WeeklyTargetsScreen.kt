package com.example.hmbuddy.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.hmbuddy.data.RaceGoal
import com.example.hmbuddy.ui.theme.TargetsPurple
import com.example.hmbuddy.util.FormatUtils
import com.example.hmbuddy.viewmodel.TargetViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyTargetsScreen(
    targetViewModel: TargetViewModel,
    onTargetsSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTarget by targetViewModel.weeklyTarget.collectAsState()
    val currentRaceGoal by targetViewModel.raceGoal.collectAsState()

    var zone2Minutes by remember { mutableStateOf("") }
    var zone2Seconds by remember { mutableStateOf("") }
    var zone2Note by remember { mutableStateOf("") }
    var tempoMinutes by remember { mutableStateOf("") }
    var tempoSeconds by remember { mutableStateOf("") }
    var tempoNote by remember { mutableStateOf("") }
    var weeklyDuration by remember { mutableStateOf("") }

    // Race goal state
    var isEditingRaceGoal by remember { mutableStateOf(false) }
    var raceName by remember { mutableStateOf("") }
    var raceDate by remember { mutableStateOf<LocalDate?>(null) }
    var targetHours by remember { mutableStateOf("") }
    var targetMinutes by remember { mutableStateOf("") }
    var targetSeconds by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(currentTarget) {
        currentTarget?.let { target ->
            val z2Mins = target.zone2PaceSecondsPerKm / 60
            val z2Secs = target.zone2PaceSecondsPerKm % 60
            zone2Minutes = z2Mins.toString()
            zone2Seconds = z2Secs.toString().padStart(2, '0')
            zone2Note = target.zone2Note

            val tMins = target.tempoPaceSecondsPerKm / 60
            val tSecs = target.tempoPaceSecondsPerKm % 60
            tempoMinutes = tMins.toString()
            tempoSeconds = tSecs.toString().padStart(2, '0')
            tempoNote = target.tempoNote

            weeklyDuration = target.weeklyDurationMinutes.toString()
        }
    }

    LaunchedEffect(currentRaceGoal) {
        currentRaceGoal?.let { goal ->
            raceName = goal.raceName
            raceDate = goal.raceDate
            goal.targetTimeSeconds?.let { seconds ->
                targetHours = (seconds / 3600).toString()
                targetMinutes = ((seconds % 3600) / 60).toString().padStart(2, '0')
                targetSeconds = (seconds % 60).toString().padStart(2, '0')
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = raceDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
                ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        raceDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Race Goal Section
        Text(
            text = "Race Goal",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Optional - Set your upcoming race target",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (currentRaceGoal != null && !isEditingRaceGoal) {
            // Show current race goal
            RaceGoalCard(
                raceGoal = currentRaceGoal!!,
                onEdit = { isEditingRaceGoal = true },
                onClear = { targetViewModel.clearRaceGoal() }
            )
        } else if (isEditingRaceGoal || currentRaceGoal == null) {
            // Show edit form or empty state
            RaceGoalEditCard(
                raceName = raceName,
                raceDate = raceDate,
                targetHours = targetHours,
                targetMinutes = targetMinutes,
                targetSeconds = targetSeconds,
                onRaceNameChange = { raceName = it },
                onDateClick = { showDatePicker = true },
                onTargetHoursChange = { targetHours = it.filter { c -> c.isDigit() }.take(1) },
                onTargetMinutesChange = { targetMinutes = it.filter { c -> c.isDigit() }.take(2) },
                onTargetSecondsChange = { targetSeconds = it.filter { c -> c.isDigit() }.take(2) },
                onSave = {
                    if (raceName.isNotBlank() && raceDate != null) {
                        val totalSeconds = if (targetHours.isNotBlank() || targetMinutes.isNotBlank()) {
                            (targetHours.toIntOrNull() ?: 0) * 3600 +
                                    (targetMinutes.toIntOrNull() ?: 0) * 60 +
                                    (targetSeconds.toIntOrNull() ?: 0)
                        } else null
                        targetViewModel.saveRaceGoal(raceName, raceDate!!, totalSeconds)
                        isEditingRaceGoal = false
                    }
                },
                onCancel = if (currentRaceGoal != null) {
                    {
                        isEditingRaceGoal = false
                        // Reset to current values
                        currentRaceGoal?.let { goal ->
                            raceName = goal.raceName
                            raceDate = goal.raceDate
                            goal.targetTimeSeconds?.let { seconds ->
                                targetHours = (seconds / 3600).toString()
                                targetMinutes = ((seconds % 3600) / 60).toString().padStart(2, '0')
                                targetSeconds = (seconds % 60).toString().padStart(2, '0')
                            }
                        }
                    }
                } else null,
                isEditing = currentRaceGoal != null
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Weekly Targets Section
        Text(
            text = "Weekly Training",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Set your weekly pace and duration targets",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
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

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = zone2Note,
                    onValueChange = { zone2Note = it },
                    label = { Text("Zone 2 Note") },
                    placeholder = { Text("e.g., Keep heart rate below 150 bpm") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
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

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = tempoNote,
                    onValueChange = { tempoNote = it },
                    label = { Text("Tempo Note") },
                    placeholder = { Text("e.g., Focus on consistent splits") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = weeklyDuration,
                    onValueChange = { weeklyDuration = it.filter { c -> c.isDigit() } },
                    label = { Text("Weekly Duration Target (minutes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val z2PaceSecs = (zone2Minutes.toIntOrNull() ?: 0) * 60 + (zone2Seconds.toIntOrNull() ?: 0)
                val tempoPaceSecs = (tempoMinutes.toIntOrNull() ?: 0) * 60 + (tempoSeconds.toIntOrNull() ?: 0)
                val duration = weeklyDuration.toIntOrNull() ?: 0

                if (z2PaceSecs > 0 && tempoPaceSecs > 0 && duration > 0) {
                    targetViewModel.saveWeeklyTarget(z2PaceSecs, tempoPaceSecs, duration, zone2Note, tempoNote)
                    onTargetsSaved()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = zone2Minutes.isNotBlank() && tempoMinutes.isNotBlank() && weeklyDuration.isNotBlank()
        ) {
            Text("Save Weekly Targets")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun RaceGoalCard(
    raceGoal: RaceGoal,
    onEdit: () -> Unit,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(TargetsPurple.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = TargetsPurple,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = raceGoal.raceName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = raceGoal.raceDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    raceGoal.targetTimeSeconds?.let { seconds ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Target: ${FormatUtils.formatRaceTime(seconds)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TargetsPurple
                        )
                    }
                }
            }
            IconButton(onClick = onClear) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear race goal",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RaceGoalEditCard(
    raceName: String,
    raceDate: LocalDate?,
    targetHours: String,
    targetMinutes: String,
    targetSeconds: String,
    onRaceNameChange: (String) -> Unit,
    onDateClick: () -> Unit,
    onTargetHoursChange: (String) -> Unit,
    onTargetMinutesChange: (String) -> Unit,
    onTargetSecondsChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: (() -> Unit)?,
    isEditing: Boolean
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (!isEditing) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = {}),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(TargetsPurple.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = TargetsPurple,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Set your race goal",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = raceName,
                onValueChange = onRaceNameChange,
                label = { Text("Race Name") },
                placeholder = { Text("e.g., Vancouver Half Marathon") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = raceDate?.format(DateTimeFormatter.ofPattern("MMM d, yyyy")) ?: "",
                onValueChange = {},
                label = { Text("Race Date") },
                placeholder = { Text("Select date") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onDateClick),
                enabled = false,
                trailingIcon = {
                    IconButton(onClick = onDateClick) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Select date"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Target Time (optional)",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = targetHours,
                    onValueChange = onTargetHoursChange,
                    label = { Text("H") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                Text(
                    text = ":",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                OutlinedTextField(
                    value = targetMinutes,
                    onValueChange = onTargetMinutesChange,
                    label = { Text("M") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                Text(
                    text = ":",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                OutlinedTextField(
                    value = targetSeconds,
                    onValueChange = onTargetSecondsChange,
                    label = { Text("S") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (onCancel != null) {
                    TextButton(onClick = onCancel) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Button(
                    onClick = onSave,
                    enabled = raceName.isNotBlank() && raceDate != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TargetsPurple
                    )
                ) {
                    Text(if (isEditing) "Update" else "Save Race Goal")
                }
            }
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
