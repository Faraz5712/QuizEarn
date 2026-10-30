# QuizEarn — Master Roadmap & Acceptance Criteria

> Complete phase-by-phase roadmap for QuizEarn, including all prompt IDs (QZ-00 to QZ-35), objectives, dependencies, and verifiable acceptance criteria.

---

## Phase 1: Project Foundation

### QZ-00: Create Local Control Files and Initial Audit
- **Objective:** Establish the local folder progress tracking system (`PROJECT_CONTROL/` and `docs/`) and perform an initial audit of the workspace.
- **Dependencies:** None.
- **Acceptance Criteria:**
  - [x] `PROJECT_CONTROL/` directory created with all 12 specified files.
  - [x] `docs/` directory created with all 9 core documentation files.
  - [x] Workspace audited with no guessed or fabricated statuses.
  - [x] No Git repository initialized.
  - [x] Next prompt (QZ-01) saved in `NEXT_PROMPT.md`.

### QZ-01: Confirm Product Requirements and Architecture
- **Objective:** Finalize product requirements, feature scope, tech stack, modular architecture, and non-negotiable security boundaries.
- **Dependencies:** QZ-00.
- **Acceptance Criteria:**
  - Complete Android architecture documented in `docs/ARCHITECTURE.md`.
  - Feature scope, user flows, and state machines documented.
  - Server-authoritative boundaries and wallet ledger requirements confirmed.
  - `CURRENT_STATE.md` and `TASK_CHECKLIST.md` updated; next prompt set to QZ-02.

### QZ-02: Stabilize Android Build and Dependencies
- **Objective:** Create baseline Android project structure, Gradle build files (`settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`), Gradle wrapper, and verify successful local compilation.
- **Dependencies:** QZ-01.
- **Acceptance Criteria:**
  - Standard Android Gradle project created with application ID `com.quizearn.app`.
  - Kotlin, Jetpack Compose, Material 3, Lifecycle, Navigation, Coroutines dependencies configured.
  - Successful execution of `./gradlew help` or `./gradlew assembleDebug` (or documented environment blocker).
  - No secret keys added to source files.

### QZ-03: Create Package Structure and App Navigation Foundation
- **Objective:** Establish clean package structure (data, domain, ui, navigation, core) and working navigation shell.
- **Dependencies:** QZ-02.
- **Acceptance Criteria:**
  - Package hierarchy created under `com.quizearn.app`.
  - Type-safe Compose navigation routes and top-level navigation shell created.
  - Application entry point (`MainActivity.kt`, `QuizEarnApplication.kt`) compiling.
  - Navigation smoke-tested via unit/preview verification.

---

## Phase 2: Brand and Design System

### QZ-04: Create QuizEarn Brand Specification
- **Objective:** Define brand identity, typography, color palette, component design tokens, and accessibility standards.
- **Dependencies:** QZ-03.
- **Acceptance Criteria:**
  - `docs/BRAND_GUIDELINES.md` created with original branding guidelines.
  - Color palette defined: Primary `#5B5FEF`, Secondary `#8B5CF6`, Mint `#2DD4BF`, Reward Gold `#F5B942`, Background Light `#F7F8FC`, Dark `#10111A`.
  - Contrast ratios pass WCAG AA standards.
  - No copied proprietary logos or copyrighted assets.

### QZ-05: Create Logo and Launcher Asset Plan/Assets
- **Objective:** Produce original vector logo, launcher adaptive icons (foreground/background), splash icon, and in-app brand headers.
- **Dependencies:** QZ-04.
- **Acceptance Criteria:**
  - Vector drawable XML assets generated in `res/drawable/` and `res/mipmap/`.
  - Adaptive icon configured in `AndroidManifest.xml`.
  - All assets verified to compile in Android resource pipeline.

### QZ-06: Implement Colors, Typography, Themes, Reusable Components
- **Objective:** Build Jetpack Compose Material 3 theme (`Color.kt`, `Type.kt`, `Theme.kt`, `Shape.kt`) and reusable UI components.
- **Dependencies:** QZ-05.
- **Acceptance Criteria:**
  - Centralized theme supporting dynamic light and dark modes.
  - Reusable components created: PrimaryButton, OutlineButton, RewardBadge, ScoreCard, QuizOptionItem, LoadingSpinner, ErrorCard, EmptyStateView.
  - Composable previews compiling without errors.

---

## Phase 3: Frontend

