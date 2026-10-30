# QuizEarn — Privacy Data Map & Google Play Data Safety

> Comprehensive mapping of collected data, processing legal bases, retention schedules, Google Play Data Safety declarations, and global privacy regulation compliance (GDPR, CCPA/CPRA, COPPA).

---

## 1. Granular Data Collection & Processing Inventory

| Data Category | Specific Data Element | Collection Mode | Mandatory / Optional | Business & Operational Purpose | Storage & Retention Schedule |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Personal Info** | Email Address | Direct User Input | Mandatory for Registered Accounts; Optional in Guest Mode | Account authentication, credential recovery, fraud detection, and electronic reward delivery. | Stored encrypted in PostgreSQL database. Retained until account deletion request. |
| **Personal Info** | Display Name / Username | Direct User Input | Mandatory | Public leaderboard display, tournament rankings, user profile greeting. | Retained until user modifies or requests account deletion. |
| **Personal Info** | User Account Identifier (`usr_...`) | System Generated | Mandatory | Unique relational key linking quiz sessions, double-entry wallet ledger, and task progress. | Retained for lifetime of account; pseudonymized upon account deletion. |
| **Financial Info** | Payout Destination (e.g. PayPal Email, UPI ID, Digital Card Target) | Direct User Input | Optional (Required only at Redemption submission) | Disbursing rewards, electronic gift cards, or cash payouts to verified fulfillment partners. | Stored using AES-256 field-level encryption. Retained for 12 months post-fulfillment for statutory accounting/tax audits, then hard-deleted. |
| **Financial Info** | Virtual Points Balance & Ledger History | System Computed | Mandatory | Server-authoritative double-entry accounting records, point credits/debits, balance tracking. | Retained indefinitely in pseudonymized immutable ledger to maintain mathematical double-entry reconciliation integrity (`sum(credits) - sum(debits) == balance`). |
| **App Activity** | Quiz Gameplay & Session History | Automated Logging | Mandatory | Server-authoritative answer evaluation, tournament score computation, anti-cheat detection (speed checks, answer variance). | Active session data retained for 90 days; aggregated performance stats retained for lifetime of account. |
| **App Activity** | Daily Streak & Task Progression | Automated Logging | Mandatory | Awarding daily bonus points, validating daily/weekly challenge completions. | Retained for lifetime of account. |
| **App Info & Performance** | Crash Logs & ANR Diagnostics | Automated System Diagnostic | Automatic | Diagnosing application crashes, unhandled coroutine exceptions, and performance bottlenecks. | Retained for 90 days in aggregated crash analysis tools; contains zero PII. |
| **Device or other IDs** | Push Notification Token (FCM) | OS Provided | Optional (Requires User Permission on Android 13+) | Delivering tournament notifications, streak reminders, and redemption approval notices. | Stored in preferences; invalidated and deleted immediately upon user sign-out or push token rotation. |
| **Device or other IDs** | Pseudonymous Device Identifier & App Version | Automated Handshake | Mandatory | Session authentication token binding, preventing multi-accounting and bot farming. | Rotated periodically; purged 90 days after last active session. |

---

## 2. Google Play Console Data Safety Questionnaire Mapping

This section provides the exact answers required when completing the Google Play Console Data Safety declaration form for `com.quizearn.app`:

### 2.1 Overview Declarations
1. **Does your app collect or share any of the required user data types?**
   - **Answer:** **Yes**.
2. **Is all of the user data collected by your app encrypted in transit?**
   - **Answer:** **Yes** (All client-server network traffic is transmitted over HTTPS/TLS 1.3 with certificate pinning).
3. **Do you provide a way for users to request that their data be deleted?**
   - **Answer:** **Yes** (Both within the mobile app via `SettingsScreen.kt` and via an external web deletion form).
4. **Does your app provide account creation?**
   - **Answer:** **Yes** (Email/Password registration and anonymous Guest Mode).
5. **Does the app target children under 13?**
   - **Answer:** **No** (QuizEarn is restricted to users aged 13 and older per our Terms of Service).

### 2.2 Granular Category Questionnaire Responses

