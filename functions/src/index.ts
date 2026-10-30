import { randomInt, randomUUID } from "node:crypto";
import { initializeApp } from "firebase-admin/app";
import { FieldValue, getFirestore } from "firebase-admin/firestore";
import { HttpsError, onCall, type CallableRequest } from "firebase-functions/v2/https";

initializeApp();
const db = getFirestore();

const WELCOME_POINTS = 100;
const QUIZ_POINTS_PER_CORRECT = 10;
const MIN_REDEMPTION_POINTS = 5_000;
const QUIZ_QUESTION_COUNT = 10;
const QUIZ_SECONDS_PER_QUESTION = 15;
const QUIZ_GRACE_SECONDS = 15;

function requireUid(request: CallableRequest<unknown>): string {
  const uid = request.auth?.uid;
  if (!uid) throw new HttpsError("unauthenticated", "Sign-in is required.");
  return uid;
}

function requireAdmin(request: CallableRequest<unknown>): string {
  const uid = requireUid(request);
  const token = request.auth?.token;
  if (token?.admin !== true && token?.role !== "SUPER_ADMIN" && token?.role !== "ADMIN") {
    throw new HttpsError("permission-denied", "Administrator access is required.");
  }
  return uid;
}

type AdminRole = "SUPER_ADMIN" | "CONTENT_MANAGER" | "SUPPORT_AGENT";
type AdminPermission =
  | "MANAGE_QUESTIONS"
  | "MANAGE_CATEGORIES"
  | "MANAGE_TASKS"
  | "MANAGE_TOURNAMENTS"
  | "MANAGE_REWARDS"
  | "VIEW_USER_PROFILES"
  | "REVIEW_REDEMPTIONS"
  | "APPROVE_REDEMPTION"
  | "REJECT_REDEMPTION";

const ROLE_PERMISSIONS: Record<AdminRole, readonly AdminPermission[]> = {
  SUPER_ADMIN: [
    "MANAGE_QUESTIONS", "MANAGE_CATEGORIES", "MANAGE_TASKS", "MANAGE_TOURNAMENTS",
    "MANAGE_REWARDS", "VIEW_USER_PROFILES", "REVIEW_REDEMPTIONS",
    "APPROVE_REDEMPTION", "REJECT_REDEMPTION"
  ],
  CONTENT_MANAGER: [
    "MANAGE_QUESTIONS", "MANAGE_CATEGORIES", "MANAGE_TASKS",
    "MANAGE_TOURNAMENTS", "MANAGE_REWARDS"
  ],
  SUPPORT_AGENT: ["VIEW_USER_PROFILES", "REVIEW_REDEMPTIONS", "APPROVE_REDEMPTION", "REJECT_REDEMPTION"]
};

function requirePermission(request: CallableRequest<unknown>, permission: AdminPermission): string {
  const uid = requireUid(request);
  const token = request.auth?.token;
  const role = token?.admin === true ? "SUPER_ADMIN" : token?.role;
  if (typeof role !== "string" ||
      !(role in ROLE_PERMISSIONS) ||
      !ROLE_PERMISSIONS[role as AdminRole].includes(permission)) {
    throw new HttpsError("permission-denied", `Permission '${permission}' is required.`);
  }
  return uid;
}

function requiredString(value: unknown, name: string, maxLength = 200): string {
  if (typeof value !== "string" || value.trim().length === 0 || value.length > maxLength) {
    throw new HttpsError("invalid-argument", `${name} is invalid.`);
  }
  return value.trim();
}

function requiredDocumentId(value: unknown, name = "id"): string {
  const id = requiredString(value, name, 120);
  if (!/^[A-Za-z0-9_-]+$/.test(id)) {
    throw new HttpsError("invalid-argument", `${name} contains unsupported characters.`);
  }
  return id;
}

function requiredInteger(value: unknown, name: string, min: number, max = Number.MAX_SAFE_INTEGER): number {
  if (!Number.isSafeInteger(value) || Number(value) < min || Number(value) > max) {
    throw new HttpsError("invalid-argument", `${name} is invalid.`);
  }
  return Number(value);
}

function requiredRecord(value: unknown, name = "record"): Record<string, unknown> {
  if (!value || typeof value !== "object" || Array.isArray(value)) {
    throw new HttpsError("invalid-argument", `${name} is invalid.`);
  }
  return value as Record<string, unknown>;
}

function idempotencyKey(value: unknown): string {
  const key = requiredString(value, "idempotencyKey", 128);
  if (!/^[A-Za-z0-9_-]+$/.test(key)) {
    throw new HttpsError("invalid-argument", "idempotencyKey contains unsupported characters.");
  }
  return key;
}

function taskPeriodKey(type: unknown, date = new Date()): string {
  const day = date.toISOString().slice(0, 10);
  if (type === "DAILY") return day;
  if (type !== "WEEKLY") throw new HttpsError("failed-precondition", "Task period is invalid.");
  const monday = new Date(`${day}T00:00:00.000Z`);
  const daysSinceMonday = (monday.getUTCDay() + 6) % 7;
  monday.setUTCDate(monday.getUTCDate() - daysSinceMonday);
  return monday.toISOString().slice(0, 10);
}

function profileReference(uid: string) {
  return db.collection("users").doc(uid);
}

function transactionReference(uid: string, key: string) {
  return profileReference(uid).collection("walletTransactions").doc(key);
}

function transactionRecord(
  uid: string,
  key: string,
  type: "CREDIT" | "DEBIT",
  amount: number,
  balanceAfter: number,
  reason: string,
  referenceId: string | null,
  description: string
) {
  return {
    userId: uid,
    type,
    amount,
    balanceAfter,
    reason,
    referenceId,
    description,
    timestampIso: new Date().toISOString(),
    idempotencyKey: key
  };
}

function redemptionResult(id: string, data: FirebaseFirestore.DocumentData) {
  return {
    id,
    userId: data.userId as string,
    rewardId: data.rewardId as string,
    rewardTitle: data.rewardTitle as string,
    pointsDeducted: data.pointsDeducted as number,
    payoutDetails: data.payoutDetails as string,
    status: data.status as string,
    rejectionReason: (data.rejectionReason as string | null) ?? null,
    digitalCode: (data.digitalCode as string | null) ?? null,
    createdAtIso: data.createdAtIso as string,
    processedAtIso: (data.processedAtIso as string | null) ?? null
  };
}

export const ensureProfile = onCall(async (request) => {
  const uid = requireUid(request);
  const rawUsername = typeof request.data?.username === "string"
    ? request.data.username.trim().slice(0, 40)
    : "";
  const email = typeof request.auth?.token.email === "string" ? request.auth.token.email : "";
  const emailUsername = email.split("@")[0] ?? "";
  const usernameHint = rawUsername.length >= 3
    ? rawUsername
    : emailUsername.length >= 3 ? emailUsername : "Player";
  const userRef = profileReference(uid);
  const welcomeKey = "welcome_bonus_v1";
  await db.runTransaction(async (tx) => {
    const snapshot = await tx.get(userRef);
    const existing = snapshot.data();
    const firebaseClaims = request.auth?.token.firebase as { sign_in_provider?: string } | undefined;
    const isPermanentAccount = firebaseClaims?.sign_in_provider !== "anonymous";
    if (!snapshot.exists) {
      const pointsBalance = isPermanentAccount ? WELCOME_POINTS : 0;
      tx.create(userRef, {
        email,
        username: usernameHint,
        pointsBalance,
        level: 1,
        totalQuizzesPlayed: 0,
        accuracyRate: 0,
        role: "USER",
        avatarColorHex: 0xff5b5fef,
        welcomeAwarded: isPermanentAccount,
        createdAt: FieldValue.serverTimestamp(),
        updatedAt: FieldValue.serverTimestamp()
      });
      if (pointsBalance > 0) {
        tx.create(transactionReference(uid, welcomeKey), transactionRecord(
          uid, welcomeKey, "CREDIT", pointsBalance, pointsBalance,
          "WELCOME_BONUS", null, "Welcome bonus"
        ));
      }
    } else if (isPermanentAccount && existing?.welcomeAwarded !== true) {
      const balance = Number(existing?.pointsBalance ?? 0);
      if (!Number.isSafeInteger(balance) || balance < 0 || balance > Number.MAX_SAFE_INTEGER - WELCOME_POINTS) {
        throw new HttpsError("failed-precondition", "Account balance is invalid.");
      }
      const newBalance = balance + WELCOME_POINTS;
      tx.update(userRef, {
        email,
        welcomeAwarded: true,
        pointsBalance: newBalance,
        updatedAt: FieldValue.serverTimestamp()
      });
      tx.create(transactionReference(uid, welcomeKey), transactionRecord(
        uid, welcomeKey, "CREDIT", WELCOME_POINTS, newBalance,
        "WELCOME_BONUS", null, "Welcome bonus"
      ));
    }
  });
  const profile = (await userRef.get()).data();
  if (!profile) throw new HttpsError("internal", "Profile could not be loaded.");
  return {
    username: profile.username,
    pointsBalance: profile.pointsBalance,
    level: profile.level,
    totalQuizzesPlayed: profile.totalQuizzesPlayed,
    accuracyRate: profile.accuracyRate
  };
});

