# Commit Message Automation

## 목적

이 문서는 세미나 2부에서 "AI를 활용해 커밋 메시지 작성 반복 업무를 줄이는 방법"을 보여주기 위한 시연용 가이드다. 실제 AI API 호출은 구현하지 않고, 사람이 준비한 diff context와 프롬프트 템플릿을 사용해 커밋 메시지 초안을 만드는 흐름만 다룬다.

생성된 커밋 메시지는 초안일 뿐이며, 사람이 반드시 최종 검토해야 한다.

## 입력값

- staged diff: `git diff --cached`
- 더미 티켓 번호: 예 `SECMF-9999`, `P260601-00000`
- cell 구분: `(D)`, `(U)`, `(P)`
- action: `add`, `fix`, `apply`, `restore`, `separate`, `update`, `refactor`
- 이슈 현상
- 원인 분석
- 검증 방법
- 영향 범위

## 출력값

- 팀 컨벤션에 맞는 커밋 제목 1개
- 1. 이슈 현상
- 2. 원인 분석
- 3. 해결 방법
- 4. 검증 방법
- 5. 영향 범위

## 커밋 제목 규칙

형식:

```text
[action] [subject] ([cell]) [[ticket id]] - [optional detail]
```

예:

```text
fix media preview removal state (U) [SECMF-9999] - handle empty selected items
```

cell 구분:

- `(D)`: Domain, data, business logic
- `(U)`: UI, View, 화면 표시
- `(P)`: Presenter, ViewModel, 상태 관리

## 커밋 본문 규칙

본문은 아래 순서를 유지한다.

1. 이슈 현상
2. 원인 분석
3. 해결 방법
4. 검증 방법
5. 영향 범위

각 문단은 실제 사용자가 겪는 현상과 코드 변경 이유를 설명해야 한다. diff를 그대로 요약하는 대신, 왜 변경했는지와 어떤 위험을 줄였는지를 드러내야 한다.

## 좋은 예시

### 예시 1

제목:

```text
fix media preview removal state (U) [SECMF-9999] - handle empty selected items
```

본문:

```text
SECMF-9999 이슈는 저장 미리보기 화면에서 사용자가 담은 항목을 모두 제거했을 때 빈 상태 안내가 명확하지 않은 문제입니다.

원인은 미디어 목록이 비어 있는 경우에도 기존 저장 CTA와 미리보기 상태가 일부 유지되어 사용자가 다음 동작을 판단하기 어려웠기 때문입니다.

이를 해결하기 위해 미리보기 항목 제거 후 빈 리스트 상태를 별도로 계산하고, 저장 버튼 비활성화 및 한국어 안내 문구를 표시하도록 UI 상태를 보완했습니다.

검증은 저장 미리보기 모델 단위 테스트와 debug unit test 실행을 통해 확인했습니다.

영향 범위는 저장 미리보기 화면의 UI 상태 처리이며, 기존 세션 저장 구조와 Room schema에는 영향이 없습니다.
```

### 예시 2

제목:

```text
add download observation model (P) [SECMF-9999] - explain QR download stage
```

본문:

```text
SECMF-9999 이슈는 실제 QR 검증 중 다운로드가 직접 미디어 URL, WebView fallback, 저장 실패 중 어느 단계에서 멈췄는지 설명하기 어려운 문제입니다.

원인은 다운로드 결과와 사용자 안내 문구가 화면 흐름 안에 흩어져 있어 검증 로그로 재사용하기 어려웠기 때문입니다.

이를 해결하기 위해 DownloadObservation 모델을 추가하고 다운로드 단계별 사용자 문구와 개발자용 의미를 분리했습니다.

검증은 DownloadObservation 단위 테스트와 전체 debug unit test로 확인했습니다.

영향 범위는 다운로드 흐름의 상태 설명이며, 실제 WebView 다운로드 방식이나 Room 저장 schema에는 영향이 없습니다.
```

### 예시 3

제목:

```text
update gallery tag filter rule (D) [P260601-00000] - make matching testable
```

본문:

```text
P260601-00000 이슈는 갤러리 태그 필터 규칙이 화면 내부 조건식으로만 존재해 테스트 자동화 예시로 설명하기 어려운 문제입니다.

원인은 세션 태그 목록과 검색어를 비교하는 규칙이 순수 함수로 분리되어 있지 않아 단일/복수/없는 태그 케이스를 독립적으로 검증하기 어려웠기 때문입니다.

이를 해결하기 위해 gallerySessionMatchesTagQuery 함수를 추가하고 기존 갤러리 필터 흐름에서 해당 함수를 사용하도록 정리했습니다.

검증은 GalleryTagSuggestionsTest의 태그 필터 케이스와 debug unit test 실행으로 확인했습니다.

영향 범위는 갤러리 태그 필터 조건이며, 화면 구조와 세션 저장 규칙에는 영향이 없습니다.
```

## 나쁜 예시

### 예시 1

```text
fix stuff
```

문제가 무엇인지, 어떤 영역인지, 어떤 티켓인지 알 수 없다.

### 예시 2

```text
update app
```

티켓 번호와 cell 구분이 없어 추적성이 없다. 시연에는 `SECMF-9999` 같은 더미 번호를 사용해야 한다.

### 예시 3

```text
fix download with <internal-url-redacted>
```

내부 URL이나 로그를 커밋 메시지에 넣으면 보안 리스크가 생긴다.

## 보안상 주의할 점

- 실제 사내 프로젝트명, 실제 Jira 번호, 실제 VOC 번호를 입력하지 않는다.
- 실제 내부 URL, 세션 토큰, 사용자 사진 파일명, 계정 정보를 넣지 않는다.
- `git diff --cached`에 민감정보가 포함되어 있지 않은지 사람이 먼저 확인한다.
- AI 출력에 민감정보가 재노출되지 않았는지 사람이 최종 검토한다.
- 커밋 메시지 자동화는 초안 생성 도구이지 승인 도구가 아니다.
- Windows PowerShell 환경에서는 Bash가 없을 수 있으므로 `scripts/prepare_commit_context.sh`는 Git Bash 또는 WSL에서 실행한다.

## 사람이 최종 검토해야 할 항목

- 제목의 action, subject, cell, ticket id가 팀 규칙에 맞는가
- 본문이 이슈 현상, 원인, 해결, 검증, 영향 범위를 빠짐없이 설명하는가
- 실제 서비스명, 내부 URL, 사용자 데이터가 포함되지 않았는가
- diff에 없는 내용을 AI가 지어내지 않았는가
- 테스트/빌드 검증 결과가 실제 실행 결과와 일치하는가
