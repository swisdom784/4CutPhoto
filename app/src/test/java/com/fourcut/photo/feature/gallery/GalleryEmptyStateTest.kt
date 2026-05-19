package com.fourcut.photo.feature.gallery

import org.junit.Assert.assertEquals
import org.junit.Test

class GalleryEmptyStateTest {
    @Test
    fun emptyQueryAndNoGroupsShowsNoSessionsState() {
        val state = galleryEmptyState(query = "", groupCount = 0)

        assertEquals(GalleryEmptyState.NoSessions, state)
    }

    @Test
    fun nonEmptyQueryAndNoGroupsShowsNoSearchResultsState() {
        val state = galleryEmptyState(query = "Hajin", groupCount = 0)

        assertEquals(GalleryEmptyState.NoSearchResults, state)
    }

    @Test
    fun groupsPresentShowsNoEmptyState() {
        val state = galleryEmptyState(query = "", groupCount = 1)

        assertEquals(null, state)
    }
}
