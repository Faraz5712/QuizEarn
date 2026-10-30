# QuizEarn — Build and Release Guide

> Comprehensive reference for Android build configurations, release artifact generation, secure keystore injection, ProGuard/R8 optimization, and Google Play Store deployment.

---

## 1. Build Types & Build Variants

QuizEarn defines standard Android build configurations under `app/build.gradle.kts`:

| Variant | Build Type | Application ID | Minification (R8) | Debuggable | Signing Config | Primary Purpose |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **`debug`** | `debug` | `com.quizearn.app.debug` | Disabled | `true` | Default Android Debug Keystore | Daily development, unit testing, Compose UI previews, and local device testing. |
| **`release`** | `release` | `com.quizearn.app` | Enabled (R8 fullMode) | `false` | Production Keystore (Injected via Environment) | Public production distribution via Google Play Store (AAB) and distribution APKs. |

---

## 2. Secure Keystore Injection & Signing Configuration

> **CRITICAL SECURITY RULE:** Never commit `quizearn-release.jks`, private keystores, or passwords to this repository. All credentials must be injected dynamically via environment variables during CI/CD execution.

### 2.1 Release Keystore Generation (One-Time Setup)
To generate a production signing key for Google Play Console:

```bash
keytool -genkey -v \
  -keystore quizearn-release.jks \
  -alias quizearn_release_key \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -dname "CN=QuizEarn Security, OU=Mobile Engineering, O=QuizEarn, L=San Francisco, ST=California, C=US"
```

### 2.2 Dynamic Signing Configuration in `app/build.gradle.kts`
Configure release signing to read environment variables without hardcoded defaults:

```kotlin
android {
    signingConfigs {
        create("release") {
            val envKeystorePath = System.getenv("KEYSTORE_PATH")
            val envKeystorePassword = System.getenv("KEYSTORE_PASSWORD")
            val envKeyAlias = System.getenv("KEY_ALIAS")
            val envKeyPassword = System.getenv("KEY_PASSWORD")

            val keystoreFile = if (!envKeystorePath.isNullOrBlank()) file(envKeystorePath) else file("keystore/release.jks")
            if (keystoreFile.exists() && !envKeystorePassword.isNullOrBlank()) {
                storeFile = keystoreFile
                storePassword = envKeystorePassword
                keyAlias = envKeyAlias
                keyPassword = envKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            val envKeystorePath = System.getenv("KEYSTORE_PATH")
            val keystoreFile = if (!envKeystorePath.isNullOrBlank()) file(envKeystorePath) else file("keystore/release.jks")
            val envKeystorePassword = System.getenv("KEYSTORE_PASSWORD")

            if (keystoreFile.exists() && !envKeystorePassword.isNullOrBlank()) {
                signingConfig = signingConfigs.getByName("release")
            } else {
                signingConfig = signingConfigs.getByName("debug")
            }
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}
```

### 2.3 CI/CD Secret Injection (GitHub Actions / Cloud Build)
In continuous integration environments, encode the keystore file as Base64:

```bash
# Encode keystore to base64
base64 -w 0 quizearn-release.jks > keystore.base64
```

In your pipeline script:
```yaml
- name: Decode Keystore
  run: |
    echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 --decode > app/keystore/release.jks
  env:
    KEYSTORE_BASE64: ${{ secrets.KEYSTORE_BASE64 }}

- name: Build Android App Bundle
  run: ./gradlew.bat bundleRelease
  env:
    KEYSTORE_PATH: app/keystore/release.jks
    KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
    KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
    KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
```

---

## 3. R8 / ProGuard Optimization (`app/proguard-rules.pro`)

QuizEarn uses R8 in full mode for code shrinking, dead-code elimination, and identifier obfuscation. Baseline rules safeguard reflective operations and serialization:

