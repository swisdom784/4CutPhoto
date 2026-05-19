package com.fourcut.photo.feature.gallery

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

data class GalleryDateGroupUiModel(
    val yearLabel: String,
    val dateLabel: String,
    val sessions: List<GallerySessionUiModel>
)

data class GallerySessionUiModel(
    val id: Long,
    val sessionTitle: String,
    val timeLabel: String,
    val sourceLabel: String?,
    val coverPath: String?,
    val tagNames: List<String>,
    val hasVideo: Boolean,
    val mediaSummary: String
)

@Composable
fun GalleryScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    groups: List<GalleryDateGroupUiModel>,
    onSessionSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Gallery",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Each QR save stays as its own session.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Search people") }
            )
        }
        galleryEmptyState(query, groups.size)?.let { state ->
            item {
                GalleryEmptyStateCard(state)
            }
        }
        groups.forEach { group ->
            item(key = "${group.yearLabel}-${group.dateLabel}") {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = group.yearLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = group.dateLabel,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            items(
                items = group.sessions,
                key = { it.id }
            ) { session ->
                GallerySessionCard(
                    session = session,
                    onClick = { onSessionSelected(session.id) }
                )
            }
        }
    }
}

@Composable
private fun GalleryEmptyStateCard(state: GalleryEmptyState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = when (state) {
                    GalleryEmptyState.NoSessions -> "No saved sessions yet"
                    GalleryEmptyState.NoSearchResults -> "No matching people tags"
                },
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = when (state) {
                    GalleryEmptyState.NoSessions ->
                        "Scan a photo booth QR code to start building your four-cut archive."
                    GalleryEmptyState.NoSearchResults ->
                        "Try another name, or add the person tag while saving or editing a session."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GallerySessionCard(
    session: GallerySessionUiModel,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(0.34f)
                    .aspectRatio(0.72f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (session.hasVideo) "Video" else "Photo",
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Column(
                modifier = Modifier.weight(0.66f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = session.sessionTitle,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = listOfNotNull(session.timeLabel, session.sourceLabel, session.mediaSummary)
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
}
