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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fourcut.photo.core.designsystem.component.QuietStateCard
import com.fourcut.photo.core.designsystem.component.QuietStateKind

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
    month: CalendarMonthUiModel,
    sessionsForSelectedDay: List<CalendarSessionUiModel>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDaySelected: (Int) -> Unit,
    onSessionSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(start = 20.dp, top = 36.dp, end = 20.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            CalendarHeader(
                monthLabel = month.monthLabel,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth
            )
        }
        item {
            CalendarGrid(
                leadingBlankCount = month.leadingBlankCount,
                days = month.days,
                onDaySelected = onDaySelected
            )
        }
        if (sessionsForSelectedDay.isEmpty()) {
            item {
                CalendarEmptyState()
            }
        } else {
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
}

@Composable
private fun CalendarHeader(
    monthLabel: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "캘린더",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = monthLabel,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(
                onClick = onPreviousMonth,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text("이전")
            }
            TextButton(
                onClick = onNextMonth,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text("다음")
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    leadingBlankCount: Int,
    days: List<CalendarDayUiModel>,
    onDaySelected: (Int) -> Unit
) {
    val cells = List(leadingBlankCount) { null } + days
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        cells.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                week.forEach { day ->
                    if (day == null) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    } else {
                        CalendarDayCell(
                            day = day,
                            onClick = { onDaySelected(day.dayOfMonth) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                repeat(7 - week.size) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
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
private fun CalendarEmptyState() {
    QuietStateCard(kind = QuietStateKind.CalendarDayEmpty)
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
                text = session.title.ifBlank { "세션 ${session.sessionIndexForDay}" },
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = listOfNotNull(session.timeLabel, session.sourceLabel, "미디어 ${session.mediaCount}개")
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
