package com.fourcut.photo.feature.download

internal enum class DownloadSaveStatus {
    Idle,
    Saving,
    Failed
}

internal fun downloadSaveStatusMessage(status: DownloadSaveStatus): String? {
    return when (status) {
        DownloadSaveStatus.Idle -> null
        DownloadSaveStatus.Saving -> "세션을 저장하고 있어요..."
        DownloadSaveStatus.Failed -> "저장하지 못했어요. 네트워크 상태를 확인한 뒤 다시 시도해주세요."
    }
}
