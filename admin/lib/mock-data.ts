import { AdminUser, DashboardMetrics, RecentActivityItem } from '../types/admin';

export const DEMO_ADMIN_USERS: Record<string, AdminUser> = {
  super: {
    uid: 'admin-super-01',
    email: 'admin@quizearn.com',
    displayName: 'Lead System Admin',
    role: 'SUPER_ADMIN',
    assignedAt: '2026-01-01T00:00:00Z',
    lastLogin: '2026-10-29T13:45:00Z',
    isActive: true,
  },
  content: {
    uid: 'admin-content-01',
    email: 'content@quizearn.com',
    displayName: 'Trivia Content Editor',
    role: 'CONTENT_MANAGER',
    assignedAt: '2026-01-15T00:00:00Z',
    lastLogin: '2026-10-29T11:20:00Z',
    isActive: true,
  },
  support: {
    uid: 'admin-support-01',
    email: 'support@quizearn.com',
    displayName: 'Tier 2 Support Specialist',
    role: 'SUPPORT_AGENT',
    assignedAt: '2026-02-01T00:00:00Z',
    lastLogin: '2026-10-29T12:05:00Z',
    isActive: true,
  },
};

export const MOCK_DASHBOARD_METRICS: DashboardMetrics = {
  totalUsers: 24850,
  totalUsersGrowthPct: 12.4,
  activeUsersToday: 3420,
  activeUsersGrowthPct: 8.7,
  quizzesCompletedToday: 8940,
  quizPassRatePct: 74.2,
  totalPointsIssued: 12480000,
  totalPointsRedeemed: 7850000,
  pendingRedemptionsCount: 14,
  openSupportTicketsCount: 6,
};

export const MOCK_RECENT_ACTIVITIES: RecentActivityItem[] = [
  {
    id: 'ACT-001',
    type: 'REDEMPTION_REQUEST',
    title: 'Amazon $10 Gift Card Claim',
    subtitle: 'Requested by player @sarah_q (5,000 points)',
    timestamp: '10 minutes ago',
    status: 'PENDING',
    amount: 5000,
  },
  {
    id: 'ACT-002',
    type: 'REDEMPTION_APPROVED',
    title: 'Google Play $5 Voucher Issued',
    subtitle: 'Approved by Support Agent (code: GPL-8942-XXXX)',
    timestamp: '25 minutes ago',
    status: 'APPROVED',
    amount: 2500,
  },
  {
    id: 'ACT-003',
    type: 'SUPPORT_TICKET_OPENED',
    title: 'Ticket #TKT-8291 Opened: Question Discrepancy',
    subtitle: 'Category: Gameplay & Questions — User: @alex_99',
    timestamp: '42 minutes ago',
    status: 'PENDING',
  },
  {
    id: 'ACT-004',
    type: 'TOURNAMENT_FINALIZED',
    title: 'Weekend Science Sprint Finalized',
    subtitle: 'Top 3 podium winners credited 5,000 pts pool',
    timestamp: '1 hour ago',
    status: 'RESOLVED',
    amount: 5000,
  },
  {
    id: 'ACT-005',
    type: 'REDEMPTION_REJECTED',
    title: 'Apple $15 Card Rejected with Point Refund',
    subtitle: 'Reason: Fraudulent multiple accounts — Refunded 7,500 pts',
    timestamp: '2 hours ago',
    status: 'REJECTED',
    amount: 7500,
  },
  {
    id: 'ACT-006',
    type: 'QUIZ_COMPLETED',
    title: 'Mega Trivia Session Spike',
    subtitle: '1,200 sessions completed in World Geography',
    timestamp: '3 hours ago',
    status: 'INFO',
  },
];
