import { AdminPermission, AdminRole } from '../types/admin';

export interface NavItemConfig {
  id: string;
  label: string;
  href: string;
  iconName: string;
  badge?: string;
  badgeVariant?: 'default' | 'warning' | 'danger';
  requiredPermission?: AdminPermission;
}

/**
 * Evaluates whether an administrative role is authorized for a specific permission.
 * SUPER_ADMIN inherits 100% of permissions.
 */
export function hasPermission(role: AdminRole, permission: AdminPermission): boolean {
  if (role === 'SUPER_ADMIN') {
    return true;
  }

  switch (role) {
    case 'CONTENT_MANAGER':
      return [
        'MANAGE_CATEGORIES',
        'MANAGE_QUESTIONS',
        'BULK_IMPORT_QUESTIONS',
        'MANAGE_TASKS',
        'MANAGE_TOURNAMENTS',
        'MANAGE_REWARDS',
      ].includes(permission);

    case 'SUPPORT_AGENT':
      return [
        'VIEW_SUPPORT_TICKETS',
        'REPLY_SUPPORT_TICKETS',
        'RESOLVE_SUPPORT_TICKETS',
        'VIEW_USER_PROFILES',
        'REVIEW_REDEMPTIONS',
        'APPROVE_REDEMPTION',
        'REJECT_REDEMPTION',
        'MANAGE_REWARDS',
      ].includes(permission);

    default:
      return false;
  }
}

/**
 * Presentation configuration for administrative roles.
 */
export const ROLE_CONFIG: Record<
  AdminRole,
  {
    displayName: string;
    level: number;
    badgeBg: string;
    badgeText: string;
    border: string;
    description: string;
  }
> = {
  SUPER_ADMIN: {
    displayName: 'Super Administrator',
    level: 100,
    badgeBg: 'bg-purple-900/40',
    badgeText: 'text-purple-300',
    border: 'border-purple-500/40',
    description: 'Full root access to all system modules, financial ledgers, and audit logs.',
  },
  CONTENT_MANAGER: {
    displayName: 'Content Manager',
    level: 50,
    badgeBg: 'bg-indigo-900/40',
    badgeText: 'text-indigo-300',
    border: 'border-indigo-500/40',
    description: 'Trivia question authoring, category management, and tournament scheduling.',
  },
  SUPPORT_AGENT: {
    displayName: 'Support Agent',
    level: 30,
    badgeBg: 'bg-teal-900/40',
    badgeText: 'text-teal-300',
    border: 'border-teal-500/40',
    description: 'Voucher redemption approvals/refunds, user inquiry replies, and ticket resolution.',
  },
};

/**
 * Sidebar navigation catalog with RBAC filtering.
 */
export const NAVIGATION_ITEMS: NavItemConfig[] = [
  {
    id: 'overview',
    label: 'Overview',
    href: '/',
    iconName: 'chart',
  },
  {
    id: 'questions',
    label: 'Question Bank',
    href: '/questions',
    iconName: 'quiz',
    requiredPermission: 'MANAGE_QUESTIONS',
  },
  {
    id: 'tournaments',
    label: 'Tournaments & Tasks',
    href: '/tournaments',
    iconName: 'trophy',
    requiredPermission: 'MANAGE_TOURNAMENTS',
  },
  {
    id: 'rewards',
    label: 'Rewards Catalog',
    href: '/rewards',
    iconName: 'gift',
    requiredPermission: 'MANAGE_REWARDS',
  },
  {
    id: 'redemptions',
    label: 'Redemptions',
    href: '/redemptions',
    iconName: 'gift',
    badge: 'Pending',
    badgeVariant: 'warning',
    requiredPermission: 'REVIEW_REDEMPTIONS',
  },
  {
    id: 'users',
    label: 'Players Directory',
    href: '/users',
    iconName: 'users',
    requiredPermission: 'VIEW_USER_PROFILES',
  },
  {
    id: 'support',
    label: 'Support Desk',
    href: '/support',
    iconName: 'help',
    badge: '6 Open',
    badgeVariant: 'default',
    requiredPermission: 'VIEW_SUPPORT_TICKETS',
  },
  {
    id: 'audit-logs',
    label: 'Audit Trail',
    href: '/audit-logs',
    iconName: 'shield',
    requiredPermission: 'VIEW_AUDIT_LOGS',
  },
];
