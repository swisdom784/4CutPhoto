package com.fourcut.photo.feature.download

import android.webkit.DownloadListener
import android.webkit.URLUtil
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.fourcut.photo.core.designsystem.component.PersonTagMiniPanel
import com.fourcut.photo.core.download.DownloadResolver
import com.fourcut.photo.core.download.DownloadResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed interface DownloadFlowUiState {
    data object Resolving : DownloadFlowUiState
    data class Preview(val sourceUrl: String, val items: List<PreviewMedia>) : DownloadFlowUiState
    data class NeedsWebView(val sourceUrl: String) : DownloadFlowUiState
    data class Error(val message: String) : DownloadFlowUiState
}

data class PreviewMedia(
    val localPath: String,
    val mimeType: String,
    val fileName: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DownloadFlowScreen(
    sourceUrl: String,
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var uiState by remember(sourceUrl) { mutableStateOf<DownloadFlowUiState>(DownloadFlowUiState.Resolving) }
    val selectedTags = remember { mutableStateListOf<String>() }
    var tagQuery by remember { mutableStateOf("") }

    LaunchedEffect(sourceUrl) {
        uiState = withContext(Dispatchers.Default) {
            when (val result = DownloadResolver().resolve(sourceUrl)) {
                is DownloadResult.Automatic -> DownloadFlowUiState.Preview(
                    sourceUrl = sourceUrl,
                    items = result.items.map {
                        PreviewMedia(
                            localPath = it.url,
                            mimeType = it.mimeType,
                            fileName = it.suggestedFileName
                        )
                    }
                )

                is DownloadResult.NeedsWebView -> DownloadFlowUiState.NeedsWebView(result.url)
                is DownloadResult.Unsupported -> DownloadFlowUiState.Error(result.reason)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {
        when (val state = uiState) {
            DownloadFlowUiState.Resolving -> Text(
                text = "Preparing downloads...",
                modifier = Modifier.align(Alignment.Center)
            )

            is DownloadFlowUiState.Preview -> DownloadPreview(
                state = state,
                selectedTags = selectedTags,
                tagQuery = tagQuery,
                onQueryChange = { tagQuery = it },
                onTagSelected = { tag ->
                    if (selectedTags.none { it.equals(tag, ignoreCase = true) }) selectedTags.add(tag)
                },
                onCreateTag = { tag ->
                    val normalized = tag.trim()
                    if (normalized.isNotBlank() && selectedTags.none { it.equals(normalized, ignoreCase = true) }) {
                        selectedTags.add(normalized)
                        tagQuery = ""
                    }
                },
                onSaved = onSaved,
                onCancel = onCancel
            )

            is DownloadFlowUiState.NeedsWebView -> DownloadWebViewFallback(
                sourceUrl = state.sourceUrl,
                onMediaCaptured = { media ->
                    uiState = DownloadFlowUiState.Preview(sourceUrl, listOf(media))
                },
                onCancel = onCancel
            )

            is DownloadFlowUiState.Error -> Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(state.message)
                Button(onClick = onCancel) {
                    Text("Back to Scan")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DownloadPreview(
    state: DownloadFlowUiState.Preview,
    selectedTags: List<String>,
    tagQuery: String,
    onQueryChange: (String) -> Unit,
    onTagSelected: (String) -> Unit,
    onCreateTag: (String) -> Unit,
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Save session",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = state.sourceUrl,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(state.items) { item ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(item.fileName, style = MaterialTheme.typography.bodyMedium)
                        Text(item.mimeType, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        PersonTagMiniPanel(
            selectedTags = selectedTags,
            suggestedTags = emptyList(),
            query = tagQuery,
            onQueryChange = onQueryChange,
            onTagSelected = onTagSelected,
            onCreateTag = onCreateTag,
            onDeleteTagRequested = {}
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = onSaved) {
                Text("Save")
            }
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    }
}

@Composable
private fun DownloadWebViewFallback(
    sourceUrl: String,
    onMediaCaptured: (PreviewMedia) -> Unit,
    onCancel: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Select downloads",
            style = MaterialTheme.typography.headlineSmall
        )
        AndroidView(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    setDownloadListener(
                        DownloadListener { url, _, contentDisposition, mimeType, _ ->
                            val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
                            if (mimeType.startsWith("image/") || mimeType.startsWith("video/")) {
                                onMediaCaptured(
                                    PreviewMedia(
                                        localPath = url,
                                        mimeType = mimeType,
                                        fileName = fileName
                                    )
                                )
                            }
                        }
                    )
                    loadUrl(sourceUrl)
                }
            }
        )
        TextButton(onClick = onCancel) {
            Text("Cancel")
        }
    }
}
