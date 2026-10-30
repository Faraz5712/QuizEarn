# QuizEarn — Architecture & Project Decision Records (ADRs)

> Record of all key product, technical, and operational decisions. Do not silently modify or revert these decisions without documented rationale.

---

### ADR-001: Local Project-Control System over Git Tracking
- **Date:** 2026-10-29
- **Status:** Accepted
- **Context:** Development sessions may end, change accounts, or migrate between environments. The user explicitly specified: *"I do NOT want Git-based progress tracking. All handoff reports must be saved inside this project folder."*
- **Decision:** All project state, task checklists, roadmap progress, error logs, and session handoffs are managed via markdown control files under `PROJECT_CONTROL/` and technical specifications under `docs/`. Git repositories must NOT be initialized unless explicitly requested by the user.
- **Consequences:** Every agent or developer must read `PROJECT_CONTROL/00_START_HERE.md` and `CURRENT_STATE.md` at the start of a session and update reports at the end of every task.

---

### ADR-002: Native Android with Kotlin and Jetpack Compose
- **Date:** 2026-10-29
- **Status:** Accepted
- **Context:** QuizEarn requires responsive, fluid animations (timers, option highlights, streak animations), accessibility support, and modern Android design system compliance.
- **Decision:** Build QuizEarn as an original native Android application using Kotlin, Jetpack Compose, Material 3, Android Jetpack Navigation, Coroutines, StateFlow, and Clean Architecture (Presentation, Domain, Data layers).
- **Consequences:** Native UI performance, streamlined lifecycle handling, and no cross-platform bridge overhead. Minimum SDK is set to 24 (Android 7.0) to maximize user reach while targeting SDK 35 (Android 15).

---

### ADR-003: Server-Authoritative Quiz Scoring and Question Delivery
- **Date:** 2026-10-29
- **Status:** Accepted
- **Context:** As a rewards-bearing quiz platform, QuizEarn is vulnerable to client-side reverse engineering, memory tampering, and replay attacks if answer keys or scoring are trusted on the client.
- **Decision:** Scoring must be strictly server-authoritative.
  1. The client receives question text and options *without* indications of which option is correct.
  2. The server issues a cryptographically secure, time-bounded session token.
  3. Answers are evaluated server-side upon submission. Submissions exceeding the allowed time window plus reasonable network jitter are invalidated.
  4. Client-side scoring in early development phases is strictly labeled as demo/local preview.
- **Consequences:** Prevents client-side cheating and protects the integrity of the reward economy.

---

### ADR-004: Immutable Append-Only Wallet Ledger
- **Date:** 2026-10-29
- **Status:** Accepted
- **Context:** User points correspond to redemption eligibility for real rewards (gift cards, merchandise). Preventing double-spending, race conditions, and arbitrary balance tampering is essential.
- **Decision:** User balances must be backed by an append-only, immutable `Transaction` ledger.
  1. No client endpoint can directly mutate a user balance.
  2. Every credit (quiz reward, daily streak, tournament prize) and debit (redemption request) must be an atomic database transaction.
  3. Idempotency keys must be enforced on all financial mutations.
  4. Points are treated strictly as points; no cash claims or unverified monetary payouts are allowed.
- **Consequences:** Auditability, zero negative balances, and deterministic balance reconciliation.

---

### ADR-005: Strict Evidence-Based Verification Standard
- **Date:** 2026-10-29
- **Status:** Accepted
- **Context:** In multi-session AI agent workflows, hallucinations or optimistic claims of "everything is working" can derail project state.
- **Decision:** A feature is marked complete in `TASK_CHECKLIST.md` or `CURRENT_STATE.md` ONLY if code exists on disk and verification (build, test, or inspection) has succeeded. If a build has not run or an external service is not configured, it must be explicitly labeled `UNVERIFIED` or `BLOCKED`.
- **Consequences:** High reliability across session handoffs, with zero ambiguity regarding real project readiness.

---

### ADR-006: Firebase-First Serverless Architecture Selection
- **Date:** 2026-10-29
- **Status:** Accepted
- **Context:** QuizEarn requires a secure, scalable backend supporting user authentication, real-time data sync, server-authoritative scoring, and anti-cheat hardware attestation without heavy DevOps maintenance overhead.
- **Decision:** Adopt a Firebase-First Serverless Architecture utilizing Firebase Authentication, Cloud Firestore, Firebase Cloud Functions for server-authoritative score calculation and ledger transactions, and Firebase App Check with Android Play Integrity API.
- **Consequences:** Eliminates database server provisioning; provides built-in native SDKs and declarative security rules; enables local development via Firebase Local Emulator Suite; preserves server-authoritative trust boundaries.

---

