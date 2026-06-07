package com.fourcut.photo.feature.download

import android.os.Build
import android.os.Message
import android.webkit.DownloadListener
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.fourcut.photo.core.designsystem.component.PersonTagMiniPanel
import com.fourcut.photo.core.download.DownloadableMedia
import com.fourcut.photo.core.download.DownloadResolver
import com.fourcut.photo.core.download.DownloadResult
import com.fourcut.photo.core.download.captureWebViewDownload
import com.fourcut.photo.core.download.openRemoteMediaStream
import com.fourcut.photo.core.media.AppMediaStorage
import com.fourcut.photo.core.tag.addSelectedTag
import com.fourcut.photo.core.tag.removeSelectedTag
import com.fourcut.photo.data.local.session.MediaType
import com.fourcut.photo.data.repository.SaveMediaInput
import com.fourcut.photo.data.repository.SessionRepository
import com.fourcut.photo.data.repository.TagRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URI
import java.net.URL
import java.time.ZoneId

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
    sessionRepository: SessionRepository,
    tagRepository: TagRepository,
    mediaStorage: AppMediaStorage,
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var uiState by remember(sourceUrl) { mutableStateOf<DownloadFlowUiState>(DownloadFlowUiState.Resolving) }
    val selectedTags = remember { mutableStateListOf<String>() }
    var suggestedTags by remember { mutableStateOf<List<String>>(emptyList()) }
    var tagQuery by remember { mutableStateOf("") }
    var saveStatus by remember(sourceUrl) { mutableStateOf(DownloadSaveStatus.Idle) }
    var downloadObservation by remember(sourceUrl) { mutableStateOf<DownloadObservation?>(null) }
    val capturedAtMillis = remember(sourceUrl) { System.currentTimeMillis() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(sourceUrl) {
        val result = withContext(Dispatchers.Default) {
            DownloadResolver().resolve(sourceUrl)
        }
        downloadObservation = downloadObservationForResult(result)
        uiState = when (result) {
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

    LaunchedEffect(tagQuery) {
        suggestedTags = tagRepository.search(tagQuery).map { it.name }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(start = 20.dp, top = 32.dp, end = 20.dp, bottom = 20.dp)
    ) {
        when (val state = uiState) {
            DownloadFlowUiState.Resolving -> Text(
                text = "다운로드를 준비하고 있어요...",
                modifier = Modifier.align(Alignment.Center)
            )

            is DownloadFlowUiState.Preview -> DownloadPreview(
                state = state,
                selectedTags = selectedTags,
                saveStatus = saveStatus,
                tagQuery = tagQuery,
                capturedAtMillis = capturedAtMillis,
                observation = downloadObservation,
                onQueryChange = { tagQuery = it },
                onTagSelected = { tag ->
                    selectedTags.replaceWith(addSelectedTag(selectedTags, tag))
                },
                onSelectedTagRemoved = { tag ->
                    selectedTags.replaceWith(removeSelectedTag(selectedTags, tag))
                },
                onCreateTag = { tag ->
                    val nextTags = addSelectedTag(selectedTags, tag)
                    if (nextTags != selectedTags) {
                        selectedTags.replaceWith(nextTags)
                        tagQuery = ""
                    }
                },
                suggestedTags = suggestedTags,
                onItemRemoved = { item ->
                    uiState = state.copy(items = removePreviewMedia(state.items, item))
                },
                onSaved = {
                    val preview = uiState as? DownloadFlowUiState.Preview
                    if (preview != null && preview.items.isNotEmpty() && saveStatus != DownloadSaveStatus.Saving) {
                        saveStatus = DownloadSaveStatus.Saving
                        scope.launch {
                            runCatching {
                                sessionRepository.saveSession(
                                    capturedAt = capturedAtMillis,
                                    sourceQrUrl = sourceUrl,
                                    sourceHost = runCatching { URI(sourceUrl).host }.getOrNull(),
                                    sourceLabel = runCatching { URI(sourceUrl).host }.getOrNull(),
                                    media = preview.items.map { it.toSaveMediaInput() },
                                    tagNames = selectedTags,
                                    persistMedia = { sessionId, input ->
                                        input.persistToAppStorage(sessionId, mediaStorage)
                                    }
                                )
                            }.onSuccess {
                                onSaved()
                            }.onFailure {
                                saveStatus = DownloadSaveStatus.Failed
                                downloadObservation = saveFailedObservation()
                            }
                        }
                    }
                },
                onCancel = onCancel
            )

            is DownloadFlowUiState.NeedsWebView -> DownloadWebViewFallback(
                sourceUrl = state.sourceUrl,
                onReviewCaptured = { items ->
                    uiState = DownloadFlowUiState.Preview(
                        sourceUrl = sourceUrl,
                        items = items
                    )
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
                    Text("스캔으로 돌아가기")
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
    suggestedTags: List<String>,
    saveStatus: DownloadSaveStatus,
    tagQuery: String,
    capturedAtMillis: Long,
    observation: DownloadObservation?,
    onQueryChange: (String) -> Unit,
    onTagSelected: (String) -> Unit,
    onSelectedTagRemoved: (String) -> Unit,
    onCreateTag: (String) -> Unit,
    onItemRemoved: (PreviewMedia) -> Unit,
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    val summary = buildPreviewMediaSummary(
        sourceUrl = state.sourceUrl,
        capturedAtMillis = capturedAtMillis,
        items = state.items,
        selectedTags = selectedTags,
        zoneId = ZoneId.systemDefault()
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "세션 저장",
            style = MaterialTheme.typography.headlineSmall
        )
        observation?.let {
            Text(
                text = it.userMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = state.sourceUrl,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        PreviewSummaryCard(summary = summary)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(state.items) { item ->
                PreviewMediaCard(
                    item = item,
                    onRemove = { onItemRemoved(item) }
                )
            }
        }
        if (summary.isEmpty) {
            Text(
                text = summary.emptyMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
        PersonTagMiniPanel(
            selectedTags = selectedTags,
            suggestedTags = suggestedTags,
            query = tagQuery,
            onQueryChange = onQueryChange,
            onTagSelected = onTagSelected,
            onSelectedTagRemoved = onSelectedTagRemoved,
            onCreateTag = onCreateTag,
            onDeleteTagRequested = {}
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onSaved,
                enabled = saveStatus != DownloadSaveStatus.Saving && !summary.isEmpty
            ) {
                Text("저장")
            }
            TextButton(
                onClick = onCancel,
                enabled = saveStatus != DownloadSaveStatus.Saving
            ) {
                Text("취소")
            }
        }
        downloadSaveStatusMessage(saveStatus)?.let { message ->
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (saveStatus == DownloadSaveStatus.Saving) {
                    CircularProgressIndicator()
                }
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PreviewSummaryCard(summary: PreviewMediaSummary) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("담은 항목 ${summary.totalCount}개", style = MaterialTheme.typography.titleMedium)
            Text(
                text = summary.mediaCountLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "출처 ${summary.sourceHost} · 저장 날짜 ${summary.saveDateLabel}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = summary.selectedTagLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun PreviewMediaCard(
    item: PreviewMedia,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier.width(156.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PreviewMediaThumb(item = item)
            Text(item.fileName, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = if (item.mimeType.startsWith("video/")) "영상" else "사진",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onRemove) {
                Text("제거")
            }
        }
    }
}

@Composable
private fun PreviewMediaThumb(item: PreviewMedia) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        if (item.mimeType.startsWith("image/")) {
            AsyncImage(
                model = item.localPath,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("영상", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "재생 가능한 항목",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

internal fun addCapturedPreviewMedia(
    current: List<PreviewMedia>,
    captured: PreviewMedia
): List<PreviewMedia> {
    if (current.any { it.localPath == captured.localPath }) return current
    return current + captured
}

private fun MutableList<String>.replaceWith(tags: List<String>) {
    clear()
    addAll(tags)
}

private fun PreviewMedia.toSaveMediaInput(): SaveMediaInput {
    return SaveMediaInput(
        type = if (mimeType.startsWith("video/")) MediaType.VIDEO else MediaType.IMAGE,
        localPath = localPath,
        mimeType = mimeType,
        fileName = fileName
    )
}

private suspend fun SaveMediaInput.persistToAppStorage(
    sessionId: Long,
    mediaStorage: AppMediaStorage
): SaveMediaInput {
    if (!localPath.startsWith("http://") && !localPath.startsWith("https://")) {
        return this
    }

    val file = withContext(Dispatchers.IO) {
        URL(localPath).openRemoteMediaStream().use { input ->
            mediaStorage.saveOriginal(sessionId, fileName, input)
        }
    }
    return copy(localPath = file.absolutePath)
}

private fun DownloadableMedia.toPreviewMedia(): PreviewMedia {
    return PreviewMedia(
        localPath = url,
        mimeType = mimeType,
        fileName = suggestedFileName
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DownloadWebViewFallback(
    sourceUrl: String,
    onReviewCaptured: (List<PreviewMedia>) -> Unit,
    onCancel: () -> Unit
) {
    var capturedItems by remember(sourceUrl) { mutableStateOf<List<PreviewMedia>>(emptyList()) }
    var captureStatus by remember(sourceUrl) {
        mutableStateOf<WebViewCaptureStatus>(WebViewCaptureStatus.Waiting)
    }
    var pageStatus by remember(sourceUrl) { mutableStateOf(WebViewPageStatus.Loading) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "다운로드 선택",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = webViewCaptureStatusMessage(captureStatus),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        webViewPageStatusMessage(pageStatus)?.let { message ->
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (pageStatus == WebViewPageStatus.Loading) {
                    CircularProgressIndicator()
                }
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        AndroidView(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            factory = { context ->
                WebView(context).apply {
                    val downloadPageConfig = defaultWebViewDownloadPageConfig()
                    configureDownloadPageWebView(downloadPageConfig)
                    webChromeClient = object : WebChromeClient() {
                        override fun onCreateWindow(
                            view: WebView?,
                            isDialog: Boolean,
                            isUserGesture: Boolean,
                            resultMsg: Message?
                        ): Boolean {
                            val transport = resultMsg?.obj as? WebView.WebViewTransport ?: return false
                            transport.webView = this@apply
                            resultMsg.sendToTarget()
                            return true
                        }
                    }
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                            pageStatus = WebViewPageStatus.Loading
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            if (pageStatus != WebViewPageStatus.Failed) {
                                pageStatus = WebViewPageStatus.Loaded
                            }
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            if (request?.isForMainFrame != false) {
                                pageStatus = WebViewPageStatus.Failed
                            }
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            if (!downloadPageConfig.keepNavigationInsideWebView) return false
                            val nextUrl = request?.url?.toString() ?: return false
                            view?.loadUrl(nextUrl)
                            return true
                        }
                    }
                    setDownloadListener(
                        DownloadListener { url, _, contentDisposition, mimeType, _ ->
                            val capturedMedia = captureWebViewDownload(
                                url = url,
                                contentDisposition = contentDisposition,
                                mimeType = mimeType
                            )?.toPreviewMedia()

                            if (capturedMedia == null) {
                                captureStatus = WebViewCaptureStatus.IgnoredUnsupported
                            } else {
                                val nextItems = addCapturedPreviewMedia(capturedItems, capturedMedia)
                                capturedItems = nextItems
                                captureStatus = WebViewCaptureStatus.Captured(nextItems.size)
                            }
                        }
                    )
                    loadUrl(sourceUrl)
                }
            }
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                enabled = capturedItems.isNotEmpty(),
                onClick = { onReviewCaptured(capturedItems) }
            ) {
                Text("담은 항목 확인")
            }
            TextButton(onClick = onCancel) {
                Text("취소")
            }
        }
    }
}

private fun WebView.configureDownloadPageWebView(config: WebViewDownloadPageConfig) {
    settings.javaScriptEnabled = config.javaScriptEnabled
    settings.domStorageEnabled = config.domStorageEnabled
    settings.setSupportMultipleWindows(config.supportMultipleWindows)
    settings.javaScriptCanOpenWindowsAutomatically = config.javaScriptCanOpenWindowsAutomatically
    settings.loadWithOverviewMode = config.loadWithOverviewMode
    settings.useWideViewPort = config.useWideViewPort
    if (config.allowMixedContentCompatibilityMode && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
    }
}
