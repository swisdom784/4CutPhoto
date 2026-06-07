# 4CutPhoto 하네스 엔지니어링 로그

## 현재 앱 상태 요약

4CutPhoto는 네컷사진관 QR 링크를 스캔해 사진과 영상을 앱 내부에 저장하고, QR 하나를 하나의 세션으로 관리하는 Android 앱이다. 앱 첫 화면은 스캔 화면이며, 우측 하단 플로팅 메뉴로 스캔, 캘린더, 갤러리 화면을 오간다.

현재 구현된 핵심 흐름은 다음과 같다.

- CameraX와 ML Kit으로 QR을 인식한다.
- 직접 이미지/영상 URL은 저장 미리보기로 이동한다.
- 일반 다운로드 페이지 URL은 앱 내부 WebView fallback으로 연다.
- WebView DownloadListener로 지원 가능한 사진/영상 다운로드를 감지한다.
- Room에 세션, 미디어, 사람 태그를 저장한다.
- 같은 날짜의 여러 QR은 절대 합치지 않고 `sessionIndexForDay`로 구분한다.
- 캘린더와 갤러리는 날짜별로 세션을 보여주되, QR 단위 세션 경계를 유지한다.

## 이번 하네스 엔지니어링 목표

이번 작업의 목표는 대형 기능 추가가 아니라, 실제 QR 실기기 검증과 세미나 설명에 필요한 관찰 가능성을 높이는 것이다.

중점은 다음 세 가지다.

- 다운로드 흐름이 직접 미디어 URL, WebView fallback, WebView 다운로드 감지, unsupported, 저장 실패 중 어디에 있는지 설명 가능하게 만든다.
- 저장 미리보기에서 사진/영상 개수, 출처, 저장 날짜, 선택 태그, 빈 항목 상태를 확인할 수 있게 만든다.
- 실제 인생네컷/포토이즘 QR 없이도 fake/sample URL로 자동 테스트 가능한 경계를 남긴다.

## 발견한 리스크

### 현재 대응 가능한 케이스

- 직접 이미지 URL: 예 `https://sample.invalid/photo.jpg`
- 직접 영상 URL: 예 `https://sample.invalid/video.mp4`
- 일반 HTTP/HTTPS 다운로드 페이지 URL: WebView fallback으로 진입
- `content-disposition`의 filename 기반 다운로드 파일명 추출
- `mimeType`이 image/video인 WebView 다운로드 감지
- URL 확장자 기반 mimeType fallback
- 새 창 다운로드: WebView `onCreateWindow`를 같은 WebView로 연결
- mixed content: Android Lollipop 이상에서 compatibility mode 적용

### 제한적으로 대응하는 케이스

- 만료 URL: 페이지 로딩 실패나 저장 실패 메시지로 안내한다. 만료 여부 자체를 서비스별로 판별하지는 않는다.
- 쿠키/session 의존 다운로드: WebView 안에서 같은 세션으로 다운로드가 발생하면 감지 가능하지만, 별도 로그인이나 서비스별 인증 우회는 하지 않는다.

### 현재 의도적으로 지원하지 않는 케이스

- `blob:` URL 다운로드
- JavaScript fetch 후 client-side로 생성되는 다운로드
- 앱 외부 전용 scheme
- 로그인 우회, 내부 API 추정, 서비스별 해킹성 처리

이런 케이스는 사용자에게 "앱에서 바로 저장할 수 없는 방식이에요. 원본 페이지에서 다른 다운로드 버튼을 시도해주세요."라고 안내한다.

## 수정한 내용

- 다운로드 결과를 설명 가능한 `DownloadObservation` 모델로 분리했다.
- 저장 미리보기 계산을 `PreviewMediaSummary`로 분리했다.
- 저장 미리보기 화면에 담은 항목 수, 사진/영상 개수, 출처, 저장 날짜, 선택 태그를 표시했다.
- 이미지 항목은 썸네일을 표시하고, 영상 항목은 영상 타일로 표시한다.
- 저장 전 담은 항목을 개별 제거할 수 있게 했다.
- 담은 항목이 비어 있으면 저장 버튼이 비활성화되고 한국어 안내가 표시된다.
- WebView DownloadListener가 `blob:`, `data:`, `javascript:` 같은 비 HTTP(S) 다운로드를 지원 미디어로 오인하지 않게 했다.
- 저장 실패 시 다운로드 관찰 상태도 저장 실패로 업데이트한다.

## 수정한 파일