### ADR-007: AndroidX DataStore for Auth Session & Token Persistence
- **Date:** 2026-10-29
- **Status:** Accepted
- **Context:** Users and guest sessions require seamless session resumption across application restarts without re-entering credentials on every cold start.
- **Decision:** Use AndroidX DataStore (`quizearn_auth_session`) coordinated by `AuthSessionManager` to securely persist active user sessions, tokens, user roles, points balance, and expiration timestamps. An in-memory storage fallback (`InMemoryAuthSessionStorage`) is provided for unit testing and Compose previews.
- **Consequences:** Asynchronous, non-blocking disk I/O with reactive Kotlin Flow state, preventing UI freezing during session checks on application launch.

---

### ADR-008: Server-Authoritative Quiz Session Time Bounds & Secret Answer Key Stripping
- **Date:** 2026-10-29
- **Status:** Accepted
- **Context:** As a real-rewards app, QuizEarn is an attractive target for memory inspection, decompilation, and automated bot replays. Exposing answer keys in client JSON or trusting client submission timestamps compromises the rewards economy.
- **Decision:** 
  1. Question payloads generated server-side are strictly sanitized: `correctOptionIndex` and `explanationText` are stripped prior to network transmission.
  2. Each session is issued a cryptographically random session token and absolute server-side `expiresAtTimestamp` (question count * 15s + 15s network grace buffer). Submissions arriving after this server deadline are rejected.
  3. Every submission must include a client-generated UUID idempotency key; duplicate submission attempts are rejected to eliminate replay attacks and double crediting.
  4. Scoring, accuracy rate, performance tier, and wallet points rewards are computed authoritatively on the backend.
- **Consequences:** Eliminates client-side memory inspection hacks; guarantees deterministic and fair competition across all players; protects reward payout integrity.

---

### ADR-009: Strict Mathematical Double-Entry Ledger Reconciliation & Invariant
- **Date:** 2026-10-29
- **Status:** Accepted
- **Context:** To ensure complete fiscal integrity and prevent balance discrepancies, point duplication, or arbitrary balance modification, user points balances must never be stored or manipulated as isolated standalone integers.
- **Decision:**
  1. Points balances are strictly backed by an append-only, immutable `Transaction` ledger (`Transaction.kt`).
  2. Every balance modification must be an atomic `CREDIT` or `DEBIT` operation protected by concurrency mutex locks, guaranteeing zero race conditions and zero negative balances.
  3. Every mutation requires a client-generated UUID idempotency key; repeated requests return cached transactions without double-mutating the ledger.
  4. The double-entry invariant is mathematically enforced: $\text{CurrentBalance} == \sum \text{Credits} - \sum \text{Debits}$. A runtime `reconcileLedger()` method is provided to audit and guarantee $100\%$ ledger balance fidelity.
- **Consequences:** Eliminates financial race conditions; ensures complete auditability; guarantees deterministic reconciliation across all balance inquiries.

---

### ADR-010: State Machine & Automatic Point Refunds on Redemption Rejection
- **Date:** 2026-10-29
- **Status:** Accepted
- **Context:** Digital voucher redemptions involve real economic value (Amazon, Google Play, Apple, Mobile Airtime). When a user requests a reward, points must be immediately held to prevent double-spending. If an administrator or automated validation engine rejects the redemption, points must be automatically and reliably restored, and voucher stock replenished.
- **Decision:**
  1. A dedicated `Redemption` domain model (`Redemption.kt`) manages the redemption lifecycle through strict state transitions: `PENDING` -> `PROCESSING` -> `APPROVED` or `REJECTED`. Finalized states (`APPROVED`, `REJECTED`) are strictly immutable and cannot be modified or re-processed.
  2. Initiating a redemption invokes an atomic `debit(..., reason = REDEMPTION_HOLD)` operation on the double-entry transaction ledger and decrements physical reward inventory `stockQuantity`.
  3. Approval requires a non-blank `digitalCode`, sets status to `APPROVED`, and records the fulfillment timestamp.
  4. Rejection strictly requires a non-blank rejection reason, transitions status to `REJECTED`, and automatically executes an atomic `credit(..., reason = REDEMPTION_REFUND)` back to the user's wallet ledger while restoring `stockQuantity` by +1.
  5. The client UI displays real-time status badges, masks sensitive codes until approved, and dynamically controls affordability buttons based on live ledger balances.
- **Consequences:** Eliminates point-loss vulnerabilities during rejection; prevents fraud and double-spend race conditions; maintains perfect mathematical parity between wallet ledger and redemption records.

---

