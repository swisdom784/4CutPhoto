package com.fourcut.photo.feature.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.fourcut.photo.core.designsystem.component.QuietStateCard
import com.fourcut.photo.core.designsystem.component.QuietStateKind

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
    val mediaSummary: String,
    val coverMimeType: String? = null
) {
    val hasImageCover: Boolean = !coverPath.isNullOrBlank() && coverMimeType?.startsWith("image/") == true
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GalleryScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    availableTagNames: List<String>,
    groups: List<GalleryDateGroupUiModel>,
    onOpenScan: () -> Unit,
    onSessionSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(start = 20.dp, top = 36.dp, end = 20.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "갤러리",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "QR 하나가 하나의 세션으로 따로 저장돼요.",
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
                placeholder = { Text("사람 태그 검색") }
            )
        }
        val tagSuggestions = galleryTagSuggestions(
            query = query,
            tagNames = availableTagNames
        )
        if (tagSuggestions.isNotEmpty()) {
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tagSuggestions.forEach { tagName ->
                        SuggestionChip(
                            onClick = { onQueryChange(tagName) },
                            label = { Text(tagName) }
                        )
                    }
                }
            }
        }
        galleryEmptyState(query, groups.size)?.let { state ->
            item {
                GalleryEmptyStateCard(
                    state = state,
                    onOpenScan = onOpenScan
                )
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
private fun GalleryEmptyStateCard(
    state: GalleryEmptyState,
    onOpenScan: () -> Unit
) {
    QuietStateCard(
        kind = when (state) {
            GalleryEmptyState.NoSessions -> QuietStateKind.GalleryEmpty
            GalleryEmptyState.NoSearchResults -> QuietStateKind.GallerySearchEmpty
        },
        onPrimaryAction = if (state == GalleryEmptyState.NoSessions) onOpenScan else null
    )
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
                val coverPath = session.coverPath
                if (session.hasImageCover) {
                    AsyncImage(
                        model = coverPath,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = if (session.hasVideo) "영상" else "사진",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
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
