package com.fourcut.photo.debug

import com.fourcut.photo.data.local.session.MediaType
import com.fourcut.photo.data.repository.SaveMediaInput

data class DummySessionSeedInput(
    val sourceQrUrl: String,
    val sourceHost: String,
    val sourceLabel: String,
    val media: List<SaveMediaInput>,
    val tagNames: List<String>
)

fun buildDummySessionSeedPlan(seedId: Long): List<DummySessionSeedInput> {
    return listOf(
        DummySessionSeedInput(
            sourceQrUrl = "https://sample.invalid/qr/dummy-$seedId-1",
            sourceHost = "sample.invalid",
            sourceLabel = "더미 QR 1",
            media = dummyMedia(seedId, 1),
            tagNames = listOf("친구A", "친구B")
        ),
        DummySessionSeedInput(
            sourceQrUrl = "https://sample.invalid/qr/dummy-$seedId-2",
            sourceHost = "sample.invalid",
            sourceLabel = "더미 QR 2",
            media = dummyMedia(seedId, 2),
            tagNames = listOf("혼자")
        ),
        DummySessionSeedInput(
            sourceQrUrl = "https://sample.invalid/qr/dummy-$seedId-3",
            sourceHost = "sample.invalid",
            sourceLabel = "더미 QR 3",
            media = dummyMedia(seedId, 3),
            tagNames = emptyList()
        )
    )
}

private fun dummyMedia(seedId: Long, index: Int): List<SaveMediaInput> {
    return listOf(
        SaveMediaInput(
            type = MediaType.IMAGE,
            localPath = "dummy://image/$seedId/$index",
            mimeType = "image/png",
            fileName = "dummy-$seedId-$index.png"
        ),
        SaveMediaInput(
            type = MediaType.VIDEO,
            localPath = "dummy://video/$seedId/$index",
            mimeType = "video/mp4",
            fileName = "dummy-$seedId-$index.mp4"
        )
    )
}