### QZ-07: Implement Splash and Onboarding
- **Objective:** Create animated splash screen and multi-step onboarding carousel with state persistence.
- **Dependencies:** QZ-06.
- **Acceptance Criteria:**
  - Splash screen routing correctly based on onboarding completion flag (DataStore).
  - Onboarding pages explaining: 1) Play Quizzes, 2) Complete Tasks, 3) Earn Rewards.
  - No deceptive promises of guaranteed income or gambling terminology.
  - Skip and Continue navigation functional.

### QZ-08: Implement Authentication Screens and UI States
- **Objective:** Implement Sign-In, Sign-Up, Forgot Password, and Reset Password screens with complete form validation and loading/error states.
- **Dependencies:** QZ-07.
- **Acceptance Criteria:**
  - Robust client-side validation (email format, password complexity).
  - Accessible form fields with error messaging and password visibility toggles.
  - Clear integration boundary for backend auth provider.
  - Passwords never logged or stored in plaintext.

### QZ-09: Implement Home Dashboard
- **Objective:** Build user home dashboard with greeting, points balance, featured quizzes, category carousel, daily challenges, and quick tournament access.
- **Dependencies:** QZ-08.
- **Acceptance Criteria:**
  - Clean modular layout handling pull-to-refresh, empty states, and loading shimmers.
  - Points balance prominently displayed with "demo/preview" badge if backend offline.
  - Fast navigation hooks into quiz, wallet, tasks, and leaderboard screens.

### QZ-10: Implement Quiz Categories and Quiz Gameplay UI
- **Objective:** Build category selection grid and interactive quiz gameplay screen (question cards, 4 options, timer, answer locking, progress bar).
- **Dependencies:** QZ-09.
- **Acceptance Criteria:**
  - Smooth question transitions and timer countdown with pause-on-background handling.
  - Option selection with distinct states: Default, Selected, Correct, Incorrect (when revealed).
  - Back-press confirmation dialog to prevent accidental quiz forfeit.
  - UI kept decoupled from scoring calculations (UI emits events only).

### QZ-11: Implement Results and Quiz History
- **Objective:** Build post-quiz results screen (score, accuracy, time taken, earned points) and historical quiz attempts list.
- **Dependencies:** QZ-10.
- **Acceptance Criteria:**
  - Detailed summary screen with "Play Again" and "Return Home" CTAs.
  - Review mode for answered questions (if enabled by quiz rules).
  - Quiz history screen displaying past attempts, dates, scores, and rewards.
  - Duplicate submit protection on result submission.

### QZ-12: Implement Tasks and Tournaments UI
- **Objective:** Build screens for daily tasks (e.g., streak challenges, category mastery) and scheduled tournaments.
- **Dependencies:** QZ-11.
- **Acceptance Criteria:**
  - Tasks list showing progress bars, claimable rewards, and expiry timers.
  - Tournament lobby with entry requirements, prize pools, and live countdowns.
  - Zero tolerance for fraudulent ad-click or forced fake engagement tasks.
  - Clear state separation: Upcoming, Active, Completed.

### QZ-13: Implement Leaderboard, Profile, Settings, Help
- **Objective:** Create global/weekly leaderboards, user profile screen, edit profile, app settings, and FAQ/Support contact.
- **Dependencies:** QZ-12.
- **Acceptance Criteria:**
  - Leaderboard displays ranks, usernames, and points; hides private user emails/phone numbers.
  - Profile screen with user stats, badges, and account management.
  - Settings: Dark mode toggle, sound effects toggle, notification preferences.
  - Clear account deletion request entry point complying with Google Play policy.

### QZ-14: Implement Rewards, Wallet, Transaction History, Redemption UI
- **Objective:** Build wallet screen with balance, transaction ledger history, reward catalog, and redemption claim flow.
- **Dependencies:** QZ-13.
- **Acceptance Criteria:**
  - Real-time balance and categorized transactions (Earned, Redeemed, Refunded).
  - Reward catalog showing available items (gift cards, merchandise) and points needed.
  - Modal form for redemption submission with validation and confirmation dialog.
  - Explicit disclaimer that points have no cash value outside approved redemptions.

---

## Phase 4: Backend

### QZ-15: Choose/Confirm Backend and Document Setup
- **Objective:** Select backend architecture (Firebase-first vs. Node.js/PostgreSQL), document setup, security rules, and data contracts.
- **Dependencies:** QZ-14.
- **Acceptance Criteria:**
  - Documented stack decision in `docs/ARCHITECTURE.md` with trade-offs.
  - Environment setup instructions in `docs/ENVIRONMENT_SETUP.md`.
  - Detailed schema in `docs/DATABASE_SCHEMA.md`.
  - Zero secret keys stored in codebase.

