package com.fourcut.photo.feature.gallery

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class GalleryGroupingInput(
    val capturedAtMillis: Long,
    val session: GallerySessionUiModel
)

fun buildGalleryDateGroups(
    inputs: List<GalleryGroupingInput>,
    zoneId: ZoneId
): List<GalleryDateGroupUiModel> {
    val yearFormatter = DateTimeFormatter.ofPattern("yyyy", Locale.US).withZone(zoneId)
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.US).withZone(zoneId)

    return inputs
        .sortedByDescending { it.capturedAtMillis }
        .groupBy { input ->
            Instant.ofEpochMilli(input.capturedAtMillis).atZone(zoneId).toLocalDate()
        }
        .map { (_, inputsForDate) ->
            val firstInstant = Instant.ofEpochMilli(inputsForDate.first().capturedAtMillis)
            GalleryDateGroupUiModel(
                yearLabel = yearFormatter.format(firstInstant),
                dateLabel = dateFormatter.format(firstInstant),
                sessions = inputsForDate.map { it.session }
            )
        }
}
