package com.fourcut.photo.feature.download

internal enum class WebViewPageStatus {
    Loading,
    PageStarted,
    PageVisible,
    Loaded,
    Error,
    Timeout,
    RenderGone,
    Failed
}

internal enum class WebViewPageEvent {
    PageStarted,
    PageCommitVisible,
    PageFinished,
    Error,
    Timeout,
    RenderGone,
    Retry
}

internal fun webViewPageStateReducer(
    current: WebViewPageStatus,
    event: WebViewPageEvent
): WebViewPageStatus {
    return when (event) {
        WebViewPageEvent.PageStarted -> WebViewPageStatus.PageStarted
        WebViewPageEvent.PageCommitVisible -> WebViewPageStatus.PageVisible
        WebViewPageEvent.PageFinished -> when (current) {
            WebViewPageStatus.Error,
            WebViewPageStatus.Timeout,
            WebViewPageStatus.RenderGone,
            WebViewPageStatus.Failed -> current
            else -> WebViewPageStatus.Loaded
        }
        WebViewPageEvent.Error -> WebViewPageStatus.Error
        WebViewPageEvent.Timeout -> when (current) {
            WebViewPageStatus.PageVisible,
            WebViewPageStatus.Loaded -> current
            else -> WebViewPageStatus.Timeout
        }
        WebViewPageEvent.RenderGone -> WebViewPageStatus.RenderGone
        WebViewPageEvent.Retry -> WebViewPageStatus.Loading
    }
}

internal fun webViewPageStatusMessage(status: WebViewPageStatus): String? {
    return when (status) {
        WebViewPageStatus.Loading -> "다운로드 페이지를 여는 중이에요..."
        WebViewPageStatus.PageStarted -> "다운로드 페이지를 불러오고 있어요..."
        WebViewPageStatus.PageVisible,
        WebViewPageStatus.Loaded -> null
        WebViewPageStatus.Error,
        WebViewPageStatus.Failed -> "페이지를 불러오지 못했어요. 네트워크 상태를 확인하거나 외부 브라우저로 열어보세요."
        WebViewPageStatus.Timeout -> "페이지 응답이 지연되고 있어요. 다시 시도하거나 외부 브라우저로 열어보세요."
        WebViewPageStatus.RenderGone -> "페이지를 표시하지 못했어요. 다시 시도하거나 외부 브라우저로 열어보세요."
    }
}

internal fun webViewPageCanRetry(status: WebViewPageStatus): Boolean {
    return status == WebViewPageStatus.Error ||
        status == WebViewPageStatus.Timeout ||
        status == WebViewPageStatus.RenderGone ||
        status == WebViewPageStatus.Failed
}

internal fun webViewPageCanOpenExternalBrowser(status: WebViewPageStatus): Boolean {
    return webViewPageCanRetry(status)
}