export const updateProfile = onCall(async (request) => {
  const uid = requireUid(request);
  const username = requiredString(request.data?.username, "username", 40);
  if (username.length < 3) throw new HttpsError("invalid-argument", "Username must be at least 3 characters.");
  const avatarColorHex = request.data?.avatarColorHex;
  if (!Number.isSafeInteger(avatarColorHex) || avatarColorHex < 0 || avatarColorHex > 0xffffffff) {
    throw new HttpsError("invalid-argument", "Avatar color is invalid.");
  }
  const userRef = profileReference(uid);
  await userRef.update({ username, avatarColorHex, updatedAt: FieldValue.serverTimestamp() });
  return { ok: true };
});

export const startQuizSession = onCall(async (request) => {
  const uid = requireUid(request);
  const categoryId = requiredString(request.data?.categoryId, "categoryId", 80);
  const questionSnapshot = await db.collection("questionBank")
    .where("categoryId", "==", categoryId)
    .where("active", "==", true)
    .limit(100)
    .get();
  const available = questionSnapshot.docs.flatMap((document) => {
    const data = document.data();
    if (
      typeof data.text !== "string" ||
      !Array.isArray(data.options) ||
      data.options.length < 2 ||
      !data.options.every((option: unknown) => typeof option === "string") ||
      !Number.isInteger(data.correctOptionIndex) ||
      data.correctOptionIndex < 0 ||
      data.correctOptionIndex >= data.options.length
    ) return [];
    return [{
      id: document.id,
      categoryId,
      categoryTitle: typeof data.categoryTitle === "string" ? data.categoryTitle : categoryId,
      text: data.text,
      options: data.options as string[],
      correctOptionIndex: data.correctOptionIndex as number,
      explanationText: typeof data.explanationText === "string" ? data.explanationText : ""
    }];
  });
  if (available.length === 0) {
    throw new HttpsError("failed-precondition", "No valid active questions are configured for this category.");
  }
  for (let i = available.length - 1; i > 0; i--) {
    const j = randomInt(i + 1);
    [available[i], available[j]] = [available[j]!, available[i]!];
  }
  const questions = available.slice(0, Math.min(QUIZ_QUESTION_COUNT, available.length));
  const now = Date.now();
  const expiresAt = now + (questions.length * QUIZ_SECONDS_PER_QUESTION + QUIZ_GRACE_SECONDS) * 1000;
  const sessionId = randomUUID();
  await db.collection("quizSessions").doc(sessionId).create({
    userId: uid,
    categoryId,
    categoryTitle: questions[0]!.categoryTitle,
    questions,
    status: "ACTIVE",
    startedAt: now,
    expiresAt,
    createdAt: FieldValue.serverTimestamp()
  });
  return {
    sessionId,
    userId: uid,
    categoryId,
    startedAt: now,
    expiresAt,
    timeLimitSeconds: questions.length * QUIZ_SECONDS_PER_QUESTION,
    questions: questions.map(({ id, text, options }) => ({
      id,
      categoryId,
      text,
      options,
      timeLimitSeconds: QUIZ_SECONDS_PER_QUESTION,
      pointsReward: QUIZ_POINTS_PER_CORRECT
    }))
  };
});

export const submitQuizAnswers = onCall(async (request) => {
  const uid = requireUid(request);
  const sessionId = requiredString(request.data?.sessionId, "sessionId", 100);
  const key = idempotencyKey(request.data?.idempotencyKey);
  const submittedAnswers = request.data?.answers;
  if (!Array.isArray(submittedAnswers) || submittedAnswers.length > 100) {
    throw new HttpsError("invalid-argument", "Quiz answers are invalid.");
  }
  const sessionRef = db.collection("quizSessions").doc(sessionId);
  const userRef = profileReference(uid);
  const resultRef = userRef.collection("quizResults").doc(sessionId);
  const idempotencyRef = userRef.collection("quizIdempotency").doc(key);

  return db.runTransaction(async (tx) => {
    const [sessionSnapshot, userSnapshot, previousKey, existingResult] = await Promise.all([
      tx.get(sessionRef), tx.get(userRef), tx.get(idempotencyRef), tx.get(resultRef)
    ]);
    if (previousKey.exists) {
      const previousSessionId = previousKey.get("sessionId");
      if (previousSessionId !== sessionId || !existingResult.exists) {
        throw new HttpsError("already-exists", "Idempotency key was already used for a different operation.");
      }
      return existingResult.data();
    }
    if (!sessionSnapshot.exists) throw new HttpsError("not-found", "Quiz session was not found.");
    if (!userSnapshot.exists) throw new HttpsError("failed-precondition", "User profile is missing.");
    const session = sessionSnapshot.data()!;
    if (session.userId !== uid) throw new HttpsError("permission-denied", "Quiz session belongs to another user.");
    if (session.status !== "ACTIVE") throw new HttpsError("failed-precondition", "Quiz session is not active.");
    const now = Date.now();
    if (now > Number(session.expiresAt)) throw new HttpsError("deadline-exceeded", "Quiz session expired.");
    const questions = session.questions as Array<{
      id: string;
      text: string;
      options: string[];
      correctOptionIndex: number;
      explanationText: string;
    }>;
    const byId = new Map(questions.map((question) => [question.id, question]));
    const answerMap = new Map<string, number>();
    for (const answer of submittedAnswers as Array<{ questionId?: unknown; selectedOption?: unknown }>) {
      const questionId = requiredString(answer?.questionId, "questionId", 100);
      const question = byId.get(questionId);
      if (!question || answerMap.has(questionId)) {
        throw new HttpsError("invalid-argument", "Quiz contains an unknown or duplicate answer.");
      }
      if (!Number.isInteger(answer.selectedOption) ||
          Number(answer.selectedOption) < -1 ||
          Number(answer.selectedOption) >= question.options.length) {
        throw new HttpsError("invalid-argument", "A selected answer is invalid.");
      }
      answerMap.set(questionId, Number(answer.selectedOption));
    }
    const correctCount = questions.reduce(
      (count, question) => count + (answerMap.get(question.id) === question.correctOptionIndex ? 1 : 0),
      0
    );
    const earned = correctCount * QUIZ_POINTS_PER_CORRECT;
    const accuracy = questions.length > 0 ? correctCount * 100 / questions.length : 0;
    const utcDay = new Date(now).toISOString().slice(0, 10);
    const todayRef = userRef.collection("quizDays").doc(utcDay);
    const yesterday = new Date(`${utcDay}T00:00:00.000Z`);
    yesterday.setUTCDate(yesterday.getUTCDate() - 1);
    const yesterdayRef = userRef.collection("quizDays").doc(yesterday.toISOString().slice(0, 10));
    const [todaySnapshot, yesterdaySnapshot, configuredTasks] = await Promise.all([
      tx.get(todayRef),
      tx.get(yesterdayRef),
      tx.get(db.collection("tasks").where("isActive", "==", true).limit(100))
    ]);
    const taskProgressRefs = configuredTasks.docs.map((task) =>
      userRef.collection("taskProgress").doc(`${taskPeriodKey(task.get("type"), new Date(now))}_${task.id}`)
    );
    const taskProgressSnapshots = await Promise.all(taskProgressRefs.map((ref) => tx.get(ref)));
    const currentStreak = Number(userSnapshot.get("streakDays") ?? 0);
    const streakDays = todaySnapshot.exists
      ? currentStreak
      : yesterdaySnapshot.exists ? currentStreak + 1 : 1;
    const oldBalance = Number(userSnapshot.get("pointsBalance") ?? 0);
    if (!Number.isSafeInteger(oldBalance) || oldBalance < 0 || oldBalance > Number.MAX_SAFE_INTEGER - earned) {
      throw new HttpsError("failed-precondition", "Wallet balance is invalid or would overflow.");
    }
    const newBalance = oldBalance + earned;
    const oldQuizCount = Number(userSnapshot.get("totalQuizzesPlayed") ?? 0);
    const oldAccuracy = Number(userSnapshot.get("accuracyRate") ?? 0);
    const totalQuizzesPlayed = oldQuizCount + 1;
    const accuracyRate = ((oldAccuracy * oldQuizCount) + accuracy) / totalQuizzesPlayed;
    const reviews = questions.map((question, index) => {
      const selected = answerMap.get(question.id) ?? -1;
      return {
        questionNumber: index + 1,
        questionText: question.text,
        options: question.options,
        playerSelectedOptionIndex: selected,
        correctOptionIndex: question.correctOptionIndex,
        isCorrect: selected === question.correctOptionIndex,
        explanationText: question.explanationText
      };
    });
    const result = {
      summary: {
        sessionId,
        categoryTitle: session.categoryTitle,
        score: correctCount * QUIZ_POINTS_PER_CORRECT,
        totalQuestions: questions.length,
        correctCount,
        pointsEarned: earned,
        accuracyRate: accuracy,
        timeElapsedSeconds: Math.max(1, Math.floor((now - Number(session.startedAt)) / 1000))
      },
      categoryId: session.categoryId,
      reviews
    };
    tx.update(userRef, {
      pointsBalance: newBalance,
      totalQuizzesPlayed,
      accuracyRate,
      streakDays,
      updatedAt: FieldValue.serverTimestamp()
    });
    if (!todaySnapshot.exists) {
      tx.create(todayRef, { completedAt: FieldValue.serverTimestamp() });
    }
    configuredTasks.docs.forEach((task, index) => {
      const taskData = task.data();
      const progressSnapshot = taskProgressSnapshots[index]!;
      const target = Number(taskData.targetGoal);
      if (!Number.isSafeInteger(target) || target <= 0) return;
      const existing = Number(progressSnapshot.get("currentProgress") ?? 0);
      if (!Number.isSafeInteger(existing) || existing < 0) return;
      let progress = existing;
      switch (taskData.criterion) {
        case "QUIZ_COMPLETION":
          progress = Math.min(target, existing + 1);
          break;
        case "ACCURACY_SCORE":
          progress = Math.max(existing, Math.min(target, Math.floor(accuracy)));
          break;
        case "STREAK_MAINTAIN":
          progress = Math.max(existing, Math.min(target, streakDays));
          break;
        case "CATEGORY_QUIZ":
          if (taskData.categoryKey === session.categoryId) {
            progress = Math.min(target, existing + earned);
          }
          break;
        default:
          return;
      }
      if (progress === existing && progressSnapshot.exists) return;
      const progressRef = taskProgressRefs[index]!;
      tx.set(progressRef, {
        taskId: task.id,
        periodKey: taskPeriodKey(taskData.type, new Date(now)),
        currentProgress: progress,
        isClaimed: progressSnapshot.get("isClaimed") === true,
        claimedAtTimestamp: progressSnapshot.get("claimedAtTimestamp") ?? null,
        updatedAt: FieldValue.serverTimestamp()
      });
    });
    if (earned > 0) {
      tx.create(transactionReference(uid, `quiz_${sessionId}`), transactionRecord(
        uid,
        `quiz_${sessionId}`,
        "CREDIT",
        earned,
        newBalance,
        "QUIZ_REWARD",
        sessionId,
        `Quiz reward: ${session.categoryTitle}`
      ));
    }
    tx.create(resultRef, { ...result, userId: uid, createdAt: FieldValue.serverTimestamp() });
    tx.create(idempotencyRef, { sessionId, createdAt: FieldValue.serverTimestamp() });
    tx.update(sessionRef, { status: "COMPLETED", completedAt: FieldValue.serverTimestamp() });
    return result;
  });
});

