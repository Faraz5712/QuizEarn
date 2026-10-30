'use client';

import { AdminRedemption, AdminReward } from '../types/reward';

export const INITIAL_REWARDS: AdminReward[] = [
  {
    id: 'RWD-AMZ-005',
    title: '$5 Amazon Gift Card',
    description: 'Digital gift card claim code redeemable on Amazon.com for millions of items.',
    pointsCost: 500,
    stockCount: 24,
    category: 'GIFT_CARD',
    minLevelRequired: 1,
    provider: 'Amazon',
    terms: 'Valid on US accounts. Code delivered instantly upon administrative approval.',
    isActive: true,
    createdAt: '2026-10-20T00:00:00Z',
  },
  {
    id: 'RWD-AMZ-010',
    title: '$10 Amazon Gift Card',
    description: 'Digital gift card voucher with discounted bulk points redemption rate.',
    pointsCost: 950,
    stockCount: 12,
    category: 'GIFT_CARD',
    minLevelRequired: 2,
    provider: 'Amazon',
    terms: 'Requires Player Level 2. Digital code issued to verified account email.',
    isActive: true,
    createdAt: '2026-10-20T00:00:00Z',
  },
  {
    id: 'RWD-GPL-005',
    title: '$5 Google Play Code',
    description: 'Top up your Google Play balance for apps, games, books, and movies.',
    pointsCost: 500,
    stockCount: 30,
    category: 'GIFT_CARD',
    minLevelRequired: 1,
    provider: 'Google Play',
    terms: 'Redeemable via Google Play Store on Android devices.',
    isActive: true,
    createdAt: '2026-10-21T00:00:00Z',
  },
  {
    id: 'RWD-GPL-010',
    title: '$10 Google Play Code',
    description: 'Tier 2 Google Play store credit voucher code.',
    pointsCost: 950,
    stockCount: 15,
    category: 'GIFT_CARD',
    minLevelRequired: 2,
    provider: 'Google Play',
    terms: 'Redeemable on all Android Google Play accounts.',
    isActive: true,
    createdAt: '2026-10-21T00:00:00Z',
  },
  {
    id: 'RWD-PPL-010',
    title: '$10 PayPal Cashout',
    description: 'Direct cash deposit transfer sent to your confirmed PayPal address.',
    pointsCost: 1000,
    stockCount: 45,
    category: 'CASHOUT',
    minLevelRequired: 2,
    provider: 'PayPal',
    terms: 'Requires confirmed PayPal email address. Transferred within 24-48 business hours.',
    isActive: true,
    createdAt: '2026-10-22T00:00:00Z',
  },
  {
    id: 'RWD-PPL-025',
    title: '$25 PayPal Cashout',
    description: 'High-tier real money cashout reward for experienced players.',
    pointsCost: 2400,
    stockCount: 18,
    category: 'CASHOUT',
    minLevelRequired: 3,
    provider: 'PayPal',
    terms: 'Requires Player Level 3. Subject to anti-fraud review prior to disbursement.',
    isActive: true,
    createdAt: '2026-10-22T00:00:00Z',
  },
  {
    id: 'RWD-STM-010',
    title: '$10 Steam Wallet Code',
    description: 'Redeem directly on Steam to purchase PC games and DLC content.',
    pointsCost: 1000,
    stockCount: 8,
    category: 'DIGITAL_VOUCHER',
    minLevelRequired: 2,
    provider: 'Valve / Steam',
    terms: 'Global activation key delivered to user in-app inbox.',
    isActive: true,
    createdAt: '2026-10-23T00:00:00Z',
  },
  {
    id: 'RWD-SPT-001',
    title: 'Spotify 1-Month Premium',
    description: 'Enjoy uninterrupted ad-free music and podcast streaming on any device.',
    pointsCost: 600,
    stockCount: 20,
    category: 'DIGITAL_VOUCHER',
    minLevelRequired: 1,
    provider: 'Spotify',
    terms: 'Redeemable for new or existing standard Spotify individual subscriptions.',
    isActive: true,
    createdAt: '2026-10-23T00:00:00Z',
  },
];

