package com.fourcut.photo.core.designsystem.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun PersonTagMiniPanel(
    selectedTags: List<String>,
    suggestedTags: List<String>,
    query: String,
    onQueryChange: (String) -> Unit,
    onTagSelected: (String) -> Unit,
    onSelectedTagRemoved: (String) -> Unit = {},
    onCreateTag: (String) -> Unit,
    onDeleteTagRequested: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var pendingDelete by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            selectedTags.forEach { tag ->
                InputChip(
                    selected = true,
                    onClick = { onSelectedTagRemoved(tag) },
                    label = { Text(tag) }
                )
            }

            suggestedTags
                .filterNot { suggestion ->
                    selectedTags.any { it.equals(suggestion, ignoreCase = true) }
                }
                .forEach { tag ->
                    FilterChip(
                        selected = false,
                        onClick = { onTagSelected(tag) },
                        label = { Text(tag) },
                        modifier = Modifier.combinedClickable(
                            onClick = { onTagSelected(tag) },
                            onLongClick = { pendingDelete = tag }
                        )
                    )
                }

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .widthIn(min = 132.dp)
                    .onPreviewKeyEvent { event ->
                        if (event.key == Key.Enter && query.isNotBlank()) {
                            onCreateTag(query)
                            true
                        } else {
                            false
                        }
                    },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                placeholder = { Text("이름") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (query.isNotBlank()) {
                            onCreateTag(query)
                        }
                    }
                )
            )
        }
    }

    pendingDelete?.let { tag ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteTagRequested(tag)
                        pendingDelete = null
                    }
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("취소")
                }
            },
            title = { Text("태그를 삭제할까요?") },
            text = { Text("저장된 모든 세션에서 이 태그가 제거돼요.") }
        )
    }
}