export const getQuizResult = onCall(async (request) => {
  const uid = requireUid(request);
  const sessionId = requiredString(request.data?.sessionId, "sessionId", 100);
  const snapshot = await profileReference(uid).collection("quizResults").doc(sessionId).get();
  if (!snapshot.exists) throw new HttpsError("not-found", "Quiz result was not found.");
  const { userId: _userId, createdAt: _createdAt, ...result } = snapshot.data()!;
  return result;
});

export const getTaskState = onCall(async (request) => {
  const uid = requireUid(request);
  const tasks = await db.collection("tasks").where("isActive", "==", true).limit(100).get();
  const now = new Date();
  const refs = tasks.docs.map((task) =>
    profileReference(uid).collection("taskProgress")
      .doc(`${taskPeriodKey(task.get("type"), now)}_${task.id}`)
  );
  const progress = refs.length === 0 ? [] : await db.getAll(...refs);
  return {
    tasks: tasks.docs.map((task, index) => {
      const state = progress[index];
      return {
        id: task.id,
        title: task.get("title"),
        description: task.get("description"),
        type: task.get("type"),
        criterion: task.get("criterion"),
        targetGoal: task.get("targetGoal"),
        rewardPoints: task.get("rewardPoints"),
        categoryKey: task.get("categoryKey") ?? null,
        periodKey: taskPeriodKey(task.get("type"), now),
        currentProgress: state?.get("currentProgress") ?? 0,
        isClaimed: state?.get("isClaimed") === true,
        claimedAtTimestamp: state?.get("claimedAtTimestamp") ?? null
      };
    })
  };
});

export const claimTaskReward = onCall(async (request) => {
  const uid = requireUid(request);
  const taskId = requiredString(request.data?.taskId, "taskId", 100);
  const key = idempotencyKey(request.data?.idempotencyKey);
  const taskRef = db.collection("tasks").doc(taskId);
  const userRef = profileReference(uid);
  const claimRef = userRef.collection("taskClaims").doc(key);

  return db.runTransaction(async (tx) => {
    const [claimSnapshot, taskSnapshot, userSnapshot] = await Promise.all([
      tx.get(claimRef), tx.get(taskRef), tx.get(userRef)
    ]);
    if (claimSnapshot.exists) {
      if (claimSnapshot.get("taskId") !== taskId) {
        throw new HttpsError("already-exists", "Idempotency key was used for a different task.");
      }
      return {
        taskId,
        pointsEarned: claimSnapshot.get("pointsEarned"),
        balance: claimSnapshot.get("balanceAfter")
      };
    }
    if (!taskSnapshot.exists || taskSnapshot.get("isActive") !== true) {
      throw new HttpsError("failed-precondition", "Task is not available.");
    }
    if (!userSnapshot.exists) throw new HttpsError("failed-precondition", "User profile is missing.");
    const task = taskSnapshot.data()!;
    const reward = Number(task.rewardPoints);
    const target = Number(task.targetGoal);
    if (!Number.isSafeInteger(reward) || reward <= 0 ||
        !Number.isSafeInteger(target) || target <= 0) {
      throw new HttpsError("failed-precondition", "Task reward configuration is invalid.");
    }
    const periodKey = taskPeriodKey(task.type);
    const progressRef = userRef.collection("taskProgress").doc(`${periodKey}_${taskId}`);
    const progressSnapshot = await tx.get(progressRef);
    if (progressSnapshot.get("isClaimed") === true) {
      throw new HttpsError("already-exists", "Task reward has already been claimed.");
    }
    if (Number(progressSnapshot.get("currentProgress") ?? 0) < target) {
      throw new HttpsError("failed-precondition", "Task is not complete.");
    }
    const oldBalance = Number(userSnapshot.get("pointsBalance") ?? 0);
    if (!Number.isSafeInteger(oldBalance) || oldBalance < 0 ||
        oldBalance > Number.MAX_SAFE_INTEGER - reward) {
      throw new HttpsError("failed-precondition", "Wallet balance is invalid or would overflow.");
    }
    const balance = oldBalance + reward;
    const walletKey = `task_${key}`;
    tx.update(userRef, { pointsBalance: balance, updatedAt: FieldValue.serverTimestamp() });
    tx.update(progressRef, {
      isClaimed: true,
      claimedAtTimestamp: Date.now(),
      updatedAt: FieldValue.serverTimestamp()
    });
    tx.create(claimRef, {
      taskId,
      pointsEarned: reward,
      balanceAfter: balance,
      createdAt: FieldValue.serverTimestamp()
    });
    tx.create(transactionReference(uid, walletKey), transactionRecord(
      uid, walletKey, "CREDIT", reward, balance,
      "TASK_COMPLETED", taskId, `Task reward: ${requiredString(task.title, "task.title")}`
    ));
    return { taskId, pointsEarned: reward, balance };
  });
});

export const getTournaments = onCall(async (request) => {
  const uid = requireUid(request);
  const snapshot = await db.collection("tournaments").limit(200).get();
  const activeDocs = snapshot.docs.filter((document) =>
    document.get("status") !== "CANCELLED" && document.get("status") !== "ENDED"
  );
  const participantRefs = activeDocs.map((document) =>
    document.ref.collection("participants").doc(uid)
  );
  const participantDocs = participantRefs.length === 0 ? [] : await db.getAll(...participantRefs);
  return {
    tournaments: activeDocs.map((document, index) => ({
        id: document.id,
        ...document.data(),
        isRegistered: participantDocs[index]?.exists === true
      }))
    };
});

