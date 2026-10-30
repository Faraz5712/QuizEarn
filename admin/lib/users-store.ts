'use client';

import { AdminPlayer } from '../types/user';

export const INITIAL_USERS: AdminPlayer[] = [
  {
    id: 'usr_sarah_connor',
    displayName: 'Sarah Connor',
    email: 'sarah.c@resistance.org',
    avatarColor: '#5B5FEF',
    level: 4,
    pointsBalance: 3450,
    quizzesPlayed: 48,
    accuracyRate: 89,
    winStreak: 7,
    status: 'ACTIVE',
    createdAtIso: '2026-10-15T09:00:00Z',
    lastActiveIso: '2026-10-29T11:20:00Z',
    ledger: [
      {
        id: 'TXN-9001',
        userId: 'usr_sarah_connor',
        type: 'CREDIT',
        amount: 100,
        balanceAfter: 100,
        reason: 'WELCOME_BONUS',
        description: 'New account onboarding bonus credited',
        timestampIso: '2026-10-15T09:01:00Z',
      },
      {
        id: 'TXN-9012',
        userId: 'usr_sarah_connor',
        type: 'CREDIT',
        amount: 150,
        balanceAfter: 250,
        reason: 'QUIZ_REWARD',
        referenceId: 'SES-SCI-1182',
        description: 'Completed Science Sprint with 100% accuracy',
        timestampIso: '2026-10-18T14:22:00Z',
      },
      {
        id: 'TXN-9045',
        userId: 'usr_sarah_connor',
        type: 'CREDIT',
        amount: 2500,
        balanceAfter: 2750,
        reason: 'TOURNAMENT_PRIZE',
        referenceId: 'TRN-2026-004',
        description: '1st Place Winner — Autumn History Marathon',
        timestampIso: '2026-10-25T22:30:00Z',
      },
      {
        id: 'TXN-9080',
        userId: 'usr_sarah_connor',
        type: 'DEBIT',
        amount: 1000,
        balanceAfter: 1750,
        reason: 'REDEMPTION_HOLD',
        referenceId: 'RDM-2026-101',
        description: 'Held points for $10 PayPal Cashout request',
        timestampIso: '2026-10-29T11:20:00Z',
      },
      {
        id: 'TXN-9099',
        userId: 'usr_sarah_connor',
        type: 'CREDIT',
        amount: 1700,
        balanceAfter: 3450,
        reason: 'QUIZ_REWARD',
        referenceId: 'SES-ALL-3392',
        description: 'Trivia gameplay points accumulation',
        timestampIso: '2026-10-29T13:45:00Z',
      },
    ],
  },
  {
    id: 'usr_alex_dev',
    displayName: 'Alex Turner',
    email: 'alex.turner@devlabs.io',
    avatarColor: '#10B981',
    level: 3,
    pointsBalance: 1820,
    quizzesPlayed: 32,
    accuracyRate: 84,
    winStreak: 3,
    status: 'ACTIVE',
    createdAtIso: '2026-10-18T12:00:00Z',
    lastActiveIso: '2026-10-29T12:05:00Z',
    ledger: [
      {
        id: 'TXN-8801',
        userId: 'usr_alex_dev',
        type: 'CREDIT',
        amount: 100,
        balanceAfter: 100,
        reason: 'WELCOME_BONUS',
        description: 'New account onboarding bonus credited',
        timestampIso: '2026-10-18T12:01:00Z',
      },
      {
        id: 'TXN-8822',
        userId: 'usr_alex_dev',
        type: 'CREDIT',
        amount: 2670,
        balanceAfter: 2770,
        reason: 'QUIZ_REWARD',
        description: 'Accumulated score rewards across Tech quizzes',
        timestampIso: '2026-10-24T18:10:00Z',
      },
      {
        id: 'TXN-8840',
        userId: 'usr_alex_dev',
        type: 'DEBIT',
        amount: 950,
        balanceAfter: 1820,
        reason: 'REDEMPTION_HOLD',
        referenceId: 'RDM-2026-102',
        description: 'Held points for $10 Amazon Gift Card request',
        timestampIso: '2026-10-29T12:05:00Z',
      },
    ],
  },
  {
    id: 'usr_marcus_pro',
    displayName: 'Marcus Vance',
    email: 'marcus.v@speedmail.com',
    avatarColor: '#F59E0B',
    level: 2,
    pointsBalance: 750,
    quizzesPlayed: 19,
    accuracyRate: 78,
    winStreak: 2,
    status: 'ACTIVE',
    createdAtIso: '2026-10-22T08:30:00Z',
    lastActiveIso: '2026-10-29T13:40:00Z',
    ledger: [
      {
        id: 'TXN-7701',
        userId: 'usr_marcus_pro',
        type: 'CREDIT',
        amount: 100,
        balanceAfter: 100,
        reason: 'WELCOME_BONUS',
        description: 'New account onboarding bonus credited',
        timestampIso: '2026-10-22T08:31:00Z',
      },
      {
        id: 'TXN-7720',
        userId: 'usr_marcus_pro',
        type: 'CREDIT',
        amount: 1150,
        balanceAfter: 1250,
        reason: 'QUIZ_REWARD',
        description: 'Sports & Pop Culture quiz earnings',
        timestampIso: '2026-10-27T16:00:00Z',
      },
      {
        id: 'TXN-7735',
        userId: 'usr_marcus_pro',
        type: 'DEBIT',
        amount: 500,
        balanceAfter: 750,
        reason: 'REDEMPTION_HOLD',
        referenceId: 'RDM-2026-103',
        description: 'Held points for $5 Google Play Code request',
        timestampIso: '2026-10-29T13:40:00Z',
      },
    ],
  },
  {
    id: 'usr_clara_quiz',
    displayName: 'Clara Oswald',
    email: 'clara.o@tardis.ac.uk',
    avatarColor: '#8B5CF6',
    level: 5,
    pointsBalance: 5120,
    quizzesPlayed: 95,
    accuracyRate: 94,
    winStreak: 12,
    status: 'ACTIVE',
    createdAtIso: '2026-10-10T14:00:00Z',
    lastActiveIso: '2026-10-28T16:15:00Z',
    ledger: [
      {
        id: 'TXN-6601',
        userId: 'usr_clara_quiz',
        type: 'CREDIT',
        amount: 100,
        balanceAfter: 100,
        reason: 'WELCOME_BONUS',
        description: 'New account onboarding bonus credited',
        timestampIso: '2026-10-10T14:01:00Z',
      },
      {
        id: 'TXN-6620',
        userId: 'usr_clara_quiz',
        type: 'CREDIT',
        amount: 5620,
        balanceAfter: 5720,
        reason: 'QUIZ_REWARD',
        description: 'High streak milestone & daily challenges completed',
        timestampIso: '2026-10-25T11:00:00Z',
      },
      {
        id: 'TXN-6640',
        userId: 'usr_clara_quiz',
        type: 'DEBIT',
        amount: 600,
        balanceAfter: 5120,
        reason: 'REDEMPTION_HOLD',
        referenceId: 'RDM-2026-104',
        description: 'Redeemed Spotify 1-Month Premium',
        timestampIso: '2026-10-28T15:00:00Z',
      },
    ],
  },
  {
    id: 'usr_demo_001',
    displayName: 'John Doe',
    email: 'j.doe@example.com',
    avatarColor: '#EF4444',
    level: 1,
    pointsBalance: 2400,
    quizzesPlayed: 12,
    accuracyRate: 65,
    winStreak: 0,
    status: 'SUSPENDED',
    statusReason: 'Flagged for abusive script automation and multiple concurrent sessions from suspicious proxies.',
    statusUpdatedAtIso: '2026-10-27T10:30:00Z',
    createdAtIso: '2026-10-26T15:00:00Z',
    lastActiveIso: '2026-10-27T10:25:00Z',
    ledger: [
      {
        id: 'TXN-5501',
        userId: 'usr_demo_001',
        type: 'CREDIT',
        amount: 100,
        balanceAfter: 100,
        reason: 'WELCOME_BONUS',
        description: 'New account onboarding bonus credited',
        timestampIso: '2026-10-26T15:01:00Z',
      },
      {
        id: 'TXN-5520',
        userId: 'usr_demo_001',
        type: 'CREDIT',
        amount: 2300,
        balanceAfter: 2400,
        reason: 'QUIZ_REWARD',
        description: 'Rapid quiz score injection detected',
        timestampIso: '2026-10-27T08:50:00Z',
      },
      {
        id: 'TXN-5535',
        userId: 'usr_demo_001',
        type: 'DEBIT',
        amount: 2400,
        balanceAfter: 0,
        reason: 'REDEMPTION_HOLD',
        referenceId: 'RDM-2026-105',
        description: 'Held points for $25 PayPal Cashout request',
        timestampIso: '2026-10-27T09:10:00Z',
      },
      {
        id: 'TXN-5542',
        userId: 'usr_demo_001',
        type: 'CREDIT',
        amount: 2400,
        balanceAfter: 2400,
        reason: 'REDEMPTION_REFUND',
        referenceId: 'RDM-2026-105',
        description: 'Redemption request rejected: points refunded to balance',
        timestampIso: '2026-10-27T10:30:00Z',
      },
    ],
  },
  {
    id: 'usr_emily_w',
    displayName: 'Emily Watson',
    email: 'emily.w@gamersguild.net',
    avatarColor: '#06B6D4',
    level: 3,
    pointsBalance: 2150,
    quizzesPlayed: 38,
    accuracyRate: 88,
    winStreak: 5,
    status: 'ACTIVE',
    createdAtIso: '2026-10-19T10:00:00Z',
    lastActiveIso: '2026-10-29T10:00:00Z',
    ledger: [
      {
        id: 'TXN-4401',
        userId: 'usr_emily_w',
        type: 'CREDIT',
        amount: 100,
        balanceAfter: 100,
        reason: 'WELCOME_BONUS',
        description: 'New account onboarding bonus credited',
        timestampIso: '2026-10-19T10:01:00Z',
      },
      {
        id: 'TXN-4420',
        userId: 'usr_emily_w',
        type: 'CREDIT',
        amount: 3050,
        balanceAfter: 3150,
        reason: 'QUIZ_REWARD',
        description: 'Completed weekly gaming trivia challenges',
        timestampIso: '2026-10-26T17:00:00Z',
      },
      {
        id: 'TXN-4435',
        userId: 'usr_emily_w',
        type: 'DEBIT',
        amount: 1000,
        balanceAfter: 2150,
        reason: 'REDEMPTION_HOLD',
        referenceId: 'RDM-2026-106',
        description: 'Redeemed $10 Steam Wallet Code',
        timestampIso: '2026-10-26T18:22:00Z',
      },
    ],
  },
];

