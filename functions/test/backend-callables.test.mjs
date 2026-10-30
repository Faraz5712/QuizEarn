import assert from 'node:assert/strict';
import { randomUUID } from 'node:crypto';
import { after, before, describe, it } from 'node:test';

const emulatorConfigured = Boolean(process.env.FIRESTORE_EMULATOR_HOST);

describe('Firebase task and tournament callables', { skip: !emulatorConfigured }, () => {
  let db;
  let claimTaskReward;
  let registerForTournament;
  let adminListRecords;
  const cleanup = [];

  before(async () => {
    process.env.GCLOUD_PROJECT ||= 'demo-quizearn';
    const [{ getFirestore }, functions] = await Promise.all([
      import('firebase-admin/firestore'),
      import('../lib/index.js'),
    ]);
    db = getFirestore();
    claimTaskReward = functions.claimTaskReward;
    registerForTournament = functions.registerForTournament;
    adminListRecords = functions.adminListRecords;
  });

  after(async () => {
    for (const reference of cleanup) await db.recursiveDelete(reference);
  });

  async function seedCompletedTask(rewardPoints = 25) {
    const uid = `test-${randomUUID()}`;
    const taskId = `task-${randomUUID()}`;
    const periodKey = new Date().toISOString().slice(0, 10);
    const userRef = db.collection('users').doc(uid);
    const taskRef = db.collection('tasks').doc(taskId);
    cleanup.push(userRef, taskRef);
    await Promise.all([
      userRef.set({ pointsBalance: 100, username: 'Test Player', level: 1 }),
      taskRef.set({
        title: 'Test reward',
        type: 'DAILY',
        isActive: true,
        targetGoal: 1,
        rewardPoints,
      }),
      userRef.collection('taskProgress').doc(`${periodKey}_${taskId}`).set({
        taskId,
        periodKey,
        currentProgress: 1,
        isClaimed: false,
      }),
    ]);
    return { uid, taskId };
  }

  function invoke(data, uid) {
    return claimTaskReward.run({
      data,
      auth: uid ? { uid, token: { firebase: { sign_in_provider: 'password' } } } : null,
    });
  }

  async function seedTournament(entryFeePoints = 20) {
    const uid = `test-${randomUUID()}`;
    const tournamentId = `tournament-${randomUUID()}`;
    const userRef = db.collection('users').doc(uid);
    const tournamentRef = db.collection('tournaments').doc(tournamentId);
    cleanup.push(userRef, tournamentRef);
    await Promise.all([
      userRef.set({ pointsBalance: 100, username: 'Test Player', level: 1 }),
      tournamentRef.set({
        title: 'Test Tournament',
        status: 'LIVE',
        startTimeIso: new Date(Date.now() - 60_000).toISOString(),
        endTimeIso: new Date(Date.now() + 3_600_000).toISOString(),
        registeredParticipants: 0,
        collectedEntryFees: 0,
        maxParticipants: 10,
        minLevelRequired: 1,
        entryFeePoints,
      }),
    ]);
    return { uid, tournamentId };
  }

  function register(data, uid) {
    return registerForTournament.run({
      data,
      auth: uid ? { uid, token: { firebase: { sign_in_provider: 'password' } } } : null,
    });
  }

  it('credits the catalog reward once and returns the same result for a retry', async () => {
    const { uid, taskId } = await seedCompletedTask();
    const request = { taskId, idempotencyKey: `claim-${randomUUID()}` };

    const first = await invoke(request, uid);
    const retry = await invoke(request, uid);
    const user = await db.collection('users').doc(uid).get();
    const transactions = await db.collection('users').doc(uid)
      .collection('walletTransactions').get();

    assert.equal(first.pointsEarned, 25);
    assert.deepEqual(retry, first);
    assert.equal(user.get('pointsBalance'), 125);
    assert.equal(transactions.size, 1);
  });

  it('rejects a claim without authenticated identity', async () => {
    const { taskId } = await seedCompletedTask();
    await assert.rejects(
      invoke({ taskId, idempotencyKey: `claim-${randomUUID()}` }, null),
      (error) => error.code === 'unauthenticated'
    );
  });

  it('rejects invalid server reward configuration without changing the wallet', async () => {
    const { uid, taskId } = await seedCompletedTask(-1);
    await assert.rejects(
      invoke({ taskId, idempotencyKey: `claim-${randomUUID()}` }, uid),
      (error) => error.code === 'failed-precondition'
    );
    const user = await db.collection('users').doc(uid).get();
    assert.equal(user.get('pointsBalance'), 100);
  });

  it('allows only one concurrent claim for a completed task', async () => {
    const { uid, taskId } = await seedCompletedTask(40);
    const attempts = await Promise.allSettled([
      invoke({ taskId, idempotencyKey: `claim-${randomUUID()}` }, uid),
      invoke({ taskId, idempotencyKey: `claim-${randomUUID()}` }, uid),
    ]);
    const successful = attempts.filter((attempt) => attempt.status === 'fulfilled');
    const rejected = attempts.filter((attempt) => attempt.status === 'rejected');
    const user = await db.collection('users').doc(uid).get();
    const transactions = await db.collection('users').doc(uid)
      .collection('walletTransactions').get();

    assert.equal(successful.length, 1);
    assert.equal(rejected.length, 1);
    assert.equal(user.get('pointsBalance'), 140);
    assert.equal(transactions.size, 1);
  });

  it('charges the configured entry fee once across retries', async () => {
    const { uid, tournamentId } = await seedTournament();
    const request = { tournamentId, idempotencyKey: `register-${randomUUID()}` };

    const first = await register(request, uid);
    const retry = await register(request, uid);
    const user = await db.collection('users').doc(uid).get();
    const tournament = await db.collection('tournaments').doc(tournamentId).get();
    const participant = await db.collection('tournaments').doc(tournamentId)
      .collection('participants').doc(uid).get();

    assert.equal(first.alreadyRegistered, false);
    assert.equal(retry.alreadyRegistered, true);
    assert.equal(user.get('pointsBalance'), 80);
    assert.equal(tournament.get('registeredParticipants'), 1);
    assert.equal(participant.exists, true);
  });

  it('rejects tournament registration without authenticated identity', async () => {
    const { tournamentId } = await seedTournament();
    await assert.rejects(
      register({ tournamentId, idempotencyKey: `register-${randomUUID()}` }, null),
      (error) => error.code === 'unauthenticated'
    );
  });

  it('rejects invalid tournament fees without changing wallet or registration count', async () => {
    const { uid, tournamentId } = await seedTournament(-1);
    await assert.rejects(
      register({ tournamentId, idempotencyKey: `register-${randomUUID()}` }, uid),
      (error) => error.code === 'failed-precondition'
    );
    const user = await db.collection('users').doc(uid).get();
    const tournament = await db.collection('tournaments').doc(tournamentId).get();
    assert.equal(user.get('pointsBalance'), 100);
    assert.equal(tournament.get('registeredParticipants'), 0);
  });

  it('serializes concurrent tournament registration and charges only once', async () => {
    const { uid, tournamentId } = await seedTournament(35);
    const attempts = await Promise.all([
      register({ tournamentId, idempotencyKey: `register-${randomUUID()}` }, uid),
      register({ tournamentId, idempotencyKey: `register-${randomUUID()}` }, uid),
    ]);
    const user = await db.collection('users').doc(uid).get();
    const tournament = await db.collection('tournaments').doc(tournamentId).get();
    const participants = await db.collection('tournaments').doc(tournamentId)
      .collection('participants').get();
    const transactions = await db.collection('users').doc(uid)
      .collection('walletTransactions').get();

    assert.deepEqual(attempts.map((attempt) => attempt.alreadyRegistered).sort(), [false, true]);
    assert.equal(user.get('pointsBalance'), 65);
    assert.equal(tournament.get('registeredParticipants'), 1);
    assert.equal(participants.size, 1);
    assert.equal(transactions.size, 1);
  });

  it('enforces admin role claims on privileged listing callables', async () => {
    await assert.rejects(
      adminListRecords.run({ data: { resource: 'users' }, auth: null }),
      (error) => error.code === 'unauthenticated'
    );
    await assert.rejects(
      adminListRecords.run({
        data: { resource: 'users' },
        auth: { uid: `test-${randomUUID()}`, token: { role: 'CONTENT_MANAGER' } },
      }),
      (error) => error.code === 'permission-denied'
    );

    const result = await adminListRecords.run({
      data: { resource: 'users' },
      auth: { uid: `admin-${randomUUID()}`, token: { role: 'SUPER_ADMIN' } },
    });
    assert.ok(Array.isArray(result.records));
  });
});