- **Source Debugging:** `-keepattributes SourceFile,LineNumberTable` preserves de-obfuscation line numbers for Google Play Console crash diagnostics.
- **Domain & DTO Protection:** Preserves `com.quizearn.app.domain.model.**`, `com.quizearn.app.data.remote.**`, and `com.quizearn.app.data.local.**` to prevent stripping fields utilized in JSON serialization and double-entry accounting calculations.
- **Kotlinx Serialization:** Keeps `@Serializable` companions, serializers, and properties.
- **AndroidX DataStore:** Protects preferences keys and DataStore accessors.
- **Kotlin Coroutines:** Preserves `MainDispatcherFactory` and `CoroutineExceptionHandler`.
- **Jetpack Compose:** Preserves Compose runtime classes, state holders, and generated layout metadata.
- **AndroidX ViewModel & Lifecycle:** Preserves ViewModel constructors (`<init>(...)`) for reflective `ViewModelProvider.Factory` instantiation.
- **Navigation & Screens:** Preserves `com.quizearn.app.navigation.Screen` singleton objects and navigation graph route identifiers.

---

## 4. Release Build Reproduction Commands

Execute the following commands in order for clean, reproducible production builds:

### Step 1: Clean Build Cache
```powershell
.\gradlew.bat clean
```

### Step 2: Run Full Automated Verification Suite
```powershell
.\gradlew.bat testDebugUnitTest
```
- Validates all unit, concurrency, security, and end-to-end user journey tests across the JVM.

### Step 3: Compile Android App Bundle (.aab)
```powershell
.\gradlew.bat bundleRelease
```
- **Primary Artifact:** `app/build/outputs/bundle/release/app-release.aab`
- **Purpose:** Primary distribution format uploaded to Google Play Console for Dynamic Feature Delivery and split APK generation.

### Step 4: Compile Standalone Release APK (For Internal Testing)
```powershell
.\gradlew.bat assembleRelease
```
- **Artifact:** `app/build/outputs/apk/release/app-release.apk`
- **Purpose:** Standalone signed APK for internal QA testing, device staging, and Firebase App Distribution.

### Step 5: Validate App Bundle with `bundletool`
To verify that the generated `.aab` installs cleanly across all device configurations:

```bash
# Generate device-specific APK set from bundle
java -jar bundletool.jar build-apks \
  --bundle=app/build/outputs/bundle/release/app-release.aab \
  --output=app-release.apks \
  --mode=universal

# Install onto connected test device
java -jar bundletool.jar install-apks --apks=app-release.apks
```

---

## 5. Versioning Strategy

QuizEarn enforces semantic versioning aligned with monotonically increasing integer version codes:

```kotlin
defaultConfig {
    applicationId = "com.quizearn.app"
    minSdk = 24       // Android 7.0 Nougat
    targetSdk = 35    // Android 15
    versionCode = 1   // Monotonically increasing integer (e.g., 10001 for v1.0.1)
    versionName = "1.0.0" // MAJOR.MINOR.PATCH
}
```

- **`versionCode` Rule:** Every release bundle uploaded to Google Play must increment `versionCode` by at least +1.
- **`versionName` Rule:** Follows `[Major].[Minor].[Patch]` (e.g. `1.0.0` for initial launch, `1.1.0` for feature additions, `1.0.1` for hotfixes).

---

## 6. Pre-Launch Release Checklist for Google Play Store

Before publishing QuizEarn on Google Play Console, confirm all checklist requirements:

- [x] **SDK Targets:** `compileSdk = 35`, `targetSdk = 35`, and `minSdk = 24` verified in `app/build.gradle.kts`.
- [x] **ProGuard Configuration:** `app/proguard-rules.pro` fully configured for Compose, Coroutines, DataStore models, ViewModels, and Navigation.
- [x] **Release Build Variants & R8:** `isMinifyEnabled = true`, `isShrinkResources = true`, and dynamic release signing configured.
- [x] **Zero Hardcoded Secrets:** Codebase scanned and confirmed 0 secret leaks (`node verify_security.js`).
- [x] **Store Listing Metadata:** App Title, Short Description, and Full Description fully drafted (see Section 7).
- [x] **Graphic Asset Specifications:** App Icon, Feature Graphic, and 8-screen phone/tablet specifications compiled (see Section 8).
- [x] **Data Safety Declarations:** Full Google Play Data Safety declaration responses documented (see Section 9 & `docs/PRIVACY_DATA_MAP.md`).
- [x] **Legal Policies:** Comprehensive Privacy Policy and Terms of Service drafted (see Sections 10 & 11).
- [ ] **Release Keystore Provisioning:** Keystore generated and credentials stored in secure CI/CD secrets manager.
- [ ] **Google Play Console App Creation:** Application registered with package `com.quizearn.app`.
- [ ] **AAB Upload:** Initial production App Bundle uploaded to Play Console Internal Testing track.

