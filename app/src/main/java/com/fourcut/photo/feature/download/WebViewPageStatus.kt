package com.fourcut.photo.feature.download

internal enum class WebViewPageStatus {
    Loading,
    Loaded,
    Failed
}

internal fun webViewPageStatusMessage(status: WebViewPageStatus): String? {
    return when (status) {
        WebViewPageStatus.Loading -> "다운로드 페이지를 여는 중이에요..."
        WebViewPageStatus.Loaded -> null
        WebViewPageStatus.Failed -> "페이지를 열지 못했어요. 네트워크 상태를 확인한 뒤 다시 시도해주세요."
    }
}
