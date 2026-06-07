package com.fourcut.photo.feature.download

import java.net.URI
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class PreviewMediaSummary(
    val totalCount: Int,
    val photoCount: Int,
    val videoCount: Int,
    val sourceHost: String,
    val saveDateLabel: String,
    val mediaCountLabel: String,
    val selectedTagLabel: String,
    val isEmpty: Boolean,
    val emptyMessage: String
)

fun buildPreviewMediaSummary(
    sourceUrl: String,
    capturedAtMillis: Long,
    items: List<PreviewMedia>,
    selectedTags: List<String>,
    zoneId: ZoneId = ZoneId.systemDefault()
): PreviewMediaSummary {
    val photoCount = items.count { it.mimeType.startsWith("image/") }
    val videoCount = items.count { it.mimeType.startsWith("video/") }
    val mediaCountParts = buildList {
        if (photoCount > 0) add("사진 ${photoCount}개")
        if (videoCount > 0) add("영상 ${videoCount}개")
    }
    val normalizedSelectedTags = selectedTags
        .map { it.trim() }
        .filter { it.isNotBlank() }
    val selectedTagLabel = if (normalizedSelectedTags.isEmpty()) {
        "사람 태그 없음"
    } else {
        normalizedSelectedTags.joinToString(prefix = "#", separator = " #")
    }

    return PreviewMediaSummary(
        totalCount = items.size,
        photoCount = photoCount,
        videoCount = videoCount,
        sourceHost = runCatching { URI(sourceUrl).host }.getOrNull().orEmpty().ifBlank { "알 수 없는 출처" },
        saveDateLabel = previewSaveDateFormatter.withZone(zoneId).format(Instant.ofEpochMilli(capturedAtMillis)),
        mediaCountLabel = mediaCountParts.joinToString(" · ").ifBlank { "담은 항목 0개" },
        selectedTagLabel = selectedTagLabel,
        isEmpty = items.isEmpty(),
        emptyMessage = "담은 항목이 없어요. 사진이나 영상을 하나 이상 담아주세요."
    )
}

fun removePreviewMedia(
    current: List<PreviewMedia>,
    item: PreviewMedia
): List<PreviewMedia> {
    return current.filterNot { it == item }
}

private val previewSaveDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy년 M월 d일 HH:mm", Locale.KOREAN)
