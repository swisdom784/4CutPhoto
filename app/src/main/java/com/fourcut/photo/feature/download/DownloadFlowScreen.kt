package com.fourcut.photo.feature.download

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Message
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import org.json.JSONArray
import org.json.JSONObject
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val thumbnailStore = remember(context) { VideoThumbnailStore(context) }
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
            resolveDownloadWithWebViewTimeout(sourceUrl = sourceUrl) {
                DownloadResolver().resolve(it)
            }
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
                thumbnailStore = thumbnailStore,
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
    thumbnailStore: VideoThumbnailStore,
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
        PreviewSummaryCard(summary = summary)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(state.items) { item ->
                PreviewMediaCard(
                    item = item,
                    thumbnailStore = thumbnailStore,
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
    thumbnailStore: VideoThumbnailStore,
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
            PreviewMediaThumb(
                item = item,
                thumbnailStore = thumbnailStore
            )
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
private fun PreviewMediaThumb(
    item: PreviewMedia,
    thumbnailStore: VideoThumbnailStore
) {
    var thumbnailPath by remember(item.localPath) { mutableStateOf<String?>(null) }
    val videoState = buildVideoThumbnailDisplayState(
        source = item.localPath,
        mimeType = item.mimeType,
        thumbnailPath = thumbnailPath
    )
    LaunchedEffect(item.localPath, item.mimeType) {
        if (videoState.shouldGenerateThumbnail) {
            thumbnailPath = withContext(Dispatchers.IO) {
                thumbnailStore.getOrCreateThumbnail(item.localPath)
            }
        }
    }
    val displaySource = buildPreviewMediaDisplaySource(item)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        if (displaySource.showImage) {
            AsyncImage(
                model = displaySource.model,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        } else if (videoState.showThumbnailImage) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = videoState.imageModel,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
                Text(
                    text = "영상",
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        } else if (displaySource.showVideoTile) {
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
        } else {
            Text(
                text = "미리보기를 표시할 수 없어요",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val contentResolver = context.contentResolver
    var capturedItems by remember(sourceUrl) { mutableStateOf<List<PreviewMedia>>(emptyList()) }
    var captureStatus by remember(sourceUrl) {
        mutableStateOf<WebViewCaptureStatus>(WebViewCaptureStatus.Waiting)
    }
    var pageStatus by remember(sourceUrl) { mutableStateOf(WebViewPageStatus.Loading) }
    var webView by remember(sourceUrl) { mutableStateOf<WebView?>(null) }
    var retryKey by remember(sourceUrl) { mutableStateOf(0) }
    var diagnosticState by remember(sourceUrl) {
        mutableStateOf(WebViewDiagnosticState.initial(sourceUrl, System.currentTimeMillis()))
    }
    var domMediaCandidates by remember(sourceUrl) { mutableStateOf<List<WebViewDomMediaCandidate>>(emptyList()) }
    var popupDiagnostic by remember(sourceUrl) { mutableStateOf<WebViewPopupDiagnostic?>(null) }
    var popupDismissMessage by remember(sourceUrl) { mutableStateOf<String?>(null) }
    val diagnosticTimeoutPolicy = remember { WebViewDiagnosticTimeoutPolicy() }
    val manualImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        val picked = uris.mapNotNull { uri ->
            runCatching {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            buildPickedPreviewMedia(
                uri = uri.toString(),
                mimeType = contentResolver.getType(uri)
            )
        }
        if (picked.isNotEmpty()) {
            val nextItems = addPickedPreviewMedia(capturedItems, picked)
            capturedItems = nextItems
            captureStatus = WebViewCaptureStatus.Captured(nextItems.size)
        }
    }

    fun recordDiagnostic(event: WebViewDiagnosticEvent) {
        val nextState = reduceWebViewDiagnostic(diagnosticState, event)
        diagnosticState = nextState
        WebViewDiagnosticRuntime.log(nextState, nextState.lastEvent)
    }

    fun recordJsProbe(view: WebView?) {
        if (!WebViewDiagnosticRuntime.isEnabled || view == null) return
        view.evaluateJavascript(webViewDiagnosticProbeScript()) { result ->
            val probe = parseWebViewDiagnosticProbe(result)
            val candidates = buildWebViewDomMediaCandidates(probe?.rawMediaCandidates.orEmpty())
            domMediaCandidates = candidates
            popupDiagnostic = probe?.popupDiagnostic
            recordDiagnostic(
                WebViewDiagnosticEvent.JsProbe(
                    nowMillis = System.currentTimeMillis(),
                    readyState = probe?.readyState,
                    hasTitle = probe?.hasTitle ?: false,
                    titleLength = probe?.titleLength,
                    hasBody = probe?.hasBody ?: false,
                    bodyTextLength = probe?.bodyTextLength,
                    bodyChildCount = probe?.bodyChildCount,
                    documentElementChildCount = probe?.documentElementChildCount,
                    bodyBackgroundColor = probe?.bodyBackgroundColor,
                    bodyTextColor = probe?.bodyTextColor,
                    documentBackgroundColor = probe?.documentBackgroundColor,
                    viewportWidth = probe?.viewportWidth,
                    viewportHeight = probe?.viewportHeight,
                    scrollHeight = probe?.scrollHeight,
                    bodyClientWidth = probe?.bodyClientWidth,
                    bodyClientHeight = probe?.bodyClientHeight,
                    linkCount = probe?.linkCount,
                    buttonCount = probe?.buttonCount,
                    imageCount = probe?.imageCount,
                    videoCount = probe?.videoCount,
                    iframeCount = probe?.iframeCount,
                    formCount = probe?.formCount,
                    scriptCount = probe?.scriptCount,
                    anchorHostCount = probe?.anchorHostCount,
                    focusedElementTag = probe?.focusedElementTag,
                    isAndroidWebView = probe?.isAndroidWebView ?: false,
                    layoutSuspicious = probe?.layoutSuspicious ?: false,
                    domCandidateProbeRequired = probe?.domCandidateProbeRequired ?: false,
                    mediaCandidateCount = candidates.size,
                    previewableCandidateCount = candidates.count {
                        it.kind == WebViewDomMediaCandidateKind.Previewable
                    },
                    unsupportedCandidateCount = candidates.count {
                        it.kind == WebViewDomMediaCandidateKind.Unsupported
                    },
                    succeeded = probe != null
                )
            )
        }
    }

    fun dismissPopup(view: WebView?) {
        if (view == null) return
        view.evaluateJavascript(webViewPopupDismissScript()) { result ->
            popupDismissMessage = if (result?.contains("true") == true) {
                "팝업 닫기를 시도했어요."
            } else {
                "앱에서 팝업을 닫지 못했어요. 외부 브라우저에서 다운로드한 뒤 앱으로 가져올 수 있어요."
            }
            recordJsProbe(view)
        }
    }

    LaunchedEffect(sourceUrl, retryKey, webView) {
        val currentWebView = webView ?: return@LaunchedEffect
        capturedItems = emptyList()
        captureStatus = WebViewCaptureStatus.Waiting
        pageStatus = webViewPageStateReducer(pageStatus, WebViewPageEvent.Retry)
        recordDiagnostic(WebViewDiagnosticEvent.Retry(System.currentTimeMillis()))
        currentWebView.loadUrl(sourceUrl)
    }

    LaunchedEffect(sourceUrl, retryKey, pageStatus, diagnosticState) {
        if (pageStatus != WebViewPageStatus.Loading && pageStatus != WebViewPageStatus.PageStarted) {
            return@LaunchedEffect
        }
        kotlinx.coroutines.delay(10_000L)
        if (
            (pageStatus == WebViewPageStatus.Loading || pageStatus == WebViewPageStatus.PageStarted) &&
            diagnosticTimeoutPolicy.shouldTimeout(diagnosticState, System.currentTimeMillis())
        ) {
            pageStatus = webViewPageStateReducer(pageStatus, WebViewPageEvent.Timeout)
            recordDiagnostic(WebViewDiagnosticEvent.Timeout(System.currentTimeMillis()))
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val fallbackActionState = buildWebViewFallbackActionState(
            diagnosticKind = diagnosticState.kind,
            candidates = domMediaCandidates,
            capturedItemCount = capturedItems.size,
            canRetry = webViewPageCanRetry(pageStatus),
            canOpenExternalBrowser = diagnosticState.externalBrowserAvailable ||
                webViewPageCanOpenExternalBrowser(pageStatus)
        )
        val popupActionState = buildWebViewPopupActionState(popupDiagnostic)
        Text(
            text = "다운로드 선택",
            style = MaterialTheme.typography.headlineSmall
        )
        if (popupActionState.message.isNotBlank()) {
            Text(
                text = popupActionState.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        popupDismissMessage?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = fallbackActionState.message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        fallbackActionState.candidateSummary?.let { summary ->
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
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
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { viewContext ->
                    WebView(viewContext).apply {
                        setBackgroundColor(android.graphics.Color.WHITE)
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
                                recordDiagnostic(WebViewDiagnosticEvent.NewWindow(System.currentTimeMillis()))
                                return true
                            }
                        }
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                pageStatus = webViewPageStateReducer(pageStatus, WebViewPageEvent.PageStarted)
                                recordDiagnostic(
                                    WebViewDiagnosticEvent.PageStarted(
                                        nowMillis = System.currentTimeMillis(),
                                        isMainFrame = true
                                    )
                                )
                            }

                            override fun onPageCommitVisible(view: WebView?, url: String?) {
                                pageStatus = webViewPageStateReducer(pageStatus, WebViewPageEvent.PageCommitVisible)
                                recordDiagnostic(WebViewDiagnosticEvent.PageCommitVisible(System.currentTimeMillis()))
                                recordJsProbe(view)
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                pageStatus = webViewPageStateReducer(pageStatus, WebViewPageEvent.PageFinished)
                                recordDiagnostic(WebViewDiagnosticEvent.PageFinished(System.currentTimeMillis()))
                                recordJsProbe(view)
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                if (request?.isForMainFrame != false) {
                                    pageStatus = webViewPageStateReducer(pageStatus, WebViewPageEvent.Error)
                                    recordDiagnostic(
                                        WebViewDiagnosticEvent.PageError(
                                            nowMillis = System.currentTimeMillis(),
                                            isMainFrame = request?.isForMainFrame != false,
                                            errorCode = error?.errorCode
                                        )
                                    )
                                }
                            }

                            override fun onReceivedHttpError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                errorResponse: WebResourceResponse?
                            ) {
                                if (request?.isForMainFrame != false) {
                                    pageStatus = webViewPageStateReducer(pageStatus, WebViewPageEvent.Error)
                                    recordDiagnostic(
                                        WebViewDiagnosticEvent.HttpError(
                                            nowMillis = System.currentTimeMillis(),
                                            isMainFrame = request?.isForMainFrame != false,
                                            statusCode = errorResponse?.statusCode
                                        )
                                    )
                                }
                            }

                            override fun onRenderProcessGone(
                                view: WebView?,
                                detail: RenderProcessGoneDetail?
                            ): Boolean {
                                pageStatus = webViewPageStateReducer(pageStatus, WebViewPageEvent.RenderGone)
                                recordDiagnostic(WebViewDiagnosticEvent.RenderGone(System.currentTimeMillis()))
                                return true
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                if (!downloadPageConfig.keepNavigationInsideWebView) return false
                                val nextUrl = request?.url?.toString() ?: return false
                                recordDiagnostic(
                                    WebViewDiagnosticEvent.NavigationOverride(
                                        nowMillis = System.currentTimeMillis(),
                                        isMainFrame = request.isForMainFrame
                                    )
                                )
                                if (!nextUrl.startsWith("http://") && !nextUrl.startsWith("https://")) {
                                    captureStatus = WebViewCaptureStatus.IgnoredUnsupported
                                    recordDiagnostic(WebViewDiagnosticEvent.UnsupportedDownload(System.currentTimeMillis()))
                                    return true
                                }
                                return false
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
                                    recordDiagnostic(WebViewDiagnosticEvent.UnsupportedDownload(System.currentTimeMillis()))
                                } else {
                                    val nextItems = addCapturedPreviewMedia(capturedItems, capturedMedia)
                                    capturedItems = nextItems
                                    captureStatus = WebViewCaptureStatus.Captured(nextItems.size)
                                    recordDiagnostic(WebViewDiagnosticEvent.CapturedDownload(System.currentTimeMillis()))
                                }
                            }
                        )
                        webView = this
                    }
                }
            )
            val pageMessage = webViewPageStatusMessage(pageStatus)
            if (pageMessage != null) {
                WebViewStatusOverlay(
                    message = pageMessage,
                    showLoading = pageStatus == WebViewPageStatus.Loading ||
                        pageStatus == WebViewPageStatus.PageStarted,
                    canRetry = webViewPageCanRetry(pageStatus),
                    canOpenExternalBrowser = webViewPageCanOpenExternalBrowser(pageStatus),
                    onRetry = { retryKey += 1 },
                    onOpenExternalBrowser = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(sourceUrl)))
                    }
                )
            }
        }
        buildWebViewDiagnosticOverlayState(
            state = diagnosticState,
            enabled = WebViewDiagnosticRuntime.isEnabled
        )?.let { overlay ->
            WebViewDiagnosticPanel(overlay)
        }
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
            if (fallbackActionState.showRetry) {
                TextButton(onClick = { retryKey += 1 }) {
                    Text("다시 시도")
                }
            }
            if (fallbackActionState.showExternalBrowser) {
                TextButton(onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(sourceUrl)))
                }) {
                    Text("외부 브라우저로 열기")
                }
            }
            val previewableDomCandidates = domMediaCandidates
                .filter { it.isCollectableDomCandidate() }
            val recommendedDomCandidates = previewableDomCandidates
                .filter { it.recommended }
                .ifEmpty { previewableDomCandidates }
            if (fallbackActionState.showCollectDomCandidates) {
                TextButton(onClick = {
                    val nextItems = recommendedDomCandidates.fold(capturedItems) { current, candidate ->
                        addCapturedPreviewMedia(current, candidate.toPreviewMedia())
                    }
                    capturedItems = nextItems
                    captureStatus = WebViewCaptureStatus.Captured(nextItems.size)
                }) {
                    Text("미디어 후보 담기")
                }
            }
            if (popupActionState.showDismissPopup) {
                TextButton(onClick = { dismissPopup(webView) }) {
                    Text("팝업 닫기 시도")
                }
            }
            if (fallbackActionState.showManualImport) {
                TextButton(onClick = {
                    manualImportLauncher.launch(arrayOf("image/*", "video/*"))
                }) {
                    Text("기기에서 사진/영상 가져오기")
                }
            }
        }
    }
}

