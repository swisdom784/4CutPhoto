package com.fourcut.photo.feature.gallery

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class GalleryGroupingTest {
    private val zoneId = ZoneId.of("Asia/Seoul")

    @Test
    fun sameMonthAndDayAcrossDifferentYearsStaySeparate() {
        val groups = buildGalleryDateGroups(
            inputs = listOf(
                input(1L, LocalDateTime.of(2026, 5, 19, 14, 0)),
                input(2L, LocalDateTime.of(2025, 5, 19, 14, 0))
            ),
            zoneId = zoneId
        )

        assertEquals(2, groups.size)
        assertEquals("2026", groups[0].yearLabel)
        assertEquals("2025", groups[1].yearLabel)
    }

    @Test
    fun groupsAndSessionsAreNewestFirst() {
        val groups = buildGalleryDateGroups(
            inputs = listOf(
                input(1L, LocalDateTime.of(2026, 5, 18, 14, 0)),
                input(2L, LocalDateTime.of(2026, 5, 19, 9, 0)),
                input(3L, LocalDateTime.of(2026, 5, 19, 18, 0))
            ),
            zoneId = zoneId
        )

        assertEquals("5월 19일", groups.first().dateLabel)
        assertEquals(listOf(3L, 2L), groups.first().sessions.map { it.id })
    }

    private fun input(
        id: Long,
        dateTime: LocalDateTime
    ): GalleryGroupingInput {
        return GalleryGroupingInput(
            capturedAtMillis = dateTime.atZone(zoneId).toInstant().toEpochMilli(),
            session = GallerySessionUiModel(
                id = id,
                sessionTitle = "Session $id",
                timeLabel = "14:00",
                sourceLabel = null,
                coverPath = null,
                tagNames = emptyList(),
                hasVideo = false,
                mediaSummary = "1 photo"
            )
        )
    }
}