---

## 7. Google Play Store Listing Copy & Metadata

### 7.1 App Title (Max 30 Characters)
```text
QuizEarn: Trivia & Rewards
```
- **Character Count:** 26 / 30 characters.
- **Keywords Included:** Quiz, Earn, Trivia, Rewards.

### 7.2 Short Description (Max 80 Characters)
```text
Test your knowledge, compete in live tournaments, and redeem exciting rewards!
```
- **Character Count:** 79 / 80 characters.
- **Value Proposition:** Knowledge testing, competition, and rewards.

### 7.3 Full Description (Up to 4,000 Characters)
```text
Welcome to QuizEarn — the premier trivia and live tournament competition where your knowledge unlocks real, tangible rewards!

Are you ready to test your intellect, challenge players worldwide, and earn points that you can redeem for popular digital gift cards and rewards? Whether you are a history buff, a science enthusiast, a pop culture connoisseur, or a sports fanatic, QuizEarn delivers fast-paced, brain-stimulating trivia crafted for curious minds.

═════════════════════════════════════════════
🔥 KEY FEATURES THAT SET QUIZEARN APART
═════════════════════════════════════════════

📚 6 RICH TRIVIA CATEGORIES
Explore thousands of high-quality, server-verified questions across diverse topics:
• Science & Nature: Biology, physics, astronomy, chemistry, and ecology.
• History & Civilization: Ancient empires, world revolutions, famous figures, and pivotal milestones.
• World Geography: Capital cities, world flags, mountain ranges, and cultural wonders.
• Sports & Athletics: Football, basketball, tennis, Olympics, records, and championships.
• Pop Culture & Media: Movies, music, television, literature, and gaming lore.
• Technology & Innovation: Computing history, artificial intelligence, software, and inventors.

⚡ REAL-TIME TOURNAMENTS & PRIZE POOLS
Step into the competitive arena! Enter scheduled daily showdowns and weekend grand tournaments with massive points prize pools. Compete head-to-head on the live leaderboard and walk away with champion rewards.

🎯 STRATEGIC LIFELINES
Stuck on a challenging brain teaser? Use your tactical lifelines wisely:
• 50:50 Lifeline: Instantly eliminates two incorrect answers, narrowing your odds.
• Skip Lifeline: Safely bypasses an impossible question without losing your streak.

💎 DAILY STREAKS & PROGRESSIVE QUESTS
Maintain your daily check-in streak to claim escalating coin bonuses. Complete daily challenges and weekly milestone quests to rapidly boost your points balance.

🏆 GLOBAL LEADERBOARDS & PLAYER TIERS
Rise through the ranks from Novice to Trivia Grandmaster. Compare your all-time earnings, weekly win rates, and category masteries against top players across the globe.

🔒 100% FAIR PLAY & TRANSPARENT LEDGER
QuizEarn is engineered with radical integrity:
• Server-Authoritative Scoring: Every question timer and answer evaluation is computed securely on our servers — no client-side spoofing or cheating.
• Double-Entry Wallet Ledger: Every point earned, tournament fee deducted, and reward redeemed is tracked in an immutable, balanced ledger. View your complete transaction history anytime!

🎁 TRANSPARENT REWARD REDEMPTIONS
Reach the 5,000-point threshold and redeem your hard-earned points for authentic rewards:
• Digital Gift Cards: Amazon, Google Play, Apple, and leading retailers.
• Cashout Vouchers: Verified cash transfers via PayPal or direct electronic payout.
• Fast Fulfillment: Track your redemption status in real time with automated fulfillment notifications.

═════════════════════════════════════════════
🚀 HOW TO PLAY & EARN IN 4 EASY STEPS
═════════════════════════════════════════════
1. Pick a Category: Select your favorite subject or jump into a Live Tournament.
2. Answer Fast: Beat the countdown clock to maximize bonus points.
3. Complete Daily Tasks: Maintain your streak and unlock achievement chests.
4. Redeem Rewards: Exchange your points for gift cards and digital rewards!

No purchase is necessary to play or win. QuizEarn is free to play and designed to reward genuine knowledge, curiosity, and skill.

Download QuizEarn today, sharpen your mind, and turn your trivia expertise into rewarding achievements!
```
- **Character Count:** ~3,120 / 4,000 characters.
- **Formatting:** Formatted with bullet points, dividers, and clear sections optimized for Google Play Store rendering.

