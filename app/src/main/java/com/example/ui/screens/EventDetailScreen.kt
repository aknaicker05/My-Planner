package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.PlannerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    viewModel: PlannerViewModel,
    eventId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Int) -> Unit
) {
    // Select the active event first
    LaunchedEffect(eventId) {
        viewModel.selectEvent(eventId)
    }

    val event by viewModel.selectedEvent.collectAsState()
    val segments by viewModel.travelSegments.collectAsState()
    val accommodations by viewModel.accommodations.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val vendors by viewModel.vendors.collectAsState()
    val guests by viewModel.guests.collectAsState()

    var selectedTabIdx by remember { mutableStateOf(0) }
    val tabTitles = listOf("Overview", "Travel & Beds", "Tasks Board", "Vendors", "Guests List")

    // Overlay dialog triggers
    var showAddSegment by remember { mutableStateOf(false) }
    var showAddAccommodation by remember { mutableStateOf(false) }
    var showTravelSearchBooking by remember { mutableStateOf(false) }
    var showAddTask by remember { mutableStateOf(false) }
    var showAddVendor by remember { mutableStateOf(false) }
    var showAddGuest by remember { mutableStateOf(false) }

    // Backup notification triggers
    var aiChecklistSuggestedToast by remember { mutableStateOf(false) }
    var showShareSimulatedDialog by remember { mutableStateOf(false) }

    if (event == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = event!!.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${event!!.locationCity}, ${event!!.locationCountry} • ${event!!.startDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_button_detail")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Return back")
                    }
                },
                actions = {
                    // Quick Action: Edit Profile Event specifications
                    IconButton(onClick = { onNavigateToEdit(event!!.id) }, modifier = Modifier.testTag("edit_detail_shortcut")) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit event specifications")
                    }
                    IconButton(onClick = { showShareSimulatedDialog = true }, modifier = Modifier.testTag("share_snapshot_btn")) {
                        Icon(Icons.Default.Share, contentDescription = "Share portfolio summary")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = Modifier.testTag("event_detail_root")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Scrollable Tab navigation
            ScrollableTabRow(
                selectedTabIndex = selectedTabIdx,
                edgePadding = 12.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth().testTag("detail_tabs")
            ) {
                tabTitles.forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedTabIdx == idx,
                        onClick = { selectedTabIdx = idx },
                        text = { Text(title, fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("tab_entry_$idx")
                    )
                }
            }

            // Tab rendering switch
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTabIdx) {
                    0 -> OverviewTab(
                        event = event!!,
                        viewModel = viewModel,
                        onTriggerAIChecklist = {
                            viewModel.generateAISuggestedChecklist(event!!.type, event!!.locationCountry)
                            aiChecklistSuggestedToast = true
                        },
                        aiChecklistSuggestedToast = aiChecklistSuggestedToast,
                        onDismissToast = { aiChecklistSuggestedToast = false },
                        onDuplicateSuccess = { onNavigateBack() },
                        onDeleteSuccess = { onNavigateBack() }
                    )
                    1 -> TravelTab(
                        segments = segments,
                        accommodations = accommodations,
                        onAddSegmentClick = { showAddSegment = true },
                        onAddAccommodationClick = { showAddAccommodation = true },
                        onDeleteSegment = { viewModel.deleteTravelSegment(it) },
                        onDeleteAccommodation = { viewModel.deleteAccommodation(it) },
                        onSearchBookingClick = { showTravelSearchBooking = true }
                    )
                    2 -> TasksTab(
                        tasks = tasks,
                        onAddTaskClick = { showAddTask = true },
                        onToggleTaskDone = { viewModel.toggleTaskDone(it) },
                        onDeleteTask = { viewModel.deleteTask(it) }
                    )
                    3 -> VendorsTab(
                        vendors = vendors,
                        tasks = tasks,
                        onAddVendorClick = { showAddVendor = true },
                        onDeleteVendor = { viewModel.deleteVendor(it) }
                    )
                    4 -> GuestsTab(
                        guests = guests,
                        onAddGuestClick = { showAddGuest = true },
                        onDeleteGuest = { viewModel.deleteGuest(it) }
                    )
                }
            }
        }

        // Overlay Interactive Dialog sheets
        if (showAddSegment) {
            AddSegmentDialog(
                onDismiss = { showAddSegment = false },
                onAdd = { from, to, date, mode, notes ->
                    viewModel.addTravelSegment(from, to, date, mode, notes)
                    showAddSegment = false
                }
            )
        }

        if (showAddAccommodation) {
            AddAccommodationDialog(
                onDismiss = { showAddAccommodation = false },
                onAdd = { name, checkIn, checkOut, address, ref ->
                    viewModel.addAccommodation(name, checkIn, checkOut, address, ref)
                    showAddAccommodation = false
                }
            )
        }

        if (showAddTask) {
            AddTaskDialog(
                onDismiss = { showAddTask = false },
                onAdd = { title, category, dueDate, assigned ->
                    viewModel.addTask(title, category, dueDate, assigned)
                    showAddTask = false
                }
            )
        }

        if (showAddVendor) {
            AddVendorDialog(
                onDismiss = { showAddVendor = false },
                tasks = tasks,
                onAdd = { name, type, country, city, phone, email, web, notes, linkedId ->
                    viewModel.addVendor(name, type, country, city, phone, email, web, notes, linkedId)
                    showAddVendor = false
                }
            )
        }

        if (showAddGuest) {
            AddGuestDialog(
                onDismiss = { showAddGuest = false },
                onAdd = { name, email, phone, country, rsvp, notes ->
                    viewModel.addGuest(name, email, phone, country, rsvp, notes)
                    showAddGuest = false
                }
            )
        }

        if (showTravelSearchBooking) {
            TravelSearchBookingDialog(
                onDismiss = { 
                    showTravelSearchBooking = false
                    viewModel.clearSearchResults()
                },
                viewModel = viewModel,
                event = event!!
            )
        }

        if (showShareSimulatedDialog) {
            ShareSummaryDialog(
                event = event!!,
                guests = guests,
                tasks = tasks,
                onDismiss = { showShareSimulatedDialog = false }
            )
        }
    }
}

