# Photism WebView Diagnostic Notes

## 검은 화면 원인 후보

실제 포토이즘 QR 테스트에서는 QR 인식 이후 다운로드 선택 화면으로 진입했지만 WebView 영역이 검은 화면처럼 보이고 다운로드 항목이 캡처되지 않았다.

추가 재검증에서는 host가 `qr.seobuk.kr`로 표시되고, readyState가 complete이며 button/image/video 요소가 존재하지만 body client height가 0인 상태가 확인됐다.

후속 재검증에서는 외부 브라우저에서 페이지 안내 팝업 또는 모달이 먼저 표시되고, 이를 닫아야 다운로드 버튼이 보이는 흐름이 확인됐다.

현재 원인 후보는 다음으로 나눈다.

1. pageStarted는 호출됐지만 pageCommitVisible이 호출되지 않음.
2. pageFinished는 호출됐지만 pageCommitVisible이 호출되지 않음.
3. pageCommitVisible은 호출됐지만 DownloadListener가 호출되지 않음.
4. onReceivedError 또는 onReceivedHttpError가 발생함.
5. WebView render process가 종료됨.
6. 새 창, 외부 scheme, blob/data/javascript 다운로드로 흐름이 빠짐.
7. timeout 동안 visible/error/download 이벤트가 없음.
8. page visible 상태지만 사용자가 원본 페이지의 다운로드 버튼을 찾거나 누르기 어려움.
9. body 높이가 0이라 DOM 요소가 존재해도 viewport에 정상 배치되지 않음.
10. 검은 overlay 또는 fixed/absolute layout이 콘텐츠를 덮음.
11. 팝업/모달이 WebView 안에서 보이지 않거나 닫히지 않아 다운로드 버튼을 가림.

## 추가한 진단 장치

debug build에서만 WebView lifecycle event를 진단 상태로 기록한다.

추적 대상:

1. onPageStarted
2. onPageCommitVisible
3. onPageFinished
4. onReceivedError
5. onReceivedHttpError
6. onRenderProcessGone
7. shouldOverrideUrlLoading
8. onCreateWindow
9. DownloadListener

page visible 또는 finished 이후에는 debug build에서만 최소 JS probe를 실행한다.

확인 항목:

1. document.readyState
2. document.title 존재 여부와 길이
3. document.body 존재 여부
4. body innerText 길이
5. body child count
6. documentElement child count
7. body background color
8. body text color
9. documentElement background color
10. viewport width/height
11. scroll height
12. body client width/height
13. link/button/image/video/iframe/form/script count
14. anchor host count
15. focused element tag name
16. Android WebView 여부
17. layout suspicious 여부
18. DOM media candidate count
19. previewable/unsupported candidate count

본문 원문, title 원문, 전체 URL, query parameter는 기록하지 않는다.

## DOM Media Candidate Fallback

DownloadListener가 호출되지 않는 경우를 대비해 DOM 후보를 수집한다.

수집 대상:

1. img src/currentSrc
2. video src/currentSrc
3. source src
4. a href/download
5. CSS background-image url
6. button 또는 role=button 주변 data attribute 중 URL처럼 보이는 값
7. button onclick 문자열 안의 URL 존재 여부

진단 overlay와 logcat에는 실제 URL 전체를 표시하지 않는다.

표시하는 값:

1. scheme
2. host
3. extension
4. mime type 추정값
5. element tag
6. visible 여부
7. viewport 내부 여부
8. natural/video size
9. previewable/unsupported 여부

http(s) 이미지/영상 후보가 있으면 debug 화면에서 `미디어 후보 담기` 버튼을 표시한다.

이 버튼은 자동 저장이 아니라 기존 preview 저장 흐름에 후보를 임시로 담는 용도다.

blob/data 후보는 앱에서 직접 다운로드할 수 없는 후보로 분류한다.

후보가 없거나 unsupported 후보만 있는 경우에는 외부 브라우저에서 사용자가 직접 저장한 뒤 `기기에서 사진/영상 가져오기`로 앱에 가져오는 fallback을 제공한다.

수동 가져오기는 사용자가 선택한 파일만 처리하며, 기기의 다운로드 폴더를 자동 스캔하지 않는다.

## Popup / Modal Handling

debug probe는 팝업/모달 후보를 count 기반으로 탐지한다.

탐지 기준:

1. role=dialog
2. aria-modal=true
3. dialog element
4. fixed/absolute이며 화면 대부분을 덮는 높은 z-index 후보
5. X, ×, 닫기, 확인, close, dismiss 계열 close candidate