### 7.4 Categorization & Store Metadata
- **App Category:** Games > Trivia (Secondary: Education).
- **Content Rating:** Everyone (ESRB) / PEGI 3 / IARC 3+.
- **Target Audience:** 13 years and older.
- **Contact Email:** `support@quizearn.app`
- **Privacy Policy URL:** `https://quizearn.app/privacy`
- **Account Deletion URL:** `https://quizearn.app/privacy/delete-account`

---

## 8. Screenshot & Graphic Asset Specification

All promotional artwork must adhere to Google Play Console technical requirements and QuizEarn Brand Guidelines (`docs/BRAND_GUIDELINES.md`):

### 8.1 App Icon (High-Resolution)
- **Dimensions:** `512 x 512` pixels (1:1 aspect ratio).
- **Format:** 32-bit PNG with alpha channel (transparent background permitted).
- **Max File Size:** 1,024 KB.
- **Design Composition:** Electric Indigo (`#5B5FEF`) squircle base with Vibrant Violet (`#8B5CF6`) depth gradient; centered Amber Gold (`#F5B942`) lightning-bolt/star emblem symbolizing knowledge and reward points; crisp white typography accents.

### 8.2 Feature Graphic
- **Dimensions:** `1024 x 500` pixels (approx 2:1 aspect ratio).
- **Format:** 24-bit PNG or JPEG without alpha channel.
- **Max File Size:** 15 MB.
- **Design Composition:**
  - Background: Deep Dark background (`#0B0E14`) fading into rich violet/indigo ambient glow.
  - Left Side: Bold headline *"Play Trivia. Master Knowledge. Earn Real Rewards."* in Outfit Bold with Amber Gold accent badge *"Over 10,000+ Verified Questions"*.
  - Right Side: Tilted 3D mockups of the Android client displaying an active Quiz gameplay session with a correct answer highlight in Mint Teal (`#2DD4BF`) and a Reward Card preview.

### 8.3 Phone Screenshots (8 Curated Visual Compositions)
- **Specification:** Minimum 4, recommended 8 screenshots at `1080 x 2400` pixels (20:9 aspect ratio) or `1080 x 1920` (16:9).
- **Visual Style:** Modern device mockup frame with bold contrast header bar and concise 2-line benefit caption.

| Screen # | Frame Subject | Headline Caption | Sub-Caption / Value Highlight | Primary UI Elements Displayed |
| :---: | :--- | :--- | :--- | :--- |
| **01** | **Home Dashboard** | *"Play Trivia & Earn Real Rewards"* | *"Explore categories, daily streaks, and live events"* | Daily streak bonus pill (+25 pts), category cards, points balance (1,250 pts), quick play button. |
| **02** | **Quiz Gameplay** | *"Fast-Paced Brain Challenges"* | *"Server-verified scoring with urgent countdown timers"* | Circular timer ring (15s), question prompt, 4 interactive answer options, 50:50 and Skip lifelines. |
| **03** | **Results & Review** | *"Instant Results & Detailed Review"* | *"Earn points and analyze question explanations"* | Score card (9/10), points earned (+20 pts), Performance Tier ("Grandmaster"), Question Review list with correct explanations. |
| **04** | **Tournaments Lobby** | *"Compete in Live Tournaments"* | *"Scheduled battles with massive points prize pools"* | Weekend Grand Tournament card, 5,000 pt prize pool badge, countdown to start, participant counter, entry fee button. |
| **05** | **Daily Challenges** | *"Daily Streaks & Milestone Quests"* | *"Complete daily tasks for escalating bonus points"* | Streak calendar tracker (Day 5/7 active), daily quiz quest progress bar, claim reward button with Amber Gold shine. |
| **06** | **Global Leaderboard** | *"Climb the Ranks to Glory"* | *"Compete against trivia masters worldwide"* | Top 3 podium avatars with gold/silver/bronze trophies, user current rank pill, weekly filter tabs. |
| **07** | **Wallet & Ledger** | *"Transparent Double-Entry Ledger"* | *"Every point earned and spent tracked with 100% integrity"* | Available points card ($12.50 cash value conversion), balance reconciliation badge, chronological credit/debit transaction history. |
| **08** | **Reward Catalog** | *"Redeem Popular Gift Cards & Cash"* | *"Fast digital voucher delivery starting at 5,000 points"* | Reward cards (Amazon $10, Google Play $15, PayPal Cashout $25), points cost pill, stock indicators, 1-click redeem modal. |