### ADR-011: Server-Authoritative Progression, Deterministic Tournament Tie-Breaking & Cached Leaderboards
- **Date:** 2026-10-29
- **Status:** Accepted
- **Context:** To ensure fair competition and prevent malicious manipulation, challenge progression, tournament registration/scoring, and global leaderboards must be server-authoritative. Clients must not be able to forge tournament scores, bypass entry fees, spam leaderboard queries, or access player PII.
- **Decision:**
  1. **Task Progression & UTC Rollover:** Task progression and claim validations are executed server-side. Daily tasks automatically reset according to UTC midnight boundaries while maintaining consecutive play streaks. Claims credit points via the double-entry transaction ledger (`TASK_COMPLETED`) with UUID idempotency deduplication.
  2. **Tournament Eligibility & Atomic Entry Fees:** Tournaments enforce player level requirements and entry fee deductions via atomic ledger debits (`TOURNAMENT_ENTRY_FEE`).
  3. **Deterministic Tie-Breaking:** Tournament participants are ranked strictly by: (a) higher score, (b) higher accuracy rate, (c) lower elapsed time, (d) earlier completion timestamp. Top 3 winners receive authoritative prize distributions via ledger credits (`TOURNAMENT_PRIZE`).
  4. **Cached Leaderboards & Zero-PII Fair Play:** Leaderboard standings are served from an in-memory cache with a 60-second TTL and guarded by a 5-second minimum query rate limit to shield backends from denial-of-service traffic spikes. All entries expose only sanitized usernames, avatar colors, ranks, and point totals with zero PII.
- **Consequences:** Protects competitive integrity; eliminates bot leaderboard scraping; guarantees fair, deterministic ranking across all tournament events; integrates seamlessly with the double-entry wallet ledger.

---

### ADR-012: In-App Notification Feed, Conditional FCM Push Fallback & Structured Customer Support
- **Date:** 2026-10-29
- **Status:** Accepted
- **Context:** Player retention and customer satisfaction require timely alerts (wallet credits, tournament announcements, daily streak warnings) and responsive support channels. However, external push messaging services (FCM) may be unconfigured in local development or sandboxed preview runs. Hardcoding API secrets or failing on missing push credentials violates our core security rules.
- **Decision:**
  1. **In-App Notification Feed:** An authoritative `NotificationRepository` maintains the user's notification feed with unread badge tracking, mark-as-read actions, and deep-link routing. Critical financial and gamification events (points credited, redemptions approved/refunded, tournament updates) automatically trigger notifications.
  2. **Conditional FCM Push Fallback:** A dedicated `ConditionalPushNotificationService` inspects the active backend environment. In offline or development modes, it operates safely in local simulation without throwing exceptions or crashing. Zero FCM server keys or tokens are stored in source code.
  3. **Structured Support Ticketing:** Customer inquiries are managed via `SupportRepository` with mandatory subject/description validation, PII sanitization, and lifecycle tracking (`OPEN` -> `IN_PROGRESS` -> `RESOLVED`). Agent replies automatically dispatch notifications to the user.
  4. **Persistent User Preferences:** All communication and gameplay toggles (daily reminders, tournament alerts, streak warnings, sound effects, haptics) are persisted to AndroidX DataStore via `PreferencesRepository`.
- **Consequences:** Provides a seamless user communication loop; eliminates app crashes when FCM is unconfigured; guarantees responsive, auditable customer support.

---

### ADR-013: Admin Web Stack (Next.js/React/Tailwind), Firebase Custom Claims & Role-Based Access Control (RBAC)
- **Date:** 2026-10-29
- **Status:** Accepted
- **Context:** Managing questions, tournament schedules, task rewards, redemption processing, and customer support requires a dedicated administrative back-office portal. The admin panel must be secure against privilege escalation, strictly separated from the public mobile app, and enforce role-based segregation of duties.
- **Decision:**
  1. **Admin Web Stack Selection:** Adopt Next.js 14+ (App Router) / React 18 with TypeScript and Tailwind CSS, hosted on Firebase App Hosting / Firebase Hosting. It provides server components, edge middleware for immediate route protection, and native integration with the Firebase Web SDK and TanStack Table.
  2. **Role-Based Access Control (RBAC):** Define a 3-tier hierarchy:
     - `SUPER_ADMIN` (Level 100): Full platform access, admin role management, financial ledger auditing, global system configuration.
     - `CONTENT_MANAGER` (Level 50): Question bank CRUD, bulk CSV/JSON import, category taxonomies, tournament scheduling, daily task configuration. (No financial or user mutation permissions).
     - `SUPPORT_AGENT` (Level 30): Review redemption queue (approve with digital code or reject with mandatory refund reason), view user profiles, reply to and resolve support tickets. (No question bank or tournament editing).
  3. **Server-Authoritative Enforcement:** Client-side route guards are strictly cosmetic UX conveniences. All privileged operations require cryptographically signed Firebase Authentication Custom Claims (`{ admin: true, role: 'SUPER_ADMIN' }`) verified inside Cloud Functions and Firestore Security Rules.
  4. **Tamper-Resistant Audit Logging:** Every administrative mutation writes an append-only document to `/admin_audit_logs` capturing operator UID, email, role, action, target entity, timestamp, and IP address. Modifications and deletions of audit records are blocked at the database level.
- **Consequences:** Guarantees strict segregation of duties; prevents privilege escalation; provides a high-performance web dashboard experience for operators; preserves complete auditability across all platform operations.