- `app/src/main/java/com/fourcut/photo/core/download/WebViewDownloadCapture.kt`
- `app/src/main/java/com/fourcut/photo/feature/download/DownloadFlowScreen.kt`
- `app/src/main/java/com/fourcut/photo/feature/download/DownloadObservation.kt`
- `app/src/main/java/com/fourcut/photo/feature/download/PreviewMediaSummary.kt`
- `app/src/main/java/com/fourcut/photo/feature/download/WebViewCaptureStatus.kt`
- `app/src/test/java/com/fourcut/photo/core/download/DownloadResolverTest.kt`
- `app/src/test/java/com/fourcut/photo/core/download/WebViewDownloadCaptureTest.kt`
- `app/src/test/java/com/fourcut/photo/data/SessionRepositoryTest.kt`
- `app/src/test/java/com/fourcut/photo/feature/download/DownloadObservationTest.kt`
- `app/src/test/java/com/fourcut/photo/feature/download/PreviewMediaSummaryTest.kt`
- `app/src/test/java/com/fourcut/photo/feature/download/WebViewCaptureStatusTest.kt`

## 추가한 테스트

- malformed URL은 unsupported로 분류된다.
- `blob:` URL 다운로드는 mimeType이 video여도 앱 저장 대상으로 캡처하지 않는다.
- WebView 다운로드 감지 전, 감지 후, 빈 항목, unsupported 안내 문구를 검증한다.
- 저장 미리보기에서 사진/영상 개수와 전체 개수를 계산한다.
- 저장 미리보기에서 출처 host와 저장 날짜를 계산한다.
- 저장 미리보기에서 선택 태그 라벨과 빈 상태 메시지를 계산한다.
- 저장 미리보기 항목을 개별 제거한다.
- 같은 날짜의 여러 QR은 별도 세션으로 저장되고 `sessionIndexForDay`가 증가한다.
- 한 세션 안에는 사진과 영상 여러 개가 함께 저장될 수 있다.

## 실제 QR 실기기 검증 체크리스트

- [ ] 실제 인생네컷 QR을 물리 기기 카메라로 스캔한다.
- [ ] 실제 포토이즘 QR을 물리 기기 카메라로 스캔한다.
- [ ] 직접 미디어 URL이면 "사진이나 영상 파일 링크를 바로 찾았어요." 흐름으로 가는지 확인한다.
- [ ] 일반 다운로드 페이지면 WebView fallback으로 열리는지 확인한다.
- [ ] WebView 페이지에서 사진 다운로드 버튼을 눌렀을 때 앱에 항목이 담기는지 확인한다.
- [ ] WebView 페이지에서 영상 다운로드 버튼을 눌렀을 때 앱에 항목이 담기는지 확인한다.
- [ ] 한 QR에서 사진과 영상을 모두 담은 뒤 하나의 세션으로 저장되는지 확인한다.
- [ ] 같은 날 다른 QR을 다시 저장했을 때 갤러리와 캘린더에서 별도 세션으로 보이는지 확인한다.
- [ ] 만료된 QR이나 네트워크 실패에서 한국어 실패 안내가 보이는지 확인한다.
- [ ] `blob:` 또는 client-side 생성 다운로드처럼 앱에서 바로 저장할 수 없는 방식은 안전한 안내로 끝나는지 확인한다.

실제 서비스 QR, 실제 사용자 사진, 실제 계정 정보, 실제 내부 URL은 문서와 테스트에 기록하지 않는다. 세미나 재현에는 `https://sample.invalid/photo.jpg`, `https://sample.invalid/video.mp4`, `https://sample.invalid/download` 같은 fake URL만 사용한다.

## 세미나 Before / After 포인트

### Before

- 다운로드 실패 시 직접 URL 판별, WebView fallback, WebView 감지, 저장 실패 중 어느 단계인지 설명하기 어려웠다.
- 저장 미리보기가 파일명과 mimeType 중심이라 앱 화면처럼 보이기 어려웠다.
- WebView가 `blob:` URL을 지원 미디어처럼 오인할 위험이 있었다.
- 실제 QR 검증 시 확인해야 할 항목이 QA 문서에만 흩어져 있었다.

### After

- `DownloadObservation`으로 다운로드 단계와 사용자 문구를 분리했다.
- `PreviewMediaSummary`로 저장 미리보기의 개수, 출처, 날짜, 태그, 빈 상태를 테스트 가능하게 만들었다.
- 저장 전 이미지 썸네일, 영상 타일, 개별 제거, 빈 항목 안내를 제공한다.
- 비 HTTP(S) 다운로드는 안전하게 미지원 처리한다.
- 실제 QR 실기기 검증 체크리스트와 자동 테스트 가능한 fake URL 전략을 한 문서에 남겼다.

## 아직 남은 한계

- 실제 인생네컷/포토이즘 QR 호환성은 물리 기기에서 직접 검증해야 한다.
- `blob:` URL이나 JavaScript client-side 생성 다운로드를 앱 내부에서 추출하는 기능은 없다.
- 로그인/session/cookie에 강하게 의존하는 서비스는 WebView 안에서 다운로드 이벤트가 발생할 때만 감지할 수 있다.
- 영상은 아직 앱 내부 재생이 아니라 영상 타일로만 표시한다.
- `FourCutPhotoApp.kt`는 아직 ViewModel과 DI로 분리되지 않았다. 이번 작업 범위에서는 대규모 리팩터링을 하지 않았다.