export const registerForTournament = onCall(async (request) => {
  const uid = requireUid(request);
  const tournamentId = requiredString(request.data?.tournamentId, "tournamentId", 100);
  const key = idempotencyKey(request.data?.idempotencyKey);
  const tournamentRef = db.collection("tournaments").doc(tournamentId);
  const userRef = profileReference(uid);
  const participantRef = tournamentRef.collection("participants").doc(uid);
  const idempotencyRef = userRef.collection("tournamentRegistrationKeys").doc(key);
  return db.runTransaction(async (tx) => {
    const [idempotencySnapshot, tournamentSnapshot, userSnapshot, participantSnapshot] = await Promise.all([
      tx.get(idempotencyRef), tx.get(tournamentRef), tx.get(userRef), tx.get(participantRef)
    ]);
    if (idempotencySnapshot.exists && idempotencySnapshot.get("tournamentId") !== tournamentId) {
      throw new HttpsError("already-exists", "Idempotency key was used for a different tournament.");
    }
    if (!tournamentSnapshot.exists) throw new HttpsError("not-found", "Tournament was not found.");
    if (!userSnapshot.exists) throw new HttpsError("failed-precondition", "User profile is missing.");
    if (participantSnapshot.exists) {
      if (!idempotencySnapshot.exists) {
        tx.create(idempotencyRef, { tournamentId, createdAt: FieldValue.serverTimestamp() });
      }
      return { tournamentId, alreadyRegistered: true };
    }
    const tournament = tournamentSnapshot.data()!;
    const now = Date.now();
    const start = Date.parse(requiredString(tournament.startTimeIso, "tournament.startTimeIso", 40));
    const end = Date.parse(requiredString(tournament.endTimeIso, "tournament.endTimeIso", 40));
    const status = tournament.status;
    if (!Number.isFinite(start) || !Number.isFinite(end) || end <= start ||
        now < start || now >= end ||
        (status !== "LIVE" && status !== "SCHEDULED")) {
      throw new HttpsError("failed-precondition", "Tournament registration is closed.");
    }
    const registered = Number(tournament.registeredParticipants ?? 0);
    const collectedFees = Number(tournament.collectedEntryFees ?? 0);
    const maxParticipants = tournament.maxParticipants == null ? null : Number(tournament.maxParticipants);
    if (!Number.isSafeInteger(registered) || registered < 0 ||
        (maxParticipants !== null && (!Number.isSafeInteger(maxParticipants) || registered >= maxParticipants))) {
      throw new HttpsError("resource-exhausted", "Tournament is full or registration data is invalid.");
    }
    const level = Number(userSnapshot.get("level") ?? 1);
    const requiredLevel = Number(tournament.minLevelRequired ?? 1);
    const fee = Number(tournament.entryFeePoints ?? 0);
    const balance = Number(userSnapshot.get("pointsBalance") ?? 0);
    if (!Number.isSafeInteger(level) || !Number.isSafeInteger(requiredLevel) ||
        level < requiredLevel || !Number.isSafeInteger(fee) || fee < 0) {
      throw new HttpsError("failed-precondition", "Tournament eligibility or fee configuration is invalid.");
    }
    if (!Number.isSafeInteger(collectedFees) || collectedFees < 0 ||
        collectedFees > Number.MAX_SAFE_INTEGER - fee) {
      throw new HttpsError("failed-precondition", "Tournament entry-fee accounting is invalid.");
    }
    if (!Number.isSafeInteger(balance) || balance < fee) {
      throw new HttpsError("failed-precondition", "Insufficient points balance.");
    }
    const newBalance = balance - fee;
    const walletKey = `tournament_entry_${tournamentId}`;
    tx.update(tournamentRef, {
      registeredParticipants: registered + 1,
      collectedEntryFees: collectedFees + fee,
      updatedAt: FieldValue.serverTimestamp()
    });
    tx.create(participantRef, {
      userId: uid,
      username: userSnapshot.get("username") ?? "Player",
      entryFeePoints: fee,
      registeredAt: FieldValue.serverTimestamp(),
      score: null
    });
    tx.update(userRef, { pointsBalance: newBalance, updatedAt: FieldValue.serverTimestamp() });
    if (fee > 0) {
      tx.create(transactionReference(uid, walletKey), transactionRecord(
        uid, walletKey, "DEBIT", fee, newBalance,
        "TOURNAMENT_ENTRY_FEE", tournamentId, `Tournament entry: ${tournament.title}`
      ));
    }
    if (!idempotencySnapshot.exists) {
      tx.create(idempotencyRef, { tournamentId, createdAt: FieldValue.serverTimestamp() });
    }
    return { tournamentId, alreadyRegistered: false };
  });
});

export const submitTournamentResult = onCall(async (request) => {
  const uid = requireUid(request);
  const tournamentId = requiredString(request.data?.tournamentId, "tournamentId", 100);
  const sessionId = requiredString(request.data?.sessionId, "sessionId", 100);
  const key = idempotencyKey(request.data?.idempotencyKey);
  const tournamentRef = db.collection("tournaments").doc(tournamentId);
  const participantRef = tournamentRef.collection("participants").doc(uid);
  const userRef = profileReference(uid);
  const quizResultRef = userRef.collection("quizResults").doc(sessionId);
  const resultUseRef = userRef.collection("tournamentResultUses").doc(sessionId);
  const idempotencyRef = userRef.collection("tournamentResultKeys").doc(key);

  return db.runTransaction(async (tx) => {
    const [tournamentSnapshot, participantSnapshot, resultSnapshot, useSnapshot, keySnapshot] =
      await Promise.all([
        tx.get(tournamentRef), tx.get(participantRef), tx.get(quizResultRef),
        tx.get(resultUseRef), tx.get(idempotencyRef)
      ]);
    if (keySnapshot.exists) {
      if (keySnapshot.get("tournamentId") !== tournamentId || keySnapshot.get("sessionId") !== sessionId) {
        throw new HttpsError("already-exists", "Idempotency key was used for a different result.");
      }
      return participantSnapshot.data();
    }
    if (!tournamentSnapshot.exists || !participantSnapshot.exists || !resultSnapshot.exists) {
      throw new HttpsError("failed-precondition", "Tournament registration or verified quiz result is missing.");
    }
    if (useSnapshot.exists && useSnapshot.get("tournamentId") !== tournamentId) {
      throw new HttpsError("already-exists", "Quiz result was already used in another tournament.");
    }
    const tournament = tournamentSnapshot.data()!;
    const now = Date.now();
    const start = Date.parse(requiredString(tournament.startTimeIso, "tournament.startTimeIso", 40));
    const end = Date.parse(requiredString(tournament.endTimeIso, "tournament.endTimeIso", 40));
    const result = resultSnapshot.data()!;
    const resultTimestamp = result.createdAt as FirebaseFirestore.Timestamp | undefined;
    const summary = result.summary as Record<string, unknown> | undefined;
    if (tournament.status !== "LIVE" || now < start || now >= end ||
        result.categoryId !== tournament.categoryId || !resultTimestamp ||
        resultTimestamp.toMillis() < start || !summary) {
      throw new HttpsError("failed-precondition", "Verified quiz result is not eligible for this tournament.");
    }
    const score = Number(summary.score);
    const accuracyRate = Number(summary.accuracyRate);
    const timeElapsedSeconds = Number(summary.timeElapsedSeconds);
    if (!Number.isSafeInteger(score) || score < 0 || !Number.isFinite(accuracyRate) ||
        accuracyRate < 0 || accuracyRate > 100 ||
        !Number.isSafeInteger(timeElapsedSeconds) || timeElapsedSeconds <= 0) {
      throw new HttpsError("failed-precondition", "Verified quiz result is invalid.");
    }
    const previous = participantSnapshot.data()!;
    const improvesResult = previous.score == null ||
      score > Number(previous.score) ||
      (score === Number(previous.score) && accuracyRate > Number(previous.accuracyRate)) ||
      (score === Number(previous.score) && accuracyRate === Number(previous.accuracyRate) &&
        timeElapsedSeconds < Number(previous.timeElapsedSeconds));
    const participant = improvesResult ? {
      userId: uid,
      username: previous.username,
      score,
      accuracyRate,
      timeElapsedSeconds,
      completedAtTimestamp: resultTimestamp.toMillis()
    } : previous;
    if (improvesResult) tx.update(participantRef, participant);
    if (!useSnapshot.exists) {
      tx.create(resultUseRef, { tournamentId, createdAt: FieldValue.serverTimestamp() });
    }
    tx.create(idempotencyRef, { tournamentId, sessionId, createdAt: FieldValue.serverTimestamp() });
    return participant;
  });
});

