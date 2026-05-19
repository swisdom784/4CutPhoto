package com.fourcut.photo

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
import androidx.compose.ui.unit.dp
import com.fourcut.photo.core.designsystem.component.FloatingNavMenu
import com.fourcut.photo.feature.download.DownloadFlowScreen
import com.fourcut.photo.feature.calendar.CalendarDayUiModel
import com.fourcut.photo.feature.calendar.CalendarScreen
import com.fourcut.photo.feature.calendar.CalendarSessionUiModel
import com.fourcut.photo.feature.scan.ScanScreen
import com.fourcut.photo.navigation.AppDestination

@Composable
fun FourCutPhotoApp() {
    var currentDestination by remember { mutableStateOf(AppDestination.Scan) }
    var pendingQrUrl by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        val qrUrl = pendingQrUrl
        if (qrUrl != null) {
            DownloadFlowScreen(
                sourceUrl = qrUrl,
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
                    days = (1..35).map { day ->
                        CalendarDayUiModel(
                            dayOfMonth = day,
                            hasSessions = day == 12 || day == 21,
                            isSelected = day == 12
                        )
                    },
                    sessionsForSelectedDay = listOf(
                        CalendarSessionUiModel(
                            id = 1L,
                            title = "Session 1",
                            timeLabel = "14:10",
                            sourceLabel = "Photo booth",
                            sessionIndexForDay = 1,
                            tagNames = listOf("Hajin", "JungHyun"),
                            mediaCount = 2
                        ),
                        CalendarSessionUiModel(
                            id = 2L,
                            title = "Session 2",
                            timeLabel = "18:42",
                            sourceLabel = "Life4Cuts",
                            sessionIndexForDay = 2,
                            tagNames = listOf("Hajin"),
                            mediaCount = 2
                        )
                    ),
                    onDaySelected = {},
                    onSessionSelected = {}
                )

                AppDestination.Gallery -> Text(
                    text = "Gallery",
                    modifier = Modifier.align(Alignment.Center)
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
