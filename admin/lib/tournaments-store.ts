'use client';

import { AdminTask, AdminTournament } from '../types/tournament';

export const INITIAL_TOURNAMENTS: AdminTournament[] = [
  {
    id: 'TRN-2026-001',
    title: 'Weekend Science Sprint',
    description: 'Battle for top honours in astrophysics, biology, and chemistry trivia.',
    categoryId: 'cat_science',
    categoryName: 'Science & Nature',
    status: 'LIVE',
    startTimeIso: '2026-10-29T10:00:00Z',
    endTimeIso: '2026-10-29T22:00:00Z',
    entryFeePoints: 50,
    prizePoolPoints: 5000,
    registeredParticipants: 84,
    maxParticipants: 100,
    minLevelRequired: 1,
    createdAt: '2026-10-28T08:00:00Z',
  },
  {
    id: 'TRN-2026-002',
    title: 'Tech Titans Championship',
    description: 'Algorithmic computing, hardware architecture, and software engineering trivia.',
    categoryId: 'cat_tech',
    categoryName: 'Technology & Computing',
    status: 'SCHEDULED',
    startTimeIso: '2026-10-30T12:00:00Z',
    endTimeIso: '2026-10-31T00:00:00Z',
    entryFeePoints: 100,
    prizePoolPoints: 10000,
    registeredParticipants: 42,
    maxParticipants: 250,
    minLevelRequired: 2,
    createdAt: '2026-10-28T09:30:00Z',
  },
  {
    id: 'TRN-2026-003',
    title: 'World Geography Blitz',
    description: 'Fast-paced global capitals, mountain ranges, and international borders.',
    categoryId: 'cat_geography',
    categoryName: 'Geography & Capitals',
    status: 'SCHEDULED',
    startTimeIso: '2026-11-01T14:00:00Z',
    endTimeIso: '2026-11-01T20:00:00Z',
    entryFeePoints: 25,
    prizePoolPoints: 2500,
    registeredParticipants: 18,
    maxParticipants: 150,
    minLevelRequired: 1,
    createdAt: '2026-10-28T14:00:00Z',
  },
  {
    id: 'TRN-2026-004',
    title: 'Autumn History Marathon',
    description: 'Test your historical knowledge of ancient empires and modern treaties.',
    categoryId: 'cat_history',
    categoryName: 'World History',
    status: 'ENDED',
    startTimeIso: '2026-10-25T10:00:00Z',
    endTimeIso: '2026-10-25T22:00:00Z',
    entryFeePoints: 50,
    prizePoolPoints: 5000,
    registeredParticipants: 95,
    maxParticipants: 100,
    minLevelRequired: 1,
    createdAt: '2026-10-24T12:00:00Z',
  },
  {
    id: 'TRN-2026-005',
    title: 'Flash Trivia Cup (Legacy)',
    description: 'Experimental speedrun competition cancelled due to question bank revisions.',
    categoryId: 'cat_pop_culture',
    categoryName: 'Pop Culture & Arts',
    status: 'CANCELLED',
    startTimeIso: '2026-10-26T16:00:00Z',
    endTimeIso: '2026-10-26T20:00:00Z',
    entryFeePoints: 50,
    prizePoolPoints: 2000,
    registeredParticipants: 16,
    maxParticipants: 50,
    minLevelRequired: 1,
    cancellationReason: 'Scheduled maintenance and question revision. All 16 entry fees refunded.',
    cancelledAtIso: '2026-10-26T15:30:00Z',
    totalRefundedPoints: 800,
    createdAt: '2026-10-26T10:00:00Z',
  },
];

export const INITIAL_TASKS: AdminTask[] = [
  {
    id: 'TSK-DLY-001',
    title: 'Daily Quiz Master',
    description: 'Complete 3 quiz sessions across any category today.',
    type: 'DAILY',
    criterion: 'QUIZ_COMPLETION',
    targetGoal: 3,
    rewardPoints: 25,
    isActive: true,
    createdAt: '2026-10-20T00:00:00Z',
  },
  {
    id: 'TSK-DLY-002',
    title: 'Accuracy Ace',
    description: 'Achieve 80% or higher score accuracy on any standard quiz.',
    type: 'DAILY',
    criterion: 'ACCURACY_SCORE',
    targetGoal: 80,
    rewardPoints: 35,
    isActive: true,
    createdAt: '2026-10-20T00:00:00Z',
  },
  {
    id: 'TSK-DLY-003',
    title: 'Science Scholar',
    description: 'Answer at least 5 questions correctly in Science & Nature.',
    type: 'DAILY',
    criterion: 'CATEGORY_QUIZ',
    targetGoal: 5,
    rewardPoints: 30,
    categoryKey: 'cat_science',
    isActive: true,
    createdAt: '2026-10-21T00:00:00Z',
  },
  {
    id: 'TSK-WKL-001',
    title: 'Weekly Trivia Titan',
    description: 'Complete 15 quizzes over the course of the current week.',
    type: 'WEEKLY',
    criterion: 'QUIZ_COMPLETION',
    targetGoal: 15,
    rewardPoints: 150,
    isActive: true,
    createdAt: '2026-10-20T00:00:00Z',
  },
  {
    id: 'TSK-WKL-002',
    title: 'Flawless Week',
    description: 'Complete 3 quizzes with 100% accuracy this week.',
    type: 'WEEKLY',
    criterion: 'ACCURACY_SCORE',
    targetGoal: 3,
    rewardPoints: 200,
    isActive: true,
    createdAt: '2026-10-20T00:00:00Z',
  },
  {
    id: 'TSK-WKL-003',
    title: 'Tech Mastery',
    description: 'Accumulate 200 points in Technology & Computing category.',
    type: 'WEEKLY',
    criterion: 'CATEGORY_QUIZ',
    targetGoal: 200,
    rewardPoints: 120,
    categoryKey: 'cat_tech',
    isActive: false,
    createdAt: '2026-10-22T00:00:00Z',
  },
];

const TOURNAMENTS_KEY = 'quizearn_admin_tournaments';
const TASKS_KEY = 'quizearn_admin_tasks';

export function loadStoredTournaments(): AdminTournament[] {
  if (typeof window === 'undefined') return INITIAL_TOURNAMENTS;
  try {
    const raw = localStorage.getItem(TOURNAMENTS_KEY);
    if (!raw) {
      localStorage.setItem(TOURNAMENTS_KEY, JSON.stringify(INITIAL_TOURNAMENTS));
      return INITIAL_TOURNAMENTS;
    }
    return JSON.parse(raw);
  } catch {
    return INITIAL_TOURNAMENTS;
  }
}

export function saveStoredTournaments(tournaments: AdminTournament[]): void {
  if (typeof window === 'undefined') return;
  try {
    localStorage.setItem(TOURNAMENTS_KEY, JSON.stringify(tournaments));
  } catch (err) {
    console.error('Failed to persist tournaments to localStorage', err);
  }
}

export function loadStoredTasks(): AdminTask[] {
  if (typeof window === 'undefined') return INITIAL_TASKS;
  try {
    const raw = localStorage.getItem(TASKS_KEY);
    if (!raw) {
      localStorage.setItem(TASKS_KEY, JSON.stringify(INITIAL_TASKS));
      return INITIAL_TASKS;
    }
    return JSON.parse(raw);
  } catch {
    return INITIAL_TASKS;
  }
}

export function saveStoredTasks(tasks: AdminTask[]): void {
  if (typeof window === 'undefined') return;
  try {
    localStorage.setItem(TASKS_KEY, JSON.stringify(tasks));
  } catch (err) {
    console.error('Failed to persist tasks to localStorage', err);
  }
}