export const getTournamentLeaderboard = onCall(async (request) => {
  requireUid(request);
  const tournamentId = requiredString(request.data?.tournamentId, "tournamentId", 100);
  const tournamentRef = db.collection("tournaments").doc(tournamentId);
  const snapshot = await tournamentRef.collection("participants").limit(500).get();
  const participants = snapshot.docs
    .map((document) => document.data())
    .filter((participant) => Number.isSafeInteger(participant.score))
    .sort((left, right) =>
      Number(right.score) - Number(left.score) ||
      Number(right.accuracyRate) - Number(left.accuracyRate) ||
      Number(left.timeElapsedSeconds) - Number(right.timeElapsedSeconds) ||
      Number(left.completedAtTimestamp) - Number(right.completedAtTimestamp)
    )
    .map((participant, index) => ({ ...participant, rank: index + 1 }));
  return { participants };
});

export const finalizeTournament = onCall(async (request) => {
  const adminUid = requirePermission(request, "MANAGE_TOURNAMENTS");
  const tournamentId = requiredDocumentId(request.data?.tournamentId, "tournamentId");
  const tournamentRef = db.collection("tournaments").doc(tournamentId);
  const finalizationRef = db.collection("tournamentFinalizations").doc(tournamentId);
  return db.runTransaction(async (tx) => {
    const [tournamentSnapshot, finalizationSnapshot, participantSnapshot] = await Promise.all([
      tx.get(tournamentRef),
      tx.get(finalizationRef),
      tx.get(tournamentRef.collection("participants").limit(300))
    ]);
    if (!tournamentSnapshot.exists) throw new HttpsError("not-found", "Tournament was not found.");
    if (finalizationSnapshot.exists) {
      return { tournamentId, alreadyFinalized: true, awards: finalizationSnapshot.get("awards") ?? [] };
    }
    const tournament = tournamentSnapshot.data()!;
    const end = Date.parse(requiredString(tournament.endTimeIso, "tournament.endTimeIso", 40));
    if (!["SCHEDULED", "LIVE", "ENDED"].includes(String(tournament.status)) ||
        !Number.isFinite(end) || Date.now() < end) {
      throw new HttpsError("failed-precondition", "Tournament cannot be finalized before its scheduled end.");
    }
    if (participantSnapshot.size >= 300) {
      throw new HttpsError("resource-exhausted", "Tournament participant limit exceeds the supported finalization batch.");
    }
    const ranked = participantSnapshot.docs
      .map((document) => ({ ref: document.ref, data: document.data() }))
      .filter(({ data }) => Number.isSafeInteger(data.score))
      .sort((left, right) =>
        Number(right.data.score) - Number(left.data.score) ||
        Number(right.data.accuracyRate) - Number(left.data.accuracyRate) ||
        Number(left.data.timeElapsedSeconds) - Number(right.data.timeElapsedSeconds) ||
        Number(left.data.completedAtTimestamp) - Number(right.data.completedAtTimestamp)
      );
    const fundedPoints = Number(tournament.collectedEntryFees ?? 0);
    const configuredPool = Number(tournament.prizePoolPoints ?? 0);
    if (!Number.isSafeInteger(fundedPoints) || fundedPoints < 0 ||
        !Number.isSafeInteger(configuredPool) || configuredPool < 0) {
      throw new HttpsError("failed-precondition", "Tournament funding data is invalid.");
    }
    const prizePool = Math.min(fundedPoints, configuredPool);
    const percentages = [50, 30, 20];
    const podium = ranked.slice(0, percentages.length);
    const awards = podium.map(({ data }, index) => ({
      userId: data.userId as string,
      rank: index + 1,
      amount: Math.floor((prizePool * percentages[index]!) / 100)
    })).filter((award) => award.amount > 0);
    const userRefs = awards.map((award) => profileReference(award.userId));
    const userSnapshots = userRefs.length === 0 ? [] : await Promise.all(userRefs.map((ref) => tx.get(ref)));
    const walletRefs = awards.map((award) =>
      transactionReference(award.userId, `tournament_prize_${tournamentId}_${award.rank}`)
    );
    const walletSnapshots = walletRefs.length === 0 ? [] : await Promise.all(walletRefs.map((ref) => tx.get(ref)));
    for (let index = 0; index < awards.length; index += 1) {
      const award = awards[index]!;
      const userSnapshot = userSnapshots[index]!;
      const walletSnapshot = walletSnapshots[index]!;
      if (!userSnapshot.exists || walletSnapshot.exists) {
        throw new HttpsError("failed-precondition", "Tournament award data is inconsistent.");
      }
      const balance = Number(userSnapshot.get("pointsBalance") ?? 0);
      if (!Number.isSafeInteger(balance) || balance < 0 ||
          balance > Number.MAX_SAFE_INTEGER - award.amount) {
        throw new HttpsError("failed-precondition", "Tournament award would overflow a wallet.");
      }
    }

    awards.forEach((award, index) => {
      const userRef = userRefs[index]!;
      const walletRef = walletRefs[index]!;
      const balance = Number(userSnapshots[index]!.get("pointsBalance") ?? 0);
      const newBalance = balance + award.amount;
      tx.update(userRef, { pointsBalance: newBalance, updatedAt: FieldValue.serverTimestamp() });
      tx.create(walletRef, transactionRecord(
        award.userId,
        `tournament_prize_${tournamentId}_${award.rank}`,
        "CREDIT",
        award.amount,
        newBalance,
        "TOURNAMENT_PRIZE",
        tournamentId,
        `Tournament prize: ${tournament.title} (Rank #${award.rank})`
      ));
    });
    ranked.forEach(({ ref }, index) => tx.update(ref, { rank: index + 1 }));
    tx.update(tournamentRef, {
      status: "COMPLETED",
      finalizedAt: FieldValue.serverTimestamp(),
      finalizedBy: adminUid,
      awardedPoints: awards.reduce((total, award) => total + award.amount, 0),
      updatedAt: FieldValue.serverTimestamp()
    });
    tx.create(finalizationRef, {
      tournamentId,
      awards,
      finalizedBy: adminUid,
      createdAt: FieldValue.serverTimestamp()
    });
    return { tournamentId, alreadyFinalized: false, awards };
  });
});

export const cancelTournament = onCall(async (request) => {
  const adminUid = requirePermission(request, "MANAGE_TOURNAMENTS");
  const tournamentId = requiredDocumentId(request.data?.tournamentId, "tournamentId");
  const reason = requiredString(request.data?.reason, "reason", 500);
  const tournamentRef = db.collection("tournaments").doc(tournamentId);
  return db.runTransaction(async (tx) => {
    const [tournamentSnapshot, participantSnapshot] = await Promise.all([
      tx.get(tournamentRef),
      tx.get(tournamentRef.collection("participants").limit(300))
    ]);
    if (!tournamentSnapshot.exists) throw new HttpsError("not-found", "Tournament was not found.");
    if (tournamentSnapshot.get("status") === "CANCELLED") {
      return { tournamentId, alreadyCancelled: true };
    }
    if (tournamentSnapshot.get("status") === "COMPLETED") {
      throw new HttpsError("failed-precondition", "Completed tournament cannot be cancelled.");
    }
    if (participantSnapshot.size >= 300) {
      throw new HttpsError("resource-exhausted", "Tournament participant limit exceeds the supported refund batch.");
    }
    const participants = participantSnapshot.docs.filter((document) =>
      Number(document.get("entryFeePoints") ?? 0) > 0
    );
    const userRefs = participants.map((participant) => profileReference(participant.get("userId")));
    const userSnapshots = userRefs.length === 0 ? [] : await Promise.all(userRefs.map((ref) => tx.get(ref)));
    const refundRefs = participants.map((participant) =>
      transactionReference(participant.get("userId"), `tournament_refund_${tournamentId}`)
    );
    const refundSnapshots = refundRefs.length === 0 ? [] : await Promise.all(refundRefs.map((ref) => tx.get(ref)));
    for (let index = 0; index < participants.length; index += 1) {
      const amount = Number(participants[index]!.get("entryFeePoints"));
      const balance = Number(userSnapshots[index]?.get("pointsBalance") ?? 0);
      if (!userSnapshots[index]?.exists || refundSnapshots[index]?.exists ||
          !Number.isSafeInteger(amount) || !Number.isSafeInteger(balance) || balance < 0 ||
          balance > Number.MAX_SAFE_INTEGER - amount) {
        throw new HttpsError("failed-precondition", "Tournament refund data is inconsistent.");
      }
    }
    participants.forEach((participant, index) => {
      const userId = participant.get("userId") as string;
      const amount = Number(participant.get("entryFeePoints"));
      const balance = Number(userSnapshots[index]!.get("pointsBalance"));
      const newBalance = balance + amount;
      tx.update(userRefs[index]!, { pointsBalance: newBalance, updatedAt: FieldValue.serverTimestamp() });
      tx.create(refundRefs[index]!, transactionRecord(
        userId,
        `tournament_refund_${tournamentId}`,
        "CREDIT",
        amount,
        newBalance,
        "TOURNAMENT_ENTRY_REFUND",
        tournamentId,
        `Tournament cancellation refund: ${reason}`
      ));
    });
    tx.update(tournamentRef, {
      status: "CANCELLED",
      cancellationReason: reason,
      cancelledAtIso: new Date().toISOString(),
      totalRefundedPoints: participants.reduce(
        (total, participant) => total + Number(participant.get("entryFeePoints")),
        0
      ),
      cancelledBy: adminUid,
      updatedAt: FieldValue.serverTimestamp()
    });
    return { tournamentId, alreadyCancelled: false };
  });
});

