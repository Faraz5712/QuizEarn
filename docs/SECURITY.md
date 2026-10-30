# QuizEarn — Security Architecture & Threat Mitigation

> Security policies, threat modeling, anti-cheat mechanisms, and data integrity safeguards.

---

## 1. Threat Modeling & Attack Vectors

| Attack Vector | Risk | Mitigation Strategy |
| :--- | :--- | :--- |
| **Client Answer Interception** | High | Answers are NOT included in the quiz payload sent to the client. Scoring is performed exclusively server-side. |
| **Session Replay / Clock Tampering** | High | Every quiz session is issued a cryptographically random session ID with a fixed server-side expiry window. |
| **Wallet Balance Tampering** | Critical | Client has strictly read-only access to balances. Point balances are backed by an immutable append-only transaction ledger. |
| **Race Conditions (Double Spend)** | Critical | Point deductions and reservations use atomic database transactions and server-side locks with mandatory `Idempotency-Key` validation. |
| **Privilege Escalation** | High | Client UI visibility controls are purely cosmetic. Admin endpoints verify server-validated custom claims/roles (`role === 'admin'`). |
| **Man-in-the-Middle (MitM)** | Medium | Mandatory HTTPS/TLS 1.3 across all endpoints. Android Network Security Configuration blocks cleartext traffic. |
| **Bot Scraping & Automated Gameplay** | Medium | Rate-limiting per IP/user, app attestation (Google Play App Check / Play Integrity API), and randomized question order. |
| **Memory Extraction / Local Storage** | Low | Auth tokens stored using Android Jetpack `EncryptedSharedPreferences` / `EncryptedDataStore`. Passwords never cached in plaintext. |

---

## 2. Server-Authoritative Anti-Cheat Rules

1. **Question Stripping**:
   - The question payload delivered to the client must omit `correctOptionIndex` and `explanation`.
2. **Server-Side Session Lifecycle**:
   - When `/quizzes/sessions/start` is called, the server records `startedAt = now()` and sets `expiresAt = now() + (totalQuestions * timeLimitPerQuestion) + graceBufferSeconds (10s)`.
   - Submissions received after `expiresAt` are automatically marked `EXPIRED` and award zero points.
3. **Idempotency Protection**:
   - Submitting the same `sessionId` multiple times is blocked. Subsequent requests return the previously calculated result without executing additional ledger credits.
4. **Latency & Anomaly Detection**:
   - Minimum human reading latency threshold (~1.2 seconds per question). Submissions completing 10 questions in under 5 seconds are flagged as bot activity for manual review.

---

## 3. Financial & Ledger Integrity

1. **Immutable Ledger**:
   - The `transactions` table/collection is strictly append-only.
   - Deletions and updates on transaction records are blocked at the database security rules / ORM level.
2. **Atomic Invariant**:
   - User `pointsBalance` must strictly equal:
     $$\text{pointsBalance} = \sum(\text{credits}) - \sum(\text{debits})$$
   - Any transaction that would result in `pointsBalance < 0` is rolled back immediately.
3. **Redemption Safety**:
   - Point reservation occurs atomically when a redemption is requested.
   - Points are held in a `PENDING` state. If rejected by an administrator, an automatic compensatory credit is logged with reason `REDEMPTION_REFUND`.
4. **Idempotency-Key Header**:
   - Every redemption and quiz finish request must carry a client-generated UUID in the `Idempotency-Key` header.
   - The server caches operation results for 24 hours keyed by `(userId, idempotencyKey)`.

---

## 4. App Attestation & Play Integrity

- **Google Play Integrity API / Firebase App Check**:
  - Enforced on all sensitive endpoints (Quiz Session Start/Submit, Wallet Balance, Redemption Request).
  - Verifies that requests originate from an authentic, unmodified QuizEarn binary running on a certified Android device.
  - Emulators and modified APKs attempting to call production Cloud Functions are denied access with `403 App Check Attestation Failed`.

---

## 5. Secret & Credential Handling Rules

- **Zero Secret Commits**:
  - API secrets, private keys, database passwords, and keystore passwords must never be stored in source code, control files, or documentation.
- **Environment Ingestion**:
  - In production, credentials are provided via environment variables, secret managers (Google Secret Manager), or CI/CD pipelines.
- **Mobile Client Boundaries**:
  - The Android app must only contain public client identifiers (e.g. Firebase `google-services.json` client keys which are public by design, protected via App Check and SHA-256 fingerprint restrictions).

---

