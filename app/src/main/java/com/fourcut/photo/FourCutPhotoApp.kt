package com.fourcut.photo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import com.fourcut.photo.feature.scan.ScanScreen
import com.fourcut.photo.navigation.AppDestination

@Composable
fun FourCutPhotoApp() {
    var currentDestination by remember { mutableStateOf(AppDestination.Scan) }
    var pendingQrUrl by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        val qrUrl = pendingQrUrl
        if (qrUrl != null) {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Download: $qrUrl",
                    modifier = Modifier.align(Alignment.Center)
                )
                Button(
                    onClick = { pendingQrUrl = null },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(24.dp)
                ) {
                    Text("Back to Scan")
                }
            }
        } else {
            when (currentDestination) {
                AppDestination.Scan -> ScanScreen(
                    onBack = {},
                    onQrDetected = { pendingQrUrl = it }
                )

                AppDestination.Calendar -> Text(
                    text = "Calendar",
                    modifier = Modifier.align(Alignment.Center)
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
