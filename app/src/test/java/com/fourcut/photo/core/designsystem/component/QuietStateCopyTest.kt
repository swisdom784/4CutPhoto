package com.fourcut.photo.core.designsystem.component

import org.junit.Assert.assertEquals
import org.junit.Test

class QuietStateCopyTest {
    @Test
    fun galleryEmptyCopyInvitesFirstScan() {
        val copy = quietStateCopy(QuietStateKind.GalleryEmpty)

        assertEquals("첫 QR부터 시작해보세요", copy.title)
        assertEquals("저장한 네컷사진 세션이 날짜별로 모이고, 같은 날의 QR도 각각 따로 보관돼요.", copy.message)
        assertEquals("스캔 열기", copy.primaryActionLabel)
    }

    @Test
    fun gallerySearchCopySuggestsChangingTagSearch() {
        val copy = quietStateCopy(QuietStateKind.GallerySearchEmpty)

        assertEquals("일치하는 사람 태그가 없어요", copy.title)
        assertEquals("다른 이름으로 검색하거나, 저장/수정할 때 새 사람 태그를 추가해보세요.", copy.message)
        assertEquals(null, copy.primaryActionLabel)
    }

    @Test
    fun calendarDayCopyKeepsSameDaySessionContext() {
        val copy = quietStateCopy(QuietStateKind.CalendarDayEmpty)

        assertEquals("이 날 저장된 기록이 없어요", copy.title)
        assertEquals("같은 날 여러 QR을 저장해도 세션별로 나뉘어 여기에 표시돼요.", copy.message)
        assertEquals(null, copy.primaryActionLabel)
    }

    @Test
    fun cameraPermissionCopyShowsCameraAction() {
        val copy = quietStateCopy(QuietStateKind.CameraPermission)

        assertEquals("카메라 권한이 필요해요", copy.title)
        assertEquals("앱을 열자마자 네컷사진 QR을 스캔할 수 있도록 카메라를 허용해주세요.", copy.message)
        assertEquals("카메라 허용", copy.primaryActionLabel)
        assertEquals(null, copy.secondaryActionLabel)
    }
}
