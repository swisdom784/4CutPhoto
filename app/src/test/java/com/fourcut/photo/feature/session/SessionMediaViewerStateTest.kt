package com.fourcut.photo.feature.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SessionMediaViewerStateTest {
    @Test
    fun imageMediaClickOpensViewer() {
        val media = SessionDetailMediaUiModel(
            path = "https://sample.invalid/photo.jpg",
            mimeType = "image/jpeg",
            fileName = "photo.jpg"
        )

        val state = openSessionMediaViewer(current = null, media = media)

        assertEquals(media, state?.media)
        assertEquals(SessionMediaViewerType.Image, state?.type)
    }

    @Test
    fun videoMediaClickOpensViewer() {
        val media = SessionDetailMediaUiModel(
            path = "https://sample.invalid/movie.mp4",
            mimeType = "video/mp4",
            fileName = "movie.mp4"
        )

        val state = openSessionMediaViewer(current = null, media = media)

        assertEquals(SessionMediaViewerType.Video, state?.type)
    }

    @Test
    fun closeClearsViewer() {
        val closed = closeSessionMediaViewer(
            SessionMediaViewerState(
                media = SessionDetailMediaUiModel("x", "image/jpeg", "x.jpg"),
                type = SessionMediaViewerType.Image
            )
        )

        assertNull(closed)
    }

    @Test
    fun videoViewerUsesThumbnailWhenAvailable() {
        val viewer = SessionMediaViewerState(
            media = SessionDetailMediaUiModel("content://sample.invalid/video/1", "video/mp4", "movie.mp4"),
            type = SessionMediaViewerType.Video
        )

        val display = buildSessionMediaViewerDisplayState(
            state = viewer,
            videoThumbnailPath = "C:/cache/thumb.jpg"
        )

        assertEquals("C:/cache/thumb.jpg", display.imageModel)
        assertEquals(true, display.showVideoThumbnail)
        assertEquals(false, display.showVideoFallback)
    }

    @Test
    fun videoViewerFallsBackWithoutThumbnail() {
        val viewer = SessionMediaViewerState(
            media = SessionDetailMediaUiModel("content://sample.invalid/video/1", "video/mp4", "movie.mp4"),
            type = SessionMediaViewerType.Video
        )

        val display = buildSessionMediaViewerDisplayState(
            state = viewer,
            videoThumbnailPath = null
        )

        assertEquals(true, display.showVideoFallback)
    }
}
