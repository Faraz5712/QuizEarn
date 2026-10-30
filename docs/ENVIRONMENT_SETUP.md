# QuizEarn — Environment Setup & Toolchain Guide

> Comprehensive instructions for configuring the developer environment, toolchain versions, SDK components, build reproduction profiles, and environment variables for QuizEarn.

---

## 1. Prerequisites & Verified Toolchain Specifications

QuizEarn requires a standardized toolchain for reproducible compilation across the Android mobile client and the Next.js/React administrative web panel.

| Tool / Component | Specification | Host Machine Verification | Status |
| :--- | :--- | :--- | :--- |
| **Operating System** | Windows 10/11, macOS 13+, or Linux (Ubuntu 22.04+) | Windows 11 Pro 64-bit | **Verified** |
| **Java Development Kit (JDK)** | OpenJDK 17 LTS (Zulu OpenJDK 17.0.20.1) | `C:\Program Files\Zulu\zulu-17` | **Installed & Active** |
| **Gradle Engine** | Gradle 8.4 (via bundled Gradle Wrapper) | `gradle-8.4-bin.zip` | **Verified (`.\gradlew.bat --version`)** |
| **Android SDK Platform** | Android 15 (API Level 35) | `platforms/android-35` | **Configured** |
| **Android Build Tools** | Build Tools 35.0.0 | `build-tools/35.0.0` | **Configured** |
| **Android Min SDK** | API Level 24 (Android 7.0 Nougat) | `minSdk = 24` | **Configured** |
| **Kotlin Version** | Kotlin 1.9.24 / Compose Compiler 1.5.14 | JVM Target 17 | **Configured** |
| **Node.js Toolchain** | Node.js v20+ LTS or v24+ | Node.js `v24.19.0` | **Installed & Active** |
| **Package Manager** | npm v10+ | npm `10.x` | **Installed & Active** |
| **IDE Environment** | Android Studio Koala / Ladybug or Antigravity | Antigravity Agentic IDE | **Active** |

---

## 2. Environment Variables Configuration

Set or verify the following environment variables in your Windows PowerShell environment or System Properties.

### PowerShell Session Initialization:
```powershell
# 1. Java 17 Home Configuration
$env:JAVA_HOME = "C:\Program Files\Zulu\zulu-17"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

# 2. Android SDK Configuration
$env:ANDROID_HOME = "C:\Users\User\android-sdk"
$env:Path = "$env:ANDROID_HOME\platform-tools;$env:ANDROID_HOME\cmdline-tools\latest\bin;$env:Path"

# 3. Verify toolchain availability
java -version
node -v
npm -v
```

### Expected Toolchain Verification Outputs:
```text
openjdk version "17.0.20.1" 2026-01-20 LTS
OpenJDK Runtime Environment Zulu17.48+15-CA (build 17.0.20.1+1-LTS)
OpenJDK 64-Bit Server VM Zulu17.48+15-CA (build 17.0.20.1+1-LTS, mixed mode, sharing)

v24.19.0
```

---

## 3. Local Properties & Path Management

The Android build system requires a local pointer to the Android SDK via `local.properties`:

```properties
## This file must NOT be checked into Version Control Systems,
# as it contains information specific to your local machine configuration.
#
# Location of the SDK. This is only used by Gradle.
sdk.dir=C\:\\Users\\User\\android-sdk
```

> **SECURITY MANDATE:** `local.properties` contains user-specific host filesystem paths and must never be committed to shared version control. In CI/CD pipelines, configure the Android SDK directory via the `ANDROID_HOME` or `ANDROID_SDK_ROOT` environment variable instead of generating hardcoded `local.properties` files.

---

## 4. Execution Profiles & Sandbox Isolation (B-004)