export const INITIAL_REDEMPTIONS: AdminRedemption[] = [
  {
    id: 'RDM-2026-101',
    userId: 'usr_sarah_connor',
    userName: 'Sarah Connor',
    userEmail: 'sarah.c@resistance.org',
    rewardId: 'RWD-PPL-010',
    rewardTitle: '$10 PayPal Cashout',
    rewardCategory: 'CASHOUT',
    pointsDeducted: 1000,
    payoutDetails: 'sarah.c@resistance.org (PayPal)',
    status: 'PENDING',
    requestedAtIso: '2026-10-29T11:20:00Z',
  },
  {
    id: 'RDM-2026-102',
    userId: 'usr_alex_dev',
    userName: 'Alex Turner',
    userEmail: 'alex.turner@devlabs.io',
    rewardId: 'RWD-AMZ-010',
    rewardTitle: '$10 Amazon Gift Card',
    rewardCategory: 'GIFT_CARD',
    pointsDeducted: 950,
    payoutDetails: 'alex.turner@devlabs.io',
    status: 'PENDING',
    requestedAtIso: '2026-10-29T12:05:00Z',
  },
  {
    id: 'RDM-2026-103',
    userId: 'usr_marcus_pro',
    userName: 'Marcus Vance',
    userEmail: 'marcus.v@speedmail.com',
    rewardId: 'RWD-GPL-005',
    rewardTitle: '$5 Google Play Code',
    rewardCategory: 'GIFT_CARD',
    pointsDeducted: 500,
    payoutDetails: 'marcus.v@speedmail.com',
    status: 'PENDING',
    requestedAtIso: '2026-10-29T13:40:00Z',
  },
  {
    id: 'RDM-2026-104',
    userId: 'usr_clara_quiz',
    userName: 'Clara Oswald',
    userEmail: 'clara.o@tardis.ac.uk',
    rewardId: 'RWD-SPT-001',
    rewardTitle: 'Spotify 1-Month Premium',
    rewardCategory: 'DIGITAL_VOUCHER',
    pointsDeducted: 600,
    payoutDetails: 'clara.o@tardis.ac.uk',
    status: 'APPROVED',
    digitalCodeOrReference: 'SPOT-8842-KL99-PQ12',
    requestedAtIso: '2026-10-28T15:00:00Z',
    processedAtIso: '2026-10-28T16:15:00Z',
    processedByAdminId: 'adm_support_01',
  },
  {
    id: 'RDM-2026-105',
    userId: 'usr_demo_001',
    userName: 'John Doe',
    userEmail: 'j.doe@example.com',
    rewardId: 'RWD-PPL-025',
    rewardTitle: '$25 PayPal Cashout',
    rewardCategory: 'CASHOUT',
    pointsDeducted: 2400,
    payoutDetails: 'j.doe@example.com',
    status: 'REJECTED',
    rejectionReason: 'Unverified PayPal recipient address and high risk score flag on rapid score accumulation.',
    requestedAtIso: '2026-10-27T09:10:00Z',
    processedAtIso: '2026-10-27T10:30:00Z',
    processedByAdminId: 'adm_super_01',
  },
  {
    id: 'RDM-2026-106',
    userId: 'usr_emily_w',
    userName: 'Emily Watson',
    userEmail: 'emily.w@gamersguild.net',
    rewardId: 'RWD-STM-010',
    rewardTitle: '$10 Steam Wallet Code',
    rewardCategory: 'DIGITAL_VOUCHER',
    pointsDeducted: 1000,
    payoutDetails: 'emily.w@gamersguild.net',
    status: 'APPROVED',
    digitalCodeOrReference: 'STEAM-9081-3341-XV88',
    requestedAtIso: '2026-10-26T18:22:00Z',
    processedAtIso: '2026-10-26T19:00:00Z',
    processedByAdminId: 'adm_support_01',
  },
];

const REWARDS_KEY = 'quizearn_admin_rewards';
const REDEMPTIONS_KEY = 'quizearn_admin_redemptions';

export function loadStoredRewards(): AdminReward[] {
  if (typeof window === 'undefined') return INITIAL_REWARDS;
  try {
    const raw = localStorage.getItem(REWARDS_KEY);
    if (!raw) {
      localStorage.setItem(REWARDS_KEY, JSON.stringify(INITIAL_REWARDS));
      return INITIAL_REWARDS;
    }
    return JSON.parse(raw);
  } catch {
    return INITIAL_REWARDS;
  }
}

export function saveStoredRewards(rewards: AdminReward[]): void {
  if (typeof window === 'undefined') return;
  try {
    localStorage.setItem(REWARDS_KEY, JSON.stringify(rewards));
  } catch (err) {
    console.error('Failed to persist rewards to localStorage', err);
  }
}

export function loadStoredRedemptions(): AdminRedemption[] {
  if (typeof window === 'undefined') return INITIAL_REDEMPTIONS;
  try {
    const raw = localStorage.getItem(REDEMPTIONS_KEY);
    if (!raw) {
      localStorage.setItem(REDEMPTIONS_KEY, JSON.stringify(INITIAL_REDEMPTIONS));
      return INITIAL_REDEMPTIONS;
    }
    return JSON.parse(raw);
  } catch {
    return INITIAL_REDEMPTIONS;
  }
}

export function saveStoredRedemptions(redemptions: AdminRedemption[]): void {
  if (typeof window === 'undefined') return;
  try {
    localStorage.setItem(REDEMPTIONS_KEY, JSON.stringify(redemptions));
  } catch (err) {
    console.error('Failed to persist redemptions to localStorage', err);
  }
}
