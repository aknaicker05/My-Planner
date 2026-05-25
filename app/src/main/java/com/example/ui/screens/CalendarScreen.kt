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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.PlannerViewModel
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalendarScreen(
    viewModel: PlannerViewModel,
    onNavigateToEvent: (Int) -> Unit
) {
    val events by viewModel.events.collectAsState()
    val showEventsInCalendar by viewModel.showEventsInCalendar.collectAsState()

    // Hold all task aggregates for calendar mapping
    var allTasks by remember { mutableStateOf<List<TaskEntity>>(emptyList()) }

    LaunchedEffect(events) {
        val loadedTasks = mutableListOf<TaskEntity>()
        events.forEach { event ->
            viewModel.getTasksForEvent(event.id).collect { tasksList ->
                loadedTasks.removeAll { it.eventId == event.id }
                loadedTasks.addAll(tasksList)
                allTasks = loadedTasks.toList()
            }
        }
    }

    // Calendar state (Using fixed calendar for 2026-07 to default to Italy Wedding seed)
    val calendarInstance = remember {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.JULY)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    var currentYear by remember { mutableStateOf(2026) }
    var currentMonth by remember { mutableStateOf(Calendar.JULY) } // 0-indexed: 6 = July
    var selectedDay by remember { mutableStateOf(20) } // Default selected is 20th of July, 2026

    // Calculate month attributes
    val monthName = remember(currentMonth) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.MONTH, currentMonth)
        }
        SimpleDateFormat("MMMM", Locale.getDefault()).format(cal.time)
    }

    // Days grid builder
    val daysInMonth = remember(currentYear, currentMonth) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
        }
        cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    val firstDayOfWeek = remember(currentYear, currentMonth) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday...
    }

    // Helper functions to correlate items to date strings
    fun getDateString(day: Int): String {
        val m = String.format("%02d", currentMonth + 1)
        val d = String.format("%02d", day)
        return "$currentYear-$m-$d"
    }

    val selectedDateString = remember(currentYear, currentMonth, selectedDay) {
        getDateString(selectedDay)
    }

    // Events filter
    val eventsOnSelectedDay = remember(events, selectedDateString, showEventsInCalendar) {
        if (!showEventsInCalendar) emptyList()
        else events.filter { it.startDate == selectedDateString && !it.isDraft }
    }

    // Tasks filter
    val tasksOnSelectedDay = remember(allTasks, selectedDateString) {
        allTasks.filter { it.dueDate == selectedDateString }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("calendar_root"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Toggle Settings controls
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FilterAlt,
                            contentDescription = "Filter events toggle icon",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Map Event Markers",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Toggle event highlights on/off grid",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = showEventsInCalendar,
                        onCheckedChange = { viewModel.toggleCalendarEvents() },
                        modifier = Modifier.testTag("calendar_toggle_switch")
                    )
                }
            }
        }

        // Calendar Grid Card
        item {
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
                    // Grid Header: Month Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (currentMonth == Calendar.JANUARY) {
                                    currentMonth = Calendar.DECEMBER
                                    currentYear -= 1
                                } else {
                                    currentMonth -= 1
                                }
                            },
                            modifier = Modifier.testTag("calendar_prev_month")
                        ) {
                            Icon(Icons.Default.ChevronLeft, "Previous Month")
                        }

                        Text(
                            text = "$monthName $currentYear",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        IconButton(
                            onClick = {
                                if (currentMonth == Calendar.DECEMBER) {
                                    currentMonth = Calendar.JANUARY
                                    currentYear += 1
                                } else {
                                    currentMonth += 1
                                }
                            },
                            modifier = Modifier.testTag("calendar_next_month")
                        ) {
                            Icon(Icons.Default.ChevronRight, "Next Month")
                        }
                    }

                    // Days of Week labels Sun..Sat
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa").forEach { dayLabel ->
                            Text(
                                text = dayLabel,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Build Days cells
                    val cellsCount = daysInMonth + (firstDayOfWeek - 1)
                    val rows = (cellsCount / 7) + (if (cellsCount % 7 > 0) 1 else 0)

                    for (rowIdx in 0 until rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (colIdx in 0..6) {
                                val cellIdx = (rowIdx * 7) + colIdx
                                val dayNum = cellIdx - (firstDayOfWeek - 2)

                                val isValidDayByMonth = dayNum in 1..daysInMonth

                                if (isValidDayByMonth) {
                                    val cellDateStr = getDateString(dayNum)
                                    val hasDayEvent = showEventsInCalendar && events.any { it.startDate == cellDateStr && !it.isDraft }
                                    val hasDayTask = allTasks.any { it.dueDate == cellDateStr }
                                    val isCellSelected = dayNum == selectedDay

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isCellSelected -> MaterialTheme.colorScheme.primary
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .clickable { selectedDay = dayNum }
                                            .testTag("calendar_day_cell_$dayNum"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = dayNum.toString(),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isCellSelected) FontWeight.Bold else FontWeight.Medium
                                                ),
                                                color = when {
                                                    isCellSelected -> MaterialTheme.colorScheme.onPrimary
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                }
                                            )

                                            // Sub-dots showing Event (Teal) or Task (Amber) indicators
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                                modifier = Modifier.padding(top = 2.dp)
                                            ) {
                                                if (hasDayEvent) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(5.dp)
                                                            .clip(CircleShape)
                                                            .background(if (isCellSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
                                                    )
                                                }
                                                if (hasDayTask) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(5.dp)
                                                            .clip(CircleShape)
                                                            .background(if (isCellSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.tertiary)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // Empty Cell filler
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section header details
        item {
            Text(
                text = "Day Schedule: $monthName $selectedDay, $currentYear",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Daily listings
        if (eventsOnSelectedDay.isEmpty() && tasksOnSelectedDay.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        "No events or task deadlines planned on this date.",
                        modifier = Modifier.padding(20.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Render Scheduled Events
            items(eventsOnSelectedDay) { event ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToEvent(event.id) }
                        .testTag("calendar_event_link_${event.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = "Event icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = event.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Venue: ${event.venueName} (${event.locationCity})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Navigate to event details",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Render Task Deadlines
            items(tasksOnSelectedDay) { task ->
                val parentEvent = events.find { it.id == task.eventId }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            // Checkbox status toggle
                            Checkbox(
                                checked = task.status == "Done",
                                onCheckedChange = { viewModel.toggleTaskDone(task) },
                                modifier = Modifier.testTag("calendar_task_checkbox_${task.id}")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (task.status == "Done") MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Portfolio: ${parentEvent?.name ?: "Unknown"} • Category: ${task.category}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (task.status == "Done") MaterialTheme.colorScheme.secondaryContainer
                                    else MaterialTheme.colorScheme.tertiaryContainer
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = task.status,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (task.status == "Done") MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onTertiaryContainer,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
