package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.PlannerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: PlannerViewModel,
    onAuthSuccess: () -> Unit
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    // Additional profile fields for registration
    var name by remember { mutableStateOf("") }
    var countryCity by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("English (US)") }
    var timezone by remember { mutableStateOf("GMT-7") }
    var plannerType by remember { mutableStateOf("Individual") }

    val loginError by viewModel.loginError.collectAsState()
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .widthIn(max = 500.dp)
                .padding(vertical = 24.dp)
                .testTag("auth_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shape = RoundedCornerShape(32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Display
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = "My Planner Globe",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                )

                Text(
                    text = "My Planner",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (isRegisterMode) "Create your professional or personal planner profile" else "Access your world-wide events dashboard",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Error Message Display
                loginError?.let { err ->
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                }

                // Core Fields: Email & Password
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, "Email Icon") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("email_input")
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, "Lock Icon") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_input")
                )

                // Registration Mode Fields
                AnimatedVisibility(
                    visible = isRegisterMode,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Your Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, "Profile Icon") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("name_input")
                        )

                        OutlinedTextField(
                            value = countryCity,
                            onValueChange = { countryCity = it },
                            label = { Text("Default Country/City") },
                            leadingIcon = { Icon(Icons.Default.Place, "Location Icon") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("country_city_input")
                        )

                        OutlinedTextField(
                            value = language,
                            onValueChange = { language = it },
                            label = { Text("Preferred Language") },
                            leadingIcon = { Icon(Icons.Default.Translate, "Language Icon") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = timezone,
                            onValueChange = { timezone = it },
                            label = { Text("Primary Time Zone") },
                            leadingIcon = { Icon(Icons.Default.Schedule, "Time Zone Icon") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Planner Type Picker dropdown or field
                        var plannerTypeExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = plannerTypeExpanded,
                            onExpandedChange = { plannerTypeExpanded = !plannerTypeExpanded }
                        ) {
                            OutlinedTextField(
                                value = plannerType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Event Planner Role") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = plannerTypeExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = plannerTypeExpanded,
                                onDismissRequest = { plannerTypeExpanded = false }
                            ) {
                                listOf("Individual", "Professional Planner", "Organization").forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(item) },
                                        onClick = {
                                            plannerType = item
                                            plannerTypeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Submit Action Button
                Button(
                    onClick = {
                        if (isRegisterMode) {
                            viewModel.register(
                                email = email.trim(),
                                passwordHash = password,
                                name = name.trim(),
                                countryCity = countryCity.trim(),
                                language = language,
                                timezone = timezone,
                                plannerType = plannerType,
                                onSuccess = onAuthSuccess
                            )
                        } else {
                            viewModel.login(
                                email = email.trim(),
                                passwordHash = password,
                                onSuccess = onAuthSuccess
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("auth_submit_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isRegisterMode) "Register Account" else "Sign In",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Mode Toggle Link
                TextButton(
                    onClick = { isRegisterMode = !isRegisterMode },
                    modifier = Modifier.testTag("auth_mode_toggle")
                ) {
                    Text(
                        text = if (isRegisterMode) "Already have an account? Sign In" else "New to Global Planner? Create an Account",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                // Professional Immediate Demonstration Access
                Button(
                    onClick = {
                        // Populate default test credentials
                        email = "aknaicker05@gmail.com"
                        password = "password"
                        viewModel.login("aknaicker05@gmail.com", "password", onAuthSuccess)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("demo_login_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Celebration, contentDescription = "Demo Play Icon", modifier = Modifier.padding(end = 8.dp))
                    Text(
                        text = "Instant Demo Login",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