export const requestRedemption = onCall(async (request) => {
  const uid = requireUid(request);
  const rewardId = requiredString(request.data?.rewardId, "rewardId", 100);
  const payoutDetails = requiredString(request.data?.payoutDetails, "payoutDetails", 500);
  const key = idempotencyKey(request.data?.idempotencyKey);
  const userRef = profileReference(uid);
  const rewardRef = db.collection("rewards").doc(rewardId);
  const redemptionRef = userRef.collection("redemptions").doc(key);
  const idempotencyRef = userRef.collection("redemptionIdempotency").doc(key);

  const result = await db.runTransaction(async (tx) => {
    const [previousKey, rewardSnapshot, userSnapshot, existingRedemption] = await Promise.all([
      tx.get(idempotencyRef), tx.get(rewardRef), tx.get(userRef), tx.get(redemptionRef)
    ]);
    if (previousKey.exists) {
      if (previousKey.get("rewardId") !== rewardId || previousKey.get("payoutDetails") !== payoutDetails ||
          !existingRedemption.exists) {
        throw new HttpsError("already-exists", "Idempotency key was already used for a different redemption.");
      }
      return {
        id: existingRedemption.get("id") as string,
        data: existingRedemption.data()!
      };
    }
    if (!rewardSnapshot.exists || rewardSnapshot.get("active") !== true) {
      throw new HttpsError("failed-precondition", "Reward is not available.");
    }
    if (!userSnapshot.exists) throw new HttpsError("failed-precondition", "User profile is missing.");
    const reward = rewardSnapshot.data()!;
    const user = userSnapshot.data()!;
    const cost = Number(reward.pointsCost);
    const stock = Number(reward.stockQuantity);
    const level = Number(user.level ?? 1);
    if (!Number.isSafeInteger(cost) || cost < MIN_REDEMPTION_POINTS ||
        !Number.isSafeInteger(stock) || stock <= 0 ||
        level < Number(reward.minLevelRequired ?? 1)) {
      throw new HttpsError("failed-precondition", "Reward requirements are not met.");
    }
    const balance = Number(user.pointsBalance ?? 0);
    if (!Number.isSafeInteger(balance) || balance < cost) {
      throw new HttpsError("failed-precondition", "Insufficient points balance.");
    }
    const newBalance = balance - cost;
    const createdAtIso = new Date().toISOString();
    const redemption = {
      id: randomUUID(),
      userId: uid,
      rewardId,
      rewardTitle: requiredString(reward.title, "reward.title", 200),
      rewardCategory: typeof reward.category === "string" ? reward.category : "GIFT_CARD",
      pointsDeducted: cost,
      payoutDetails,
      status: "PENDING",
      rejectionReason: null,
      digitalCode: null,
      createdAtIso,
      processedAtIso: null
    };
    tx.update(userRef, { pointsBalance: newBalance, updatedAt: FieldValue.serverTimestamp() });
    tx.update(rewardRef, { stockQuantity: stock - 1, updatedAt: FieldValue.serverTimestamp() });
    tx.create(redemptionRef, redemption);
    tx.create(idempotencyRef, { rewardId, payoutDetails, createdAt: FieldValue.serverTimestamp() });
    tx.create(transactionReference(uid, `redemption_${key}`), transactionRecord(
      uid, `redemption_${key}`, "DEBIT", cost, newBalance, "REDEMPTION_HOLD",
      redemption.id, `Redemption hold: ${redemption.rewardTitle}`
    ));
    return { id: redemption.id, data: redemption };
  });
  return { redemption: redemptionResult(result.id, result.data) };
});

export const processRedemption = onCall(async (request) => {
  const redemptionId = requiredString(request.data?.redemptionId, "redemptionId", 100);
  const decision = requiredString(request.data?.decision, "decision", 10);
  if (decision !== "APPROVE" && decision !== "REJECT") {
    throw new HttpsError("invalid-argument", "Decision must be APPROVE or REJECT.");
  }
  const adminUid = requirePermission(
    request,
    decision === "APPROVE" ? "APPROVE_REDEMPTION" : "REJECT_REDEMPTION"
  );
  const digitalCode = decision === "APPROVE"
    ? requiredString(request.data?.digitalCode, "digitalCode", 500)
    : null;
  const rejectionReason = decision === "REJECT"
    ? requiredString(request.data?.reason, "reason", 500)
    : null;
  const matches = await db.collectionGroup("redemptions").where("id", "==", redemptionId).limit(1).get();
  if (matches.empty) throw new HttpsError("not-found", "Redemption was not found.");
  const redemptionRef = matches.docs[0]!.ref;
  const userRef = redemptionRef.parent.parent;
  if (!userRef) throw new HttpsError("failed-precondition", "Redemption path is invalid.");
  const rewardRef = db.collection("rewards").doc(requiredDocumentId(matches.docs[0]!.get("rewardId"), "rewardId"));
  const result = await db.runTransaction(async (tx) => {
    const [redemptionSnapshot, userSnapshot, rewardSnapshot] = await Promise.all([
      tx.get(redemptionRef), tx.get(userRef), tx.get(rewardRef)
    ]);
    if (!redemptionSnapshot.exists || !userSnapshot.exists || !rewardSnapshot.exists) {
      throw new HttpsError("not-found", "Redemption or user profile was not found.");
    }
    const redemption = redemptionSnapshot.data()!;
    if (redemption.status !== "PENDING" && redemption.status !== "PROCESSING") {
      throw new HttpsError("failed-precondition", "Redemption is already finalized.");
    }
    const processedAtIso = new Date().toISOString();
    if (decision === "APPROVE") {
      const approved = { ...redemption, status: "APPROVED", digitalCode, processedAtIso, processedByAdminId: adminUid };
      tx.update(redemptionRef, {
        status: approved.status, digitalCode, processedAtIso, processedByAdminId: adminUid
      });
      return { id: redemptionSnapshot.id, data: approved };
    }
    const userId = redemption.userId as string;
    const refundKey = `refund_${redemptionSnapshot.id}`;
    const refundRef = transactionReference(userId, refundKey);
    const balance = Number(userSnapshot.get("pointsBalance") ?? 0);
    const refundAmount = Number(redemption.pointsDeducted);
    if (!Number.isSafeInteger(balance) || !Number.isSafeInteger(refundAmount) ||
        refundAmount <= 0 || balance > Number.MAX_SAFE_INTEGER - refundAmount) {
      throw new HttpsError("failed-precondition", "Refund would exceed the supported wallet balance.");
    }
    const newBalance = balance + refundAmount;
    const stock = Number(rewardSnapshot.get("stockQuantity"));
    if (!Number.isSafeInteger(stock) || stock < 0 || stock >= Number.MAX_SAFE_INTEGER) {
      throw new HttpsError("failed-precondition", "Reward stock is invalid.");
    }
    const rejected = {
      ...redemption,
      status: "REJECTED",
      rejectionReason,
      processedAtIso,
      processedByAdminId: adminUid
    };
    tx.update(userRef, { pointsBalance: newBalance, updatedAt: FieldValue.serverTimestamp() });
    tx.update(rewardRef, { stockQuantity: stock + 1, updatedAt: FieldValue.serverTimestamp() });
    tx.update(redemptionRef, {
      status: rejected.status,
      rejectionReason,
      processedAtIso,
      processedByAdminId: adminUid
    });
    tx.create(refundRef, transactionRecord(
      userId, refundKey, "CREDIT", refundAmount, newBalance, "REDEMPTION_REFUND",
      redemptionSnapshot.id, `Redemption refund: ${rejectionReason}`
    ));
    return { id: redemptionSnapshot.id, data: rejected };
  });
  return { redemption: redemptionResult(result.id, result.data) };
});

type AdminResource =
  | "categories"
  | "questions"
  | "tasks"
  | "tournaments"
  | "rewards"
  | "users"
  | "redemptions";