## 6. Administrative Role-Based Access Control (RBAC) & Permissions Hierarchy

Privileged system operations are segmented across three mutually distinct roles. Server-authoritative enforcement prevents privilege escalation attacks.

### 6.1 Role Hierarchy & Scopes

1. **`SUPER_ADMIN` (Level 100)**:
   - Root operational authority.
   - Authorized actions: Manage admin accounts & assign roles, inspect the immutable audit log, review complete financial ledger reconciliation, adjust global economy constants (e.g. minimum withdrawal thresholds), and execute emergency overrides.
   - Security rule: Only an active `SUPER_ADMIN` can invoke `grantAdminRole` or read `/admin_audit_logs`.

2. **`CONTENT_MANAGER` (Level 50)**:
   - Trivia & competition authority.
   - Authorized actions: Question bank CRUD, bulk CSV/JSON question import with schema validation, category configuration, tournament creation/scheduling, daily/weekly task configuration.
   - Boundaries: Strictly barred from inspecting user financial ledgers, altering points balances, reviewing redemptions, or changing user roles.

3. **`SUPPORT_AGENT` (Level 30)**:
   - Customer support and dispute resolution authority.
   - Authorized actions: View user support tickets, respond to user inquiries, inspect user profile status, review redemption queue, approve redemptions with digital voucher codes, or reject redemptions with mandatory reasons.
   - Boundaries: Rejection automatically triggers an immutable ledger refund. Agents cannot manually add points to any user account directly or modify quiz question banks.

---

## 7. Firebase Custom Claims Enforcement & Token Lifecycle

Client-side user tokens do not possess administrative authority by default. Admin roles are asserted strictly via **Firebase Authentication Custom Claims**.

### 7.1 Claims Minting & Revocation Architecture
- Custom claims cannot be assigned from client applications or the Android app.
- Claims can only be minted via privileged backend Cloud Functions invoking `admin.auth().setCustomUserClaims(uid, { admin: true, role: 'SUPER_ADMIN' | 'CONTENT_MANAGER' | 'SUPPORT_AGENT' })`.
- Role changes immediately increment a `claimsVersion` timestamp on the user record, causing subsequent API calls to force token refresh and invalidate stale tokens within 60 seconds.

### 7.2 Callable Functions Middleware Pattern
All admin endpoints implement the mandatory role assertion guard:

```typescript
export function assertAdminRole(
  context: CallableContext,
  requiredRole?: 'SUPER_ADMIN' | 'CONTENT_MANAGER' | 'SUPPORT_AGENT'
) {
  if (!context.auth) {
    throw new HttpsError('unauthenticated', 'User must be signed in.');
  }

  const token = context.auth.token;
  if (!token.admin || !token.role) {
    throw new HttpsError('permission-denied', 'Administrative authorization required.');
  }

  if (requiredRole && token.role !== 'SUPER_ADMIN' && token.role !== requiredRole) {
    throw new HttpsError('permission-denied', `Requires ${requiredRole} privileges.`);
  }
}
```

---

## 8. Tamper-Resistant Administrative Audit Logging Specification

To maintain accountability, fulfill compliance requirements, and prevent insider threats or unauthorized tampering:

1. **Immutable Append-Only Log**:
   - The `/admin_audit_logs` collection is strictly append-only.
   - Document deletion and update permissions are disabled at the Firestore Security Rules layer (`allow write: if false;`).
   - Entries are written exclusively by server-side Cloud Functions inside the same transaction as the administrative action.

2. **Mandatory Audit Log Payload**:
   - Every log record must capture:
     - `id`: Cryptographically unique audit identifier (`AUDIT-UUID`).
     - `adminUid`: Unique identifier of the authenticated operator.
     - `adminEmail`: Verified email address of the operator.
     - `adminRole`: Active role when the action was performed.
     - `action`: Specific operation code (e.g., `REDEMPTION_APPROVAL`, `QUESTION_CREATE`, `USER_SUSPEND`).
     - `targetType`: Entity type affected (`REDEMPTION`, `QUESTION`, `USER`, `TOURNAMENT`).
     - `targetId`: Unique ID of the affected entity.
     - `details`: Human-readable description and parameter delta.
     - `timestampIso`: ISO 8601 UTC timestamp.
     - `ipAddress`: Remote client IP address from request headers.

3. **Auditing Verification**:
   - Only `SUPER_ADMIN` accounts have permission to query `/admin_audit_logs`.
   - Automated anomaly alerts trigger if an administrative account attempts unauthorized actions outside its RBAC scope.