#### Category: Personal Info
- **Email Address:**
  - Collected: **Yes**
  - Shared with third parties: **No**
  - Ephemeral processing: **No** (Stored persistently)
  - Required or Optional: **Required for registered users; Optional for guest exploration**
  - Purposes: **App functionality, Account management, Fraud prevention & security**
- **Name / Display Name:**
  - Collected: **Yes**
  - Shared with third parties: **No** (Publicly displayed to other players on leaderboards)
  - Ephemeral processing: **No**
  - Required or Optional: **Required**
  - Purposes: **App functionality, Personalization, Account management**
- **User IDs:**
  - Collected: **Yes**
  - Shared with third parties: **No**
  - Ephemeral processing: **No**
  - Required or Optional: **Required**
  - Purposes: **App functionality, Account management, Analytics, Fraud prevention**

#### Category: Financial Info
- **User Payment Info (Payout Destination Identifier):**
  - Collected: **Yes**
  - Shared with third parties: **Yes** (Shared strictly with reward fulfillment processors such as PayPal or Amazon Gift Card API for the sole purpose of delivering redeemed rewards)
  - Ephemeral processing: **No** (Retained for audit history)
  - Required or Optional: **Optional** (Only collected if the user initiates a redemption)
  - Purposes: **App functionality (Reward disbursement), Fraud prevention & legal compliance**
- **Purchase History (In-App Ledger History):**
  - Collected: **Yes**
  - Shared with third parties: **No**
  - Ephemeral processing: **No**
  - Required or Optional: **Required**
  - Purposes: **App functionality (Ledger accounting), Fraud prevention**

#### Category: App Activity
- **App Interactions & Gameplay Stats:**
  - Collected: **Yes**
  - Shared with third parties: **No**
  - Ephemeral processing: **No**
  - Required or Optional: **Required**
  - Purposes: **App functionality (Scoring, tournaments, leaderboards), Analytics, Fraud prevention (Anti-cheat)**

#### Category: App Info and Performance
- **Crash Logs & Diagnostics:**
  - Collected: **Yes**
  - Shared with third parties: **No**
  - Ephemeral processing: **No**
  - Required or Optional: **Automatic**
  - Purposes: **Analytics, Developer diagnostics, Performance optimization**

#### Category: Device or other IDs
- **Device or other IDs:**
  - Collected: **Yes**
  - Shared with third parties: **No**
  - Ephemeral processing: **No**
  - Required or Optional: **Required**
  - Purposes: **App functionality, Account security, Fraud prevention (Sybil/bot mitigation)**

---

## 3. Third-Party Data Disclosures & No-Sale Policy

> **ZERO DATA SALE COMMITMENT:** QuizEarn does NOT sell, rent, monetize, or trade any personal data, gameplay statistics, email addresses, or financial records to third-party data brokers, advertising networks, or marketing conglomerates.

### Permitted Disclosures:
- **Reward Fulfillment Partners:** When a player requests redemption of points for digital vouchers (e.g., Amazon, Apple Gift Cards) or cash payouts (PayPal), only the necessary disbursement details (e.g., recipient email address, voucher reference ID) are transmitted to the fulfillment provider via secure API.
- **Infrastructure & Cloud Hosting:** QuizEarn operates on SOC 2 Type II certified cloud infrastructure (e.g., Google Cloud Platform). All hosting vendors act as data processors bound by strict Data Processing Agreements (DPAs).
- **Legal Compliance:** Personal and ledger data may be disclosed only when compelled by valid legal process, subpoena, or applicable regulatory inquiry.

---

## 4. Cryptographic Safeguards & Security Engineering

QuizEarn implements defense-in-depth security engineering to ensure user data remains confidential and tamper-resistant:

1. **Encryption in Transit:** All client-to-server communications use HTTPS with TLS 1.3. Plaintext HTTP traffic is strictly prohibited by Android network security configuration (`cleartextTrafficPermitted="false"`).
2. **Field-Level Encryption at Rest:** Sensitive payout destinations (e.g., PayPal email, bank identifiers) are encrypted using AES-256-GCM before storage.
3. **Password Security:** User authentication passwords are never stored in plaintext or reversibly encrypted. Passwords are hashed using salted Argon2id/bcrypt algorithms.
4. **Server-Authoritative Double-Entry Ledger:** All point grants, quiz earnings, tournament prize payouts, and redemption debits are recorded in an append-only double-entry ledger. Balance mutations are guarded by mutual exclusion (Mutex) locks and atomic database transactions to eliminate race conditions and double-spending.
5. **Session Security:** Bearer tokens are cryptographically signed JWTs with strict expiration windows (15-minute access token, 7-day refresh token) stored in secure Android DataStore encrypted preferences.

