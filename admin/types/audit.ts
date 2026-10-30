/**
 * QuizEarn Admin Panel — Administrative Audit Trail Types
 */

import { AdminRole } from './admin';

export type AdminAuditAction =
  | 'USER_SUSPEND'
  | 'USER_REACTIVATE'
  | 'REDEMPTION_APPROVE'
  | 'REDEMPTION_REJECT'
  | 'TOURNAMENT_CREATE'
  | 'TOURNAMENT_UPDATE'
  | 'TOURNAMENT_CANCEL'
  | 'TASK_CREATE'
  | 'TASK_UPDATE'
  | 'TASK_DELETE'
  | 'QUESTION_CREATE'
  | 'QUESTION_UPDATE'
  | 'QUESTION_DELETE'
  | 'QUESTION_BULK_IMPORT'
  | 'CATEGORY_CREATE'
  | 'CATEGORY_UPDATE'
  | 'REWARD_CREATE'
  | 'REWARD_UPDATE'
  | 'REWARD_STOCK_ADJUST'
  | 'SUPPORT_TICKET_REPLY'
  | 'SUPPORT_TICKET_STATUS_CHANGE'
  | 'ADMIN_ROLE_CHANGE'
  | 'SYSTEM_CONFIG_UPDATE'
  | 'SECURITY_AUDIT_PASS';

export type AdminAuditTargetType =
  | 'USER'
  | 'REDEMPTION'
  | 'TOURNAMENT'
  | 'TASK'
  | 'QUESTION'
  | 'CATEGORY'
  | 'REWARD'
  | 'SUPPORT_TICKET'
  | 'SYSTEM';

export interface AdminAuditLogEntry {
  id: string;
  adminId: string;
  adminEmail: string;
  adminRole: AdminRole;
  action: AdminAuditAction;
  targetType: AdminAuditTargetType;
  targetId: string;
  summary: string;
  details: Record<string, unknown>;
  reason?: string;
  ipAddress: string;
  userAgent: string;
  checksumSignature: string;
  timestampIso: string;
}