private fun WebViewDomMediaCandidate.toPreviewMedia(): PreviewMedia {
    val extensionPart = extension?.let { ".$it" } ?: ""
    val safeTag = tag.ifBlank { "media" }
    return PreviewMedia(
        localPath = actualUrl,
        mimeType = mimeType ?: if (safeTag == "video") "video/*" else "image/*",
        fileName = "${safeTag}-${displayHost}$extensionPart"
    )
}

@Composable
private fun WebViewDiagnosticPanel(state: WebViewDiagnosticOverlayState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "진단 ${state.statusLabel} · ${state.host}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "마지막 이벤트 ${state.lastEvent} · ${state.elapsedMillis}ms · 담은 항목 ${state.capturedItemCount}개",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "visible=${state.pageVisible}, captured=${state.downloadCaptured}, timeout=${state.timedOut}, external=${state.externalBrowserAvailable}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            state.jsSummary?.let { summary ->
                Text(
                    text = "JS $summary",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WebViewStatusOverlay(
    message: String,
    showLoading: Boolean,
    canRetry: Boolean,
    canOpenExternalBrowser: Boolean,
    onRetry: () -> Unit,
    onOpenExternalBrowser: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (showLoading) {
                CircularProgressIndicator()
            }
            Text(
                text = message,
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            FlowRow(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (canRetry) {
                    Button(onClick = onRetry) {
                        Text("다시 시도")
                    }
                }
                if (canOpenExternalBrowser) {
                    TextButton(onClick = onOpenExternalBrowser) {
                        Text("외부 브라우저로 열기")
                    }
                }
            }
        }
    }
}

private fun WebView.configureDownloadPageWebView(config: WebViewDownloadPageConfig) {
    settings.javaScriptEnabled = config.javaScriptEnabled
    settings.domStorageEnabled = config.domStorageEnabled
    settings.setSupportMultipleWindows(config.supportMultipleWindows)
    settings.javaScriptCanOpenWindowsAutomatically = config.javaScriptCanOpenWindowsAutomatically
    settings.mediaPlaybackRequiresUserGesture = false
    settings.loadWithOverviewMode = config.loadWithOverviewMode
    settings.useWideViewPort = config.useWideViewPort
    if (config.allowMixedContentCompatibilityMode && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
    }
    CookieManager.getInstance().setAcceptCookie(true)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        settings.safeBrowsingEnabled = true
    }
}

private data class WebViewJsProbeResult(
    val readyState: String?,
    val hasTitle: Boolean,
    val titleLength: Int?,
    val hasBody: Boolean,
    val bodyTextLength: Int?,
    val bodyChildCount: Int?,
    val documentElementChildCount: Int?,
    val bodyBackgroundColor: String?,
    val bodyTextColor: String?,
    val documentBackgroundColor: String?,
    val viewportWidth: Int?,
    val viewportHeight: Int?,
    val scrollHeight: Int?,
    val bodyClientWidth: Int?,
    val bodyClientHeight: Int?,
    val linkCount: Int?,
    val buttonCount: Int?,
    val imageCount: Int?,
    val videoCount: Int?,
    val iframeCount: Int?,
    val formCount: Int?,
    val scriptCount: Int?,
    val anchorHostCount: Int?,
    val focusedElementTag: String?,
    val isAndroidWebView: Boolean,
    val layoutSuspicious: Boolean,
    val domCandidateProbeRequired: Boolean,
    val popupDiagnostic: WebViewPopupDiagnostic,
    val rawMediaCandidates: List<WebViewDomMediaCandidateRaw>
)

private fun webViewDiagnosticProbeScript(): String {
    return """
        (function() {
          var body = document.body;
          var doc = document.documentElement;
          var bodyStyle = body ? window.getComputedStyle(body) : null;
          var docStyle = doc ? window.getComputedStyle(doc) : null;
          var anchors = Array.prototype.slice.call(document.querySelectorAll('a[href]'));
          var anchorHosts = {};
          anchors.forEach(function(anchor) {
            try {
              var parsed = new URL(anchor.href);
              if (parsed.host) {
                anchorHosts[parsed.host] = true;
              }
            } catch (e) {}
          });
          var focused = document.activeElement && document.activeElement.tagName
            ? document.activeElement.tagName
            : null;
          function rectInfo(element) {
            if (!element || !element.getBoundingClientRect) {
              return { width: 0, height: 0, inViewport: false, visible: false };
            }
            var rect = element.getBoundingClientRect();
            var style = window.getComputedStyle(element);
            var visible = style.display !== 'none' && style.visibility !== 'hidden' && Number(style.opacity || '1') > 0;
            var inViewport = rect.width > 0 && rect.height > 0 &&
              rect.bottom >= 0 && rect.right >= 0 &&
              rect.top <= (window.innerHeight || 0) &&
              rect.left <= (window.innerWidth || 0);
            return {
              width: Math.round(rect.width || 0),
              height: Math.round(rect.height || 0),
              inViewport: !!inViewport,
              visible: !!visible
            };
          }
          function mediaCandidate(tag, url, element) {
            var rect = rectInfo(element);
            return {
              tag: tag,
              url: url || '',
              visible: rect.visible,
              inViewport: rect.inViewport,
              width: rect.width,
              height: rect.height,
              naturalWidth: element && element.naturalWidth ? element.naturalWidth : 0,
              naturalHeight: element && element.naturalHeight ? element.naturalHeight : 0,
              videoWidth: element && element.videoWidth ? element.videoWidth : 0,
              videoHeight: element && element.videoHeight ? element.videoHeight : 0,
              hint: elementTextHint(element)
            };
          }
          function pushUrlLikeCandidate(tag, value, element) {
            if (!value || typeof value !== 'string') {
              return;
            }
            var match = value.match(/(https?:\/\/[^\s"'<>]+|blob:[^\s"'<>]+|data:[^\s"'<>]+)/);
            if (match && match[1]) {
              mediaCandidates.push(mediaCandidate(tag, match[1], element));
            }
          }
          var mediaCandidates = [];
          Array.prototype.slice.call(document.images || []).forEach(function(image) {
            mediaCandidates.push(mediaCandidate('img', image.currentSrc || image.src, image));
          });
          Array.prototype.slice.call(document.querySelectorAll('video')).forEach(function(video) {
            mediaCandidates.push(mediaCandidate('video', video.currentSrc || video.src, video));
          });
          Array.prototype.slice.call(document.querySelectorAll('source[src]')).forEach(function(source) {
            mediaCandidates.push(mediaCandidate('source', source.src, source));
          });
          Array.prototype.slice.call(document.querySelectorAll('a[href],a[download]')).forEach(function(anchor) {
            mediaCandidates.push(mediaCandidate('a', anchor.href, anchor));
          });
          Array.prototype.slice.call(document.querySelectorAll('button,[role="button"],input[type="button"],input[type="submit"]')).forEach(function(button) {
            Array.prototype.slice.call(button.attributes || []).forEach(function(attribute) {
              if (attribute && attribute.name && attribute.name.indexOf('data-') === 0) {
                pushUrlLikeCandidate('button-data', attribute.value, button);
              }
            });
            pushUrlLikeCandidate('button-onclick', button.getAttribute('onclick') || '', button);
          });
          Array.prototype.slice.call(document.querySelectorAll('[style*="background-image"]')).forEach(function(element) {
            var style = window.getComputedStyle(element);
            var match = style.backgroundImage ? style.backgroundImage.match(/url\(["']?([^"')]+)["']?\)/) : null;
            if (match && match[1]) {
              mediaCandidates.push(mediaCandidate('background', match[1], element));
            }
          });
          var layoutSuspicious = !!body && body.clientHeight === 0 &&
            (document.images.length > 0 || document.querySelectorAll('video').length > 0 || document.querySelectorAll('button,[role="button"]').length > 0);
          function elementTextHint(element) {
            if (!element) {
              return '';
            }
            return [
              element.getAttribute('alt') || '',
              element.getAttribute('aria-label') || '',
              element.getAttribute('class') || '',
              element.getAttribute('id') || '',
              element.getAttribute('download') || ''
            ].join(' ');
          }
          function isCloseCandidate(element) {
            var hint = elementTextHint(element).toLowerCase();
            var text = (element && element.innerText ? element.innerText : '').trim().toLowerCase();
            return text === 'x' || text === '×' || text.indexOf('닫기') >= 0 || text.indexOf('확인') >= 0 ||
              hint.indexOf('close') >= 0 || hint.indexOf('dismiss') >= 0 || hint.indexOf('modal-close') >= 0;
          }
          var modalCandidates = Array.prototype.slice.call(document.querySelectorAll('[role="dialog"],[aria-modal="true"],dialog'));
          var closeCandidates = Array.prototype.slice.call(document.querySelectorAll('button,[role="button"],a,input[type="button"]')).filter(isCloseCandidate);
          var dominantOverlayCandidates = Array.prototype.slice.call(document.querySelectorAll('body *')).filter(function(element) {
            var rect = element.getBoundingClientRect ? element.getBoundingClientRect() : null;
            if (!rect) return false;
            var style = window.getComputedStyle(element);
            var position = style.position || '';
            var zIndex = parseInt(style.zIndex || '0', 10);
            var coversMostViewport = rect.width >= (window.innerWidth || 0) * 0.7 && rect.height >= (window.innerHeight || 0) * 0.5;
            return coversMostViewport && (position === 'fixed' || position === 'absolute' || zIndex >= 100);
          });
          return JSON.stringify({
            readyState: document.readyState || null,
            hasTitle: !!document.title,
            titleLength: document.title ? document.title.length : 0,
            hasBody: !!body,
            bodyTextLength: body && body.innerText ? body.innerText.length : 0,
            bodyChildCount: body ? body.children.length : 0,
            documentElementChildCount: doc ? doc.children.length : 0,
            bodyBackgroundColor: bodyStyle ? bodyStyle.backgroundColor : null,
            bodyTextColor: bodyStyle ? bodyStyle.color : null,
            documentBackgroundColor: docStyle ? docStyle.backgroundColor : null,
            viewportWidth: window.innerWidth || 0,
            viewportHeight: window.innerHeight || 0,
            scrollHeight: doc ? doc.scrollHeight : 0,
            bodyClientWidth: body ? body.clientWidth : 0,
            bodyClientHeight: body ? body.clientHeight : 0,
            linkCount: document.querySelectorAll('a[href]').length,
            buttonCount: document.querySelectorAll('button,[role="button"],input[type="button"],input[type="submit"]').length,
            imageCount: document.images ? document.images.length : 0,
            videoCount: document.querySelectorAll('video').length,
            iframeCount: document.querySelectorAll('iframe').length,
            formCount: document.forms ? document.forms.length : 0,
            scriptCount: document.scripts ? document.scripts.length : 0,
            anchorHostCount: Object.keys(anchorHosts).length,
            focusedElementTag: focused,
            isAndroidWebView: navigator.userAgent ? navigator.userAgent.indexOf('; wv') >= 0 : false,
            layoutSuspicious: layoutSuspicious,
            domCandidateProbeRequired: mediaCandidates.length > 0 && document.querySelectorAll('video,img,source,a[href],a[download]').length > 0,
            modalCandidateCount: modalCandidates.length,
            closeCandidateCount: closeCandidates.length,
            dominantOverlayCandidateCount: dominantOverlayCandidates.length,
            mediaCandidates: mediaCandidates.slice(0, 20),
            host: window.location ? window.location.host : null
          });
        })();
    """.trimIndent()
}

private fun parseWebViewDiagnosticProbe(result: String?): WebViewJsProbeResult? {
    val raw = result
        ?.trim()
        ?.takeIf { it.isNotBlank() && it != "null" }
        ?: return null
    val jsonText = runCatching {
        if (raw.startsWith("\"")) {
            JSONArray("[$raw]").getString(0)
        } else {
            raw
        }
    }.getOrElse { return null }

    val json = runCatching { JSONObject(jsonText) }.getOrElse { return null }
    return WebViewJsProbeResult(
        readyState = json.optString("readyState").takeIf { it.isNotBlank() },
        hasTitle = json.optBoolean("hasTitle", false),
        titleLength = json.optNullableInt("titleLength"),
        hasBody = json.optBoolean("hasBody", false),
        bodyTextLength = json.optNullableInt("bodyTextLength"),
        bodyChildCount = json.optNullableInt("bodyChildCount"),
        documentElementChildCount = json.optNullableInt("documentElementChildCount"),
        bodyBackgroundColor = json.optString("bodyBackgroundColor").takeIf { it.isNotBlank() },
        bodyTextColor = json.optString("bodyTextColor").takeIf { it.isNotBlank() },
        documentBackgroundColor = json.optString("documentBackgroundColor").takeIf { it.isNotBlank() },
        viewportWidth = json.optNullableInt("viewportWidth"),
        viewportHeight = json.optNullableInt("viewportHeight"),
        scrollHeight = json.optNullableInt("scrollHeight"),
        bodyClientWidth = json.optNullableInt("bodyClientWidth"),
        bodyClientHeight = json.optNullableInt("bodyClientHeight"),
        linkCount = json.optNullableInt("linkCount"),
        buttonCount = json.optNullableInt("buttonCount"),
        imageCount = json.optNullableInt("imageCount"),
        videoCount = json.optNullableInt("videoCount"),
        iframeCount = json.optNullableInt("iframeCount"),
        formCount = json.optNullableInt("formCount"),
        scriptCount = json.optNullableInt("scriptCount"),
        anchorHostCount = json.optNullableInt("anchorHostCount"),
        focusedElementTag = json.optString("focusedElementTag").takeIf { it.isNotBlank() && it != "null" },
        isAndroidWebView = json.optBoolean("isAndroidWebView", false),
        layoutSuspicious = json.optBoolean("layoutSuspicious", false),
        domCandidateProbeRequired = json.optBoolean("domCandidateProbeRequired", false),
        popupDiagnostic = WebViewPopupDiagnostic(
            modalCandidateCount = json.optNullableInt("modalCandidateCount") ?: 0,
            closeCandidateCount = json.optNullableInt("closeCandidateCount") ?: 0,
            dominantOverlayCandidateCount = json.optNullableInt("dominantOverlayCandidateCount") ?: 0
        ),
        rawMediaCandidates = parseRawMediaCandidates(json.optJSONArray("mediaCandidates"))
    )
}

private fun JSONObject.optNullableInt(name: String): Int? {
    return if (has(name) && !isNull(name)) optInt(name) else null
}

private fun parseRawMediaCandidates(array: org.json.JSONArray?): List<WebViewDomMediaCandidateRaw> {
    if (array == null) return emptyList()
    return buildList {
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            add(
                WebViewDomMediaCandidateRaw(
                    tag = item.optString("tag").takeIf { it.isNotBlank() } ?: "unknown",
                    url = item.optString("url").takeIf { it.isNotBlank() },
                    visible = item.optBoolean("visible", false),
                    inViewport = item.optBoolean("inViewport", false),
                    width = item.optNullableInt("width"),
                    height = item.optNullableInt("height"),
                    naturalWidth = item.optNullableInt("naturalWidth"),
                    naturalHeight = item.optNullableInt("naturalHeight"),
                    videoWidth = item.optNullableInt("videoWidth"),
                    videoHeight = item.optNullableInt("videoHeight"),
                    hint = item.optString("hint").takeIf { it.isNotBlank() }
                )
            )
        }
    }
}

private fun webViewPopupDismissScript(): String {
    return """
        (function() {
          function hint(element) {
            return [
              element && element.innerText ? element.innerText : '',
              element && element.getAttribute ? element.getAttribute('aria-label') || '' : '',
              element && element.getAttribute ? element.getAttribute('class') || '' : '',
              element && element.getAttribute ? element.getAttribute('id') || '' : ''
            ].join(' ').trim().toLowerCase();
          }
          function isClose(element) {
            var value = hint(element);
            return value === 'x' || value === '×' || value.indexOf('닫기') >= 0 || value.indexOf('확인') >= 0 ||
              value.indexOf('close') >= 0 || value.indexOf('dismiss') >= 0 || value.indexOf('modal-close') >= 0;
          }
          var candidates = Array.prototype.slice.call(document.querySelectorAll('button,[role="button"],a,input[type="button"]')).filter(isClose);
          if (candidates.length === 0) {
            return false;
          }
          candidates[0].click();
          return true;
        })();
    """.trimIndent()
}
