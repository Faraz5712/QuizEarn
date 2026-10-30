'use client';

import { AdminTicket } from '../types/support';

export const INITIAL_TICKETS: AdminTicket[] = [
  {
    id: 'TCK-2026-001',
    userId: 'usr_sarah_connor',
    userName: 'Sarah Connor',
    userEmail: 'sarah.c@resistance.org',
    subject: 'Discrepancy in Tournament Prize points delivery',
    category: 'WALLET_POINTS',
    priority: 'MEDIUM',
    status: 'OPEN',
    createdAtIso: '2026-10-28T14:30:00Z',
    updatedAtIso: '2026-10-28T14:30:00Z',
    messages: [
      {
        id: 'MSG-001',
        sender: 'USER',
        senderName: 'Sarah Connor',
        message:
          'Hi team! I finished 1st in the Autumn History Marathon yesterday. My points updated from 250 to 2,750, but my profile screen showed a brief delay. Could you confirm the transaction ID in my ledger?',
        timestampIso: '2026-10-28T14:30:00Z',
      },
    ],
  },
  {
    id: 'TCK-2026-002',
    userId: 'usr_alex_dev',
    userName: 'Alex Turner',
    userEmail: 'alex.turner@devlabs.io',
    subject: 'Amazon Gift Card claim processing status',
    category: 'REDEMPTION',
    priority: 'HIGH',
    status: 'IN_PROGRESS',
    createdAtIso: '2026-10-29T12:10:00Z',
    updatedAtIso: '2026-10-29T12:45:00Z',
    messages: [
      {
        id: 'MSG-002-1',
        sender: 'USER',
        senderName: 'Alex Turner',
        message:
          'I submitted a redemption request for a $10 Amazon Gift Card around an hour ago (RDM-2026-102). Just checking when digital codes are typically distributed?',
        timestampIso: '2026-10-29T12:10:00Z',
      },
      {
        id: 'MSG-002-2',
        sender: 'SUPPORT',
        senderName: 'Support Desk (Agent Miller)',
        message:
          'Hello Alex! Thank you for contacting us. Your claim RDM-2026-102 has been queued and passed automated security verification. An administrator is currently reviewing the voucher stock and will dispatch your code shortly.',
        timestampIso: '2026-10-29T12:45:00Z',
      },
    ],
  },
  {
    id: 'TCK-2026-003',
    userId: 'usr_marcus_pro',
    userName: 'Marcus Vance',
    userEmail: 'marcus.v@speedmail.com',
    subject: 'Question timer froze on Question 7 in Science quiz',
    category: 'QUIZ_TECHNICAL',
    priority: 'LOW',
    status: 'RESOLVED',
    createdAtIso: '2026-10-27T08:00:00Z',
    updatedAtIso: '2026-10-27T11:20:00Z',
    messages: [
      {
        id: 'MSG-003-1',
        sender: 'USER',
        senderName: 'Marcus Vance',
        message:
          'While playing a science quiz on Android 14, question 7 stayed at 15 seconds for about 4 seconds before resuming. It did not affect my final score, just letting you know!',
        timestampIso: '2026-10-27T08:00:00Z',
      },
      {
        id: 'MSG-003-2',
        sender: 'SUPPORT',
        senderName: 'Support Desk (Admin)',
        message:
          'Hi Marcus! Thanks for flagging this. We verified server session metrics; the server-authoritative timer smoothly synchronized without penalty. We appreciate your report!',
        timestampIso: '2026-10-27T11:20:00Z',
      },
    ],
  },
  {
    id: 'TCK-2026-004',
    userId: 'usr_clara_quiz',
    userName: 'Clara Oswald',
    userEmail: 'clara.o@tardis.ac.uk',
    subject: 'Request to customize avatar hex color palette',
    category: 'ACCOUNT',
    priority: 'LOW',
    status: 'CLOSED',
    createdAtIso: '2026-10-25T16:00:00Z',
    updatedAtIso: '2026-10-26T09:00:00Z',
    messages: [
      {
        id: 'MSG-004-1',
        sender: 'USER',
        senderName: 'Clara Oswald',
        message: 'Can I pick a custom hex color for my profile avatar badge?',
        timestampIso: '2026-10-25T16:00:00Z',
      },
      {
        id: 'MSG-004-2',
        sender: 'SUPPORT',
        senderName: 'Support Desk (Agent Miller)',
        message:
          'Hi Clara, avatar colors are currently mapped to player tier levels and achievements. We have logged your suggestion for custom palette selection with the product team.',
        timestampIso: '2026-10-26T09:00:00Z',
      },
    ],
  },
  {
    id: 'TCK-2026-005',
    userId: 'usr_emily_w',
    userName: 'Emily Watson',
    userEmail: 'emily.w@gamersguild.net',
    subject: 'Weekly gaming challenge reward claim confirmation',
    category: 'WALLET_POINTS',
    priority: 'MEDIUM',
    status: 'OPEN',
    createdAtIso: '2026-10-29T10:15:00Z',
    updatedAtIso: '2026-10-29T10:15:00Z',
    messages: [
      {
        id: 'MSG-005-1',
        sender: 'USER',
        senderName: 'Emily Watson',
        message:
          'I completed the "Weekly Trivia Titan" task this morning. The +150 points reflect in my balance, but I wanted to make sure my streak remains protected for tomorrow.',
        timestampIso: '2026-10-29T10:15:00Z',
      },
    ],
  },
];

const TICKETS_KEY = 'quizearn_admin_support_tickets';

export function loadStoredTickets(): AdminTicket[] {
  if (typeof window === 'undefined') return INITIAL_TICKETS;
  try {
    const raw = localStorage.getItem(TICKETS_KEY);
    if (!raw) {
      localStorage.setItem(TICKETS_KEY, JSON.stringify(INITIAL_TICKETS));
      return INITIAL_TICKETS;
    }
    return JSON.parse(raw);
  } catch {
    return INITIAL_TICKETS;
  }
}

export function saveStoredTickets(tickets: AdminTicket[]): void {
  if (typeof window === 'undefined') return;
  try {
    localStorage.setItem(TICKETS_KEY, JSON.stringify(tickets));
  } catch (err) {
    console.error('Failed to persist tickets to localStorage', err);
  }
}
