package com.fourcut.photo.feature.download

internal sealed interface WebViewCaptureStatus {
    data object Waiting : WebViewCaptureStatus
    data class Captured(val itemCount: Int) : WebViewCaptureStatus
    data object IgnoredUnsupported : WebViewCaptureStatus
    data object EmptyCaptured : WebViewCaptureStatus
}

internal fun webViewCaptureStatusMessage(status: WebViewCaptureStatus): String {
    return when (status) {
        WebViewCaptureStatus.Waiting -> "원본 페이지에서 사진 또는 영상 다운로드 버튼을 눌러주세요."
        is WebViewCaptureStatus.Captured -> {
            "${status.itemCount}개 항목을 담았어요. 더 선택하거나 세션을 검토해주세요."
        }
        WebViewCaptureStatus.IgnoredUnsupported -> "앱에서 바로 저장할 수 없는 다운로드 방식이에요. 원본 페이지에서 다시 시도하거나 외부 브라우저로 열어보세요."
        WebViewCaptureStatus.EmptyCaptured -> "아직 담은 사진이나 영상이 없어요."
    }
}