### 8.4 Tablet Screenshots (7-inch & 10-inch Form Factors)
- **7-inch Tablet Screenshots:** `1200 x 1920` px (portrait) or `1920 x 1200` px (landscape).
- **10-inch Tablet Screenshots:** `1600 x 2560` px (portrait) or `2560 x 1600` px (landscape).
- **Layout Adaptations:** Showcases adaptive 2-column Compose grid layouts for Category selection, split master-detail view on Wallet ledger, and expansive tournament leaderboard tables.

---

## 9. Google Play Data Safety Section Declarations

A complete audit of QuizEarn data practices against Google Play Console Data Safety requirements:

| Declaration Field | Response | Specification / Technical Detail |
| :--- | :--- | :--- |
| **Data Collection** | **Yes** | App collects personal info, financial info, app activity, diagnostics, and device IDs. |
| **Data Sharing** | **Yes (Limited)** | Only payout destinations (e.g. PayPal email, voucher ID) are transmitted to fulfillment processors strictly upon redemption request. |
| **Data Encrypted in Transit** | **Yes** | All network traffic is encrypted via TLS 1.3/HTTPS. Cleartext traffic disabled. |
| **Account Deletion Supported** | **Yes** | In-app deletion pathway (`SettingsScreen.kt`) and external web deletion portal. |
| **Target Audience** | **13+ Only** | Does not target children under 13; COPPA compliant. |
| **Personal Info Collected** | Name, Email, User ID | Mandatory for account management, authentication, leaderboard display, and fraud prevention. |
| **Financial Info Collected** | Payout info, Ledger history | Payout info collected only at redemption; ledger history computed for double-entry balance accounting. |
| **App Activity Collected** | Gameplay history, Interactions | Used for server-side score validation, tournament rankings, and anti-cheat speed checks. |
| **Diagnostics Collected** | Crash logs, Performance stats | Anonymized crash stack traces for bug fixing and stability optimization. |
| **Device / Other IDs** | Device ID, FCM Push Token | Device ID for bot/multi-account prevention; FCM token for opted-in push notifications. |

---

## 10. Official Privacy Policy Draft

> **Published URL:** `https://quizearn.app/privacy`  
> **Effective Date:** October 29, 2026  
> **Last Updated:** October 29, 2026

### 1. Introduction
QuizEarn ("we", "us", or "our") operates the QuizEarn mobile application (`com.quizearn.app`) and associated web services. We are committed to protecting your privacy and ensuring the security of your personal information. This Privacy Policy explains how we collect, use, disclose, and safeguard your data when you use QuizEarn.

By accessing or using QuizEarn, you agree to the collection and use of information in accordance with this Privacy Policy. If you do not agree, please do not use our application.

### 2. Information We Collect
- **Information You Provide Directly:**
  - **Account Credentials:** When you register, we collect your email address, chosen display name, and an encrypted password hash.
  - **Redemption & Payout Information:** When you submit a point redemption request, we collect the necessary payout destination (such as your PayPal email address or digital gift card delivery email).
  - **Customer Support Communications:** If you contact us via in-app support or email, we collect the content of your message and related ticket correspondence.
