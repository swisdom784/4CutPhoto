package com.fourcut.photo.feature.scan

sealed interface QrScanState {
    data object Idle : QrScanState
    data class Detected(val value: String) : QrScanState
    data class Unsupported(val value: String) : QrScanState
}
