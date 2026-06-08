package com.fourcut.photo.feature.download

import java.net.URI

internal enum class WebViewDiagnosticKind {
    Loading,
    Started,
    Visible,
    Finished,
    Error,
    Timeout,
    RenderGone,
    UnsupportedDownload,
    Captured,
    VisibleNoDownloadCaptured,
    VisibleButLayoutSuspicious
}

internal sealed interface WebViewDiagnosticEvent {
    val nowMillis: Long

    data class PageStarted(
        override val nowMillis: Long,
        val isMainFrame: Boolean
    ) : WebViewDiagnosticEvent

    data class PageCommitVisible(
        override val nowMillis: Long
    ) : WebViewDiagnosticEvent

    data class PageFinished(
        override val nowMillis: Long
    ) : WebViewDiagnosticEvent

    data class PageError(
        override val nowMillis: Long,
        val isMainFrame: Boolean,
        val errorCode: Int?
    ) : WebViewDiagnosticEvent

    data class HttpError(
        override val nowMillis: Long,
        val isMainFrame: Boolean,
        val statusCode: Int?
    ) : WebViewDiagnosticEvent

    data class RenderGone(
        override val nowMillis: Long
    ) : WebViewDiagnosticEvent

    data class NewWindow(
        override val nowMillis: Long
    ) : WebViewDiagnosticEvent

    data class NavigationOverride(
        override val nowMillis: Long,
        val isMainFrame: Boolean
    ) : WebViewDiagnosticEvent

    data class UnsupportedDownload(
        override val nowMillis: Long
    ) : WebViewDiagnosticEvent

    data class CapturedDownload(
        override val nowMillis: Long
    ) : WebViewDiagnosticEvent

    data class Timeout(
        override val nowMillis: Long
    ) : WebViewDiagnosticEvent

    data class JsProbe(
        override val nowMillis: Long,
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
        val layoutSuspicious: Boolean = false,
        val domCandidateProbeRequired: Boolean = false,
        val mediaCandidateCount: Int = 0,
        val previewableCandidateCount: Int = 0,
        val unsupportedCandidateCount: Int = 0,
        val succeeded: Boolean
    ) : WebViewDiagnosticEvent

    data class Retry(
        override val nowMillis: Long
    ) : WebViewDiagnosticEvent
}

