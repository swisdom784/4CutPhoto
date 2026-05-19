package com.fourcut.photo.feature.session

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.fourcut.photo.core.designsystem.component.PersonTagMiniPanel
import com.fourcut.photo.core.tag.addSelectedTag
import com.fourcut.photo.core.tag.removeSelectedTag
import coil.compose.AsyncImage

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SessionDetailScreen(
    dateLabel: String,
    sessionTitle: String,
    sourceLabel: String?,
    tagNames: List<String>,
    suggestedTags: List<String>,
    mediaPaths: List<String>,
    onBack: () -> Unit,
    onTagQueryChange: (String) -> Unit,
    onSaveTags: (List<String>) -> Unit,
    onDeleteTagRequested: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditingTags by remember { mutableStateOf(false) }
    var tagQuery by remember { mutableStateOf("") }
    val editingTags = remember { mutableStateListOf<String>() }

    LaunchedEffect(tagNames) {
        editingTags.clear()
        editingTags.addAll(tagNames)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("Back")
        }
        Text(
            text = dateLabel,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = sessionTitle,
            style = MaterialTheme.typography.headlineSmall
        )
        sourceLabel?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = if (tagNames.isEmpty()) "No people tags" else tagNames.joinToString(prefix = "#", separator = " #"),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        if (isEditingTags) {
            PersonTagMiniPanel(
                selectedTags = editingTags,
                suggestedTags = suggestedTags,
                query = tagQuery,
                onQueryChange = {
                    tagQuery = it
                    onTagQueryChange(it)
                },
                onTagSelected = { tag ->
                    editingTags.replaceWith(addSelectedTag(editingTags, tag))
                },
                onSelectedTagRemoved = { tag ->
                    editingTags.replaceWith(removeSelectedTag(editingTags, tag))
                },
                onCreateTag = { tag ->
                    val nextTags = addSelectedTag(editingTags, tag)
                    if (nextTags != editingTags) {
                        editingTags.replaceWith(nextTags)
                        tagQuery = ""
                        onTagQueryChange("")
                    }
                },
                onDeleteTagRequested = onDeleteTagRequested
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onSaveTags(editingTags)
                        isEditingTags = false
                    }
                ) {
                    Text("Save tags")
                }
                TextButton(onClick = { isEditingTags = false }) {
                    Text("Cancel")
                }
            }
        } else {
            Button(onClick = { isEditingTags = true }) {
                Text("Edit tags")
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 132.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(mediaPaths) { path ->
                MediaTile(path)
            }
        }
    }
}

private fun MutableList<String>.replaceWith(tags: List<String>) {
    clear()
    addAll(tags)
}

@Composable
private fun MediaTile(path: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.72f)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (path.startsWith("http://") || path.startsWith("https://") || path.startsWith("/")) {
            AsyncImage(
                model = path,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = path.ifBlank { "Media" },
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
