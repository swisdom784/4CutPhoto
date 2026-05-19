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
import androidx.compose.material3.AlertDialog
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

data class SessionDetailMediaUiModel(
    val path: String,
    val mimeType: String,
    val fileName: String
) {
    val isVideo: Boolean = mimeType.startsWith("video/")
    val displayName: String = fileName.trim().ifBlank {
        if (isVideo) "영상" else "사진"
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SessionDetailScreen(
    dateLabel: String,
    sessionTitle: String,
    sourceLabel: String?,
    tagNames: List<String>,
    suggestedTags: List<String>,
    media: List<SessionDetailMediaUiModel>,
    exportMessage: String?,
    onBack: () -> Unit,
    onTagQueryChange: (String) -> Unit,
    onSaveTags: (List<String>) -> Unit,
    onDeleteTagRequested: (String) -> Unit,
    onExportToGallery: () -> Unit,
    onDeleteSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditingTags by remember { mutableStateOf(false) }
    var tagQuery by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val editingTags = remember { mutableStateListOf<String>() }

    LaunchedEffect(tagNames) {
        editingTags.clear()
        editingTags.addAll(tagNames)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(start = 20.dp, top = 32.dp, end = 20.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("뒤로")
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
            text = if (tagNames.isEmpty()) "사람 태그 없음" else tagNames.joinToString(prefix = "#", separator = " #"),
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
                    Text("태그 저장")
                }
                TextButton(onClick = { isEditingTags = false }) {
                    Text("취소")
                }
            }
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = { isEditingTags = true }) {
                    Text("태그 수정")
                }
                TextButton(
                    onClick = onExportToGallery,
                    enabled = media.isNotEmpty()
                ) {
                    Text("기기 갤러리에 저장")
                }
                TextButton(onClick = { showDeleteDialog = true }) {
                    Text("세션 삭제")
                }
            }
        }
        exportMessage?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 132.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(media) { item ->
                MediaTile(item)
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("세션을 삭제할까요?") },
            text = { Text("이 세션의 사진, 영상, 사람 태그 기록이 앱에서 삭제돼요.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteSession()
                    }
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}

private fun MutableList<String>.replaceWith(tags: List<String>) {
    clear()
    addAll(tags)
}

@Composable
private fun MediaTile(media: SessionDetailMediaUiModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.72f)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (media.isVideo) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "영상",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = media.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else if (media.path.startsWith("http://") || media.path.startsWith("https://") || media.path.startsWith("/")) {
            AsyncImage(
                model = media.path,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = media.displayName,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
