package com.fourcut.photo.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class QuietStateKind {
    GalleryEmpty,
    GallerySearchEmpty,
    CalendarDayEmpty,
    CameraPermission
}

data class QuietStateCopy(
    val title: String,
    val message: String,
    val primaryActionLabel: String? = null,
    val secondaryActionLabel: String? = null
)

fun quietStateCopy(kind: QuietStateKind): QuietStateCopy {
    return when (kind) {
        QuietStateKind.GalleryEmpty -> QuietStateCopy(
            title = "Start with one QR",
            message = "Saved photo booth sessions will appear here by date, with each QR kept separate.",
            primaryActionLabel = "Open Scan"
        )
        QuietStateKind.GallerySearchEmpty -> QuietStateCopy(
            title = "No matching people yet",
            message = "Try another name or add that person tag while saving or editing a session."
        )
        QuietStateKind.CalendarDayEmpty -> QuietStateCopy(
            title = "Nothing saved on this day",
            message = "When you save several QR sessions on the same date, they will stay separated here."
        )
        QuietStateKind.CameraPermission -> QuietStateCopy(
            title = "Camera access needed",
            message = "Allow the camera to scan photo booth QR codes as soon as the app opens.",
            primaryActionLabel = "Allow camera",
            secondaryActionLabel = "Use test QR"
        )
    }
}

@Composable
fun QuietStateCard(
    kind: QuietStateKind,
    modifier: Modifier = Modifier,
    onPrimaryAction: (() -> Unit)? = null,
    onSecondaryAction: (() -> Unit)? = null
) {
    val copy = quietStateCopy(kind)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = copy.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = copy.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            copy.primaryActionLabel?.let { label ->
                Button(
                    onClick = { onPrimaryAction?.invoke() },
                    enabled = onPrimaryAction != null
                ) {
                    Text(label)
                }
            }
            copy.secondaryActionLabel?.let { label ->
                OutlinedButton(
                    onClick = { onSecondaryAction?.invoke() },
                    enabled = onSecondaryAction != null
                ) {
                    Text(label)
                }
            }
        }
    }
}
