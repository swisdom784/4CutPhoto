package com.fourcut.photo.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

data class CalendarDayUiModel(
    val dayOfMonth: Int,
    val hasSessions: Boolean,
    val isSelected: Boolean
)

data class CalendarSessionUiModel(
    val id: Long,
    val title: String,
    val timeLabel: String,
    val sourceLabel: String?,
    val sessionIndexForDay: Int,
    val tagNames: List<String>,
    val mediaCount: Int
)

@Composable
fun CalendarScreen(
    days: List<CalendarDayUiModel>,
    sessionsForSelectedDay: List<CalendarSessionUiModel>,
    onDaySelected: (Int) -> Unit,
    onSessionSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Text(
                text = "Calendar",
                style = MaterialTheme.typography.headlineMedium
            )
        }
        item {
            CalendarGrid(
                days = days,
                onDaySelected = onDaySelected
            )
        }
        items(
            items = sessionsForSelectedDay,
            key = { it.id }
        ) { session ->
            CalendarSessionRow(
                session = session,
                onClick = { onSessionSelected(session.id) }
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    days: List<CalendarDayUiModel>,
    onDaySelected: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        days.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                week.forEach { day ->
                    CalendarDayCell(
                        day = day,
                        onClick = { onDaySelected(day.dayOfMonth) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDayUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (day.isSelected) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium
            )
            if (day.hasSessions) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary)
                        .aspectRatio(1f)
                        .fillMaxWidth(0.12f)
                )
            }
        }
    }
}

@Composable
private fun CalendarSessionRow(
    session: CalendarSessionUiModel,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = session.title.ifBlank { "Session ${session.sessionIndexForDay}" },
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = listOfNotNull(session.timeLabel, session.sourceLabel, "${session.mediaCount} media")
                    .joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (session.tagNames.isNotEmpty()) {
                Text(
                    text = session.tagNames.joinToString(prefix = "#", separator = " #"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
