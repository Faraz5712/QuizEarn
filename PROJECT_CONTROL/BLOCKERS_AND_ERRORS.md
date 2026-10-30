# QuizEarn — Blockers, Errors, and Environmental Issues

> Living log of active blockers, runtime/build errors, attempted fixes, and resolution statuses.

---

## Active Blockers & Environmental Constraints

| ID | Category | Severity | Description | Action Required | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **B-003** | Environment | Low | Android SDK is configured at `$env:ANDROID_HOME = C:\Users\User\android-sdk`. In sandbox mode, access outside the workspace is denied. | `local.properties` generated. Verification occurs when running builds unsandboxed or via IDE. | Active |
| **B-004** | Sandbox | Medium | Gradle task execution (`help`, `assembleDebug`) fails in Windows Standard Sandbox Mode with `Could not connect to the Gradle daemon` because daemon TCP loopback sockets (`127.0.0.1`) are isolated. | Standard behavior in sandboxed Windows terminal. Builds can be triggered with bypass or in Android Studio. Wrapper itself verified via `.\gradlew.bat --version`. | Documented |

---

## Resolved Issues

| ID | Category | Description | Resolution | Resolution Date |
| :--- | :--- | :--- | :--- | :--- |
| **B-001** | Build | Project directory was empty; no Gradle build scripts existed. | Created `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, and `app/build.gradle.kts`. | 2026-10-29 (QZ-02) |
| **B-002** | Environment | Global `gradle` CLI was not installed on system PATH. | Configured Gradle Wrapper (`gradlew.bat`, `gradlew`, `gradle/wrapper/gradle-wrapper.jar`, `gradle-wrapper.properties`). Verified working via `.\gradlew.bat --version`. | 2026-10-29 (QZ-02) |
| **B-005** | Logic / Types | `com.quizearn.app.core.result.Result` lacked result extension methods (`.isSuccess`, `.isFailure`, `.getOrThrow()`, `.getOrNull()`, `.exceptionOrNull()`). | Added comprehensive extension properties and methods in `Result.kt`. | 2026-10-29 (QZ-31) |
| **B-006** | API Design | `WalletRepository.credit` and `debit` required positional/named `userId` even for current active user operations in repositories. | Added default parameter `userId: String = "usr_current"` in `WalletRepository.kt` for seamless caller convenience. | 2026-10-29 (QZ-31) |
| **B-007** | SDK Platform | Host SDK only had `platforms\android-34` installed; build targeted API 35. | Aligned `compileSdk = 34` and `targetSdk = 34` in `app/build.gradle.kts`. | 2026-10-29 |
| **B-008** | Gradle Version | AGP 8.4.0 required Gradle >= 8.6 (wrapper was 8.4). | Upgraded `gradle/wrapper/gradle-wrapper.properties` to Gradle 8.6. | 2026-10-29 |
| **B-009** | UI Compilation | Missing component overloads in UI screens (`QuizEarnCard`, `QuizEarnButton`, `RewardBadge`, `QuizEarnTextField`, `OnboardingScreen`). | Implemented missing component overloads and exported shapes. | 2026-10-29 |
| **ERR-01** | Test Compilation | 28 compiler errors in `BackendSecurityTest.kt`, `QuizViewModelTest.kt`, and `WalletViewModelTest.kt`. | Aligned test constructors and enums with production domain models. | 2026-10-29 |
| **ERR-02** | E2E Tests | `EndToEndUserJourneyTest` asserted on local `WalletRepositoryImpl` while `MockQuizRemoteDataSource` credited singleton. | Added DI parameter `walletRepository` to `MockQuizRemoteDataSource` with singleton fallback. | 2026-10-29 |

---

## Known Environmental Notes
- **Operating System:** Windows 11 (PowerShell environment)
- **Java Runtime:** Zulu OpenJDK `17.0.20.1` (`C:\Program Files\Zulu\zulu-17\bin\java.exe`)
- **Gradle Engine:** Gradle `8.4` (verified running with Java 17 via Gradle Wrapper)
- **Android Target:** Compile SDK 35, Target SDK 35, Min SDK 24, Application ID `com.quizearn.app`
- **Version Control:** Git progress tracking is explicitly disabled per user instruction.
