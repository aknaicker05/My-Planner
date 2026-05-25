package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EventEntity
import com.example.ui.PlannerViewModel
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyEventsScreen(
    viewModel: PlannerViewModel,
    onNavigateToAddEvent: () -> Unit,
    onNavigateToEditEvent: (Int) -> Unit,
    onNavigateToDetail: (Int) -> Unit
) {
    val events by viewModel.events.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    // Filters: 0 = All, 1 = Active, 2 = Drafts
    var selectedFilterIndex by remember { mutableStateOf(0) }
    val filters = listOf("All Portfolios", "Active", "Drafts Only")

    // Sort and filter logically
    val filteredEvents = remember(events, searchQuery, selectedFilterIndex) {
        events.filter { event ->
            // Search Query Filter
            val matchesSearch = event.name.contains(searchQuery, ignoreCase = true) ||
                    event.locationCity.contains(searchQuery, ignoreCase = true) ||
                    event.locationCountry.contains(searchQuery, ignoreCase = true) ||
                    event.type.contains(searchQuery, ignoreCase = true)
            
            // Status Tab Filter
            val matchesStatus = when (selectedFilterIndex) {
                1 -> !event.isDraft
                2 -> event.isDraft
                else -> true
            }

            matchesSearch && matchesStatus
        }
    }

    var eventToDelete by remember { mutableStateOf<EventEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddEvent,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_event_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Schedule New Event")
            }
        },
        modifier = Modifier.testTag("my_events_root")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Top search banner
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Event Portfolios",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name, city, country, type...") },
                    leadingIcon = { Icon(Icons.Default.Search, "Search icon") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_events_input")
                )

                // Tab Filter Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filters.forEachIndexed { index, title ->
                        FilterChip(
                            selected = selectedFilterIndex == index,
                            onClick = { selectedFilterIndex = index },
                            label = { Text(title) },
                            modifier = Modifier.weight(1f).testTag("filter_chip_$index")
                        )
                    }
                }
            }

            // Events List
            if (filteredEvents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = "No Events",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = if (searchQuery.isEmpty()) "Your event book is empty!" else "No matching events found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (searchQuery.isEmpty()) "Tap the '+' action button to deploy your first Wedding, Corporate Summit, or Personal gathering." else "Try adjusting your spelling or category labels.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredEvents, key = { it.id }) { event ->
                        MyEventCardItem(
                            event = event,
                            onCardClick = { onNavigateToDetail(event.id) },
                            onEditClick = { onNavigateToEditEvent(event.id) },
                            onDuplicateClick = {
                                viewModel.duplicateEvent(event.id) { newId ->
                                    // cloned successfully
                                }
                            },
                            onDeleteRequest = { eventToDelete = event },
                            viewModel = viewModel
                        )
                    }
                }
            }
        }

        // Delete Confirmation dialog
        eventToDelete?.let { event ->
            AlertDialog(
                onDismissRequest = { eventToDelete = null },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteEvent(event) {
                                eventToDelete = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete Event")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { eventToDelete = null }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Delete Event Portfolio?") },
                text = { Text("This will permanently incinerate '${event.name}', including all associated travel itineraries, hotel bookings, guests and tasks database entries. This is non-reversible.") }
            )
        }
    }
}

@Composable
fun MyEventCardItem(
    event: EventEntity,
    onCardClick: () -> Unit,
    onEditClick: () -> Unit,
    onDuplicateClick: () -> Unit,
    onDeleteRequest: () -> Unit,
    viewModel: PlannerViewModel
) {
    // Collect the live tasks for this event to render exact progress bar details
    val tasksFlow = remember(event.id) { viewModel.getTasksForEvent(event.id) }
    val tasks by tasksFlow.collectAsState(initial = emptyList())

    val totalTasks = tasks.size
    val doneTasks = tasks.count { it.status == "Done" }
    val progressFraction = if (totalTasks > 0) doneTasks.toFloat() / totalTasks else 0f
    val progressPercentage = (progressFraction * 100).toInt()

    val typeIcon = when (event.type) {
        "Wedding" -> Icons.Default.Favorite
        "Funeral / Memorial" -> Icons.Default.WavingHand
        "Birthday party" -> Icons.Default.Cake
        "Corporate event" -> Icons.Default.Business
        "Social gathering" -> Icons.Default.Groups
        else -> Icons.Default.Celebration
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .testTag("event_portfolio_card_${event.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: Logo, Name, Draft Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = typeIcon,
                        contentDescription = event.type,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = event.type,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (event.isDraft) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.tertiaryContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Draft",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Location + Date Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "Pin Icon",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${event.locationCity}, ${event.locationCountry}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Date Icon",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.startDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Progress bar
            LinearProgressIndicator(
                progress = progressFraction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$progressPercentage% core checklist",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Inline quick actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(36.dp).testTag("edit_event_btn_${event.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Event Detail",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDuplicateClick,
                        modifier = Modifier.size(36.dp).testTag("duplicate_event_btn_${event.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Duplicate Event",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDeleteRequest,
                        modifier = Modifier.size(36.dp).testTag("delete_event_btn_${event.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Event",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
