# issueissyu-FE

issueissyu(이슈있슈) Frontend는 지역 기반 핀·커뮤니티·지도 서비스를 제공하는 **Android 전용 애플리케이션**입니다.  
동네 인증, 핀 등록/조회, 이슈 해결, 커뮤니티 피드, FCM 푸시 알림, 인앱 결제 등의 기능을 직관적이고 매끄러운 UI/UX로 사용자에게 제공합니다.


## 👥 Team
|                                 FE (PartLeader)                                  |                                 FE                                  |
|:-----------------------------------------------------------------------:|:-------------------------------------------------------------------:|
|  |  |
|       김가은<br/><a href="https://github.com/silver-kg">@silver-kg</a>       |       유현<br/><a href="https://github.com/dda-zi">@dda-zi</a>        |


## 💻 Tech Stack
- **Framework & Language**: Android SDK, Jetpack Compose, Kotlin
- **Build & Package Tool**: Gradle (Version Catalogs)
- **Routing & Styling**: Jetpack Navigation (Navigation Compose), Modifier & Material Design 3
- **Networking & Async**: Retrofit2, Kotlin Coroutines & Flow
- **State Management**: ViewModel, StateFlow
- **Dependency Injection**: Hilt
- **Local Storage**: Room, DataStore
- **Third-Party SDK & Library**: 지도 SDK, Coil
- **Formatting & Workflow**: Ktlint, Detekt (Spotless 활용)
- **Deployment**: Google Play Console, Firebase App Distribution


## **📂 Project Structure**
레이어형 (Layered Architecture - MVVM 구조)
```
com.example.app/
├── ui/                             # UI 및 Presentation 레이어
│   ├── screens/                    # 화면 단위 Compose 컴포넌트 (스크린)
│   ├── components/                 # 재사용 가능한 공통 UI 컴포넌트 (버튼, 다이얼로그 등)
│   └── viewmodels/                 # UI 상태 관리 및 비즈니스 로직 연결 (ViewModel)
│
├── data/                           # 데이터 레이어 (로컬/원격 데이터 소스 및 레포지토리)
│   ├── remote/                     # 원격 데이터 원천
│   │   ├── dto/                    # Data Transfer Object (네트워크 통신 객체)
│   │   └── api/                    # Retrofit2 API 인터페이스 정의
│   ├── repository/                 # 도메인/UI 레이어와 데이터 소스를 중개하는 Repository 구현체
│   └── local/                      # 로컬 데이터 소스 (Room 데이터베이스, DataStore)
│
├── core/                           # 전역 공통 모듈 및 유틸리티
│   ├── network/                    # 네트워크 공통 설정 (OkHttpClient, Interceptor 등)
│   ├── constants/                  # 전역 상수 관리 (API URL, 키값 등)
│   └── utils/                      # 코틀린 표준 문법을 활용한 조건부 Modifier 등 공통 확장함수 및 유틸
│
└── di/                             # 의존성 주입 레이어 (Hilt 모듈 및 전역 설정 모음)
```


<details>
<summary><h2>🚀 프론트엔드 개발 환경 설정 가이드 (클릭하여 펼치기)</h2></summary>

본 프로젝트를 클론한 후 개발을 시작하기 위해 필요한 개인별 설정 단계를 안내합니다.

### 1. Android Studio 설치 및 설정

