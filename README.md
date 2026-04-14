# issueissyu-FE

## 🚀 프론트엔드 개발 환경 설정 가이드

본 프로젝트를 클론한 후 개발을 시작하기 위해 필요한 개인별 설정 단계를 안내합니다.

### 1. Android Studio 설치 및 설정

*   **Android Studio 설치**: 최신 버전의 [Android Studio](https://developer.android.com/studio)를 설치합니다.
*   **JDK 설정**:
    *   Android Studio를 실행합니다.
    *   **macOS**: `Android Studio` -> `Preferences` -> `Build, Execution, Deployment` -> `Build Tools` -> `Gradle` 로 이동합니다.
    *   **Windows**: `File` -> `Settings` -> `Build, Execution, Deployment` -> `Build Tools` -> `Gradle` 로 이동합니다.
    *   `Gradle JDK`를 프로젝트 요구사항에 맞는 버전(예: **JDK 17 또는 JBR 21**)으로 설정합니다.

### 2. Android SDK 설정

*   **macOS**: `Android Studio` -> `Preferences` -> `Appearance & Behavior` -> `System Settings` -> `Android SDK` 로 이동합니다.
*   **Windows**: `File` -> `Settings` -> `Appearance & Behavior` -> `System Settings` -> `Android SDK` 로 이동합니다.
*   `SDK Platforms` 탭에서 프로젝트의 `compileSdk` 및 `targetSdk` 버전(현재 **API 35**)에 해당하는 Android SDK Platform을 설치합니다.
*   `SDK Tools` 탭에서 `Android SDK Build-Tools`, `Android SDK Command-line Tools`, `CMake`, `NDK (Side by side)` 등 필요한 도구들을 설치합니다.

### 3. Gradle 동기화 및 빌드

*   프로젝트를 Android Studio에서 엽니다.
*   `File` -> `Sync Project with Gradle Files`를 클릭하여 Gradle 파일을 동기화합니다.
    *   **팁**: 만약 Gradle 동기화나 빌드 시 문제가 발생하면, `Build` -> `Clean Project` 및 `Build` -> `Rebuild Project`를 시도해 보세요.
    *   **네트워크 문제 발생 시**: `File` -> `Settings` (macOS: `Preferences`, Windows: `Settings`) -> `Build, Execution, Deployment` -> `Gradle` -> `Offline mode`가 체크되어 있지 않은지 확인하고, 필요한 경우 VPN 또는 프록시 설정을 확인합니다. Gradle 캐시 문제일 경우, 운영체제에 따라 다음 경로의 `caches` 디렉토리를 삭제한 후 다시 시도할 수 있습니다.
        *   **macOS**: `~/.gradle/caches`
        *   **Windows**: `%USERPROFILE%\.gradle\caches`

### 4. 네이버 지도 SDK 클라이언트 ID 설정

네이버 지도 SDK 사용을 위한 클라이언트 ID 설정은 `app/src/main/AndroidManifest.xml`에 있습니다. 아직 클라이언트 ID를 발급받지 않았습니다.
나중에 [네이버 클라우드 플랫폼](https://www.ncloud.com/)에서 발급받아 `YOUR_NCP_KEY_ID_HERE` 부분을 실제 클라이언트 ID로 대체하면 됩니다.

```xml
<application>
    <!-- ... -->
    <meta-data
        android:name="com.naver.maps.map.NCP_KEY_ID"
        android:value="YOUR_NCP_KEY_ID_HERE" /> <!-- 실제 클라이언트 ID로 대체 -->
</application>
```
클라이언트 ID는 팀원 간 공유 가능하며, 각 개발자가 개별적으로 발급받을 필요는 없습니다.

### 5. Git 설정

*   **`.gitignore` 확인**: `.idea/`와 같은 IDE 관련 파일, 빌드 결과물(`build/`), 개인 키 저장소(`.jks`, `.keystore`) 등이 `.gitignore`에 올바르게 포함되어 있는지 확인하여 불필요한 파일이 커밋되지 않도록 합니다.
