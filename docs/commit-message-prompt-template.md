# Commit Message Prompt Template

이 템플릿은 staged diff를 바탕으로 팀 커밋 컨벤션에 맞는 커밋 메시지 초안을 만들기 위한 시연용 프롬프트다. 실제 AI API 호출은 하지 않는다.

주의: 실제 사내 프로젝트명, 실제 Jira 번호, 실제 VOC 번호, 실제 내부 URL, 실제 사용자 데이터, 실제 로그는 넣지 않는다. 티켓 번호는 `SECMF-9999`, `P260601-00000` 같은 더미값만 사용한다. 생성된 결과는 사람이 반드시 최종 검토한다.

## 재사용 프롬프트

```text
너는 Android/Kotlin 프로젝트의 커밋 메시지를 팀 컨벤션에 맞게 정리하는 도우미다.

아래 입력을 바탕으로 커밋 메시지 초안을 작성해라.

규칙:
- 실제 사내 정보, 내부 URL, 사용자 데이터, 계정 정보는 절대 만들거나 포함하지 마라.
- 티켓 번호는 입력된 더미 번호만 사용해라.
- diff에 없는 내용을 지어내지 마라.
- 사람이 최종 검토해야 한다는 전제를 유지해라.

제목 형식:
"[action] [subject] ([cell]) [[ticket id]] - [optional detail]"

사용 가능한 action:
- add
- fix
- apply
- restore
- separate
- update
- refactor

cell:
- (D): Domain / data / business logic
- (U): UI / View / 화면 표시
- (P): Presenter / ViewModel / 상태 관리

본문 형식:
1. 이슈 현상
2. 원인 분석
3. 해결 방법
4. 검증 방법
5. 영향 범위

입력:
- ticket id: [SECMF-9999]
- cell: (U)
- action 후보: fix
- 현상:
  저장 미리보기 화면에서 사용자가 담은 항목을 제거했을 때 빈 상태 안내와 저장 가능 여부가 명확하지 않다.
- 원인:
  미디어 목록 요약과 빈 리스트 상태 계산이 화면 상태에서 분리되어 있지 않다.
- 검증:
  :app:testDebugUnitTest, :app:assembleDebug
- staged diff:
  <여기에 git diff --cached 결과를 붙여넣기>

출력:
- 제목
- 본문
- 사람이 검토해야 할 체크리스트
```

## Domain 예시

```text
ticket id: [P260601-00000]
cell: (D)
action 후보: update
현상:
같은 날짜의 여러 QR이 별도 세션으로 남아야 하는 비즈니스 규칙을 테스트로 설명하기 어렵다.
원인:
세션 저장 규칙 테스트가 사진/영상 복수 미디어와 같은 날짜 다중 QR을 함께 검증하지 않았다.
```

추천 제목:

```text
update session save rule tests (D) [P260601-00000] - keep same-day QR sessions separate
```

## UI 예시

```text
ticket id: [SECMF-9999]
cell: (U)
action 후보: fix
현상:
저장 미리보기 화면에서 담은 항목이 모두 제거되었을 때 다음 동작이 명확하지 않다.
원인:
빈 항목 상태와 저장 버튼 활성화 조건이 미리보기 요약 모델로 분리되어 있지 않다.
```

추천 제목:

```text
fix media preview empty state (U) [SECMF-9999] - disable save without items
```

## Presenter 예시

```text
ticket id: [SECMF-9999]
cell: (P)
action 후보: add
현상:
실제 QR 검증 중 다운로드 단계가 어디까지 진행됐는지 설명하기 어렵다.
원인:
DownloadResolver 결과와 사용자 안내 문구가 검증 가능한 관찰 모델로 분리되어 있지 않다.
```

추천 제목:

```text
add download observation state (P) [SECMF-9999] - describe QR handling stage
```

## FC 케이스 예시

```text
현상:
특정 입력에서 앱이 중단된다.
원인:
null 또는 빈 리스트 상태를 별도로 처리하지 않았다.
검증:
재현 테스트, :app:testDebugUnitTest
```

추천 action: `fix`

## OOM 케이스 예시

```text
현상:
큰 이미지 목록을 처리할 때 메모리 사용량이 급증한다.
원인:
원본 파일을 한 번에 메모리에 올리는 코드 경로가 있다.
검증:
단위 테스트, 수동 프로파일링 결과 요약
```

추천 action: `fix` 또는 `refactor`

## UX 개선 케이스 예시

```text
현상:
사용자가 다운로드 단계에서 무엇을 해야 하는지 알기 어렵다.
원인:
WebView 감지 전/후 상태 메시지가 구체적으로 나뉘어 있지 않다.
검증:
상태 메시지 단위 테스트, debug build
```

추천 action: `update`

## 테스트 추가 케이스 예시

```text
현상:
QR 분류와 갤러리 태그 필터 규칙이 세미나에서 설명 가능한 테스트로 충분히 드러나지 않는다.
원인:
기존 테스트가 대표 happy path 중심이었다.
검증:
추가된 JUnit 테스트와 전체 debug unit test
```

추천 action: `add`