internal data class WebViewDiagnosticState(
    val host: String,
    val externalBrowserUrl: String?,
    val startedAtMillis: Long,
    val lastEventAtMillis: Long,
    val kind: WebViewDiagnosticKind,
    val lastEvent: String,
    val capturedItemCount: Int,
    val pageVisible: Boolean,
    val downloadCaptured: Boolean,
    val timedOut: Boolean,
    val externalBrowserAvailable: Boolean,
    val retryAvailable: Boolean,
    val isMainFrame: Boolean?,
    val errorCode: Int?,
    val httpStatusCode: Int?,
    val jsReadyState: String?,
    val jsHasTitle: Boolean?,
    val jsTitleLength: Int?,
    val jsHasBody: Boolean?,
    val jsBodyTextLength: Int?,
    val jsBodyChildCount: Int?,
    val jsDocumentElementChildCount: Int?,
    val jsBodyBackgroundColor: String?,
    val jsBodyTextColor: String?,
    val jsDocumentBackgroundColor: String?,
    val jsViewportWidth: Int?,
    val jsViewportHeight: Int?,
    val jsScrollHeight: Int?,
    val jsBodyClientWidth: Int?,
    val jsBodyClientHeight: Int?,
    val jsLinkCount: Int?,
    val jsButtonCount: Int?,
    val jsImageCount: Int?,
    val jsVideoCount: Int?,
    val jsIframeCount: Int?,
    val jsFormCount: Int?,
    val jsScriptCount: Int?,
    val jsAnchorHostCount: Int?,
    val jsFocusedElementTag: String?,
    val jsIsAndroidWebView: Boolean?,
    val jsLayoutSuspicious: Boolean?,
    val jsDomCandidateProbeRequired: Boolean?,
    val jsMediaCandidateCount: Int?,
    val jsPreviewableCandidateCount: Int?,
    val jsUnsupportedCandidateCount: Int?
) {
    companion object {
        fun initial(sourceUrl: String, nowMillis: Long): WebViewDiagnosticState {
            val externalBrowserUrl = webViewDiagnosticExternalBrowserUrl(sourceUrl)
            return WebViewDiagnosticState(
                host = redactUrlForWebViewDiagnostic(sourceUrl),
                externalBrowserUrl = externalBrowserUrl,
                startedAtMillis = nowMillis,
                lastEventAtMillis = nowMillis,
                kind = WebViewDiagnosticKind.Loading,
                lastEvent = "Loading",
                capturedItemCount = 0,
                pageVisible = false,
                downloadCaptured = false,
                timedOut = false,
                externalBrowserAvailable = externalBrowserUrl != null,
                retryAvailable = false,
                isMainFrame = null,
                errorCode = null,
                httpStatusCode = null,
                jsReadyState = null,
                jsHasTitle = null,
                jsTitleLength = null,
                jsHasBody = null,
                jsBodyTextLength = null,
                jsBodyChildCount = null,
                jsDocumentElementChildCount = null,
                jsBodyBackgroundColor = null,
                jsBodyTextColor = null,
                jsDocumentBackgroundColor = null,
                jsViewportWidth = null,
                jsViewportHeight = null,
                jsScrollHeight = null,
                jsBodyClientWidth = null,
                jsBodyClientHeight = null,
                jsLinkCount = null,
                jsButtonCount = null,
                jsImageCount = null,
                jsVideoCount = null,
                jsIframeCount = null,
                jsFormCount = null,
                jsScriptCount = null,
                jsAnchorHostCount = null,
                jsFocusedElementTag = null,
                jsIsAndroidWebView = null,
                jsLayoutSuspicious = null,
                jsDomCandidateProbeRequired = null,
                jsMediaCandidateCount = null,
                jsPreviewableCandidateCount = null,
                jsUnsupportedCandidateCount = null
            )
        }
    }
}

internal data class WebViewDiagnosticOverlayState(
    val statusLabel: String,
    val lastEvent: String,
    val elapsedMillis: Long,
    val host: String,
    val capturedItemCount: Int,
    val pageVisible: Boolean,
    val downloadCaptured: Boolean,
    val timedOut: Boolean,
    val externalBrowserAvailable: Boolean,
    val retryAvailable: Boolean,
    val jsSummary: String?
)

internal data class WebViewDiagnosticTimeoutPolicy(
    val timeoutMillis: Long = 10_000L
) {
    fun shouldTimeout(state: WebViewDiagnosticState, nowMillis: Long): Boolean {
        if (state.pageVisible) return false
        if (state.kind == WebViewDiagnosticKind.Error) return false
        if (state.kind == WebViewDiagnosticKind.RenderGone) return false
        if (state.kind == WebViewDiagnosticKind.Timeout) return false
        if (state.kind == WebViewDiagnosticKind.Captured) return false
        return nowMillis - state.lastEventAtMillis >= timeoutMillis
    }
}