- **Information Collected Automatically:**
  - **Gameplay & Performance Data:** We collect questions answered, timestamps, scores achieved, lifelines used, and tournament entries to evaluate gameplay and compute rankings.
  - **Double-Entry Wallet Records:** All point awards, deductions, and redemption transactions are recorded in an append-only transaction ledger.
  - **Device & Technical Information:** We collect your device model, operating system version, application version, IP address, and pseudonymous device identifiers for fraud prevention and bot mitigation.
  - **Diagnostics:** In the event of an unhandled application error, anonymized crash reports and performance telemetry are logged.

### 3. How We Use Your Information
We use the collected information strictly for legitimate operational purposes:
1. To authenticate your account and maintain active user sessions.
2. To provide server-authoritative quiz gameplay, timer evaluation, and tournament competitions.
3. To calculate, record, and reconcile your virtual point balances in our double-entry ledger.
4. To disburse rewards, electronic vouchers, and prize payouts upon your verified request.
5. To detect and prevent fraudulent activity, cheating, automated botting, and multi-accounting.
6. To monitor application performance, diagnose bugs, and optimize user experience.
7. To deliver transactional notifications, tournament alerts, and streak reminders (with your permission).

### 4. Third-Party Disclosures & No Data Sale Policy
**QuizEarn does not sell, rent, monetize, or trade your personal information to third parties for advertising or marketing purposes.**

We disclose information only in the following limited circumstances:
- **Reward Fulfillment Providers:** We transmit payout destinations (e.g. PayPal email address or gift card recipient email) to authorized reward disbursement processors solely to deliver your requested reward.
- **Service Providers:** We engage trusted cloud infrastructure vendors (e.g. Google Cloud Platform) to host our backend services under strict confidentiality and data protection agreements.
- **Legal Requirements:** We may disclose your information if required to do so by applicable law, court order, or governmental regulation.

### 5. Data Security & Storage
We employ industry-leading technical and organizational security measures:
- **Encryption in Transit:** All network traffic between your device and our servers is secured using HTTPS/TLS 1.3.
- **Encryption at Rest:** Sensitive payout destinations are stored using AES-256 field-level encryption. Passwords are hashed using salted Argon2id/bcrypt.
- **Access Controls:** Administrative access to databases and user records is restricted to authenticated operators with strict role-based access control (RBAC) and immutable audit logging.

### 6. Data Retention & Account Deletion
- **Retention Schedule:** We retain your account information for as long as your account remains active. Payout transaction records are retained for 12 months post-fulfillment to comply with tax and accounting regulations.
- **Account Deletion:** You may delete your account at any time directly within the app via **Settings > Account > Delete Account**, or by submitting a request at `https://quizearn.app/privacy/delete-account`.
- **Deletion Effect:** Upon confirmation, your account record is marked `DELETED`, your personal data (name, email, credentials) is permanently scrubbed, and points balances are zeroed. Historical ledger records are anonymized to preserve mathematical double-entry accounting integrity without PII linkage.

### 7. Global Privacy Rights (GDPR & CCPA/CPRA)
- **GDPR Rights (EEA & UK):** You have the right to access, rectify, port, or erase your personal data, as well as the right to restrict or object to certain processing activities.
- **CCPA / CPRA Rights (California):** California residents have the right to know what personal information is collected, request deletion, and opt out of any potential future sale of personal information (which we do not engage in). We guarantee non-discrimination for exercising your privacy rights.
- **Contact:** To exercise any of these rights, email `privacy@quizearn.app`.

### 8. Children's Privacy
QuizEarn is strictly intended for individuals aged 13 and older. We do not knowingly collect personal information from children under 13. If we discover an account belongs to a minor under 13, we immediately terminate the account and purge all associated personal information.

### 9. Changes to This Policy
We may update this Privacy Policy from time to time. Any material changes will be notified via an in-app notice or email update prior to the effective date.

---

## 11. Official Terms of Service Draft

> **Published URL:** `https://quizearn.app/terms`  
> **Effective Date:** October 29, 2026  
> **Last Updated:** October 29, 2026

### 1. Acceptance of Terms
By downloading, installing, accessing, or using QuizEarn ("App"), you agree to be bound by these Terms of Service ("Terms"). If you do not agree to these Terms, you may not access or use the App. QuizEarn is operated by QuizEarn Inc. ("Company", "we", "us").