const ADMIN_RESOURCE_PERMISSION: Record<AdminResource, AdminPermission> = {
  categories: "MANAGE_CATEGORIES",
  questions: "MANAGE_QUESTIONS",
  tasks: "MANAGE_TASKS",
  tournaments: "MANAGE_TOURNAMENTS",
  rewards: "MANAGE_REWARDS",
  users: "VIEW_USER_PROFILES",
  redemptions: "REVIEW_REDEMPTIONS"
};

function adminResource(value: unknown): AdminResource {
  if (typeof value !== "string" || !(value in ADMIN_RESOURCE_PERMISSION)) {
    throw new HttpsError("invalid-argument", "Admin resource is invalid.");
  }
  return value as AdminResource;
}

function firestoreIso(value: unknown): string | undefined {
  if (value && typeof value === "object" && "toDate" in value &&
      typeof (value as FirebaseFirestore.Timestamp).toDate === "function") {
    return (value as FirebaseFirestore.Timestamp).toDate().toISOString();
  }
  return typeof value === "string" ? value : undefined;
}

function adminRecord(resource: AdminResource, id: string, data: FirebaseFirestore.DocumentData) {
  if (resource === "questions") {
    return {
      id,
      ...data,
      categoryName: data.categoryTitle ?? data.categoryName ?? "",
      explanation: data.explanation ?? data.explanationText ?? "",
      status: data.active === true ? "ACTIVE" : data.status ?? "DRAFT",
      createdAt: firestoreIso(data.createdAt) ?? new Date(0).toISOString()
    };
  }
  if (resource === "rewards") {
    return {
      id,
      ...data,
      stockCount: Number(data.stockQuantity ?? data.stockCount ?? 0),
      isActive: data.active === true || data.isActive === true,
      createdAt: firestoreIso(data.createdAt) ?? new Date(0).toISOString()
    };
  }
  if (resource === "tasks") {
    return {
      id,
      ...data,
      isActive: data.isActive === true,
      createdAt: firestoreIso(data.createdAt) ?? new Date(0).toISOString()
    };
  }
  if (resource === "tournaments") {
    return {
      id,
      ...data,
      status: data.status === "COMPLETED" ? "ENDED" : data.status,
      createdAt: firestoreIso(data.createdAt) ?? new Date(0).toISOString()
    };
  }
  if (resource === "users") {
    return {
      id,
      displayName: data.username ?? "Player",
      email: data.email ?? "",
      avatarColor: `#${Number(data.avatarColorHex ?? 0xFF5B5FEF).toString(16).slice(-6)}`,
      level: Number(data.level ?? 1),
      pointsBalance: Number(data.pointsBalance ?? 0),
      quizzesPlayed: Number(data.totalQuizzesPlayed ?? 0),
      accuracyRate: Number(data.accuracyRate ?? 0),
      status: data.disabled === true ? "SUSPENDED" : "ACTIVE",
      createdAtIso: firestoreIso(data.createdAt) ?? new Date(0).toISOString(),
      ledger: []
    };
  }
  if (resource === "redemptions") {
    return {
      id: typeof data.id === "string" ? data.id : id,
      userId: data.userId,
      idempotencyDocumentId: id,
      userName: data.userName ?? "",
      userEmail: data.userEmail ?? "",
      rewardId: data.rewardId,
      rewardTitle: data.rewardTitle,
      rewardCategory: data.rewardCategory ?? "GIFT_CARD",
      pointsDeducted: data.pointsDeducted,
      payoutDetails: data.payoutDetails,
      status: data.status,
      digitalCodeOrReference: data.digitalCode ?? undefined,
      rejectionReason: data.rejectionReason ?? undefined,
      requestedAtIso: data.createdAtIso,
      processedAtIso: data.processedAtIso ?? undefined,
      processedByAdminId: data.processedByAdminId ?? undefined
    };
  }
  return { id, ...data, createdAt: firestoreIso(data.createdAt) };
}

function resourceCollection(resource: AdminResource): FirebaseFirestore.CollectionReference {
  switch (resource) {
    case "categories": return db.collection("categories");
    case "questions": return db.collection("questionBank");
    case "tasks": return db.collection("tasks");
    case "tournaments": return db.collection("tournaments");
    case "rewards": return db.collection("rewards");
    case "users": return db.collection("users");
    default: throw new HttpsError("failed-precondition", "Resource is not a document collection.");
  }
}

export const adminListRecords = onCall(async (request) => {
  requireUid(request);
  const resource = adminResource(request.data?.resource);
  requirePermission(request, ADMIN_RESOURCE_PERMISSION[resource]);
  const snapshot = resource === "redemptions"
    ? await db.collectionGroup("redemptions").limit(200).get()
    : await resourceCollection(resource).limit(200).get();
  if (resource === "redemptions") {
    const records = snapshot.docs.map((document) =>
      adminRecord(resource, document.id, document.data())
    );
    const users = await Promise.all(records.map((record) =>
      typeof record.userId === "string" ? profileReference(record.userId).get() : Promise.resolve(null)
    ));
    return {
      records: records.map((record, index) => {
        const user = users[index];
        return {
          ...record,
          userName: user?.get("username") ?? "Player",
          userEmail: user?.get("email") ?? ""
        };
      })
    };
  }
  if (resource === "categories") {
    const questions = await db.collection("questionBank").limit(1000).get();
    const questionCounts = new Map<string, number>();
    for (const question of questions.docs) {
      const categoryId = question.get("categoryId");
      if (typeof categoryId === "string") {
        questionCounts.set(categoryId, (questionCounts.get(categoryId) ?? 0) + 1);
      }
    }
    return {
      records: snapshot.docs.map((document) => ({
        ...adminRecord(resource, document.id, document.data()),
        questionCount: questionCounts.get(document.id) ?? 0
      }))
    };
  }
  return {
    records: snapshot.docs.map((document) =>
      adminRecord(resource, document.id, document.data())
    )
  };
});

