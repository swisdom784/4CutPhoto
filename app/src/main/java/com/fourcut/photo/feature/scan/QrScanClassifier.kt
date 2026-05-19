package com.fourcut.photo.feature.scan

sealed interface QrScanResult {
    data class AcceptedUrl(val value: String) : QrScanResult
    data class Unsupported(val value: String) : QrScanResult
    data object DuplicateIgnored : QrScanResult
    data object Ignored : QrScanResult
}

fun classifyQrScan(
    rawValue: String?,
    lastAcceptedValue: String?,
    isProcessing: Boolean
): QrScanResult {
    val value = rawValue?.trim().orEmpty()
    if (value.isBlank()) return QrScanResult.Ignored
    if (isProcessing && value == lastAcceptedValue) return QrScanResult.DuplicateIgnored

    return if (value.startsWith("http://") || value.startsWith("https://")) {
        QrScanResult.AcceptedUrl(value)
    } else {
        QrScanResult.Unsupported(value)
    }
}
