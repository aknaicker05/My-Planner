package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.PlannerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: PlannerViewModel,
    onLogoutSuccess: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val scrollState = rememberScrollState()

    var name by remember { mutableStateOf("") }
    var countryCity by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("") }
    var timezone by remember { mutableStateOf("") }
    var plannerType by remember { mutableStateOf("") }

    // Synchronize state once user triggers
    LaunchedEffect(user) {
        user?.let {
            name = it.name
            countryCity = it.countryCity
            language = it.preferredLanguage
            timezone = it.timeZone
            plannerType = it.plannerType
        }
    }

    var showSavedToast by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(20.dp)
            .testTag("profile_root"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar Shell
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "User avatar",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        }

        Text(
            text = user?.email ?: "account@globalmoments.com",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        // Success Snack Indicator
        if (showSavedToast) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, "Success", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Global profile settings synchronized locally!",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Editable components
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Display Name") },
            leadingIcon = { Icon(Icons.Default.Badge, "Name Icon") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_name_field")
        )

        OutlinedTextField(
            value = countryCity,
            onValueChange = { countryCity = it },
            label = { Text("City & Country") },
            leadingIcon = { Icon(Icons.Default.Home, "Residence Icon") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_location_field")
        )

        OutlinedTextField(
            value = language,
            onValueChange = { language = it },
            label = { Text("Preferred System Language") },
            leadingIcon = { Icon(Icons.Default.Language, "Language Icon") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = timezone,
            onValueChange = { timezone = it },
            label = { Text("Operating Timezone") },
            leadingIcon = { Icon(Icons.Default.Timer, "Timezone Icon") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Planner Type Dropdown Selector
        var plannerTypeExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = plannerTypeExpanded,
            onExpandedChange = { plannerTypeExpanded = !plannerTypeExpanded }
        ) {
            OutlinedTextField(
                value = plannerType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Planning Profile Classification") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = plannerTypeExpanded) },
                leadingIcon = { Icon(Icons.Default.SupervisedUserCircle, "Role Icon") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = plannerTypeExpanded,
                onDismissRequest = { plannerTypeExpanded = false }
            ) {
                listOf("Individual", "Professional Planner", "Organization").forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            plannerType = type
                            plannerTypeExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Save Button
        Button(
            onClick = {
                viewModel.updateProfile(
                    name = name.trim(),
                    countryCity = countryCity.trim(),
                    language = language.trim(),
                    timezone = timezone.trim(),
                    plannerType = plannerType
                )
                showSavedToast = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("profile_save_btn")
        ) {
            Icon(Icons.Default.Save, contentDescription = "Save settings")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Update Local Profile State", fontWeight = FontWeight.Bold)
        }

        // Logout Button
        OutlinedButton(
            onClick = {
                viewModel.logout {
                    onLogoutSuccess()
                }
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("profile_logout_btn")
        ) {
            Icon(Icons.Default.PowerSettingsNew, contentDescription = "Log out icon")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Close Planner Session", fontWeight = FontWeight.Bold)
        }
    }
}
