# Test Automation Showcase

## 목적

이 문서는 세미나 2부에서 "AI를 활용해 반복적인 테스트 설계를 안전하게 줄이는 방법"을 보여주기 위한 자료다. 목표는 실제 외부 QR 서비스나 사용자 데이터를 쓰지 않고, 요구사항에서 비즈니스 규칙을 뽑아 자동화 가능한 테스트로 바꾸는 과정을 설명하는 것이다.

## 사람이 작성한 요구사항

- 앱 첫 화면은 스캔 화면이어야 한다.
- QR 분류는 HTTP/HTTPS URL과 그 외 문자열을 구분해야 한다.
- 직접 이미지/영상 URL은 WebView 없이 자동 미디어로 분류해야 한다.
- 일반 다운로드 페이지는 WebView fallback으로 보내야 한다.
- WebView 다운로드는 image/video만 앱 저장 대상으로 캡처해야 한다.
- 같은 날짜의 여러 QR은 하나의 세션으로 합치지 않아야 한다.
- QR 하나는 하나의 PhotoSession이고, 한 세션 안에는 여러 사진/영상이 들어갈 수 있다.
- 캘린더 날짜 점 개수는 해당 날짜의 세션 수를 반영해야 한다.
- 갤러리 태그 필터는 태그 없음, 단일 태그, 복수 태그, 존재하지 않는 태그를 설명 가능하게 처리해야 한다.

## AI가 도출한 테스트 케이스

### QR 분류

- HTTPS URL은 accepted URL로 분류한다.
- HTTP URL도 accepted URL로 분류한다.
- URL이 아닌 문자열은 unsupported로 분류한다.
- 공백 문자열은 ignored로 분류한다.
- 특수문자만 있는 문자열은 unsupported로 분류한다.

### DownloadResolver

- 이미지 확장자 URL은 automatic media로 분류한다.
- 영상 확장자 URL은 automatic media로 분류한다.
- query parameter가 붙은 이미지 URL도 미디어 확장자를 유지한다.
- 대소문자 확장자는 동일하게 처리한다.
- 일반 다운로드 페이지 URL은 WebView fallback으로 분류한다.
- query 안에만 파일명이 있는 URL은 직접 미디어로 과잉 판정하지 않는다.
- malformed URL은 unsupported로 분류한다.

### WebView 다운로드 캡처

- `content-disposition`에 filename이 있으면 파일명을 추출한다.
- mimeType이 image/video이면 다운로드 대상으로 캡처한다.
- 알 수 없는 mimeType은 저장 대상으로 캡처하지 않는다.
- `blob:` URL은 mimeType이 video여도 저장 대상으로 캡처하지 않는다.
- 중복 다운로드 항목은 미리보기 목록에 중복 추가하지 않는다.

### 갤러리/캘린더/세션 규칙

- 같은 날짜 여러 세션은 `sessionIndexForDay`로 구분된다.
- 한 QR 세션 안에는 IMAGE와 VIDEO가 함께 들어갈 수 있다.
- 캘린더 날짜의 `sessionCount`는 해당 날짜 세션 수를 반영한다.
- 태그 필터는 빈 query, 단일 태그, 복수 태그, 존재하지 않는 태그, 삭제 후 태그 목록을 설명 가능하게 처리한다.

## 실제 추가된 테스트 파일

- `app/src/test/java/com/fourcut/photo/feature/scan/QrScanClassifierTest.kt`
- `app/src/test/java/com/fourcut/photo/core/download/DownloadResolverTest.kt`
- `app/src/test/java/com/fourcut/photo/core/download/WebViewDownloadCaptureTest.kt`
- `app/src/test/java/com/fourcut/photo/feature/download/PreviewMediaListTest.kt`
- `app/src/test/java/com/fourcut/photo/feature/calendar/CalendarMonthModelTest.kt`
- `app/src/test/java/com/fourcut/photo/feature/gallery/GalleryTagSuggestionsTest.kt`
- `app/src/test/java/com/fourcut/photo/data/SessionRepositoryTest.kt`

## 테스트로 검증되는 핵심 비즈니스 규칙

- QR 하나는 하나의 세션이다.
- 같은 날짜의 여러 QR은 하나로 합쳐지지 않는다.
- 한 세션은 여러 사진/영상을 포함할 수 있다.
- 캘린더 점 개수는 세션 수를 의미한다.
- WebView fallback은 지원 가능한 image/video 다운로드만 저장 후보로 삼는다.
- 갤러리 태그 필터는 실제 세션 태그 목록을 기준으로 동작한다.

## 보안 리스크 없이 테스트할 수 있었던 이유

- 실제 인생네컷/포토이즘 QR을 테스트 코드에 넣지 않았다.
- 실제 사용자 사진, 영상, 계정 정보, 내부 URL을 사용하지 않았다.
- 모든 URL은 `example.com` 또는 `sample.invalid` 기반 fake/sample URL이다.
- 테스트는 URL 문자열 분류와 로컬 Room in-memory database만 사용한다.
- 외부 네트워크 요청 없이 단위 테스트로 실행된다.

## 사람이 반드시 검토해야 하는 부분

- AI가 만든 테스트 이름이 팀 컨벤션과 맞는지 확인해야 한다.
- 테스트가 실제 요구사항을 과잉 단순화하지 않았는지 확인해야 한다.
- fake URL이 실제 서비스 도메인이나 내부 도메인으로 바뀌지 않았는지 확인해야 한다.
- 테스트가 구현 세부사항만 검증하고 비즈니스 규칙을 놓치지 않았는지 확인해야 한다.
- 실제 QR 호환성은 물리 기기 테스트 체크리스트로 별도 검증해야 한다.

## 세미나 발표용 요약

1. 요구사항 문장을 비즈니스 규칙으로 쪼갠다.
2. 각 규칙을 네트워크 없는 fake data 테스트로 바꾼다.
3. 실패하는 테스트를 먼저 확인한 뒤 작은 순수 함수를 추가한다.
4. UI 안에 숨어 있던 필터 규칙을 테스트 가능한 helper로 분리한다.
5. 자동화는 반복 검증을 줄이고, 사람은 요구사항과 보안 경계를 검토한다.