* **Android Studio 설치**: 최신 버전의 [Android Studio](https://developer.android.com/studio)를 설치합니다.
* **JDK 설정**:
    * Android Studio를 실행합니다.
    * **macOS**: `Android Studio` -> `Settings` -> `Build, Execution, Deployment` -> `Build Tools` -> `Gradle` 로 이동합니다.
    * **Windows**: `File` -> `Settings` -> `Build, Execution, Deployment` -> `Build Tools` -> `Gradle` 로 이동합니다.
    * `Gradle JDK`를 프로젝트 요구사항에 맞는 버전(예: **JDK 17 또는 JBR 21**)으로 설정합니다.

### 2. Android SDK 설정

* **macOS**: `Android Studio` -> `Settings` -> `Appearance & Behavior` -> `System Settings` -> `Android SDK` 로 이동합니다.
* **Windows**: `File` -> `Settings` -> `Appearance & Behavior` -> `System Settings` -> `Android SDK` 로 이동합니다.
* `SDK Platforms` 탭에서 프로젝트의 `compileSdk` 및 `targetSdk` 버전(현재 **API 35**)에 해당하는 Android SDK Platform을 설치합니다.
* `SDK Tools` 탭에서 `Android SDK Build-Tools`, `Android SDK Command-line Tools`, `CMake`, `NDK (Side by side)` 등 필요한 도구들을 설치합니다.

### 3. Gradle 동기화 및 빌드

* 프로젝트를 Android Studio에서 엽니다.
* `File` -> `Sync Project with Gradle Files`를 클릭하여 Gradle 파일을 동기화합니다.
    * **팁**: 만약 Gradle 동기화나 빌드 시 문제가 발생하면, `Build` -> `Clean Project` 및 `Build` -> `Rebuild Project`를 시도해 보세요.
    * **네트워크 문제 발생 시**: `File` -> `Settings` (macOS: `Preferences`, Windows: `Settings`) -> `Build, Execution, Deployment` -> `Gradle` -> `Offline mode`가 체크되어 있지 않은지 확인하고, 필요한 경우 VPN 또는 프록시 설정을 확인합니다. Gradle 캐시 문제일 경우, 운영체제에 따라 다음 경로의 `caches` 디렉토리를 삭제한 후 다시 시도할 수 있습니다.
        * **macOS**: `~/.gradle/caches`
        * **Windows**: `%USERPROFILE%\.gradle\caches`

### 4. 네이버 지도 SDK 클라이언트 ID 설정

네이버 지도 SDK 사용을 위한 클라이언트 ID와 클라이언트 Secret은 `local.properties` 파일에 저장하며, 이 파일은 `.gitignore`에 의해 Git 저장소에 포함되지 않아 안전하게 관리됩니다.

1.  **클라이언트 ID 및 Secret 발급**: [네이버 클라우드 플랫폼](https://www.ncloud.com/)에서 지도 서비스 API 키(클라이언트 ID 및 클라이언트 Secret)를 발급받습니다.
    * **주의**: 애플리케이션 등록 시 `Dynamic Map` 옵션을 선택했는지 확인하십시오.

2.  **`local.properties` 파일에 추가**: 프로젝트 루트 디렉토리의 `local.properties` 파일에 발급받은 클라이언트 ID와 Secret을 다음과 같은 형식으로 추가합니다. `YOUR_NAVER_MAP_CLIENT_ID`와 `YOUR_NAVER_MAP_CLIENT_SECRET` 부분을 실제 발급받은 값으로 대체하십시오.

    ```properties
    NAVER_MAP_CLIENT_ID=YOUR_NAVER_MAP_CLIENT_ID
    NAVER_MAP_CLIENT_SECRET=YOUR_NAVER_MAP_CLIENT_SECRET
    ```

3.  **애플리케이션에서 사용**: `app/build.gradle.kts` 파일에서 `local.properties`에 정의된 키들을 읽어 `BuildConfig` 필드로 자동 생성하며, 애플리케이션의 `IssueissyuApplication.kt` 파일에서 다음과 같이 `NaverMapSdk`를 초기화할 때 사용됩니다.

    ```kotlin
    // IssueissyuApplication.kt 예시
    NaverMapSdk.getInstance(this).setClient(NaverMapSdk.NcpKeyClient(BuildConfig.NAVER_MAP_CLIENT_ID))
    ```

    `AndroidManifest.xml` 파일에는 더 이상 클라이언트 ID를 직접 설정할 필요가 없습니다. 클라이언트 ID는 팀원 간 공유 가능하며, 각 개발자가 개별적으로 발급받을 필요는 없습니다.

### 5. Git 설정

* **`.gitignore` 확인**: `.idea/`와 같은 IDE 관련 파일, 빌드 결과물(`build/`), 개인 키 저장소(`.jks`, `.keystore`) 등이 `.gitignore`에 올바르게 포함되어 있는지 확인하여 불필요한 파일이 커밋되지 않도록 합니다.

</details>


## **📝 Commit Convention**
| type | 의미 | 예시 |
| --- | --- | --- |
| ⭐ **feat** | 새로운 기능 | 로그인 화면 및 API 연동 구현 |
| 🐞 **bug** | 버그 수정 | 지도 마커 렌더링 오류 해결 |
| 📖 **docs** | 문서 수정 | README 업데이트 |
| ⚙️ **setting** | 프로젝트/환경 설정 | Version Catalogs, Spotless 설정 변경 |
| **♻️ refactor** | 기능 변화 없는 코드 리팩터링 | 공통 Modifier 유틸 분리 |
| 🎨 **style** | 포맷/세미콜론/네이밍 등 | 코드 포맷팅 및 공백 수정 (Ktlint 반영) |
| 🧪 **test** | 테스트 코드 | ViewModel 단위 테스트 구현 |
| 🚀 **deploy** | 배포, dev→main | Firebase App Distribution 배포 |
