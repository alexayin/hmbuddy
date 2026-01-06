package com.example.hmbuddy.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.hmbuddy.data.Gender
import com.example.hmbuddy.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel,
    onProfileSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentProfile by profileViewModel.userProfile.collectAsState()

    var name by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf(Gender.MALE) }
    var age by remember { mutableStateOf("") }

    LaunchedEffect(currentProfile) {
        currentProfile?.let { profile ->
            name = profile.name
            selectedGender = profile.gender
            age = profile.age.toString()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "User Profile",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Gender",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(modifier = Modifier.selectableGroup()) {
            Gender.entries.forEach { gender ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .selectable(
                            selected = (gender == selectedGender),
                            onClick = { selectedGender = gender },
                            role = Role.RadioButton
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (gender == selectedGender),
                        onClick = null
                    )
                    Text(
                        text = gender.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = age,
            onValueChange = { age = it.filter { c -> c.isDigit() }.take(3) },
            label = { Text("Age") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val ageValue = age.toIntOrNull() ?: 0
                if (name.isNotBlank() && ageValue > 0) {
                    profileViewModel.saveUserProfile(name, selectedGender, ageValue)
                    onProfileSaved()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank() && age.isNotBlank()
        ) {
            Text("Save Profile")
        }
    }
}