---

## 5. Global Privacy Rights & Regulatory Compliance

### 5.1 European Union (GDPR) Compliance
Residents of the European Economic Area (EEA) and United Kingdom enjoy rights under the General Data Protection Regulation (GDPR):
- **Right of Access (Article 15):** Users can request an export of all personal data and ledger history by contacting `privacy@quizearn.app` or viewing transaction history in the app.
- **Right to Rectification (Article 16):** Users can update their display name, password, or profile information directly within `ProfileScreen.kt`.
- **Right to Erasure / "To Be Forgotten" (Article 17):** Users can delete their account at any time via in-app Settings or through our web deletion portal.
- **Right to Data Portability (Article 20):** Users may request their profile and ledger history in structured, machine-readable JSON format.
- **Legal Basis for Processing:** Processing is necessary for the performance of our contract with the user (delivering trivia games and reward fulfillment) and for legitimate interests (preventing fraud and securing the platform).

### 5.2 California Consumer Privacy Act (CCPA / CPRA) Compliance
For California residents under the CCPA/CPRA:
- **Notice at Collection:** Categories of personal information collected are disclosed in Section 1 above.
- **Right to Know & Delete:** California consumers have the right to request disclosure of personal information collected and request deletion.
- **Do Not Sell or Share My Personal Information:** QuizEarn does not sell or share personal information for cross-context behavioral advertising.
- **Non-Discrimination:** Exercising privacy rights will never result in denied service, degraded gameplay quality, or altered reward conversion rates.

### 5.3 Children's Privacy (COPPA Compliance)
- QuizEarn is designed strictly for a general audience aged **13 and older** (or 16 and older in jurisdictions where local law requires).
- We do not knowingly collect, solicit, or maintain personal information from children under 13.
- If we discover that a user under 13 has registered or provided personal information, we immediately terminate the account, zero virtual balances, and permanently delete the personal data from our servers. Parents or guardians may contact `privacy@quizearn.app` to request immediate removal of an underage minor's information.

---

## 6. Account & Data Deletion Workflow

In compliance with Google Play's User Data policy and Account Deletion Requirement:

### 6.1 In-App Deletion Pathway
1. Open the QuizEarn mobile application.
2. Tap the **Profile** icon in the bottom navigation bar.
3. Tap **Settings** (gear icon).
4. Scroll to the **Account & Security** section and select **Delete Account**.
5. Read the warning notice explaining that deletion is permanent, points balances are forfeited, and active redemptions are cancelled.
6. Enter account password to confirm identity and tap **Permanently Delete Account**.
7. The mobile client calls the server deletion endpoint with the authorization bearer token.

### 6.2 Public Web Deletion Endpoint
For users who have uninstalled the app or cannot access their mobile device:
- **Public URL:** `https://quizearn.app/privacy/delete-account`
- **Procedure:** The user submits their registered email address. A cryptographically secure, time-bounded confirmation link (valid for 15 minutes) is sent to the registered email. Upon clicking the confirmation link, the deletion sequence is executed.

### 6.3 Technical Execution of Deletion:
- **Personal Information:** User record status transitions to `DELETED`. Email address, display name, password hash, and push notification tokens are permanently scrubbed from active databases.
- **Virtual Balance:** Any remaining point balance is zeroed.
- **Financial Ledger Integrity:** Historical ledger transactions are pseudonymized by replacing `userId` with an anonymized hash (`anonymized_usr_<sha256_prefix>`). This preserves mathematical double-entry accounting integrity (`sum(credits) - sum(debits) == 0`) for regulatory tax and financial compliance without preserving any link to the individual.
- **Backup Deletion:** Automated database backups overwrite deleted records on a rolling 30-day lifecycle.