const USERS_KEY = 'quizearn_admin_users';

export function loadStoredUsers(): AdminPlayer[] {
  if (typeof window === 'undefined') return INITIAL_USERS;
  try {
    const raw = localStorage.getItem(USERS_KEY);
    if (!raw) {
      localStorage.setItem(USERS_KEY, JSON.stringify(INITIAL_USERS));
      return INITIAL_USERS;
    }
    return JSON.parse(raw);
  } catch {
    return INITIAL_USERS;
  }
}

export function saveStoredUsers(users: AdminPlayer[]): void {
  if (typeof window === 'undefined') return;
  try {
    localStorage.setItem(USERS_KEY, JSON.stringify(users));
  } catch (err) {
    console.error('Failed to persist users to localStorage', err);
  }
}

export function refundUserRedemption(
  userId: string,
  amount: number,
  redemptionId: string,
  reason: string
): void {
  const users = loadStoredUsers();
  const updated = users.map((u) => {
    if (u.id === userId) {
      const newBal = u.pointsBalance + amount;
      const newTxn = {
        id: `TXN-${Date.now().toString().slice(-4)}`,
        userId: u.id,
        type: 'CREDIT' as const,
        amount,
        balanceAfter: newBal,
        reason: 'REDEMPTION_REFUND',
        referenceId: redemptionId,
        description: `Automatic refund for rejected claim ${redemptionId}: ${reason}`,
        timestampIso: new Date().toISOString(),
      };
      return {
        ...u,
        pointsBalance: newBal,
        ledger: [newTxn, ...u.ledger],
      };
    }
    return u;
  });
  saveStoredUsers(updated);
}
