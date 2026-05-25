package com.example.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.EventEntity
import com.example.ui.PlannerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEventScreen(
    viewModel: PlannerViewModel,
    eventId: Int?, // if null, is in CREATE mode
    onNavigateBack: () -> Unit
) {
    val events by viewModel.events.collectAsState()
    val isEditMode = eventId != null && eventId > 0
    val targetEvent = remember(eventId, events) {
        if (isEditMode) events.find { it.id == eventId } else null
    }

    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Wedding") }
    var startDate by remember { mutableStateOf("2026-08-10") }
    var endDate by remember { mutableStateOf("2026-08-15") }
    var city by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var venueName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var guestsCount by remember { mutableStateOf("") }
    var budgetMin by remember { mutableStateOf("") }
    var budgetMax by remember { mutableStateOf("") }
    var budgetCurrency by remember { mutableStateOf("USD") }
    var notes by remember { mutableStateOf("") }
    var isDraft by remember { mutableStateOf(false) }

    // Initialize values if in edit mode
    LaunchedEffect(targetEvent) {
        targetEvent?.let {
            name = it.name
            type = it.type
            startDate = it.startDate
            endDate = it.endDate
            city = it.locationCity
            country = it.locationCountry
            venueName = it.venueName
            address = it.address
            guestsCount = it.guestsCount.toString()
            budgetMin = it.budgetMin.toInt().toString()
            budgetMax = it.budgetMax.toInt().toString()
            budgetCurrency = it.budgetCurrency
            notes = it.notes
            isDraft = it.isDraft
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Event Portfolio" else "Plan Global Event") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_button_event_form")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        modifier = Modifier.testTag("add_edit_event_root")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section title: Core Specifications
            Text(
                text = "Core Event Specifications",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Event Portfolio Name") },
                placeholder = { Text("e.g. Sofia & Matteo's Destination Wedding") },
                leadingIcon = { Icon(Icons.Default.Celebration, "Event Title Icon") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("event_name_field")
            )

            // Event Type Dropdown
            var typeExpanded by remember { mutableStateOf(false) }
            val eventTypesList = listOf("Wedding", "Funeral / Memorial", "Birthday party", "Corporate event", "Social gathering", "Custom event")
            ExposedDropdownMenuBox(
                expanded = typeExpanded,
                onExpandedChange = { typeExpanded = !typeExpanded }
            ) {
                OutlinedTextField(
                    value = type,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Event Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                    leadingIcon = { Icon(Icons.Default.Style, "Event Type Icon") },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false }
                ) {
                    eventTypesList.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                type = item
                                typeExpanded = false
                            }
                        )
                    }
                }
            }

            // Dates Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Start Date") },
                    placeholder = { Text("YYYY-MM-DD") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, "Start Date Icon") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("event_start_date_field")
                )

                OutlinedTextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    label = { Text("End Date") },
                    placeholder = { Text("YYYY-MM-DD") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, "End Date Icon") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("event_end_date_field")
                )
            }

            Divider()

            // Locality and Logistics
            Text(
                text = "Locality & Logistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("Target City") },
                    placeholder = { Text("e.g. Florence") },
                    leadingIcon = { Icon(Icons.Default.LocationCity, "City Icon") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("event_city_field")
                )

                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("Target Country") },
                    placeholder = { Text("e.g. Italy") },
                    leadingIcon = { Icon(Icons.Default.Public, "Country Icon") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("event_country_field")
                )
            }

            OutlinedTextField(
                value = venueName,
                onValueChange = { venueName = it },
                label = { Text("Venue Name") },
                placeholder = { Text("e.g. Villa Cora") },
                leadingIcon = { Icon(Icons.Default.Place, "Venue Icon") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Full Venue Address") },
                placeholder = { Text("e.g. Viale Machiavelli, 18, 50125 Firenze") },
                leadingIcon = { Icon(Icons.Default.Map, "Address Icon") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = guestsCount,
                onValueChange = { guestsCount = it },
                label = { Text("Estimated Guests Count") },
                leadingIcon = { Icon(Icons.Default.People, "Guests Icon") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Divider()

            // Financial & Cultural Parameters
            Text(
                text = "Financial & Cultural Parameters",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Currency dropdown
                var curExpanded by remember { mutableStateOf(false) }
                val currencyList = listOf("USD", "EUR", "GBP", "JPY", "INR", "CAD", "AUD", "ZAR", "BRL", "CHF")
                ExposedDropdownMenuBox(
                    expanded = curExpanded,
                    onExpandedChange = { curExpanded = !curExpanded },
                    modifier = Modifier.width(105.dp)
                ) {
                    OutlinedTextField(
                        value = budgetCurrency,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cur.") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = curExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = curExpanded,
                        onDismissRequest = { curExpanded = false }
                    ) {
                        currencyList.forEach { cur ->
                            DropdownMenuItem(
                                text = { Text(cur) },
                                onClick = {
                                    budgetCurrency = cur
                                    curExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = budgetMin,
                    onValueChange = { budgetMin = it },
                    label = { Text("Min Budget") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("event_budget_min_field")
                )

                OutlinedTextField(
                    value = budgetMax,
                    onValueChange = { budgetMax = it },
                    label = { Text("Max Budget") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("event_budget_max_field")
                )
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Special Requirements / Cultural Notes") },
                placeholder = { Text("e.g., Religious, dietary, local customs, ETIAS visa constraints...") },
                leadingIcon = { Icon(Icons.Default.Language, "Cultural Notes Icon") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            )

            // Save as draft switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.Drafts,
                        contentDescription = "Draft Status Info",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Save as Draft",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Draft events are hidden from the calendar panel.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isDraft,
                    onCheckedChange = { isDraft = it },
                    modifier = Modifier.testTag("event_draft_switch")
                )
            }

            // Submission Button Actions
            Button(
                onClick = {
                    val guestsVal = guestsCount.toIntOrNull() ?: 1
                    val minB = budgetMin.toDoubleOrNull() ?: 0.0
                    val maxB = budgetMax.toDoubleOrNull() ?: 0.0

                    if (name.isBlank() || city.isBlank() || country.isBlank()) {
                        // Incomplete fields validation error simply avoided via smart fallback
                        return@Button
                    }

                    if (isEditMode) {
                        viewModel.updateEvent(
                            id = eventId!!,
                            name = name,
                            type = type,
                            startDate = startDate,
                            endDate = endDate,
                            city = city,
                            country = country,
                            venue = venueName,
                            address = address,
                            guests = guestsVal,
                            budgetMin = minB,
                            budgetMax = maxB,
                            currency = budgetCurrency,
                            notes = notes,
                            isDraft = isDraft
                        )
                        onNavigateBack()
                    } else {
                        viewModel.createEvent(
                            name = name,
                            type = type,
                            startDate = startDate,
                            endDate = endDate,
                            city = city,
                            country = country,
                            venue = venueName,
                            address = address,
                            guests = guestsVal,
                            budgetMin = minB,
                            budgetMax = maxB,
                            currency = budgetCurrency,
                            notes = notes,
                            isDraft = isDraft,
                            onSuccess = { _ ->
                                onNavigateBack()
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_event_form_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save Icon")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditMode) "Save Changes" else "Initialize Event Portfolio",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
