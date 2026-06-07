# Dummy Data Injection

## Purpose

더미 데이터 주입 기능은 실제 네컷사진관 QR을 바로 테스트할 수 없는 상황에서 갤러리, 캘린더, 세션 상세, 태그 필터 흐름을 먼저 확인하기 위한 debug-only 검증 도구입니다.

## Why Debug Only

이 기능은 실제 사용자가 쓰는 제품 기능이 아니라 개발/QA 보조 기능입니다. Release 앱에 노출되면 실제 QR에서 내려받은 기록과 더미 기록이 섞일 수 있으므로, debug build에서만 접근해야 합니다.

현재 구현은 `src/debug`와 `src/release` 소스셋을 분리합니다.

- debug: `DebugDummyDataInjection.isAvailable = true`
- release: `DebugDummyDataInjection.isAvailable = false`

앱 화면의 “더미 데이터 추가” 버튼도 이 값이 true일 때만 표시됩니다.

## Generated Dummy Data

버튼을 누르면 오늘 날짜 기준 더미 세션 3개가 생성됩니다.

- `더미 QR 1`
  - `sourceQrUrl`: `https://sample.invalid/qr/dummy-{seed}-1`
  - `sourceHost`: `sample.invalid`
  - media: IMAGE 1개, VIDEO 1개
  - tags: `친구A`, `친구B`
- `더미 QR 2`
  - `sourceQrUrl`: `https://sample.invalid/qr/dummy-{seed}-2`
  - `sourceHost`: `sample.invalid`
  - media: IMAGE 1개, VIDEO 1개
  - tags: `혼자`
- `더미 QR 3`
  - `sourceQrUrl`: `https://sample.invalid/qr/dummy-{seed}-3`
  - `sourceHost`: `sample.invalid`
  - media: IMAGE 1개, VIDEO 1개
  - tags: 없음

각 QR은 별도 `PhotoSession`으로 저장됩니다. 같은 날짜에 생성되더라도 하나로 합치지 않으며, 기존 저장 규칙대로 `sessionIndexForDay`가 증가합니다.

이미지는 앱 내부 저장소에 작은 PNG 파일로 저장합니다. 영상은 실제 재생 검증 목적이 아니므로 앱 내부 저장소에 placeholder 파일을 만들고 VIDEO 타입 미디어로 저장합니다. 네트워크 다운로드는 수행하지 않습니다.

## What This Does Not Replace

이 기능은 실제 QR 호환성 검증을 대체하지 않습니다.

다음 항목은 반드시 실기기와 실제 테스트 QR로 별도 확인해야 합니다.

- CameraX QR 인식 안정성
- ML Kit 인식 속도와 초점 UX
- 실제 인생네컷/포토이즘 다운로드 페이지 구조
- WebView DownloadListener 감지 여부
- 만료 URL, blob URL, 로그인/cookie 의존 다운로드
- Android 버전별 저장소/권한 동작

## How To Run

1. Android Studio에서 debug build로 앱을 실행합니다.
2. 앱 첫 화면인 Scan 화면에서 “더미 데이터 추가” 버튼을 누릅니다.
3. 앱이 갤러리 화면으로 이동하면 생성된 세션을 확인합니다.
4. 캘린더에서 오늘 날짜의 점 개수와 세션 목록을 확인합니다.
5. 세션 상세에서 사진/영상 타일과 태그를 확인합니다.
6. 갤러리에서 `친구A`, `친구B`, `혼자` 태그 필터를 확인합니다.

## Release Warning

Release 앱에는 더미 데이터 주입 버튼이 보여서는 안 됩니다. Release source set의 no-op 구현은 방어 장치이며, 배포 전에는 debug-only 진입점이 노출되지 않는지 반드시 확인해야 합니다.