### QZ-16: Implement Authentication/Profile Integration
- **Objective:** Connect Android client to real backend authentication (email/password, OAuth, session management).
- **Dependencies:** QZ-15.
- **Acceptance Criteria:**
  - Secure token storage using Android Jetpack EncryptedSharedPreferences/EncryptedDataStore.
  - Automatic token refresh and session restore on app launch.
  - User document creation upon registration with initial zero balance.
  - Sign-out and session revocation fully working.

### QZ-17: Implement Quiz Session and Server-Authoritative Scoring
- **Objective:** Implement server-side quiz session lifecycle, question dispatch, and server-side answer evaluation.
- **Dependencies:** QZ-16.
- **Acceptance Criteria:**
  - Client sends quiz start request -> server generates session ID and question list without correct answer flags.
  - Client submits answers -> server computes score and checks submission timestamp against session duration limit.
  - Idempotency key prevents duplicate submission of the same quiz session.
  - Answer keys never exposed in network payloads before answer lock.

### QZ-18: Implement Wallet Ledger and Transaction Integrity
- **Objective:** Implement server-side immutable wallet ledger with atomic transactions and balance consistency.
- **Dependencies:** QZ-17.
- **Acceptance Criteria:**
  - Every balance change backed by an immutable ledger entry (`Transaction` record).
  - Atomic database transactions ensure balance equals sum of credits minus debits.
  - Concurrency checks prevent race-condition double spending.
  - Client has read-only access to balance and ledger; cannot execute self-credits.

### QZ-19: Implement Rewards and Redemption Workflow
- **Objective:** Build server-side reward catalog management, inventory check, and multi-state redemption pipeline.
- **Dependencies:** QZ-18.
- **Acceptance Criteria:**
  - Redemption status lifecycle: `PENDING` -> `APPROVED` -> `FULFILLED` or `REJECTED`.
  - Points reserved/deducted atomically at request creation.
  - Rejected redemptions trigger automatic point refunds with linked ledger entry.
  - No synthetic/fictitious voucher generation without real provider integration.

### QZ-20: Implement Tasks, Tournaments, Leaderboard Backend
- **Objective:** Build server logic for daily task tracking, tournament brackets/leaderboards, and ranking aggregation.
- **Dependencies:** QZ-19.
- **Acceptance Criteria:**
  - Daily tasks reset according to UTC midnight scheduler.
  - Tournament scoring aggregated via server-side events only.
  - Tie-breaking rules deterministic (e.g., earlier completion timestamp).
  - Rate limiting on leaderboard queries with caching.

### QZ-21: Implement Notifications and Support Workflow
- **Objective:** Implement in-app notification inbox, optional FCM push notifications, and support ticket submission.
- **Dependencies:** QZ-20.
- **Acceptance Criteria:**
  - Notification feed for wallet credit alerts and tournament announcements.
  - Support ticket creation with category, description, and status tracking.
  - PII sanitized before logging support issues.

---

## Phase 5: Admin Panel

### QZ-22: Confirm Admin Architecture and Authorization
- **Objective:** Design secure admin panel architecture, role-based access control (RBAC), and session security.
- **Dependencies:** QZ-21.
- **Acceptance Criteria:**
  - Admin roles defined (SuperAdmin, ContentManager, SupportAgent).
  - Server-enforced claims/roles; client-side UI flags strictly cosmetic.
  - Architecture plan documented in `docs/ARCHITECTURE.md`.

### QZ-23: Implement Admin Login and Dashboard Shell
- **Objective:** Build responsive web admin shell with authentication, sidebar navigation, and metric overview widgets.
- **Dependencies:** QZ-22.
- **Acceptance Criteria:**
  - Protected routes redirecting unauthorized users.
  - KPI summary cards (Total Users, Quizzes Played Today, Pending Redemptions).
  - Secure session termination and token expiry.

### QZ-24: Implement Category/Question Management
- **Objective:** Build admin CRUD interface for quiz categories and question bank.
- **Dependencies:** QZ-23.
- **Acceptance Criteria:**
  - Rich question editor (text, 4 choices, correct answer selection, explanation, timer).
  - Bulk question import (JSON/CSV) with validation.
  - Category enable/disable and icon assignment.

### QZ-25: Implement Task/Tournament Management
- **Objective:** Build admin interface to create, schedule, edit, and terminate tasks and tournaments.
- **Dependencies:** QZ-24.
- **Acceptance Criteria:**
  - Date/time pickers for tournament start and end windows.
  - Reward allocation rules configuration.
  - Status management (Draft, Scheduled, Live, Ended, Cancelled).

