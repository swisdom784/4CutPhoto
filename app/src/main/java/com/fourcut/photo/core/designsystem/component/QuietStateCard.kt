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
            title = "첫 QR부터 시작해보세요",
            message = "저장한 네컷사진 세션이 날짜별로 모이고, 같은 날의 QR도 각각 따로 보관돼요.",
            primaryActionLabel = "스캔 열기"
        )
        QuietStateKind.GallerySearchEmpty -> QuietStateCopy(
            title = "일치하는 사람 태그가 없어요",
            message = "다른 이름으로 검색하거나, 저장/수정할 때 새 사람 태그를 추가해보세요."
        )
        QuietStateKind.CalendarDayEmpty -> QuietStateCopy(
            title = "이 날 저장된 기록이 없어요",
            message = "같은 날 여러 QR을 저장해도 세션별로 나뉘어 여기에 표시돼요."
        )
        QuietStateKind.CameraPermission -> QuietStateCopy(
            title = "카메라 권한이 필요해요",
            message = "앱을 열자마자 네컷사진 QR을 스캔할 수 있도록 카메라를 허용해주세요.",
            primaryActionLabel = "카메라 허용"
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
