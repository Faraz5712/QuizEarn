# QuizEarn — Issues, Errors, and Blockers Log

> **Last Updated:** October 29, 2026 (Post-DI Fix Verification)  
> **Repository:** `E:\Faraz\App\QuizEarn`

---

## Active Blockers & Architectural Gaps

| ID | Category | Severity | Description | Action Required | Status |
| :--- | :--- | :---: | :--- | :--- | :---: |
| **B-004** | Environment | Medium | Windows Sandbox blocks TCP loopback (`127.0.0.1`), preventing the Gradle daemon client from connecting in sandboxed mode. | Run Gradle commands unsandboxed (`BypassSandbox: true`) or inside Android Studio on host. | **Active / Documented** |
| **B-010** | Device | Low | No physical Android device or emulator is currently attached to ADB (`adb devices` list empty). | Connect Android device via USB with USB debugging enabled or launch an Android Virtual Device (AVD). | **Active / Awaiting Hardware** |
| **B-003** | Environment | Low | Android SDK path `C:\Users\User\android-sdk` is outside workspace boundary. | Handled via `local.properties`. Requires host permissions. | **Active / Documented** |
| **GAP-02** | Backend | High | No live cloud backend or database. Mobile client runs in `MOCK_LOCAL` simulation. | Provision Firebase Auth, Firestore, and Cloud Functions; attach `google-services.json`. | **Pending Cloud Setup** |
| **GAP-03** | Admin Web | Medium | Admin dashboard in `admin/` relies on `localStorage` and lacks `node_modules`. | Run `npm install` on host; connect to backend API shared with mobile client. | **Pending Web Setup** |
| **GAP-04** | Release | Low | Release keystore and production `.aab` bundle are not yet generated. | Create `keystore/release.jks` and run `.\gradlew.bat bundleRelease`. | **Pending Release Gate** |

---

## Resolved Issues

| ID | Category | Description | Resolution | Date |
| :--- | :--- | :--- | :--- | :---: |
| **GAP-01** | Persistence | Room SQLite was missing; transaction history, quiz records, and tasks were only in-memory. | Implemented Room 2.6.1 (`QuizEarnDatabase`) with 4 entities, 4 DAOs, migrations, unique idempotency indexes, double-entry balance aggregation, and repository wiring. Verified by 9 new unit tests and clean APK build. | 2026-10-29 |
| **ERR-01** | Unit Tests | 28 compiler errors in `BackendSecurityTest.kt`, `QuizViewModelTest.kt`, and `WalletViewModelTest.kt` caused by outdated domain model references (`AdminPermission`, `Category`, `Reward`, `Redemption`). | Aligned all three test files with current production domain models and constructors. Task `:app:compileDebugUnitTestKotlin` now compiles with 0 errors. | 2026-10-29 |
| **ERR-02** | E2E Tests | `EndToEndUserJourneyTest` asserted on local `WalletRepositoryImpl()` while `MockQuizRemoteDataSource` credited quiz rewards to singleton `WalletRepositoryImpl.getInstance()`. | Added constructor dependency injection `MockQuizRemoteDataSource(walletRepository)` with singleton fallback default. Injected test fixture's isolated instance in `EndToEndUserJourneyTest`. All 5 E2E tests now pass (100%). | 2026-10-29 |
| **B-001** | Build | Project workspace was empty; no Gradle build scripts existed. | Created `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, and `app/build.gradle.kts`. | 2026-10-29 |
| **B-002** | Environment | Global `gradle` CLI was not installed on system PATH. | Configured Gradle Wrapper (`gradlew.bat`, `gradlew`, `gradle/wrapper/gradle-wrapper.jar`, `gradle-wrapper.properties`). | 2026-10-29 |
| **B-005** | Logic / Types | `com.quizearn.app.core.result.Result` lacked extension methods (`.isSuccess`, `.isFailure`, `.getOrThrow()`). | Added comprehensive extension properties and methods in `Result.kt`. | 2026-10-29 |
| **B-006** | API Design | `WalletRepository.credit` and `debit` required explicit `userId` parameter. | Added default parameter `userId: String = "usr_current"` in `WalletRepository.kt`. | 2026-10-29 |
| **B-007** | SDK Platform | Host SDK only had `platforms\android-34` installed; build initially targeted API 35. | Aligned `compileSdk = 34` and `targetSdk = 34` in `app/build.gradle.kts`. | 2026-10-29 |
| **B-008** | Gradle Version | AGP 8.4.0 required Gradle $\ge 8.6$ (wrapper was 8.4). | Upgraded `gradle/wrapper/gradle-wrapper.properties` to Gradle 8.6. | 2026-10-29 |
| **B-009** | UI Components | Compile errors in UI screens due to missing component overloads and shapes (`QuizEarnCard`, `QuizEarnButton`, `RewardBadge`, `QuizEarnTextField`, `OnboardingScreen`). | Exported shapes in `QuizEarnCard.kt`, created `QuizEarnButton.kt`, added `RewardBadge(Int)` and `QuizEarnTextField(ImageVector)` overloads. | 2026-10-29 |