// ==========================================
// TAB 0: OVERVIEW DETAIL PANEL
// ==========================================
@Composable
fun OverviewTab(
    event: EventEntity,
    viewModel: PlannerViewModel,
    onTriggerAIChecklist: () -> Unit,
    aiChecklistSuggestedToast: Boolean,
    onDismissToast: () -> Unit,
    onDuplicateSuccess: () -> Unit,
    onDeleteSuccess: () -> Unit
) {
    val scrollState = rememberScrollState()
    val aiTips = remember(event.id) { viewModel.generateAITravelTips(event.locationCity, event.locationCountry) }
    var deleteConfirmOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Notification for AI Seeding
        if (aiChecklistSuggestedToast) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Celebration, "Sparkles", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "AI suggested checklist populated!",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "Added 5 localized cultural checklist tasks to your Tasks board.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    IconButton(onClick = onDismissToast) {
                        Icon(Icons.Default.Close, "Dismiss toast", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }

        // Event Specification card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Event Metadata",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Public, "Country", tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Target Destination: ${event.locationCity}, ${event.locationCountry}", style = MaterialTheme.typography.bodyMedium)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, "Schedule", tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Service Frame: ${event.startDate} to ${event.endDate}", style = MaterialTheme.typography.bodyMedium)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Place, "Venue", tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Venue: ${event.venueName} • Address: ${event.address}", style = MaterialTheme.typography.bodyMedium)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.People, "Guests Size", tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Estimated Audience: ${event.guestsCount} guests", style = MaterialTheme.typography.bodyMedium)
                }

                // Financial Budget scale
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MonetizationOn, "Financial Budget", tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Budget Targets: ${event.budgetMin.toInt()} - ${event.budgetMax.toInt()} (${event.budgetCurrency})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (event.notes.isNotEmpty()) {
                    Divider()
                    Text(
                        "Cultural, Religious & Dietary Guidelines",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = event.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Glowing AI Suggestions Box (Saves placeholders and hooks)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, "Sparkles Icon", tint = MaterialTheme.colorScheme.tertiary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Cultural Planner Assist",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }

                Text(
                    text = "Generate a custom localized tasks checklist tailored to ${event.type} in ${event.locationCountry} based on climate, language constraints, and native codes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )

                Button(
                    onClick = onTriggerAIChecklist,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    ),
                    modifier = Modifier.testTag("ai_suggest_checklist_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.AddTask, "Add Icon")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Inject AI Suggested Checklist", fontWeight = FontWeight.Bold)
                }

                Divider(color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "AI Destination Intelligence Tips:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )

                aiTips.forEach { tip ->
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
        }

        // Active management actions: Duplicate, Delete, Draft state info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Duplicate
            Button(
                onClick = {
                    viewModel.duplicateEvent(event.id) { _ ->
                        onDuplicateSuccess()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("duplicate_event_overview_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.ContentCopy, "Clone")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Duplicate Portfolio", fontSize = 13.sp)
            }

            // Delete
            Button(
                onClick = { deleteConfirmOpen = true },
                modifier = Modifier
                    .weight(1f)
                    .testTag("delete_event_overview_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Delete, "Delete")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Delete Portfolio", fontSize = 13.sp)
            }
        }

        if (deleteConfirmOpen) {
            AlertDialog(
                onDismissRequest = { deleteConfirmOpen = false },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteEvent(event) {
                                deleteConfirmOpen = false
                                onDeleteSuccess()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete Event")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deleteConfirmOpen = false }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Delete Event Portfolio?") },
                text = { Text("Are you absolutely sure you want to permanently erase this entire event portfolio? All child data entries will be deleted.") }
            )
        }
    }
}

// ==========================================
// TAB 1: TRAVEL & ACCED TIMELINE
// ==========================================
@Composable
fun TravelTab(
    segments: List<TravelSegmentEntity>,
    accommodations: List<AccommodationEntity>,
    onAddSegmentClick: () -> Unit,
    onAddAccommodationClick: () -> Unit,
    onDeleteSegment: (TravelSegmentEntity) -> Unit,
    onDeleteAccommodation: (AccommodationEntity) -> Unit,
    onSearchBookingClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("travel_root"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- Spotlight Banner for Skyscanner / Amadeus Travel Search ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("travel_search_banner"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Flight & Stay Search Hub",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Query Skyscanner & Amadeus rates directly and auto-register booking confirmations to your itinerary.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = onSearchBookingClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("open_travel_search_btn")
                    ) {
                        Icon(Icons.Default.Search, "Search Booking rates", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Search Rates", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        // Timeline Title & Header actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "En-Route Timeline Plans",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onAddSegmentClick,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("add_segment_header_btn")
                    ) {
                        Icon(Icons.Default.Add, "Add Segment", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Trip", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onAddAccommodationClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("add_accommodation_header_btn")
                    ) {
                        Icon(Icons.Default.Hotel, "Add Accommodation", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Hotel", fontSize = 12.sp)
                    }
                }
            }
        }

        if (segments.isEmpty() && accommodations.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.FlightLand, "Travel icon", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No travel items mapped", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "Add flights, rides, train schedules, or lodging configurations using the timeline action buttons above. All items synchronize seamlessly.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        // Render travel segment structures chronologically
        items(segments) { segment ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("segment_item_${segment.id}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val modeIcon = when (segment.mode) {
                        "Flight" -> Icons.Default.Flight
                        "Train" -> Icons.Default.Train
                        "Bus" -> Icons.Default.DirectionsBus
                        "Car" -> Icons.Default.DirectionsCar
                        else -> Icons.Default.DirectionsRun
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(modeIcon, "Transit mode icon", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${segment.fromLocation} → ${segment.toLocation}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Mode: ${segment.mode} • Ship Date: ${segment.travelDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (segment.notes.isNotEmpty()) {
                            Text(
                                text = segment.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        if (segment.bookingReference.isNotEmpty()) {
                            Text(
                                text = "Booking Reference: ${segment.bookingReference}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { onDeleteSegment(segment) },
                        modifier = Modifier.testTag("delete_segment_btn_${segment.id}")
                    ) {
                        Icon(Icons.Default.Delete, "Delete Trip Segment", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Render accommodations
        items(accommodations) { bed ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("accommodation_item_${bed.id}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Hotel, "Bed", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = bed.name,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Dates: ${bed.checkInDate} to ${bed.checkOutDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Address: ${bed.address}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (bed.bookingReference.isNotEmpty()) {
                            Text(
                                text = "Booking Reference: ${bed.bookingReference}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { onDeleteAccommodation(bed) },
                        modifier = Modifier.testTag("delete_accommodation_btn_${bed.id}")
                    ) {
                        Icon(Icons.Default.Delete, "Delete Hotel Reservation", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 2: TASKS BOARD CHECKLIST
// ==========================================
@Composable
fun TasksTab(
    tasks: List<TaskEntity>,
    onAddTaskClick: () -> Unit,
    onToggleTaskDone: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit
) {
    var selectedCategoryFilter by remember { mutableStateOf("All Categories") }
    var selectedStatusFilter by remember { mutableStateOf("All Statuses") }

    val categoriesList = listOf("All Categories", "Venue", "Catering", "Travel", "Documents", "Other")
    val statusesList = listOf("All Statuses", "Not started", "In progress", "Done")

    val filteredTasks = remember(tasks, selectedCategoryFilter, selectedStatusFilter) {
        tasks.filter { task ->
            val matchesCat = if (selectedCategoryFilter == "All Categories") true else task.category == selectedCategoryFilter
            val matchesStat = if (selectedStatusFilter == "All Statuses") true else task.status == selectedStatusFilter
            matchesCat && matchesStat
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("tasks_root"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Timeline Title & Header actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Task Checklist Board",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Button(
                onClick = onAddTaskClick,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("add_task_header_btn")
            ) {
                Icon(Icons.Default.Check, "Add task icon", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Task", fontSize = 12.sp)
            }
        }

        // Search options: Filters dropdown row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Category Dropdown
            var catExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { catExpanded = true },
                    modifier = Modifier.fillMaxWidth().testTag("task_category_filter_btn")
                ) {
                    Text(selectedCategoryFilter, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
                    Icon(Icons.Default.ArrowDropDown, "DropDown")
                }
                DropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                    categoriesList.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                selectedCategoryFilter = cat
                                catExpanded = false
                            }
                        )
                    }
                }
            }

            // Status Dropdown
            var statExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { statExpanded = true },
                    modifier = Modifier.fillMaxWidth().testTag("task_status_filter_btn")
                ) {
                    Text(selectedStatusFilter, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
                    Icon(Icons.Default.ArrowDropDown, "DropDown")
                }
                DropdownMenu(expanded = statExpanded, onDismissRequest = { statExpanded = false }) {
                    statusesList.forEach { s ->
                        DropdownMenuItem(
                            text = { Text(s) },
                            onClick = {
                                selectedStatusFilter = s
                                statExpanded = false
                            }
                        )
                    }
                }
            }
        }

        if (filteredTasks.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.DoneAll, "Done Icon", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("No tasks match these filters", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredTasks, key = { it.id }) { task ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_item_${task.id}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Checkbox(
                                    checked = task.status == "Done",
                                    onCheckedChange = { onToggleTaskDone(task) },
                                    modifier = Modifier.testTag("task_checkbox_done_${task.id}")
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = if (task.status == "Done") TextDecoration.LineThrough else TextDecoration.None
                                    )
                                    Text(
                                        text = "Due: ${task.dueDate} • Assigned: ${task.assignedTo}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = task.category,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontSize = 10.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                IconButton(
                                    onClick = { onDeleteTask(task) },
                                    modifier = Modifier.testTag("delete_task_btn_${task.id}")
                                ) {
                                    Icon(Icons.Default.Delete, "Delete task", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 3: VENDORS LOGISTICS
// ==========================================
@Composable
fun VendorsTab(
    vendors: List<VendorEntity>,
    tasks: List<TaskEntity>,
    onAddVendorClick: () -> Unit,
    onDeleteVendor: (VendorEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("vendors_root"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Logistics & Vendor Directory",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Button(
                    onClick = onAddVendorClick,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_vendor_header_btn")
                ) {
                    Icon(Icons.Default.AddBusiness, "Add Vendor Icon", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Vendor", fontSize = 12.sp)
                }
            }
        }

        if (vendors.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Store, "Vendor Icon", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No vendors registered", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "Add catering, floral artists, photography professionals or venue directors. Tap the button above to catalog critical event service teams.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        items(vendors) { vendor ->
            val linkedTask = remember(vendor.linkedTaskId, tasks) {
                tasks.find { it.id == vendor.linkedTaskId }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vendor_item_${vendor.id}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(vendor.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Category: ${vendor.type} • City: ${vendor.city}, ${vendor.country}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { onDeleteVendor(vendor) },
                            modifier = Modifier.testTag("delete_vendor_btn_${vendor.id}")
                        ) {
                            Icon(Icons.Default.Delete, "Delete Vendor", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                    // Contacts Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (vendor.phone.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Phone, "Phone", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(vendor.phone, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }

                        if (vendor.email.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Email, "Email", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(vendor.email, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }

                    if (vendor.website.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, "Web", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(vendor.website, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary, textDecoration = TextDecoration.Underline)
                        }
                    }

                    if (vendor.notes.isNotEmpty()) {
                        Text(
                            text = "Guidelines: ${vendor.notes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (linkedTask != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "🔗 Contracted Duty: ${linkedTask.title}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 4: GUEST LIST
// ==========================================
@Composable
fun GuestsTab(
    guests: List<GuestEntity>,
    onAddGuestClick: () -> Unit,
    onDeleteGuest: (GuestEntity) -> Unit
) {
    // Computes summaries by rsvp status
    val totalGuests = guests.size
    val confirmedCount = guests.count { it.rsvpStatus == "Confirmed" }
    val invitedCount = guests.count { it.rsvpStatus == "Invited" }
    val declinedCount = guests.count { it.rsvpStatus == "Declined" }
    val maybeCount = guests.count { it.rsvpStatus == "Maybe" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("guests_root"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Headers action
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "RSVP Guest Register",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Button(
                    onClick = onAddGuestClick,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_guest_header_btn")
                ) {
                    Icon(Icons.Default.PersonAdd, "Person Add Icon", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Guest", fontSize = 12.sp)
                }
            }
        }

        // Summary Badges Box
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Guest RSVP Status Analytics:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        RSVPBadge(label = "All", count = totalGuests, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                        RSVPBadge(label = "Conf.", count = confirmedCount, color = Color(0xFF10B981), modifier = Modifier.weight(1f))
                        RSVPBadge(label = "Maybe", count = maybeCount, color = Color(0xFFFBBF24), modifier = Modifier.weight(1f))
                        RSVPBadge(label = "Decl.", count = declinedCount, color = Color(0xFFEF4444), modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        if (guests.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.PeopleOutline, "Guest list", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No guests invited", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "Add wedding attendees, corporate executives, or social relatives. Tracks dietary restrictions or special accessibility needs.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        items(guests) { guest ->
            val statusColor = when (guest.rsvpStatus) {
                "Confirmed" -> Color(0xFF10B981)
                "Maybe" -> Color(0xFFFBBF24)
                "Declined" -> Color(0xFFEF4444)
                else -> Color(0xFF6B7280) // Invited
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("guest_item_${guest.id}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, "Guest avatar icon", tint = statusColor, modifier = Modifier.size(18.dp))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(guest.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "Email: ${guest.email} • Country: ${guest.country}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (guest.notes.isNotEmpty()) {
                            Text(
                                text = "Preferences: ${guest.notes}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = guest.rsvpStatus,
                            style = MaterialTheme.typography.bodySmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { onDeleteGuest(guest) },
                        modifier = Modifier.testTag("delete_guest_btn_${guest.id}")
                    ) {
                        Icon(Icons.Default.Delete, "Erase guest", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun RSVPBadge(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(count.toString(), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(label, fontSize = 10.sp, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

// ==========================================
// SUB-DIALOG COMPONENT BLOCKS (MODAL FORMS)
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSegmentDialog(onDismiss: () -> Unit, onAdd: (String, String, String, String, String) -> Unit) {
    var from by remember { mutableStateOf("") }
    var to by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("2026-07-18") }
    var mode by remember { mutableStateOf("Flight") }
    var notes by remember { mutableStateOf("") }
    var modeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (from.isNotBlank() && to.isNotBlank()) onAdd(from, to, date, mode, notes)
                },
                modifier = Modifier.testTag("submit_segment_dialog")
            ) { Text("Save Segment") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Add Travel Segment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = from, onValueChange = { from = it }, label = { Text("From (City/Country)") }, singleLine = true, modifier = Modifier.fillMaxWidth().testTag("segment_from"))
                OutlinedTextField(value = to, onValueChange = { to = it }, label = { Text("To (City/Country)") }, singleLine = true, modifier = Modifier.fillMaxWidth().testTag("segment_to"))
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Travel Date") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                
                ExposedDropdownMenuBox(expanded = modeExpanded, onExpandedChange = { modeExpanded = !modeExpanded }) {
                    OutlinedTextField(
                        value = mode,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Conveyance Mode") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = modeExpanded, onDismissRequest = { modeExpanded = false }) {
                        listOf("Flight", "Train", "Bus", "Car", "Other").forEach { d ->
                            DropdownMenuItem(text = { Text(d) }, onClick = { mode = d; modeExpanded = false })
                        }
                    }
                }

                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Flight Number / Train Booking Notes") }, modifier = Modifier.fillMaxWidth())
            }
        }
    )
}

@Composable
fun AddAccommodationDialog(onDismiss: () -> Unit, onAdd: (String, String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var checkIn by remember { mutableStateOf("2026-07-18") }
    var checkOut by remember { mutableStateOf("2026-07-25") }
    var address by remember { mutableStateOf("") }
    var ref by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && address.isNotBlank()) onAdd(name, checkIn, checkOut, address, ref)
                },
                modifier = Modifier.testTag("submit_accommodation_dialog")
            ) { Text("Save Lodging") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Add Accommodation Booking") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Hotel or Airbnb Name") }, singleLine = true, modifier = Modifier.fillMaxWidth().testTag("hotel_name"))
                OutlinedTextField(value = checkIn, onValueChange = { checkIn = it }, label = { Text("Check-In Date") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = checkOut, onValueChange = { checkOut = it }, label = { Text("Check-Out Date") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Exact Address") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = ref, onValueChange = { ref = it }, label = { Text("Booking Reference (Optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onAdd: (String, String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Venue") }
    var dueDate by remember { mutableStateOf("2026-06-20") }
    var assigned by remember { mutableStateOf("") }
    var catExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) onAdd(title, category, dueDate, assigned)
                },
                modifier = Modifier.testTag("submit_task_dialog")
            ) { Text("Save Task") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Add Task Checklist Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Task Title") }, singleLine = true, modifier = Modifier.fillMaxWidth().testTag("task_title_input"))
                
                ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = !catExpanded }) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Task Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                        listOf("Venue", "Catering", "Travel", "Documents", "Other").forEach { d ->
                            DropdownMenuItem(text = { Text(d) }, onClick = { category = d; catExpanded = false })
                        }
                    }
                }

                OutlinedTextField(value = dueDate, onValueChange = { dueDate = it }, label = { Text("Due Date") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = assigned, onValueChange = { assigned = it }, label = { Text("Assigned Delegate") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVendorDialog(onDismiss: () -> Unit, tasks: List<TaskEntity>, onAdd: (String, String, String, String, String, String, String, String, Int?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Catering") }
    var country by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    var selectedTaskId by remember { mutableStateOf<Int?>(null) }
    var typeExpanded by remember { mutableStateOf(false) }
    var taskExpanded by remember { mutableStateOf(false) }

    val vendorsCategories = listOf("Venue", "Catering", "Travel", "Photography", "Funeral home", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && country.isNotBlank() && city.isNotBlank()) {
                        onAdd(name, type, country, city, phone, email, website, notes, selectedTaskId)
                    }
                },
                modifier = Modifier.testTag("submit_vendor_dialog")
            ) { Text("Catalog Vendor") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Catalog Service Vendor") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Vendor Name") }, singleLine = true, modifier = Modifier.fillMaxWidth().testTag("vendor_name"))
                
                ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = !typeExpanded }) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Vendor Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        vendorsCategories.forEach { d ->
                            DropdownMenuItem(text = { Text(d) }, onClick = { type = d; typeExpanded = false })
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = country, onValueChange = { country = it }, label = { Text("Country") }, singleLine = true, modifier = Modifier.weight(1f))
                }

                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = website, onValueChange = { website = it }, label = { Text("Website") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Contractual Scope Details") }, modifier = Modifier.fillMaxWidth())

                // Link Vendor to Event Task check!
                if (tasks.isNotEmpty()) {
                    ExposedDropdownMenuBox(expanded = taskExpanded, onExpandedChange = { taskExpanded = !taskExpanded }) {
                        val activeTaskLabel = if (selectedTaskId == null) "Not linked (Optional)" else tasks.find { it.id == selectedTaskId }?.title ?: ""
                        OutlinedTextField(
                            value = activeTaskLabel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Link Contracted Duty") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = taskExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = taskExpanded, onDismissRequest = { taskExpanded = false }) {
                            DropdownMenuItem(text = { Text("Not linked (Optional)") }, onClick = { selectedTaskId = null; taskExpanded = false })
                            tasks.forEach { task ->
                                DropdownMenuItem(
                                    text = { Text(task.title) },
                                    onClick = { selectedTaskId = task.id; taskExpanded = false }
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGuestDialog(onDismiss: () -> Unit, onAdd: (String, String, String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var rsvp by remember { mutableStateOf("Invited") }
    var notes by remember { mutableStateOf("") }
    var rsvpExpanded by remember { mutableStateOf(false) }

    val rsvpOptions = listOf("Invited", "Confirmed", "Declined", "Maybe")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && country.isNotBlank()) onAdd(name, email, phone, country, rsvp, notes)
                },
                modifier = Modifier.testTag("submit_guest_dialog")
            ) { Text("Save Guest") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Invite Event Attendee") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, singleLine = true, modifier = Modifier.fillMaxWidth().testTag("guest_name"))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = country, onValueChange = { country = it }, label = { Text("Origin Country") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                
                ExposedDropdownMenuBox(expanded = rsvpExpanded, onExpandedChange = { rsvpExpanded = !rsvpExpanded }) {
                    OutlinedTextField(
                        value = rsvp,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("RSVP Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rsvpExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = rsvpExpanded, onDismissRequest = { rsvpExpanded = false }) {
                        rsvpOptions.forEach { r ->
                            DropdownMenuItem(text = { Text(r) }, onClick = { rsvp = r; rsvpExpanded = false })
                        }
                    }
                }

                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Dietary, Accessibility, Shuttle constraints") }, modifier = Modifier.fillMaxWidth())
            }
        }
    )
}

@Composable
fun ShareSummaryDialog(
    event: EventEntity,
    guests: List<GuestEntity>,
    tasks: List<TaskEntity>,
    onDismiss: () -> Unit
) {
    val totalGuests = guests.size
    val confirmed = guests.count { it.rsvpStatus == "Confirmed" }
    val tasksFinished = tasks.count { it.status == "Done" }
    val progress = if (tasks.isNotEmpty()) (tasksFinished.toFloat() / tasks.size * 100).toInt() else 100

    val shareLink = "https://globalmoments.com/portfolio/share/${event.id}"

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss, modifier = Modifier.testTag("dismiss_share_dialog")) {
                Text("Dismiss Snapshot")
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.IosShare, "Share", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share Event Portfolio Snapshot")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "A beautiful guest-facing status summary portal is ready to be dispatched!",
                    style = MaterialTheme.typography.bodyMedium
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Snapshot Metadata:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        Text("• Portfolio: ${event.name}", style = MaterialTheme.typography.bodySmall)
                        Text("• Location: ${event.locationCity}, ${event.locationCountry}", style = MaterialTheme.typography.bodySmall)
                        Text("• Venue: ${event.venueName}", style = MaterialTheme.typography.bodySmall)
                        Text("• Tasks Completed: $progress% ($tasksFinished/${tasks.size})", style = MaterialTheme.typography.bodySmall)
                        Text("• Confirmed Guests: $confirmed attendee responses of $totalGuests total", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Text(
                    text = "Temporary simulated access link:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(10.dp)
                ) {
                    Text(
                        text = shareLink,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Text(
                    "Pressing share will copy this localized index summary link to clipboard and trigger the native Android sharesheet (Simulated).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}
