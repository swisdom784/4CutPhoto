# Dummy Device Run Result

## 테스트 목적

debug-only 더미 데이터 주입 기능으로 실기기에서 갤러리, 캘린더, 세션 상세, 태그 필터 흐름을 안전하게 검증한다.

## 테스트 환경

- 기기: `Android 실기기 1대`
- 앱: `debug build`
- 날짜: `2026-06-07`

## 실행한 명령

- `.\gradlew.bat :app:testDebugUnitTest`
- `.\gradlew.bat :app:assembleDebug`
- `.\gradlew.bat :app:installDebug`
- `adb devices`
- `adb shell am start -n <debug launcher activity>`

## JUnit 테스트 결과

- 더미 seed plan 테스트 통과
- debug dummy injection 테스트 통과
- 세션 저장 규칙 테스트 통과
- 전체 `:app:testDebugUnitTest` 통과

## 실기기 설치/실행 결과

- `installDebug` 성공
- 앱 실행 성공
- 첫 화면이 Scan으로 유지됨
- debug build에서만 더미 데이터 추가 버튼 노출 확인
- 실기기 조작 중 crash 미발생

## 확인한 더미 시나리오

- 1회 주입 후 같은 날짜 세션 3개가 분리 저장됨
- 캘린더 오늘 날짜에 세션 수만큼 점 3개가 표시됨
- 갤러리 태그 필터 `친구A`, `친구B`, `혼자`가 각각 올바른 세션만 표시함
- 태그 없는 세션 상세가 안전하게 열림
- 세션 상세에서 사진 타일 1개와 영상 타일 1개가 표시됨
- 2회 주입 후 `세션 4`, `세션 5`, `세션 6`이 추가되어 기존 데이터가 유지됨

## 발견한 문제

- 앱 동작 문제는 발견하지 못함
- adb 좌표 탭 재현 과정에서 한 번 더 버튼 입력을 정밀하게 다시 수행함

## 수정한 내용

- `DebugDummyDataInjection`에 테스트 가능한 내부 seed 경로 추가
- seed id가 겹치지 않도록 증가 보장 로직 추가
- debug dummy injection 테스트 추가
- 더미 라벨 검증 테스트 추가

## 남은 한계

- 실제 QR 인식은 아직 미검증
- 실제 인생네컷/포토이즘 다운로드 방식은 아직 미검증
- blob URL/client-side 다운로드는 실제 서비스에서 별도 검증 필요

## 다음 단계

- 실제 QR로 스캔부터 다운로드까지 실기기 플로우 검증
- 실제 서비스별 다운로드 페이지와 WebView fallback 호환성 검증