### 4.1 Sandboxed Execution Profile (Windows Terminal Sandbox)
In isolated sandbox mode:
- **What Works:** Gradle wrapper verification commands (`.\gradlew.bat --version`), static analysis scripts (`verify_kt.ps1`), administrative web scripts (`verify_ts.js`), security verification scanners (`node verify_security.js`), and Node.js commands operate with full read/write access to workspace files.
- **Environmental Constraint (B-004):** The Gradle build daemon attempts to bind and communicate over local TCP loopback sockets (`127.0.0.1`). In Windows sandbox isolation, local socket loopback is isolated, causing `assembleDebug` or background daemon connections to fail with:
  ```text
  Could not connect to the Gradle daemon.
  Daemon unreachable, socket connection refused.
  ```

### 4.2 Unsandboxed / IDE Execution Profile
To run full native Gradle compilation and generate release APKs or Android App Bundles:
1. **In Android Studio:** Open the QuizEarn root folder directly in Android Studio. The IDE natively manages the Gradle daemon outside the sandbox boundary.
2. **In Host PowerShell (Outside Sandbox):** Run commands directly with standard host permissions:
   ```powershell
   .\gradlew.bat clean assembleDebug
   .\gradlew.bat bundleRelease
   ```
3. **Daemon-Less Compilation (Direct Execution):**
   ```powershell
   .\gradlew.bat assembleDebug --no-daemon --offline
   ```

---

## 5. Mobile Client Build & Test Reproduction

### 5.1 Verify Gradle Wrapper Integrity
```powershell
.\gradlew.bat --version
```
Expected output:
```text
------------------------------------------------------------
Gradle 8.4
------------------------------------------------------------
Build time:   2023-10-04 20:52:13 UTC
Kotlin:       1.9.10
Groovy:       3.0.17
Ant:          Apache Ant(TM) version 1.10.13
JVM:          17.0.20.1 (Azul Systems, Inc.)
OS:           Windows 11 10.0 amd64
```

### 5.2 Build Debug APK
```powershell
.\gradlew.bat assembleDebug
```
- Output Artifact: `app/build/outputs/apk/debug/app-debug.apk`

### 5.3 Execute JVM Unit Tests
```powershell
.\gradlew.bat testDebugUnitTest
```
- Test Report: `app/build/reports/tests/testDebugUnitTest/index.html`
- Validates: `QuizViewModelTest`, `WalletViewModelTest`, `AuthViewModelTest`, `WalletConcurrencyTest`, `BackendSecurityTest`, `EndToEndUserJourneyTest`.

### 5.4 Build Release Android App Bundle (.aab)
```powershell
.\gradlew.bat bundleRelease
```
- Output Artifact: `app/build/outputs/bundle/release/app-release.aab`

---

## 6. Admin Panel Setup & Reproduction Guide

The QuizEarn Administrative Dashboard is built with Next.js 14, React 18, and Tailwind CSS under `admin/`.

### 6.1 Install Dependencies
```bash
cd admin
npm install
```

### 6.2 Development Server
```bash
npm run dev
```
- Access Dashboard: `http://localhost:3000`
- Demo Operator Credentials (pre-seeded):
  - **SuperAdmin:** `admin@quizearn.app` (Full authority over economics, audit logs, and settings)
  - **ContentManager:** `content@quizearn.app` (Trivia authoring, taxonomy, and tournament management)
  - **SupportAgent:** `support@quizearn.app` (Player moderation, tickets, and redemption fulfillment)

### 6.3 Production Compilation
```bash
npm run build
```
- Validates TypeScript types, JSX compilation, and generates optimized Next.js server/static bundles in `admin/.next/`.

---

## 7. Firebase Backend & Local Emulators

For live cloud operations and local serverless testing:

```bash
# 1. Install Firebase CLI
npm install -g firebase-tools

# 2. Login to Google Cloud / Firebase
firebase login

# 3. Start local emulator suite
firebase emulators:start
```
- **Emulator UI:** `http://localhost:4000`
- **Cloud Firestore:** `localhost:8080`
- **Authentication:** `localhost:9099`
- **Cloud Functions:** `localhost:5001`