### QZ-26: Implement Rewards, Redemptions, Users, Support Management
- **Objective:** Build admin workflows for reviewing pending redemptions, managing users, and resolving support tickets.
- **Dependencies:** QZ-25.
- **Acceptance Criteria:**
  - Redemptions queue with Approve / Reject actions (rejection requires reason).
  - User search, view ledger, ban/unban controls.
  - Support ticket responder interface.

### QZ-27: Implement Audit Logs and Admin Security Verification
- **Objective:** Build immutable admin action audit log and perform security audit on all administrative endpoints.
- **Dependencies:** QZ-26.
- **Acceptance Criteria:**
  - All admin mutations log timestamp, admin ID, target resource, and delta.
  - Pen-test verification: ordinary user token rejected by admin endpoints with 403 Forbidden.
  - Results logged in `docs/SECURITY.md` and `PROJECT_CONTROL/TEST_RESULTS.md`.

---

## Phase 6: Quality & Polish

### QZ-28: Add Unit and UI Tests for Core Android Flows
- **Objective:** Write unit tests for ViewModels, Repositories, and Compose UI tests for critical screens.
- **Dependencies:** QZ-27.
- **Acceptance Criteria:**
  - Unit tests for QuizViewModel, WalletViewModel, AuthViewModel.
  - Compose UI tests for quiz gameplay option selection and answer locking.
  - All tests passing locally via `./gradlew test`.

### QZ-29: Test Backend Security, Duplicate Requests, and Wallet Concurrency
- **Objective:** Run automated security and stress tests on backend endpoints.
- **Dependencies:** QZ-28.
- **Acceptance Criteria:**
  - Concurrency tests confirm impossible double-spend on wallet debit.
  - Replay attack tests verify expired/replayed quiz answers are rejected.
  - Rate limiting active on all public endpoints.

### QZ-30: Accessibility, Performance, Error States, and Polish
- **Objective:** Conduct accessibility audit (a11y), improve font scaling, screen reader labels, reduce recompositions, and eliminate memory leaks.
- **Dependencies:** QZ-29.
- **Acceptance Criteria:**
  - All clickable icons have meaningful content descriptions.
  - Layout scales properly at 200% system font size.
  - Minimum touch targets meet 48dp guideline.
  - Smooth 60fps frame rate on standard test devices.

### QZ-31: End-to-End Testing and Bug-Fix Pass
- **Objective:** Perform complete user journey test: Sign Up -> Play Quiz -> Earn Points -> View Wallet -> Submit Redemption -> Admin Approval.
- **Dependencies:** QZ-30.
- **Acceptance Criteria:**
  - End-to-end user journey passes without uncaught crashes or data discrepancies.
  - Zero critical/high bugs remaining in `BLOCKERS_AND_ERRORS.md`.

---

## Phase 7: Release Preparation

### QZ-32: Build Verification and Environment Documentation
- **Objective:** Audit release build reproducibility, document exact environment setup, and verify all build scripts.
- **Dependencies:** QZ-31.
- **Acceptance Criteria:**
  - Clean build passes via `./gradlew clean assembleRelease` or `./gradlew bundleRelease`.
  - `docs/ENVIRONMENT_SETUP.md` accurately documents JDK, Android SDK, and environment variables.

### QZ-33: Prepare Release Configuration and APK/AAB
- **Objective:** Configure ProGuard/R8 rules, version codes, release signing configuration, and generate unsigned/signed AAB.
- **Dependencies:** QZ-32.
- **Acceptance Criteria:**
  - ProGuard rules configured and verified against runtime reflection issues.
  - Keystore parameters injected via environment variables (never committed to repository).
  - App bundle (`.aab`) successfully generated and validated with `bundletool`.

### QZ-34: Prepare Play Store Listing and Release Checklist
- **Objective:** Prepare store metadata, descriptions, screenshot guide, Google Play Data Safety declaration, and privacy policy.
- **Dependencies:** QZ-33.
- **Acceptance Criteria:**
  - App title, short description (80 chars), and full description (4000 chars) completed.
  - Google Play Data Safety declarations aligned with `docs/PRIVACY_DATA_MAP.md`.
  - Privacy Policy and Terms of Service drafts in `docs/BUILD_AND_RELEASE.md`.

### QZ-35: Final Audit and Handoff Report
- **Objective:** Comprehensive audit of all deliverables, verification of documentation, and final project handoff report.
- **Dependencies:** QZ-34.
- **Acceptance Criteria:**
  - All tasks in `TASK_CHECKLIST.md` verified.
  - `CURRENT_STATE.md` represents 100% verified state.
  - `NEXT_PROMPT.md` states project readiness or specific maintenance instructions.
  - Full handoff summary presented to the user.
