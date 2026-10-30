# QuizEarn — Configuration & Credentials Status

> Status of external services, integrations, toolchains, and environment credentials.
> **SECURITY MANDATE:** Never write secret keys, passwords, API tokens, keystore credentials, or service account JSON contents in this file.

---

## Toolchain & Runtime Environment

| Service / Tool | Configured? | Version / Location | Verification Method | Status |
| :--- | :--- | :--- | :--- | :--- |
| **Java JDK** | Yes | Zulu OpenJDK `17.0.20.1` | Executed `java -version` | Verified Active |
| **Node.js** | Yes | Node.js `v24.19.0` | Executed `node -v` | Verified Active |
| **npm** | Yes | Bundled with Node.js | Inspected path | Verified Active |
| **Gradle Wrapper** | Yes | Gradle `8.4` (`gradlew.bat` & `gradlew`) | Executed `.\gradlew.bat --version` | **Verified Active (Code 0)** |
| **Android Build System** | Yes | Target SDK 35, Min SDK 24, Compose BOM | Inspected `app/build.gradle.kts` | Scaffolded & Verified |
| **Android SDK** | Partial | `$env:ANDROID_HOME = C:\Users\User\android-sdk` | Configured in `local.properties` | Path defined |
| **Android adb** | No | Not on PATH | Executed `Get-Command adb` | Non-blocking for local compilation |

---

## Backend & Cloud Services (Confirmed: Firebase-First Serverless Architecture)

| Service | Target Phase | Provider / Technology | Credentials Status | Local Execution Status | Production Status | Notes |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Authentication** | Phase 4 (QZ-16) | Firebase Auth | None present | **MOCKED / IN-MEMORY** | NOT CONFIGURED | `AuthRepositoryImpl` provides offline demo account and guest sessions. |
| **Database** | Phase 4 (QZ-17) | Cloud Firestore | None present | **MOCKED / IN-MEMORY** | NOT CONFIGURED | Local repositories emulate atomic ledger and session state. |
| **Cloud Functions** | Phase 4 (QZ-17) | Firebase Functions (Node.js 20+) | None present | **MOCKED STUBS** | NOT CONFIGURED | `MockQuizRemoteDataSource` validates server-authoritative scoring. |
| **App Attestation** | Phase 4 (QZ-17) | Firebase App Check + Play Integrity | None present | **BYPASSED IN MOCK** | NOT CONFIGURED | Requires SHA-256 fingerprint registration in Google Cloud Console. |
| **Cloud Storage** | Phase 4 (QZ-16) | Firebase Storage | None present | **LOCAL DRAWABLES** | NOT CONFIGURED | Vector drawables and avatar color presets used locally. |
| **Push Notifications** | Phase 4 (QZ-21) | Firebase Cloud Messaging (FCM) | None present | **DISABLED** | NOT CONFIGURED | Optional / conditional on production configuration. |

---

## Security, Signing & Store Configuration

| Item | Target Phase | Status | Security Handling Policy |
| :--- | :--- | :--- | :--- |
| **Debug Keystore** | Phase 1 (QZ-02) | Default Android debug keystore | Standard debug key used for local testing. |
| **Release Keystore** | Phase 7 (QZ-33) | Not Configured | Must NEVER be stored in repo. Injected via secure environment variables. |
| **Google Play Console** | Phase 7 (QZ-34) | Not Configured | Release assets drafted in `docs/BUILD_AND_RELEASE.md`. |
| **Payment / Payout Gateway** | Phase 4 (QZ-19) | Not Configured | Real-money operations prohibited until legitimate legal & payout partner integration. |
