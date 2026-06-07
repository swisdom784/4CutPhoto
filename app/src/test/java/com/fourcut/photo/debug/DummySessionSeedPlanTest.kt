package com.fourcut.photo.debug

import com.fourcut.photo.data.local.session.MediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DummySessionSeedPlanTest {
    @Test
    fun dummyPlanCreatesThreeSeparateQrSessionsWithFakeSources() {
        val plan = buildDummySessionSeedPlan(seedId = 123L)

        assertEquals(3, plan.size)
        assertEquals(
            listOf(
                "https://sample.invalid/qr/dummy-123-1",
                "https://sample.invalid/qr/dummy-123-2",
                "https://sample.invalid/qr/dummy-123-3"
            ),
            plan.map { it.sourceQrUrl }
        )
        assertTrue(plan.all { it.sourceHost == "sample.invalid" })
        assertEquals(
            listOf("더미 QR 1", "더미 QR 2", "더미 QR 3"),
            plan.map { it.sourceLabel }
        )
    }

    @Test
    fun eachDummySessionContainsImageAndVideoMedia() {
        val plan = buildDummySessionSeedPlan(seedId = 123L)

        plan.forEach { session ->
            assertEquals(
                listOf(MediaType.IMAGE, MediaType.VIDEO),
                session.media.map { it.type }
            )
        }
    }

    @Test
    fun dummyPlanUsesDifferentTagCombinationsIncludingTaglessSession() {
        val plan = buildDummySessionSeedPlan(seedId = 123L)

        assertEquals(listOf("친구A", "친구B"), plan[0].tagNames)
        assertEquals(listOf("혼자"), plan[1].tagNames)
        assertEquals(emptyList<String>(), plan[2].tagNames)
    }
}
