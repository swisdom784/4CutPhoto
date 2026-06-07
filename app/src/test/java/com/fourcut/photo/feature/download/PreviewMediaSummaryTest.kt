package com.fourcut.photo.feature.download

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class PreviewMediaSummaryTest {
    @Test
    fun summaryCountsPhotoAndVideoItems() {
        val summary = buildPreviewMediaSummary(
            sourceUrl = "https://sample.invalid/booth/download",
            capturedAtMillis = Instant.parse("2026-06-07T12:30:00Z").toEpochMilli(),
            items = listOf(
                PreviewMedia("https://sample.invalid/photo.jpg", "image/jpeg", "photo.jpg"),
                PreviewMedia("https://sample.invalid/video.mp4", "video/mp4", "video.mp4")
            ),
            selectedTags = listOf("하진", "정현"),
            zoneId = ZoneId.of("Asia/Seoul")
        )

        assertEquals(2, summary.totalCount)
        assertEquals(1, summary.photoCount)
        assertEquals(1, summary.videoCount)
        assertEquals("sample.invalid", summary.sourceHost)
        assertEquals("2026년 6월 7일 21:30", summary.saveDateLabel)
        assertEquals("사진 1개 · 영상 1개", summary.mediaCountLabel)
        assertEquals("#하진 #정현", summary.selectedTagLabel)
    }

    @Test
    fun emptySummaryExposesEmptyState() {
        val summary = buildPreviewMediaSummary(
            sourceUrl = "https://sample.invalid/booth/download",
            capturedAtMillis = 0L,
            items = emptyList(),
            selectedTags = emptyList(),
            zoneId = ZoneId.of("Asia/Seoul")
        )

        assertTrue(summary.isEmpty)
        assertEquals("담은 항목이 없어요. 사진이나 영상을 하나 이상 담아주세요.", summary.emptyMessage)
        assertEquals("사람 태그 없음", summary.selectedTagLabel)
    }

    @Test
    fun removePreviewMediaRemovesOnlyMatchingItem() {
        val photo = PreviewMedia("https://sample.invalid/photo.jpg", "image/jpeg", "photo.jpg")
        val video = PreviewMedia("https://sample.invalid/video.mp4", "video/mp4", "video.mp4")

        val result = removePreviewMedia(listOf(photo, video), photo)

        assertEquals(listOf(video), result)
    }
}