internal fun reduceWebViewDiagnostic(
    state: WebViewDiagnosticState,
    event: WebViewDiagnosticEvent
): WebViewDiagnosticState {
    return when (event) {
        is WebViewDiagnosticEvent.PageStarted -> state.copy(
            kind = WebViewDiagnosticKind.Started,
            lastEvent = "PageStarted",
            lastEventAtMillis = event.nowMillis,
            isMainFrame = event.isMainFrame,
            timedOut = false
        )
        is WebViewDiagnosticEvent.PageCommitVisible -> state.copy(
            kind = WebViewDiagnosticKind.Visible,
            lastEvent = "PageCommitVisible",
            lastEventAtMillis = event.nowMillis,
            pageVisible = true,
            timedOut = false
        )
        is WebViewDiagnosticEvent.PageFinished -> state.copy(
            kind = WebViewDiagnosticKind.Finished,
            lastEvent = "PageFinished",
            lastEventAtMillis = event.nowMillis
        )
        is WebViewDiagnosticEvent.PageError -> state.copy(
            kind = WebViewDiagnosticKind.Error,
            lastEvent = "PageError",
            lastEventAtMillis = event.nowMillis,
            isMainFrame = event.isMainFrame,
            errorCode = event.errorCode,
            retryAvailable = true,
            externalBrowserAvailable = true
        )
        is WebViewDiagnosticEvent.HttpError -> state.copy(
            kind = WebViewDiagnosticKind.Error,
            lastEvent = "HttpError",
            lastEventAtMillis = event.nowMillis,
            isMainFrame = event.isMainFrame,
            httpStatusCode = event.statusCode,
            retryAvailable = true,
            externalBrowserAvailable = true
        )
        is WebViewDiagnosticEvent.RenderGone -> state.copy(
            kind = WebViewDiagnosticKind.RenderGone,
            lastEvent = "RenderGone",
            lastEventAtMillis = event.nowMillis,
            retryAvailable = true,
            externalBrowserAvailable = true
        )
        is WebViewDiagnosticEvent.NewWindow -> state.copy(
            lastEvent = "NewWindow",
            lastEventAtMillis = event.nowMillis,
            externalBrowserAvailable = true
        )
        is WebViewDiagnosticEvent.NavigationOverride -> state.copy(
            lastEvent = "NavigationOverride",
            lastEventAtMillis = event.nowMillis,
            isMainFrame = event.isMainFrame
        )
        is WebViewDiagnosticEvent.UnsupportedDownload -> state.copy(
            kind = WebViewDiagnosticKind.UnsupportedDownload,
            lastEvent = "UnsupportedDownload",
            lastEventAtMillis = event.nowMillis,
            downloadCaptured = false,
            externalBrowserAvailable = true
        )
        is WebViewDiagnosticEvent.CapturedDownload -> state.copy(
            kind = WebViewDiagnosticKind.Captured,
            lastEvent = "CapturedDownload",
            lastEventAtMillis = event.nowMillis,
            capturedItemCount = state.capturedItemCount + 1,
            downloadCaptured = true
        )
        is WebViewDiagnosticEvent.Timeout -> state.copy(
            kind = WebViewDiagnosticKind.Timeout,
            lastEvent = "Timeout",
            lastEventAtMillis = event.nowMillis,
            timedOut = true,
            retryAvailable = true,
            externalBrowserAvailable = true
        )
        is WebViewDiagnosticEvent.JsProbe -> {
            val hasNoDownloadAction = event.succeeded &&
                state.pageVisible &&
                state.capturedItemCount == 0 &&
                event.hasBody &&
                (event.bodyTextLength ?: 0) > 0 &&
                (event.bodyChildCount ?: 0) > 0 &&
                (event.buttonCount ?: 0) == 0 &&
                (event.imageCount ?: 0) == 0 &&
                (event.videoCount ?: 0) == 0
            val hasLayoutSuspicion = event.succeeded &&
                state.pageVisible &&
                event.layoutSuspicious

            state.copy(
                kind = when {
                    hasLayoutSuspicion -> WebViewDiagnosticKind.VisibleButLayoutSuspicious
                    hasNoDownloadAction -> WebViewDiagnosticKind.VisibleNoDownloadCaptured
                    else -> state.kind
                },
                lastEvent = if (event.succeeded) "JsProbe" else "JsProbeFailed",
                lastEventAtMillis = event.nowMillis,
                retryAvailable = state.retryAvailable || hasNoDownloadAction || hasLayoutSuspicion,
                jsReadyState = event.readyState,
                jsHasTitle = event.hasTitle,
                jsTitleLength = event.titleLength,
                jsHasBody = event.hasBody,
                jsBodyTextLength = event.bodyTextLength,
                jsBodyChildCount = event.bodyChildCount,
                jsDocumentElementChildCount = event.documentElementChildCount,
                jsBodyBackgroundColor = event.bodyBackgroundColor,
                jsBodyTextColor = event.bodyTextColor,
                jsDocumentBackgroundColor = event.documentBackgroundColor,
                jsViewportWidth = event.viewportWidth,
                jsViewportHeight = event.viewportHeight,
                jsScrollHeight = event.scrollHeight,
                jsBodyClientWidth = event.bodyClientWidth,
                jsBodyClientHeight = event.bodyClientHeight,
                jsLinkCount = event.linkCount,
                jsButtonCount = event.buttonCount,
                jsImageCount = event.imageCount,
                jsVideoCount = event.videoCount,
                jsIframeCount = event.iframeCount,
                jsFormCount = event.formCount,
                jsScriptCount = event.scriptCount,
                jsAnchorHostCount = event.anchorHostCount,
                jsFocusedElementTag = event.focusedElementTag,
                jsIsAndroidWebView = event.isAndroidWebView,
                jsLayoutSuspicious = event.layoutSuspicious,
                jsDomCandidateProbeRequired = event.domCandidateProbeRequired,
                jsMediaCandidateCount = event.mediaCandidateCount,
                jsPreviewableCandidateCount = event.previewableCandidateCount,
                jsUnsupportedCandidateCount = event.unsupportedCandidateCount
            )
        }
        is WebViewDiagnosticEvent.Retry -> WebViewDiagnosticState.initial(
            sourceUrl = state.externalBrowserUrl ?: "https://${state.host}",
            nowMillis = event.nowMillis
        )
    }
}

