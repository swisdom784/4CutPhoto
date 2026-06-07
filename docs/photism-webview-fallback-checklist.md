# Photism WebView Fallback Checklist

## 목적

실제 포토이즘 QR 인식 이후 WebView fallback과 다운로드 캡처 흐름을 실기기에서 재검증한다.

## 재검증 전제

- 실제 QR URL 전체를 문서, 테스트, 로그에 남기지 않는다.
- 실제 사용자 사진, 영상, 쿠키, 토큰을 저장하거나 공유하지 않는다.
- logcat에는 URL 전체가 아니라 host 또는 redacted 상태만 남기는지 확인한다.
- QR 초점/CameraX 구조 변경은 별도 범위였지만, 최종 PR에는 scan focus baseline 커밋을 함께 포함한다.

## 확인 항목

- QR 인식 후 `다운로드를 준비하고 있어요...` 상태가 오래 멈추지 않는지 확인.
- 일반 페이지 URL이 직접 미디어로 오인되지 않고 WebView fallback으로 전환되는지 확인.
- WebView 로딩 중 검은 화면 대신 한국어 안내 overlay가 보이는지 확인.
- 페이지가 보이면 원본 다운로드 버튼을 누를 수 있는지 확인.
- `onPageCommitVisible` 이후 로딩 안내가 사라지는지 확인.
- 페이지 오류 또는 네트워크 오류 시 `다시 시도`와 `외부 브라우저로 열기`가 보이는지 확인.
- timeout 발생 시 `페이지 응답이 지연되고 있어요...` 안내가 보이는지 확인.
- WebView render process 종료 시 안전한 안내가 보이는지 확인.
- blob/data/javascript 방식 다운로드가 저장 대상으로 오인되지 않는지 확인.
- DownloadListener가 이미지/영상 URL을 감지하면 `담은 항목 확인` 버튼이 활성화되는지 확인.
- DownloadListener가 동작하지 않아도 DOM media candidate가 있으면 `미디어 후보 담기`가 보이는지 확인.
- 앱 안내 팝업/모달이 의심되면 `팝업 닫기 시도`가 보이는지 확인.
- `팝업 닫기 시도` 후 media candidate가 다시 계산되는지 확인.
- 홍보물/로고/앱 배너 이미지가 추천 후보에서 낮은 우선순위로 밀리는지 확인.
- `미디어 후보 담기` 후 세션 저장 화면에 사진/영상 후보가 담기는지 확인.
- 저장 미리보기에서 remote image 후보 썸네일이 보이는지 확인.
- 영상 후보는 local/content 영상이면 썸네일을 시도하고, 실패하면 안정적인 영상 tile로 보이는지 확인.
- remote video 후보는 저장 전에는 무리하게 썸네일을 만들지 않고 영상 tile fallback으로 보이는지 확인.
- 후보가 없거나 unsupported 후보만 있으면 `기기에서 사진/영상 가져오기`가 보이는지 확인.
- 외부 브라우저에서 저장한 파일을 사용자가 직접 선택해 앱 세션 저장 화면으로 이어지는지 확인.
- `기기에서 사진/영상 가져오기`로 선택한 video도 저장 미리보기에서 썸네일 또는 fallback tile로 보이는지 확인.
- 저장 후 세션 상세에서 이미지/영상 카드를 눌러 크게 보기 dialog가 열리는지 확인.
- 세션 상세의 local/content video 카드에 썸네일이 보이는지 확인.
- 저장 후 갤러리 카드에서 image cover가 우선 표시되고, video-only 세션은 video thumbnail 또는 fallback cover가 보이는지 확인.
- 스캔 화면에 `QR을 사각형 안에 맞추고 잠시 멈춰주세요.`와 탭 재초점 안내가 보이는지 확인.
- 캡처 불가능한 방식이면 원본 페이지 재시도 또는 외부 브라우저 안내가 보이는지 확인.

## 남은 한계

- blob URL 또는 JavaScript client-side 생성 다운로드는 앱에서 직접 저장하지 못할 수 있다.
- 로그인, session, cookie 의존 다운로드는 실제 서비스 정책에 따라 별도 검증이 필요하다.
- 실제 포토이즘 페이지 구조가 바뀌면 WebView fallback 결과도 달라질 수 있다.
- 수동 가져오기는 사용자가 외부 브라우저에서 직접 저장한 파일을 선택해야 한다.
- 영상 크게 보기는 현재 재생기가 아니라 thumbnail 또는 큰 preview tile이다.
