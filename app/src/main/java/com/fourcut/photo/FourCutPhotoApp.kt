package com.fourcut.photo

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.fourcut.photo.core.designsystem.component.FloatingNavMenu
import com.fourcut.photo.core.media.AppMediaStorage
import com.fourcut.photo.core.media.SystemGalleryExportMedia
import com.fourcut.photo.core.media.SystemGalleryExporter
import com.fourcut.photo.core.media.exportSessionMediaToSystemGallery
import com.fourcut.photo.data.local.FourCutDatabase
import com.fourcut.photo.data.local.session.SessionWithDetails
import com.fourcut.photo.data.repository.SessionRepository
import com.fourcut.photo.data.repository.TagRepository
import com.fourcut.photo.feature.download.DownloadFlowScreen
import com.fourcut.photo.feature.calendar.CalendarScreen
import com.fourcut.photo.feature.calendar.CalendarSessionUiModel
import com.fourcut.photo.feature.calendar.buildCalendarMonthUiModel
import com.fourcut.photo.feature.gallery.GalleryDateGroupUiModel
import com.fourcut.photo.feature.gallery.GalleryScreen
import com.fourcut.photo.feature.gallery.GallerySessionUiModel
import com.fourcut.photo.feature.gallery.GalleryGroupingInput
import com.fourcut.photo.feature.gallery.buildGalleryDateGroups
import com.fourcut.photo.feature.scan.ScanScreen
import com.fourcut.photo.feature.session.SessionDetailMediaUiModel
import com.fourcut.photo.feature.session.SessionDetailScreen
import com.fourcut.photo.navigation.AppDestination
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun FourCutPhotoApp() {
    val context = LocalContext.current
    val database = remember {
        Room.databaseBuilder(context, FourCutDatabase::class.java, "fourcut.db").build()
    }
    val tagRepository = remember { TagRepository(database.personTagDao()) }
    val sessionRepository = remember { SessionRepository(database.sessionDao(), tagRepository) }
    val mediaStorage = remember { AppMediaStorage(context) }
    val galleryExporter = remember { SystemGalleryExporter(context) }
    val sessions by database.sessionDao().observeSessionsWithDetails().collectAsState(initial = emptyList())

    var currentDestination by remember { mutableStateOf(AppDestination.Scan) }
    var pendingQrUrl by remember { mutableStateOf<String?>(null) }
    var galleryQuery by remember { mutableStateOf("") }
    var selectedSessionId by remember { mutableStateOf<Long?>(null) }
    var selectedCalendarDate by remember { mutableStateOf(LocalDate.now(zoneId)) }
    var detailTagQuery by remember { mutableStateOf("") }
    var detailSuggestedTags by remember { mutableStateOf<List<String>>(emptyList()) }
    var detailExportMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(detailTagQuery) {
        detailSuggestedTags = tagRepository.search(detailTagQuery).map { it.name }
    }

    LaunchedEffect(selectedSessionId) {
        detailExportMessage = null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val qrUrl = pendingQrUrl
        val detailSessionId = selectedSessionId
        val selectedSession = sessions.firstOrNull { it.session.id == detailSessionId }
        if (detailSessionId != null && selectedSession != null) {
            SessionDetailScreen(
                dateLabel = selectedSession.detailDateLabel(),
                sessionTitle = "Session ${selectedSession.session.sessionIndexForDay}",
                sourceLabel = selectedSession.session.sourceLabel,
                tagNames = selectedSession.tags.map { it.name },
                suggestedTags = detailSuggestedTags,
                media = selectedSession.media.map {
                    SessionDetailMediaUiModel(
                        path = it.localPath,
                        mimeType = it.mimeType,
                        fileName = it.fileName
                    )
                },
                exportMessage = detailExportMessage,
                onBack = { selectedSessionId = null },
                onTagQueryChange = { detailTagQuery = it },
                onSaveTags = { tags ->
                    scope.launch {
                        sessionRepository.replaceSessionTags(selectedSession.session.id, tags)
                    }
                },
                onDeleteTagRequested = { tagName ->
                    scope.launch {
                        tagRepository.search(tagName)
                            .firstOrNull { it.name.equals(tagName, ignoreCase = true) }
                            ?.let { tagRepository.deleteTag(it.id) }
                    }
                },
                onExportToGallery = {
                    scope.launch {
                        runCatching {
                            withContext(Dispatchers.IO) {
                                exportSessionMediaToSystemGallery(
                                    media = selectedSession.media.map {
                                        SystemGalleryExportMedia(
                                            localPath = it.localPath,
                                            fileName = it.fileName,
                                            mimeType = it.mimeType
                                        )
                                    },
                                    exporter = galleryExporter
                                )
                            }
                        }.onSuccess { summary ->
                            detailExportMessage = when {
                                summary.exportedCount > 0 && summary.skippedMissingCount > 0 ->
                                    "Saved ${summary.exportedCount} item(s). ${summary.skippedMissingCount} missing file(s) were skipped."
                                summary.exportedCount > 0 ->
                                    "Saved ${summary.exportedCount} item(s) to the device gallery."
                                else ->
                                    "No local media files were available to export."
                            }
                        }.onFailure {
                            detailExportMessage = "Could not save to the device gallery. Please try again."
                        }
                    }
                },
                onDeleteSession = {
                    scope.launch {
                        sessionRepository.deleteSession(selectedSession.session.id) { sessionId ->
                            withContext(Dispatchers.IO) {
                                mediaStorage.deleteSession(sessionId)
                            }
                        }
                        selectedSessionId = null
                        detailExportMessage = null
                    }
                }
            )
        } else if (detailSessionId != null) {
            Text(
                text = "Session not found",
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (qrUrl != null) {
            DownloadFlowScreen(
                sourceUrl = qrUrl,
                sessionRepository = sessionRepository,
                tagRepository = tagRepository,
                mediaStorage = mediaStorage,
                onSaved = { pendingQrUrl = null },
                onCancel = { pendingQrUrl = null }
            )
        } else {
            when (currentDestination) {
                AppDestination.Scan -> ScanScreen(
                    onBack = {},
                    onQrDetected = { pendingQrUrl = it }
                )

                AppDestination.Calendar -> CalendarScreen(
                    month = buildCalendarMonthUiModel(
                        selectedDate = selectedCalendarDate,
                        sessionDates = sessions.map { it.localDate() }
                    ),
                    sessionsForSelectedDay = sessions
                        .filter { it.localDate() == selectedCalendarDate }
                        .map { it.toCalendarSessionUiModel() },
                    onPreviousMonth = {
                        selectedCalendarDate = selectedCalendarDate.minusMonths(1)
                    },
                    onNextMonth = {
                        selectedCalendarDate = selectedCalendarDate.plusMonths(1)
                    },
                    onDaySelected = { day ->
                        selectedCalendarDate = selectedCalendarDate.withDayOfMonth(day)
                    },
                    onSessionSelected = { selectedSessionId = it }
                )

                AppDestination.Gallery -> GalleryScreen(
                    query = galleryQuery,
                    onQueryChange = { galleryQuery = it },
                    availableTagNames = sessions.flatMap { session -> session.tags.map { it.name } },
                    groups = sessions
                        .filter { session ->
                            galleryQuery.isBlank() ||
                                session.tags.any { it.name.contains(galleryQuery, ignoreCase = true) }
                        }
                        .toGalleryGroups(),
                    onSessionSelected = { selectedSessionId = it }
                )
            }
        }

        FloatingNavMenu(
            current = currentDestination,
            onNavigate = { currentDestination = it },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        )
    }
}

private val zoneId: ZoneId = ZoneId.systemDefault()
private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(zoneId)
private val yearFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy").withZone(zoneId)
private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d").withZone(zoneId)

private fun SessionWithDetails.localDate(): LocalDate {
    return Instant.ofEpochMilli(session.capturedAt).atZone(zoneId).toLocalDate()
}

private fun SessionWithDetails.toCalendarSessionUiModel(): CalendarSessionUiModel {
    return CalendarSessionUiModel(
        id = session.id,
        title = "Session ${session.sessionIndexForDay}",
        timeLabel = timeFormatter.format(Instant.ofEpochMilli(session.capturedAt)),
        sourceLabel = session.sourceLabel,
        sessionIndexForDay = session.sessionIndexForDay,
        tagNames = tags.map { it.name },
        mediaCount = media.size
    )
}

private fun SessionWithDetails.detailDateLabel(): String {
    val instant = Instant.ofEpochMilli(session.capturedAt)
    return "${yearFormatter.format(instant)} ${dateFormatter.format(instant)} · ${timeFormatter.format(instant)}"
}

private fun List<SessionWithDetails>.toGalleryGroups(): List<GalleryDateGroupUiModel> {
    return buildGalleryDateGroups(
        inputs = map {
            GalleryGroupingInput(
                capturedAtMillis = it.session.capturedAt,
                session = it.toGallerySessionUiModel()
            )
        },
        zoneId = zoneId
    )
}

private fun SessionWithDetails.toGallerySessionUiModel(): GallerySessionUiModel {
    val photoCount = media.count { it.type.name == "IMAGE" }
    val videoCount = media.count { it.type.name == "VIDEO" }
    val summary = buildList {
        if (photoCount > 0) add("$photoCount photo")
        if (videoCount > 0) add("$videoCount video")
    }.joinToString(" · ")

    val coverMedia = media.firstOrNull { it.id == session.coverMediaId } ?: media.firstOrNull()

    return GallerySessionUiModel(
        id = session.id,
        sessionTitle = "Session ${session.sessionIndexForDay}",
        timeLabel = timeFormatter.format(Instant.ofEpochMilli(session.capturedAt)),
        sourceLabel = session.sourceLabel,
        coverPath = coverMedia?.localPath,
        tagNames = tags.map { it.name },
        hasVideo = videoCount > 0,
        mediaSummary = summary.ifBlank { "No media" },
        coverMimeType = coverMedia?.mimeType
    )
}
