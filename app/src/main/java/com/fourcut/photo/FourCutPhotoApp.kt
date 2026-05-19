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
import com.fourcut.photo.navigation.AppDestination

@Composable
fun FourCutPhotoApp() {
    var currentDestination by remember { mutableStateOf(AppDestination.Scan) }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentDestination) {
            AppDestination.Scan -> Text(
                text = "Scan",
                modifier = Modifier.align(Alignment.Center)
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

        FloatingNavMenu(
            current = currentDestination,
            onNavigate = { currentDestination = it },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        )
    }
}
