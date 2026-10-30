# QuizEarn — Database Schema & Data Models

> Data schema specification for QuizEarn. Compatible with NoSQL document stores (Firestore) and relational databases (PostgreSQL).

---

## 1. Entities & Collections Overview

```
┌──────────────────┐            ┌──────────────────┐
│      users       │ 1        * │  transactions    │
│  (Profile & Bal) ├───────────►│  (Wallet Ledger) │
└────────┬─────────┘            └──────────────────┘
         │ 1
         │
         ├─────────────────────┐
         ▼ *                   ▼ *
┌──────────────────┐    ┌──────────────────┐
│  quiz_sessions   │    │   redemptions    │
│  (Game scoring)  │    │ (Reward claims)  │
└────────┬─────────┘    └────────┬─────────┘
         │ *                     │ *
         ▼ 1                     ▼ 1
┌──────────────────┐    ┌──────────────────┐
│    questions     │    │     rewards      │
│  (Question Bank) │    │(Reward Inventory)│
└────────┬─────────┘    └──────────────────┘
         │ *
         ▼ 1
┌──────────────────┐
│    categories    │
│ (Quiz groupings) │
└──────────────────┘
```

---

## 2. Entity Specifications

### `users`
Represents an individual authenticated user.
```json
{
  "id": "uuid-or-uid",
  "username": "QuizMaster99",
  "email": "user@example.com",
  "pointsBalance": 1500,
  "level": 3,
  "totalQuizzesPlayed": 42,
  "accuracyRate": 84.5,
  "role": "USER",
  "status": "ACTIVE",
  "createdAt": "2026-10-29T10:00:00Z",
  "updatedAt": "2026-10-29T10:00:00Z"
}
```
- **Constraints:** `email` unique; `pointsBalance` >= 0; `role` in `['USER', 'SUPPORT', 'ADMIN']`.

---

### `categories`
Quiz categories available for gameplay.
```json
{
  "id": "cat_science",
  "name": "Science & Nature",
  "slug": "science-nature",
  "iconUrl": "assets/icons/science.png",
  "colorHex": "#2DD4BF",
  "displayOrder": 1,
  "isActive": true,
  "totalQuestions": 250
}
```

---

### `questions`
Question bank managed by administrators.
```json
{
  "id": "q_1001",
  "categoryId": "cat_science",
  "questionText": "What planet is known as the Red Planet?",
  "options": [
    "Venus",
    "Mars",
    "Jupiter",
    "Saturn"
  ],
  "correctOptionIndex": 1,
  "explanation": "Mars appears red due to iron oxide (rust) on its surface.",
  "difficulty": "EASY",
  "timeLimitSeconds": 15,
  "pointsReward": 10,
  "status": "PUBLISHED",
  "createdAt": "2026-10-29T10:00:00Z"
}
```
- **Security Rule:** `correctOptionIndex` and `explanation` are **never** delivered to the client during an active quiz session.

---

### `quiz_sessions`
Represents an ongoing or completed quiz attempt.
```json
{
  "id": "sess_987654",
  "userId": "uuid-or-uid",
  "categoryId": "cat_science",
  "questionIds": ["q_1001", "q_1002", "q_1003", "q_1004", "q_1005"],
  "answers": [
    {"questionId": "q_1001", "selectedOption": 1, "timeSpentSeconds": 4.2},
    {"questionId": "q_1002", "selectedOption": 0, "timeSpentSeconds": 7.1}
  ],
  "score": 4,
  "totalQuestions": 5,
  "pointsAwarded": 40,
  "status": "COMPLETED",
  "startedAt": "2026-10-29T10:05:00Z",
  "completedAt": "2026-10-29T10:06:12Z",
  "idempotencyKey": "idem_quiz_sess_987654"
}
```
- **Status Lifecycle:** `ACTIVE` -> `COMPLETED` | `EXPIRED` | `ABANDONED`.

---

### `transactions` (Immutable Wallet Ledger)
Append-only log of all point movements.
```json
{
  "id": "tx_abc123",
  "userId": "uuid-or-uid",
  "type": "CREDIT",
  "amount": 40,
  "balanceAfter": 1540,
  "reason": "QUIZ_REWARD",
  "referenceId": "sess_987654",
  "description": "Completed Science & Nature Quiz (Score: 4/5)",
  "createdAt": "2026-10-29T10:06:13Z"
}
```
- **Transaction Types:** `CREDIT` (positive), `DEBIT` (negative).
- **Reasons:** `QUIZ_REWARD`, `DAILY_CHECKIN`, `TASK_BONUS`, `TOURNAMENT_PRIZE`, `REDEMPTION_HOLD`, `REDEMPTION_REFUND`, `ADMIN_ADJUSTMENT`.
- **Integrity Rule:** Records are strictly append-only. Modification and deletion are prohibited.

---

### `rewards`
Catalog of redeemable items.
```json
{
  "id": "rew_amazon_5",
  "title": "$5 Amazon Gift Card",
  "description": "Digital gift card delivered via email.",
  "pointsCost": 5000,
  "imageUrl": "assets/rewards/amazon.png",
  "category": "GIFT_CARD",
  "stockQuantity": 50,
  "isActive": true
}
```

---

### `redemptions`
Redemption request submitted by a user.
```json
{
  "id": "red_554433",
  "userId": "uuid-or-uid",
  "rewardId": "rew_amazon_5",
  "pointsDeducted": 5000,
  "payoutDetails": "user.email@example.com",
  "status": "PENDING",
  "rejectionReason": null,
  "transactionId": "tx_debit_7766",
  "createdAt": "2026-10-29T10:10:00Z",
  "reviewedAt": null,
  "reviewedBy": null
}
```
- **Status Lifecycle:** `PENDING` -> `APPROVED` -> `FULFILLED` or `REJECTED`.

---

### `admin_audit_logs`
Immutable record of administrative actions.
```json
{
  "id": "audit_8899",
  "adminUserId": "admin_uuid",
  "action": "REDEMPTION_APPROVE",
  "targetEntity": "redemptions",
  "targetId": "red_554433",
  "delta": {"status": {"from": "PENDING", "to": "APPROVED"}},
  "ipAddress": "192.168.1.1",
  "timestamp": "2026-10-29T10:15:00Z"
}
```