사용자에게는 `팝업 닫기 시도` 버튼을 제공한다.

자동 클릭은 하지 않는다.

버튼을 누르면 가장 안전한 close candidate를 클릭한 뒤 DOM probe와 media candidate 수집을 다시 실행한다.

로그와 문서에는 닫기 버튼 텍스트 원문이나 selector 원문을 남기지 않는다.

## Candidate Ranking

DOM media candidate는 점수화한다.

높은 점수:

1. video 후보
2. video-download/download/original/media/photo 힌트
3. 충분히 큰 이미지 또는 영상 크기
4. jpg/png/webp/mp4/mov/webm 확장자

낮은 점수:

1. logo/icon/banner/promo/store/app 힌트
2. 너무 작은 이미지
3. mime type을 추정할 수 없는 후보

추천 후보가 있으면 `미디어 후보 담기`는 추천 후보를 우선 담는다.

추천 후보가 없으면 previewable 후보 전체를 담는다.

## Log Redaction 기준

logcat에는 전체 URL을 남기지 않는다.

남기는 값:

1. event name
2. elapsed time
3. host
4. main frame 여부
5. error code
6. HTTP status code
7. current diagnostic state
8. captured item count

남기지 않는 값:

1. 실제 QR URL 전체
2. query parameter
3. cookie
4. token
5. 사용자 파일명
6. 실제 다운로드 링크 전체
7. 사용자 사진/영상

## Debug Overlay 읽는 법

debug build에서 다운로드 선택 화면 하단에 작은 진단 패널이 표시된다.

표시 항목:

1. 현재 상태: Loading / Started / Visible / Finished / Error / Timeout / RenderGone / UnsupportedDownload / Captured
2. 마지막 이벤트
3. elapsed time
4. host
5. captured item count
6. page visible 여부
7. download captured 여부
8. timeout 여부
9. external browser 가능 여부
10. JS probe 요약

`VisibleNoDownloadCaptured` 상태는 WebView가 visible이고 body 내용도 있지만, 앱 관점에서 다운로드 버튼/이미지/영상 요소가 잡히지 않는 경우를 뜻한다.

이 상태에서는 원본 페이지가 비어 보이거나 다운로드 버튼이 WebView에서 확인되지 않을 수 있으므로 외부 브라우저 CTA를 확인한다.

`VisibleButLayoutSuspicious` 상태는 WebView가 visible이고 DOM 요소도 있지만 body 높이가 0이거나 media/button 요소가 정상 배치되지 않은 것으로 의심되는 경우를 뜻한다.

이 상태에서는 layout/visibility, fixed overlay, WebView compatibility 설정을 우선 확인한다.

release build에서는 diagnostic runtime이 no-op이며 overlay가 표시되지 않는다.

## 실제 포토이즘 QR 재검증 항목

실제 URL, 쿠키, 토큰, 사용자 사진/영상은 공유하거나 기록하지 않는다.

확인할 항목:

1. overlay 현재 상태
2. 마지막 이벤트
3. elapsed time
4. captured item count
5. page visible 여부
6. download captured 여부
7. timeout 여부
8. 외부 브라우저 열기 버튼 표시 여부
9. JS readyState
10. body/client 크기
11. button/image/video/iframe count
12. anchor host count
13. Android WebView 여부
14. layout suspicious 여부
15. media candidate count
16. previewable/unsupported candidate count
17. `미디어 후보 담기` 버튼 표시 여부
18. 담은 항목 확인 활성화 여부
19. 검은 화면 지속 여부
20. logcat의 `FourCutWebViewDiag` redacted summary

## 아직 해결하지 않은 것

이번 단계는 진단 장치 추가가 목적이다.

아직 해결하지 않은 범위:

1. 실제 포토이즘 다운로드 방식 우회.
2. blob URL 다운로드 캡처 구현.
3. JavaScript client-side 생성 다운로드 캡처.
4. 로그인/session/cookie 의존 다운로드 자동 처리.
5. WebView 검은 화면의 최종 원인 확정.
6. 실제 서비스별 DOM 구조에 맞춘 다운로드 우회.
7. 수동 가져오기는 사용자 조작이 필요함.
8. 서비스 구조가 바뀌면 DOM 후보 탐지 기준을 다시 검증해야 함.
9. 영상 실제 재생은 아직 Media3/ExoPlayer 없이 큰 보기 타일로만 제공함.

위 항목은 실제 QR 재검증에서 diagnostic state와 logcat summary를 확인한 뒤 별도 작업으로 다룬다.
