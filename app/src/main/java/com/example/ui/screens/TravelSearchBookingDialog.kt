package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.EventEntity
import com.example.ui.PlannerViewModel

@Composable
fun TravelSearchBookingDialog(
    onDismiss: () -> Unit,
    viewModel: PlannerViewModel,
    event: EventEntity
) {
    var selectedTabIdx by remember { mutableStateOf(0) }
    var showApiSettings by remember { mutableStateOf(false) }

    // Collect variables atomically from shared ViewModel state
    val clientId by viewModel.amadeusClientId.collectAsState()
    val clientSecret by viewModel.amadeusClientSecret.collectAsState()
    
    val flightOffers by viewModel.flightOffers.collectAsState()
    val hotelOffers by viewModel.hotelOffers.collectAsState()
    
    val isSearchingFlights by viewModel.isSearchingFlights.collectAsState()
    val isSearchingHotels by viewModel.isSearchingHotels.collectAsState()
    
    val searchError by viewModel.searchError.collectAsState()
    
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    // Set initial values based on current event destination and dates
    var flightFrom by remember { mutableStateOf("SFO") }
    var flightTo by remember { mutableStateOf(event.locationCity.take(3).uppercase()) }
    var flightDate by remember { mutableStateOf(event.startDate) }

    var hotelCity by remember { mutableStateOf(event.locationCity) }
    var checkInDate by remember { mutableStateOf(event.startDate) }
    var checkOutDate by remember { mutableStateOf(event.endDate) }

    // Keep state values cleaned or fallback
    if (flightTo.length < 3) flightTo = "FLR"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(12.dp)
                .testTag("travel_booking_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Panel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Search Flights & Lodging",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "My Planner Global Integration Hub",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_api_dialog")) {
                        Icon(Icons.Default.Close, "Dismiss dialog")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Shared API Setup / Setup Collapsible Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (clientId.trim().isEmpty()) {
                            MaterialTheme.colorScheme.surfaceVariant
                        } else {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        }
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showApiSettings = !showApiSettings },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (clientId.trim().isEmpty()) Icons.Default.CloudQueue else Icons.Default.CloudDone,
                                    contentDescription = "Api Lock status",
                                    tint = if (clientId.trim().isEmpty()) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (clientId.trim().isEmpty()) "Simulation Mode (No Keys Installed)" else "Live Amadeus Provider Activated",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Click to configure custom Amadeus REST credentials.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = if (showApiSettings) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Expand collapsible API state"
                            )
                        }

                        AnimatedVisibility(
                            visible = showApiSettings,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                            ) {
                                Text(
                                    text = "To search real-world schedules, enter your Test Client credentials from developers.amadeus.com below. If left blank, we run on our high-performance simulation model.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                OutlinedTextField(
                                    value = clientId,
                                    onValueChange = { viewModel.amadeusClientId.value = it },
                                    label = { Text("Amadeus API Client ID") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 6.dp)
                                )

                                OutlinedTextField(
                                    value = clientSecret,
                                    onValueChange = { viewModel.amadeusClientSecret.value = it },
                                    label = { Text("Amadeus API Secret") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // Inner Tabs for Search Types (Flights vs Hotels)
                TabRow(
                    selectedTabIndex = selectedTabIdx,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTabIdx == 0,
                        onClick = { 
                            selectedTabIdx = 0 
                            viewModel.clearSearchResults()
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Flight, "Flight mode")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Search Flights", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTabIdx == 1,
                        onClick = { 
                            selectedTabIdx = 1 
                            viewModel.clearSearchResults()
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Hotel, "Bed stays mode")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Search Hotels", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Error message banner
                searchError?.let { err ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .background(
                                MaterialTheme.colorScheme.errorContainer,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Search Tab Body Content
                Box(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                    if (selectedTabIdx == 0) {
                        // --- Flight Search Tab View ---
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Flight parameters form
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = flightFrom,
                                    onValueChange = { flightFrom = it.uppercase() },
                                    label = { Text("From (IATA)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = flightTo,
                                    onValueChange = { flightTo = it.uppercase() },
                                    label = { Text("To (IATA)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = flightDate,
                                    onValueChange = { flightDate = it },
                                    label = { Text("Depart Date") },
                                    modifier = Modifier.weight(1.3f),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { viewModel.searchFlights(flightFrom, flightTo, flightDate) },
                                modifier = Modifier.fillMaxWidth().testTag("launch_flight_search"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Search, "Search icon")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Query Live Tickets")
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (isSearchingFlights) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator()
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Contacting airline APIs...", style = MaterialTheme.typography.bodyMedium)
                                }
                            } else if (flightOffers.isEmpty()) {
                                Box(
                                    modifier = Modifier.weight(1f).fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Specify departure dates and destination IATA targets above to source tickets.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(flightOffers) { offer ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Column {
                                                        Text(
                                                            text = offer.airline,
                                                            fontWeight = FontWeight.Bold,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                        Text(
                                                            text = "Flight ${offer.flightNumber} • Duration: ${offer.duration}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                    Text(
                                                        text = "$${offer.price} ${offer.currency}",
                                                        fontWeight = FontWeight.Bold,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        color = MaterialTheme.colorScheme.secondary
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(8.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "Departure: ${offer.departureTime} → Arrival: ${offer.arrivalTime} (${offer.stops} stops)",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            uriHandler.openUri(offer.bookingUrl)
                                                        },
                                                        modifier = Modifier.weight(1f),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Icon(Icons.Default.Launch, "Check Web", modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Ticket Details", fontSize = 11.sp)
                                                    }

                                                    Button(
                                                        onClick = {
                                                            viewModel.addTravelSegment(
                                                                from = flightFrom,
                                                                to = flightTo,
                                                                date = flightDate,
                                                                mode = "Flight",
                                                                notes = "Simulated booking via Skyscanner Hub. Ticket: ${offer.airline} ${offer.flightNumber}",
                                                                bookingReference = offer.bookingReferencePlaceholder
                                                            )
                                                            Toast.makeText(
                                                                context,
                                                                "Flight ${offer.flightNumber} Saved & Booking Ref: ${offer.bookingReferencePlaceholder} Logged!",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                            onDismiss()
                                                        },
                                                        modifier = Modifier.weight(1.2f).testTag("select_flight_${offer.id}"),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Icon(Icons.Default.Done, "Complete Book", modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Book & Import", fontSize = 11.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // --- Hotel Search Tab View ---
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Hotel parameters form
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = hotelCity,
                                    onValueChange = { hotelCity = it },
                                    label = { Text("City") },
                                    modifier = Modifier.weight(1.2f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = checkInDate,
                                    onValueChange = { checkInDate = it },
                                    label = { Text("Check In") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = checkOutDate,
                                    onValueChange = { checkOutDate = it },
                                    label = { Text("Check Out") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { viewModel.searchHotels(hotelCity, checkInDate, checkOutDate) },
                                modifier = Modifier.fillMaxWidth().testTag("launch_hotel_search"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Icon(Icons.Default.Search, "Search stays icon")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Retrieve Stays & Rates")
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (isSearchingHotels) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Polling lodging partners...", style = MaterialTheme.typography.bodyMedium)
                                }
                            } else if (hotelOffers.isEmpty()) {
                                Box(
                                    modifier = Modifier.weight(1f).fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Search target destination lodging and check-in dates above.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(hotelOffers) { stay ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = stay.name,
                                                            fontWeight = FontWeight.Bold,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                        Text(
                                                            text = "${stay.address}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                    Column(horizontalAlignment = Alignment.End) {
                                                        Text(
                                                            text = "$${stay.pricePerNight} / night",
                                                            fontWeight = FontWeight.Bold,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = MaterialTheme.colorScheme.secondary
                                                        )
                                                        Text(
                                                            text = "Total: $${stay.totalPrice}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Star, "Rating", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "${stay.rating} • Guest Rating • High Grade Comfort",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            uriHandler.openUri(stay.bookingUrl)
                                                        },
                                                        modifier = Modifier.weight(1f),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Icon(Icons.Default.Launch, "Check Hotel", modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("View Stay Rates", fontSize = 11.sp)
                                                    }

                                                    Button(
                                                        onClick = {
                                                            viewModel.addAccommodation(
                                                                name = stay.name,
                                                                checkIn = checkInDate,
                                                                checkOut = checkOutDate,
                                                                address = stay.address,
                                                                ref = stay.bookingReferencePlaceholder
                                                            )
                                                            Toast.makeText(
                                                                context,
                                                                "${stay.name} reserved! Reference ${stay.bookingReferencePlaceholder} saved.",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                            onDismiss()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                        modifier = Modifier.weight(1.2f).testTag("select_hotel_${stay.id}"),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Icon(Icons.Default.Done, "Save Hotel", modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Book & Import", fontSize = 11.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