internal fun buildWebViewDiagnosticOverlayState(
    state: WebViewDiagnosticState,
    enabled: Boolean
): WebViewDiagnosticOverlayState? {
    if (!enabled) return null
    return WebViewDiagnosticOverlayState(
        statusLabel = state.kind.name,
        lastEvent = state.lastEvent,
        elapsedMillis = state.lastEventAtMillis - state.startedAtMillis,
        host = state.host,
        capturedItemCount = state.capturedItemCount,
        pageVisible = state.pageVisible,
        downloadCaptured = state.downloadCaptured,
        timedOut = state.timedOut,
        externalBrowserAvailable = state.externalBrowserAvailable,
        retryAvailable = state.retryAvailable,
        jsSummary = buildJsSummary(state)
    )
}

internal fun redactUrlForWebViewDiagnostic(url: String?): String {
    if (url.isNullOrBlank()) return "unknown"
    return runCatching { URI(url).host }
        .getOrNull()
        ?.takeIf { it.isNotBlank() }
        ?: "unknown"
}

internal fun webViewDiagnosticExternalBrowserUrl(url: String?): String? {
    if (url.isNullOrBlank()) return null
    val uri = runCatching { URI(url) }.getOrNull() ?: return null
    val scheme = uri.scheme?.lowercase()
    val host = uri.host?.takeIf { it.isNotBlank() } ?: return null
    if (scheme != "http" && scheme != "https") return null
    return uri.toString().takeIf { host.isNotBlank() }
}

private fun buildJsSummary(state: WebViewDiagnosticState): String? {
    if (state.jsReadyState == null && state.jsHasBody == null) return null
    return "ready=${state.jsReadyState ?: "unknown"}, body=${state.jsHasBody}, text=${state.jsBodyTextLength ?: 0}, child=${state.jsBodyChildCount ?: 0}, button=${state.jsButtonCount ?: 0}, image=${state.jsImageCount ?: 0}, video=${state.jsVideoCount ?: 0}, iframe=${state.jsIframeCount ?: 0}, bodySize=${state.jsBodyClientWidth ?: 0}x${state.jsBodyClientHeight ?: 0}, media=${state.jsMediaCandidateCount ?: 0}, preview=${state.jsPreviewableCandidateCount ?: 0}, unsupported=${state.jsUnsupportedCandidateCount ?: 0}, layout=${state.jsLayoutSuspicious}, bg=${state.jsBodyBackgroundColor ?: "unknown"}"
}
