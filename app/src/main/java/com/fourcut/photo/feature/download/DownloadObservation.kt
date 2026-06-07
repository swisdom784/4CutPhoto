package com.fourcut.photo.feature.download

import com.fourcut.photo.core.download.DownloadResult

enum class DownloadObservationStage {
    DirectMediaUrl,
    WebViewFallback,
    Unsupported,
    WebViewDownloadDetected,
    SaveFailed
}

data class DownloadObservation(
    val stage: DownloadObservationStage,
    val userMessage: String,
    val developerNote: String
)

fun downloadObservationForResult(result: DownloadResult): DownloadObservation {
    return when (result) {
        is DownloadResult.Automatic -> DownloadObservation(
            stage = DownloadObservationStage.DirectMediaUrl,
            userMessage = "사진이나 영상 파일 링크를 바로 찾았어요.",
            developerNote = "DownloadResolver classified the QR URL as a direct media URL."
        )

        is DownloadResult.NeedsWebView -> DownloadObservation(
            stage = DownloadObservationStage.WebViewFallback,
            userMessage = "원본 다운로드 페이지를 앱 안에서 열고 있어요.",
            developerNote = "DownloadResolver could not classify the URL as direct media, so WebView fallback is required."
        )

        is DownloadResult.Unsupported -> DownloadObservation(
            stage = DownloadObservationStage.Unsupported,
            userMessage = result.reason,
            developerNote = "DownloadResolver rejected the scanned value before WebView fallback."
        )
    }
}

fun webViewDownloadDetectedObservation(itemCount: Int): DownloadObservation {
    return DownloadObservation(
        stage = DownloadObservationStage.WebViewDownloadDetected,
        userMessage = "${itemCount}개 항목을 담았어요.",
        developerNote = "WebView DownloadListener captured a supported image/video download URL."
    )
}

fun saveFailedObservation(): DownloadObservation {
    return DownloadObservation(
        stage = DownloadObservationStage.SaveFailed,
        userMessage = "저장하지 못했어요. 네트워크 상태를 확인한 뒤 다시 시도해주세요.",
        developerNote = "Persisting remote media or saving the Room session failed."
    )
}
