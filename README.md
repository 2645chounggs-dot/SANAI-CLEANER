# 사나이청소기 · SANAI CLEANER v0.2.0

Android 11 이상 · 네이티브 Java · 검정/빨강/흑백 채드 테마. 기존 패키지 com.sanai.cleaner, versionCode 2.

## 스캔
- 초고속: MediaStore/SAF 메타데이터, 정크 후보 및 SHA-256 완전 중복. 유사 그룹/OCR/AI 생략. 미분석 스크린샷은 보수적으로 보호.
- 스마트 (기본): 256px 사진 특징, 동영상 3개 샘플, 후보 사진 라벨과 스크린샷 OCR. 제한된 2개 분석 작업.
- 정밀: 512px 사진 및 전체 사진 라벨, 스크린샷 OCR, 동영상 5개 샘플. 단일 분석 작업.
- SQLite 모드별 특징 캐시, 64개 단위 저장, 동일 크기/앞부분 해시 후보만 전체 SHA-256 검사, 변경 시간과 크기 기반 해시 캐시. 부분 해시만으로 완전 중복 판정하지 않음.
- 고정 페이지 목록, 검색 백그라운드 실행/180ms 지연, 24MiB 썸네일 캐시. 실제 속도 향상은 기기에서 측정 필요.
- 기존 보호, 보관 추천, 시스템 휴지통 승인, 격리/복원, 위젯, 일반 파일 권한 범위 유지. 삭제는 사용자가 승인해야 함.

## 안전 및 제한
추천 점수는 삭제 안전 확률이 아니다. 원본/편집본을 직접 검토한다. 다른 앱의 비공개 데이터 및 시스템 캐시는 접근 불가. 일반 파일은 사용자가 선택한 SAF 폴더만 분석. 유사도는 근사 비교이며 빠른 모드에서 발견되지 않는 파일도 있다. 해시/분석 캐시는 파일 ID·크기·변경시간을 이용하므로 외부 변경이 해당 메타데이터에 반영되지 않는 경우 한계가 있다. 앱 삭제 시 격리함 데이터도 삭제된다. **기존 앱 삭제 전에 격리한 파일을 모두 복원**해야 한다. 휴지통 및 격리는 즉시 공간을 확보하지 않는다. 실기기 검증 전에는 중요 파일을 정리하지 않는다.

## 고정 서명 설정 (최초 1회)
기존 V0.1.0 디버그 APK는 러너마다 서명이 다를 수 있다. 새 서명과 일치하지 않으면 덮어쓰기 불가. 기존 데이터를 보존하려면 기존 APK의 실제 서명키가 필요하다. 없으면 격리 복원 후 보호 목록/설정 재등록이 필요할 수 있다. 서명키/비밀번호를 저장소나 채팅에 올리지 않는다.
1. 로컬에서 별도 PKCS12 서명키를 안전하게 생성하거나 기존 설치 APK와 일치하는 키를 사용한다. 별도 백업 필수.
2. GitHub 저장소 Settings → Secrets and variables → Actions에 repository secrets: CLEANER_KEYSTORE_B64 (PKCS12의 base64), CLEANER_STORE_PASSWORD, CLEANER_KEY_ALIAS, 선택 CLEANER_KEY_PASSWORD.
3. Variables에 CLEANER_CERT_SHA256 등록: 인증서 DER의 SHA-256 64자리 hex. keytool -exportcert로 DER 내보낸 뒤 sha256sum 등으로 계산한다. 새 키로 바꾸지 않는다.
4. Actions → Build Android APK 실행. 고정 인증서 검증/빌드/lint 성공 후 main에서 APK Release 및 AAB artifact 생성. Secrets 미설정 시 debug 테스트 artifact만 생성한다.
5. 최초 네이티브 설치에는 Android 설치 승인이 필요하며 삼성 자동 차단은 별도 정책이다. 고정 서명이 차단을 우회하지 않는다. 보안을 유지하려면 지원되는 공식 스토어 배포가 필요하다. 현재 스토어에는 게시하지 않았다.

## 서명된 데이터 OTA
앱 홈 → 업데이트 센터 → 정책 확인 → 서명 확인 → 적용. APK 설치/출처 허용 권한이 필요하지 않으며 다음 스캔에 반영.
- ota/policy.json: nativeVersion=2, 증가 revision, thumbnail 160~384, workers 1~3, prefixBytes 16384~262144.
- main 정책 파일 변경 또는 SANAI CLEANER policy OTA 수동 실행 → 같은 APK 키로 서명 및 검증 → ota-channel에 policy.json/policy.sig 게시.
- 앱은 설치 APK 인증서의 공개키, HTTPS 허용 호스트, 대상 버전, 크기, 정책 범위, 증가 revision 검증. 실행 코드/삭제 규칙/권한 변경은 받지 않는다. 기본 정책 복구 후에도 이전 revision 재적용 불가.
- 사진/파일 데이터는 전송하지 않는다. 정책 확인 시 GitHub에 HTTPS 요청만 한다.
- 네이티브 엔진, 위젯, 새로운 기능 또는 권한 변경은 APK가 필요하다. Play App Signing 등 스토어 인증서가 APK 키와 다르면 이 OTA 검증 구조를 별도 신뢰 키로 재설계해야 한다.

## 검증
JDK 17, Gradle 8.11.1, Android SDK 35. `gradle :app:assembleDebug :app:lintDebug`. Actions 산출물 확인. 실제 설치, 파일 정리/복원, OTA 적용, 성능 측정은 별도 실기기 확인 필요.
