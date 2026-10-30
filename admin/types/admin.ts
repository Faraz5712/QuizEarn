/**
 * QuizEarn Admin Panel — Type Definitions & RBAC Contracts
 */

export type AdminRole = 'SUPER_ADMIN' | 'CONTENT_MANAGER' | 'SUPPORT_AGENT';

export type AdminPermission =
  // SuperAdmin exclusive
  | 'MANAGE_ADMIN_ROLES'
  | 'VIEW_FINANCIAL_LEDGER'
  | 'VIEW_AUDIT_LOGS'
  | 'CONFIGURE_SYSTEM_SETTINGS'
  | 'OVERRIDE_DISPUTES'
  // Content Manager
  | 'MANAGE_CATEGORIES'
  | 'MANAGE_QUESTIONS'
  | 'BULK_IMPORT_QUESTIONS'
  | 'MANAGE_TASKS'
  | 'MANAGE_TOURNAMENTS'
  // Support Agent / Rewards Manager
  | 'VIEW_SUPPORT_TICKETS'
  | 'REPLY_SUPPORT_TICKETS'
  | 'RESOLVE_SUPPORT_TICKETS'
  | 'VIEW_USER_PROFILES'
  | 'REVIEW_REDEMPTIONS'
  | 'APPROVE_REDEMPTION'
  | 'REJECT_REDEMPTION'
  | 'MANAGE_REWARDS';

export interface AdminUser {
  uid: string;
  email: string;
  displayName: string;
  role: AdminRole;
  assignedAt: string;
  lastLogin?: string;
  isActive: boolean;
}

export interface DashboardMetrics {
  totalUsers: number;
  totalUsersGrowthPct: number;
  activeUsersToday: number;
  activeUsersGrowthPct: number;
  quizzesCompletedToday: number;
  quizPassRatePct: number;
  totalPointsIssued: number;
  totalPointsRedeemed: number;
  pendingRedemptionsCount: number;
  openSupportTicketsCount: number;
}

export type ActivityType =
  | 'QUIZ_COMPLETED'
  | 'REDEMPTION_REQUEST'
  | 'REDEMPTION_APPROVED'
  | 'REDEMPTION_REJECTED'
  | 'SUPPORT_TICKET_OPENED'
  | 'TOURNAMENT_FINALIZED'
  | 'SYSTEM_ALERT';

export interface RecentActivityItem {
  id: string;
  type: ActivityType;
  title: string;
  subtitle: string;
  timestamp: string;
  status?: 'PENDING' | 'APPROVED' | 'REJECTED' | 'RESOLVED' | 'INFO';
  amount?: number;
}

export interface BreadcrumbItem {
  label: string;
  href?: string;
}
