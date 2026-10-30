# QuizEarn — API Reference & Contracts

> REST / RPC contracts for communication between Android client, Backend, and Admin Panel.

---

## 1. Global Conventions

### Base URL
- Production: `https://api.quizearn.com/v1` (or Firebase Functions equivalent)
- Development: `http://10.0.2.2:5001/quizearn/v1` (Android Emulator localhost)

### Standard Headers
```http
Authorization: Bearer <JWT_ID_TOKEN>
Content-Type: application/json
Accept: application/json
X-Client-Version: 1.0.0
X-Platform: Android
Idempotency-Key: <UUID> (Mandatory on state-mutating requests: POST, PUT, DELETE)
```

### Standard Response Envelope
```json
{
  "success": true,
  "data": {},
  "error": null,
  "timestamp": "2026-10-29T10:00:00Z"
}
```

### Standard Error Envelope
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "INSUFFICIENT_BALANCE",
    "message": "User does not have enough points for this redemption.",
    "details": []
  },
  "timestamp": "2026-10-29T10:00:00Z"
}
```

---

## 2. Authentication & User Profile

### `POST /auth/register`
Create a new user account.
- **Request Body:** `{ "email": "string", "password": "string", "username": "string" }`
- **Response:** `{ "token": "jwt_token", "user": { "id": "uuid", "email": "string", "pointsBalance": 0 } }`

### `POST /auth/login`
Authenticate existing user.
- **Request Body:** `{ "email": "string", "password": "string" }`
- **Response:** `{ "token": "jwt_token", "user": { ... } }`

### `GET /user/profile`
Retrieve authenticated user profile and stats.
- **Headers:** `Authorization: Bearer <token>`
- **Response:** User profile object including points, rank, and statistics.

---

## 3. Quiz Gameplay & Server-Authoritative Scoring

### `POST /quizzes/sessions/start`
Initiate a new quiz attempt.
- **Headers:** `Authorization: Bearer <token>`, `Idempotency-Key`
- **Request Body:** `{ "categoryId": "cat_science" }`
- **Response:**
```json
{
  "sessionId": "sess_987654",
  "categoryId": "cat_science",
  "timeLimitSeconds": 90,
  "questions": [
    {
      "id": "q_1001",
      "text": "What planet is known as the Red Planet?",
      "options": ["Venus", "Mars", "Jupiter", "Saturn"],
      "timeLimitSeconds": 15
    }
  ]
}
```
*(Notice: No correct answers or explanations are returned).*

### `POST /quizzes/sessions/{sessionId}/submit`
Submit player answers for server-side evaluation.
- **Headers:** `Authorization: Bearer <token>`, `Idempotency-Key`
- **Request Body:**
```json
{
  "answers": [
    {"questionId": "q_1001", "selectedOption": 1, "timeSpentSeconds": 4.2}
  ]
}
```
- **Response:**
```json
{
  "sessionId": "sess_987654",
  "score": 5,
  "total": 5,
  "pointsAwarded": 50,
  "newPointsBalance": 1550,
  "review": [
    {
      "questionId": "q_1001",
      "selectedOption": 1,
      "correctOption": 1,
      "isCorrect": true,
      "explanation": "Mars appears red due to iron oxide on its surface."
    }
  ]
}
```

---

## 4. Wallet & Rewards

### `GET /wallet/balance`
Retrieve current points balance and summary.
- **Response:** `{ "balance": 1550, "pendingRedemptionsPoints": 0, "lifetimeEarned": 2200 }`

### `GET /wallet/transactions`
Retrieve paginated transaction ledger.
- **Query Params:** `?page=1&limit=20&type=ALL`
- **Response:** List of transaction objects.

### `GET /rewards`
Retrieve list of active rewards in the catalog.
- **Response:** List of reward items with points cost and availability.

### `POST /rewards/redeem`
Request redemption of points for a reward.
- **Headers:** `Authorization: Bearer <token>`, `Idempotency-Key`
- **Request Body:** `{ "rewardId": "rew_amazon_5", "payoutDetails": "user@example.com" }`
- **Response:**
```json
{
  "redemptionId": "red_554433",
  "status": "PENDING",
  "pointsDeducted": 5000,
  "remainingBalance": 200
}
```

---

## 5. Administrative Endpoints (RBAC: Admin Role Required)

- `GET /admin/stats` — High-level KPI metrics (active users, quizzes played, pending redemptions).
- `GET /admin/redemptions` — Queue of pending redemption requests.
- `POST /admin/redemptions/{id}/review` — Approve or reject redemption (`{ "action": "APPROVE" | "REJECT", "reason": "..." }`).
- `POST /admin/questions` — Create new quiz question.
- `PUT /admin/questions/{id}` — Edit existing question.
- `POST /admin/questions/bulk-import` — Upload CSV/JSON question bank.
- `GET /admin/audit-logs` — Immutable log of administrator actions.