### 2. Eligibility & Account Creation
- You must be at least 13 years old (or the legal age of majority in your jurisdiction) to use QuizEarn.
- You agree to provide accurate, truthful, and complete information during registration.
- You are solely responsible for maintaining the confidentiality of your account credentials.
- **One Account Per Person:** Each individual is permitted to operate exactly one QuizEarn account. The creation or operation of multiple accounts, farming accounts, or sybil identities is strictly prohibited and constitutes grounds for immediate termination.

### 3. Nature of Virtual Points & Rewards
- **Promotional Virtual Currency:** QuizEarn points ("Points", "Coins") are promotional, virtual entertainment tokens. Points have **zero monetary, cash, or real-world value** unless and until successfully redeemed through our official in-app Reward Catalog in accordance with these Terms.
- **No Property Right:** You do not own Points. Points represent a limited, revocable, non-exclusive license to redeem promotional rewards. Points cannot be sold, bartered, gifted, or transferred between accounts.
- **Redemption Requirements:** Redemptions are subject to meeting minimum threshold requirements (starting at 5,000 Points), account verification, and inventory availability.
- **Audit & Review:** All redemption requests undergo administrative fraud screening. We reserve the right to deny or refund redemptions that violate our Fair Play Policy.

### 4. Fair Play & Anti-Cheating Policy
QuizEarn is dedicated to maintaining a level, transparent playing field for all trivia enthusiasts. You agree NOT to:
1. Use automated bots, scripts, screen-scrapers, macro tools, or AI solvers to play quizzes or submit answers.
2. Exploit software bugs, security vulnerabilities, or network latency to manipulate quiz timers or scores.
3. Run the App inside emulators, virtual environments, or rooted/jailbroken devices designed to bypass security checks.
4. Artificially inflate point balances through coordinated collusion or syndicated tournament entries.
5. Attempt to decompile, reverse-engineer, or tamper with the App's source code, APIs, or database records.

**Enforcement:** Any violation of this Fair Play Policy will result in immediate suspension, forfeiture of all accumulated points, cancellation of pending redemptions, and permanent device banning.

### 5. Intellectual Property
All content, questions, graphics, animations, branding, trademarks, sound effects, and software code within QuizEarn are the exclusive property of QuizEarn Inc. or its licensors and are protected by applicable intellectual property laws.

### 6. Account Suspension & Termination
- You may terminate your account at any time via the in-app deletion menu.
- We reserve the right, in our sole discretion, to suspend, terminate, or restrict your access to the App at any time, with or without notice, for conduct that violates these Terms, harms other users, or defrauds the platform.
- Upon termination, all unredeemed Points are immediately forfeited.

### 7. Disclaimer of Warranties
QUIZEARN IS PROVIDED "AS IS" AND "AS AVAILABLE" WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT. WE DO NOT GUARANTEE THAT THE APP WILL BE UNINTERRUPTED, ERROR-FREE, OR FREE OF HARMFUL COMPONENTS.

### 8. Limitation of Liability
TO THE MAXIMUM EXTENT PERMITTED BY LAW, IN NO EVENT SHALL QUIZEARN INC., ITS DIRECTORS, EMPLOYEES, OR PARTNERS BE LIABLE FOR ANY INDIRECT, INCIDENTAL, SPECIAL, CONSEQUENTIAL, OR PUNITIVE DAMAGES ARISING OUT OF OR IN CONNECTION WITH YOUR USE OF THE APP, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGES. OUR TOTAL AGGREGATE LIABILITY SHALL NOT EXCEED THE VALUE OF REDEEMED REWARDS OWED TO YOU IN THE THIRTY (30) DAYS PRECEDING THE CLAIM.

### 9. Governing Law & Dispute Resolution
These Terms shall be governed by and construed in accordance with the laws of the State of California, United States, without regard to its conflict of law provisions. Any dispute arising under these Terms shall be resolved through binding individual arbitration administered by the American Arbitration Association (AAA), waiving any right to participate in a class action lawsuit.

### 10. Contact Information
For questions or legal inquiries regarding these Terms:
- **Email:** `legal@quizearn.app`
- **Support:** `support@quizearn.app`
- **Address:** QuizEarn Inc., Legal Department, 500 Howard Street, Suite 400, San Francisco, CA 94105, USA.