export const adminSaveRecord = onCall(async (request) => {
  const uid = requireUid(request);
  const resource = adminResource(request.data?.resource);
  requirePermission(request, ADMIN_RESOURCE_PERMISSION[resource]);
  if (resource === "users" || resource === "redemptions") {
    throw new HttpsError("permission-denied", "This resource cannot be modified through this operation.");
  }
  const record = requiredRecord(request.data?.record);
  const suppliedId = record.id == null ? null : requiredDocumentId(record.id);
  const id = suppliedId ?? requiredDocumentId(request.data?.id);
  const ref = resourceCollection(resource).doc(id);
  const existing = await ref.get();
  const now = FieldValue.serverTimestamp();

  if (resource === "categories") {
    const name = requiredString(record.name, "name", 80);
    const slug = requiredString(record.slug, "slug", 80);
    const colorHex = requiredString(record.colorHex, "colorHex", 7);
    if (!/^#[0-9A-Fa-f]{6}$/.test(colorHex)) throw new HttpsError("invalid-argument", "colorHex is invalid.");
    await ref.set({
      name, slug, iconName: requiredString(record.iconName, "iconName", 40), colorHex,
      displayOrder: requiredInteger(record.displayOrder, "displayOrder", 0, 10000),
      description: typeof record.description === "string" ? record.description.slice(0, 500) : "",
      isActive: record.isActive === true,
      createdAt: existing.exists ? existing.get("createdAt") : now,
      updatedAt: now,
      updatedBy: uid
    }, { merge: true });
    return { id };
  }

  if (resource === "questions") {
    const categoryId = requiredDocumentId(record.categoryId, "categoryId");
    const category = await db.collection("categories").doc(categoryId).get();
    if (!category.exists || category.get("isActive") !== true) {
      throw new HttpsError("failed-precondition", "Question category is not active.");
    }
    if (!Array.isArray(record.options) || record.options.length !== 4 ||
        !record.options.every((option) => typeof option === "string" && option.trim().length <= 500)) {
      throw new HttpsError("invalid-argument", "Question options must contain four valid strings.");
    }
    const correctOptionIndex = requiredInteger(record.correctOptionIndex, "correctOptionIndex", 0, 3);
    const status = record.status === "ACTIVE" ? "ACTIVE" : record.status === "DRAFT" ? "DRAFT" : null;
    if (!status) throw new HttpsError("invalid-argument", "Question status is invalid.");
    const difficulty = record.difficulty;
    if (difficulty !== "EASY" && difficulty !== "MEDIUM" && difficulty !== "HARD") {
      throw new HttpsError("invalid-argument", "Question difficulty is invalid.");
    }
    const data = {
      categoryId,
      categoryTitle: requiredString(category.get("name"), "category.name", 80),
      text: requiredString(record.text, "text", 2000),
      options: record.options.map((option) => requiredString(option, "option", 500)),
      correctOptionIndex,
      explanationText: typeof record.explanation === "string" ? record.explanation.slice(0, 2000) : "",
      difficulty,
      timeLimitSeconds: requiredInteger(record.timeLimitSeconds, "timeLimitSeconds", 5, 120),
      pointsReward: requiredInteger(record.pointsReward, "pointsReward", 0, 1000),
      active: status === "ACTIVE",
      status,
      createdAt: existing.exists ? existing.get("createdAt") : now,
      updatedAt: now,
      updatedBy: uid
    };
    await ref.set(data, { merge: true });
    return { id };
  }

  if (resource === "tasks") {
    const type = record.type;
    if (type !== "DAILY" && type !== "WEEKLY") throw new HttpsError("invalid-argument", "Task type is invalid.");
    const criterion = record.criterion;
    if (!["QUIZ_COMPLETION", "ACCURACY_SCORE", "STREAK_MAINTAIN", "CATEGORY_QUIZ"].includes(String(criterion))) {
      throw new HttpsError("invalid-argument", "Task criterion is invalid.");
    }
    const categoryKey = record.categoryKey == null || record.categoryKey === ""
      ? null
      : requiredDocumentId(record.categoryKey, "categoryKey");
    if (criterion === "CATEGORY_QUIZ" && !categoryKey) {
      throw new HttpsError("invalid-argument", "Category tasks require a category.");
    }
    await ref.set({
      title: requiredString(record.title, "title", 100),
      description: requiredString(record.description, "description", 500),
      type,
      criterion,
      targetGoal: requiredInteger(record.targetGoal, "targetGoal", 1, 100000),
      rewardPoints: requiredInteger(record.rewardPoints, "rewardPoints", 1, 100000),
      categoryKey,
      isActive: record.isActive === true,
      createdAt: existing.exists ? existing.get("createdAt") : now,
      updatedAt: now,
      updatedBy: uid
    }, { merge: true });
    return { id };
  }

  if (resource === "tournaments") {
    const startTimeIso = requiredString(record.startTimeIso, "startTimeIso", 40);
    const endTimeIso = requiredString(record.endTimeIso, "endTimeIso", 40);
    const start = Date.parse(startTimeIso);
    const end = Date.parse(endTimeIso);
    if (!Number.isFinite(start) || !Number.isFinite(end) || end <= start) {
      throw new HttpsError("invalid-argument", "Tournament date range is invalid.");
    }
    const status = record.status;
    if (!["DRAFT", "SCHEDULED", "LIVE"].includes(String(status))) {
      throw new HttpsError("failed-precondition", "Use the dedicated finalize or cancel operation for tournament status changes.");
    }
    const categoryId = requiredDocumentId(record.categoryId, "categoryId");
    const category = await db.collection("categories").doc(categoryId).get();
    if (!category.exists) throw new HttpsError("failed-precondition", "Tournament category does not exist.");
    const fee = requiredInteger(record.entryFeePoints, "entryFeePoints", 0, 1000000);
    const prize = requiredInteger(record.prizePoolPoints, "prizePoolPoints", 0, 100000000);
    const maxParticipants = requiredInteger(record.maxParticipants, "maxParticipants", 1, 500);
    if (prize > fee * maxParticipants) {
      throw new HttpsError("failed-precondition", "Configured prize pool exceeds possible entry-fee funding.");
    }
    if (existing.exists && Number(existing.get("registeredParticipants") ?? 0) > maxParticipants) {
      throw new HttpsError("failed-precondition", "Maximum participants cannot be below current registrations.");
    }
    await ref.set({
      title: requiredString(record.title, "title", 120),
      description: requiredString(record.description, "description", 1000),
      categoryId,
      categoryName: category.get("name"),
      startTimeIso,
      endTimeIso,
      status,
      entryFeePoints: fee,
      prizePoolPoints: prize,
      maxParticipants,
      minLevelRequired: requiredInteger(record.minLevelRequired, "minLevelRequired", 1, 1000),
      registeredParticipants: existing.exists ? existing.get("registeredParticipants") : 0,
      collectedEntryFees: existing.exists ? existing.get("collectedEntryFees") : 0,
      createdAt: existing.exists ? existing.get("createdAt") : now,
      updatedAt: now,
      updatedBy: uid
    }, { merge: true });
    return { id };
  }

  const category = record.category;
  if (!["GIFT_CARD", "CASHOUT", "DIGITAL_VOUCHER"].includes(String(category))) {
    throw new HttpsError("invalid-argument", "Reward category is invalid.");
  }
  const stock = requiredInteger(record.stockCount, "stockCount", 0, 1000000);
  const cost = requiredInteger(record.pointsCost, "pointsCost", MIN_REDEMPTION_POINTS);
  const rewardData = {
    title: requiredString(record.title, "title", 200),
    description: requiredString(record.description, "description", 1000),
    pointsCost: cost,
    category,
    minLevelRequired: requiredInteger(record.minLevelRequired, "minLevelRequired", 1, 1000),
    provider: requiredString(record.provider, "provider", 100),
    terms: typeof record.terms === "string" ? record.terms.slice(0, 2000) : "",
    active: record.isActive === true,
    createdAt: existing.exists ? existing.get("createdAt") : now,
    updatedAt: now,
    updatedBy: uid
  };
  await db.runTransaction(async (tx) => {
    const [current, pendingRedemptions] = await Promise.all([
      tx.get(ref),
      tx.get(db.collectionGroup("redemptions")
        .where("rewardId", "==", id)
        .where("status", "in", ["PENDING", "PROCESSING"])
        .limit(501))
    ]);
    if (pendingRedemptions.size > stock) {
      throw new HttpsError("failed-precondition", "Stock cannot be lower than pending reserved redemptions.");
    }
    tx.set(ref, {
      ...rewardData,
      stockQuantity: stock,
      createdAt: current.exists ? current.get("createdAt") : now
    }, { merge: true });
  });
  return { id };
});

export const adminDeleteRecord = onCall(async (request) => {
  const uid = requireUid(request);
  const resource = adminResource(request.data?.resource);
  requirePermission(request, ADMIN_RESOURCE_PERMISSION[resource]);
  if (resource === "users" || resource === "redemptions") {
    throw new HttpsError("permission-denied", "This resource cannot be deleted.");
  }
  const id = requiredDocumentId(request.data?.id);
  const ref = resourceCollection(resource).doc(id);
  return db.runTransaction(async (tx) => {
    const snapshot = await tx.get(ref);
    if (!snapshot.exists) return { deleted: false };
    if (resource === "categories") {
      const [questions, tournaments, tasks] = await Promise.all([
        tx.get(db.collection("questionBank").where("categoryId", "==", id).limit(1)),
        tx.get(db.collection("tournaments").where("categoryId", "==", id).limit(1)),
        tx.get(db.collection("tasks").where("categoryKey", "==", id).limit(1))
      ]);
      if (!questions.empty || !tournaments.empty || !tasks.empty) {
        throw new HttpsError("failed-precondition", "Category is still referenced by content.");
      }
    } else if (resource === "tournaments") {
      const participants = await tx.get(ref.collection("participants").limit(1));
      if (!participants.empty) {
        throw new HttpsError("failed-precondition", "Tournament with registrations cannot be deleted; cancel it to refund entries.");
      }
    } else if (resource === "rewards") {
      const redemptions = await tx.get(
        db.collectionGroup("redemptions").where("rewardId", "==", id).limit(1)
      );
      if (!redemptions.empty) {
        throw new HttpsError("failed-precondition", "Reward with redemption history cannot be deleted.");
      }
    }
    tx.delete(ref);
    return { deleted: true, deletedBy: uid };
  });
});

export const adminAdjustRewardStock = onCall(async (request) => {
  const uid = requirePermission(request, "MANAGE_REWARDS");
  const rewardId = requiredDocumentId(request.data?.rewardId, "rewardId");
  const stockCount = requiredInteger(request.data?.stockCount, "stockCount", 0, 1000000);
  const rewardRef = db.collection("rewards").doc(rewardId);
  return db.runTransaction(async (tx) => {
    const [rewardSnapshot, pendingRedemptions] = await Promise.all([
      tx.get(rewardRef),
      tx.get(db.collectionGroup("redemptions")
        .where("rewardId", "==", rewardId)
        .where("status", "in", ["PENDING", "PROCESSING"])
        .limit(501))
    ]);
    if (!rewardSnapshot.exists) throw new HttpsError("not-found", "Reward was not found.");
    if (pendingRedemptions.size > stockCount) {
      throw new HttpsError("failed-precondition", "Stock cannot be lower than pending reserved redemptions.");
    }
    tx.update(rewardRef, {
      stockQuantity: stockCount,
      updatedAt: FieldValue.serverTimestamp(),
      stockUpdatedBy: uid
    });
    return { rewardId, stockCount };
  });
});
