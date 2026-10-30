'use client';

import { AdminAuditLogEntry } from '../types/audit';

function generateChecksum(id: string, timestampIso: string, action: string): string {
  // Deterministic simulation of SHA-256 cryptographic audit signature
  let hash = 0;
  const str = `${id}:${timestampIso}:${action}:quizearn-immutable-salt`;
  for (let i = 0; i < str.length; i++) {
    hash = (hash << 5) - hash + str.charCodeAt(i);
    hash |= 0;
  }
  const hex = Math.abs(hash).toString(16).padStart(8, '0');
  return `SHA256:${hex.repeat(4).slice(0, 32)}`;
}

export const INITIAL_AUDIT_LOGS: AdminAuditLogEntry[] = [
  {
    id: 'AUD-2026-001',
    adminId: 'admin-super-01',
    adminEmail: 'admin@quizearn.com',
    adminRole: 'SUPER_ADMIN',
    action: 'SYSTEM_CONFIG_UPDATE',
    targetType: 'SYSTEM',
    targetId: 'sys_rbac_v1',
    summary: 'Initialized Admin RBAC policy and custom claims hierarchy',
    details: {
      policyVersion: '1.0.0',
      rolesConfigured: ['SUPER_ADMIN', 'CONTENT_MANAGER', 'SUPPORT_AGENT'],
      claimsEnforcement: 'STRICT_FIREBASE_CUSTOM_CLAIMS',
    },
    ipAddress: '10.0.4.12',
    userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/130.0.0.0',
    checksumSignature: generateChecksum('AUD-2026-001', '2026-10-24T08:00:00Z', 'SYSTEM_CONFIG_UPDATE'),
    timestampIso: '2026-10-24T08:00:00Z',
  },
  {
    id: 'AUD-2026-002',
    adminId: 'admin-content-01',
    adminEmail: 'content@quizearn.com',
    adminRole: 'CONTENT_MANAGER',
    action: 'QUESTION_BULK_IMPORT',
    targetType: 'QUESTION',
    targetId: 'bulk_batch_001',
    summary: 'Ingested 25 trivia questions across 6 categories via verified CSV parser',
    details: {
      totalParsed: 25,
      categoriesCount: 6,
      easyCount: 10,
      mediumCount: 10,
      hardCount: 5,
      validationErrors: 0,
    },
    ipAddress: '192.168.1.45',
    userAgent: 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) Chrome/130.0.0.0',
    checksumSignature: generateChecksum('AUD-2026-002', '2026-10-25T11:15:00Z', 'QUESTION_BULK_IMPORT'),
    timestampIso: '2026-10-25T11:15:00Z',
  },
  {
    id: 'AUD-2026-003',
    adminId: 'admin-super-01',
    adminEmail: 'admin@quizearn.com',
    adminRole: 'SUPER_ADMIN',
    action: 'TOURNAMENT_CANCEL',
    targetType: 'TOURNAMENT',
    targetId: 'TRN-2026-005',
    summary: 'Executed emergency cancellation for Flash Trivia Cup with automated player refunds',
    details: {
      tournamentTitle: 'Flash Trivia Cup (Legacy)',
      entryFeePoints: 50,
      registeredParticipants: 16,
      totalRefundedPoints: 800,
      ledgerCreditReason: 'TOURNAMENT_REFUND',
    },
    reason: 'Scheduled maintenance and question revision. All 16 entry fees refunded.',
    ipAddress: '10.0.4.12',
    userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/130.0.0.0',
    checksumSignature: generateChecksum('AUD-2026-003', '2026-10-26T15:30:00Z', 'TOURNAMENT_CANCEL'),
    timestampIso: '2026-10-26T15:30:00Z',
  },
  {
    id: 'AUD-2026-004',
    adminId: 'admin-super-01',
    adminEmail: 'admin@quizearn.com',
    adminRole: 'SUPER_ADMIN',
    action: 'REDEMPTION_REJECT',
    targetType: 'REDEMPTION',
    targetId: 'RDM-2026-105',
    summary: 'Rejected $25 PayPal cashout and refunded 2,400 points to player wallet',
    details: {
      userId: 'usr_demo_001',
      userName: 'John Doe',
      pointsDeducted: 2400,
      rewardTitle: '$25 PayPal Cashout',
      refundTxnId: 'TXN-5542',
      restoredStockItem: 'RWD-PPL-025',
    },
    reason: 'Unverified PayPal recipient address and high risk score flag on rapid score accumulation.',
    ipAddress: '10.0.4.12',
    userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/130.0.0.0',
    checksumSignature: generateChecksum('AUD-2026-004', '2026-10-27T10:30:00Z', 'REDEMPTION_REJECT'),
    timestampIso: '2026-10-27T10:30:00Z',
  },
  {
    id: 'AUD-2026-005',
    adminId: 'admin-super-01',
    adminEmail: 'admin@quizearn.com',
    adminRole: 'SUPER_ADMIN',
    action: 'USER_SUSPEND',
    targetType: 'USER',
    targetId: 'usr_demo_001',
    summary: 'Suspended player account following repeated automated answer scripting detections',
    details: {
      userId: 'usr_demo_001',
      displayName: 'John Doe',
      email: 'j.doe@example.com',
      currentBalance: 2400,
      sessionTokenRevoked: true,
    },
    reason: 'Flagged for abusive script automation and multiple concurrent sessions from suspicious proxies.',
    ipAddress: '10.0.4.12',
    userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/130.0.0.0',
    checksumSignature: generateChecksum('AUD-2026-005', '2026-10-27T10:35:00Z', 'USER_SUSPEND'),
    timestampIso: '2026-10-27T10:35:00Z',
  },
  {
    id: 'AUD-2026-006',
    adminId: 'admin-support-01',
    adminEmail: 'support@quizearn.com',
    adminRole: 'SUPPORT_AGENT',
    action: 'REDEMPTION_APPROVE',
    targetType: 'REDEMPTION',
    targetId: 'RDM-2026-104',
    summary: 'Approved Spotify 1-Month Premium redemption and issued digital claim voucher',
    details: {
      userId: 'usr_clara_quiz',
      userName: 'Clara Oswald',
      pointsCost: 600,
      digitalCodeIssued: 'SPOT-8842-KL99-PQ12',
      payoutEmail: 'clara.o@tardis.ac.uk',
    },
    ipAddress: '172.16.0.8',
    userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Firefox/131.0',
    checksumSignature: generateChecksum('AUD-2026-006', '2026-10-28T16:15:00Z', 'REDEMPTION_APPROVE'),
    timestampIso: '2026-10-28T16:15:00Z',
  },
  {
    id: 'AUD-2026-007',
    adminId: 'admin-support-01',
    adminEmail: 'support@quizearn.com',
    adminRole: 'SUPPORT_AGENT',
    action: 'SUPPORT_TICKET_REPLY',
    targetType: 'SUPPORT_TICKET',
    targetId: 'TCK-2026-002',
    summary: 'Dispatched administrative response to player regarding claim review status',
    details: {
      ticketSubject: 'Amazon Gift Card claim processing status',
      userId: 'usr_alex_dev',
      replyLength: 218,
      newStatus: 'IN_PROGRESS',
    },
    ipAddress: '172.16.0.8',
    userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Firefox/131.0',
    checksumSignature: generateChecksum('AUD-2026-007', '2026-10-29T12:45:00Z', 'SUPPORT_TICKET_REPLY'),
    timestampIso: '2026-10-29T12:45:00Z',
  },
  {
    id: 'AUD-2026-008',
    adminId: 'admin-content-01',
    adminEmail: 'content@quizearn.com',
    adminRole: 'CONTENT_MANAGER',
    action: 'REWARD_STOCK_ADJUST',
    targetType: 'REWARD',
    targetId: 'RWD-AMZ-010',
    summary: 'Adjusted stock inventory for $10 Amazon Gift Card (+10 units)',
    details: {
      rewardTitle: '$10 Amazon Gift Card',
      previousStock: 2,
      newStock: 12,
      delta: 10,
      source: 'Restock batch from verified vendor purchase order',
    },
    ipAddress: '192.168.1.45',
    userAgent: 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) Chrome/130.0.0.0',
    checksumSignature: generateChecksum('AUD-2026-008', '2026-10-29T13:00:00Z', 'REWARD_STOCK_ADJUST'),
    timestampIso: '2026-10-29T13:00:00Z',
  },
  {
    id: 'AUD-2026-009',
    adminId: 'admin-super-01',
    adminEmail: 'admin@quizearn.com',
    adminRole: 'SUPER_ADMIN',
    action: 'SECURITY_AUDIT_PASS',
    targetType: 'SYSTEM',
    targetId: 'sys_sec_audit_q4',
    summary: 'Executed automated secret scan and client/server RBAC authorization test suite',
    details: {
      hardcodedSecretsFound: 0,
      unauthorizedRoutesBlocked: true,
      nonPrivilegedTokenRejections: 'VERIFIED_403_FORBIDDEN',
      hashChainIntegrity: '100%_VALID',
    },
    ipAddress: '10.0.4.12',
    userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/130.0.0.0',
    checksumSignature: generateChecksum('AUD-2026-009', '2026-10-29T14:45:00Z', 'SECURITY_AUDIT_PASS'),
    timestampIso: '2026-10-29T14:45:00Z',
  },
];

const AUDIT_LOGS_KEY = 'quizearn_admin_audit_logs';

export function loadStoredAuditLogs(): AdminAuditLogEntry[] {
  if (typeof window === 'undefined') return INITIAL_AUDIT_LOGS;
  try {
    const raw = localStorage.getItem(AUDIT_LOGS_KEY);
    if (!raw) {
      localStorage.setItem(AUDIT_LOGS_KEY, JSON.stringify(INITIAL_AUDIT_LOGS));
      return INITIAL_AUDIT_LOGS;
    }
    return JSON.parse(raw);
  } catch {
    return INITIAL_AUDIT_LOGS;
  }
}

export function saveStoredAuditLogs(logs: AdminAuditLogEntry[]): void {
  if (typeof window === 'undefined') return;
  try {
    localStorage.setItem(AUDIT_LOGS_KEY, JSON.stringify(logs));
  } catch (err) {
    console.error('Failed to persist audit logs to localStorage', err);
  }
}

export function recordAuditLog(
  entry: Omit<AdminAuditLogEntry, 'id' | 'timestampIso' | 'checksumSignature'>
): AdminAuditLogEntry {
  const logs = loadStoredAuditLogs();
  const id = `AUD-2026-${Date.now().toString().slice(-4)}`;
  const timestampIso = new Date().toISOString();
  const checksumSignature = generateChecksum(id, timestampIso, entry.action);

  const fullEntry: AdminAuditLogEntry = {
    id,
    timestampIso,
    checksumSignature,
    ...entry,
  };

  const updated = [fullEntry, ...logs];
  saveStoredAuditLogs(updated);
  return fullEntry;
}

export function exportAuditLogsToJSON(logs: AdminAuditLogEntry[]): string {
  return JSON.stringify(logs, null, 2);
}

export function exportAuditLogsToCSV(logs: AdminAuditLogEntry[]): string {
  const headers = [
    'Log ID',
    'Timestamp (UTC)',
    'Operator Email',
    'Role',
    'Action',
    'Target Type',
    'Target ID',
    'Summary',
    'Reason',
    'IP Address',
    'Checksum Signature',
  ];

  const rows = logs.map((log) => [
    log.id,
    log.timestampIso,
    log.adminEmail,
    log.adminRole,
    log.action,
    log.targetType,
    log.targetId,
    `"${(log.summary || '').replace(/"/g, '""')}"`,
    `"${(log.reason || '').replace(/"/g, '""')}"`,
    log.ipAddress,
    log.checksumSignature,
  ]);

  return [headers.join(','), ...rows.map((r) => r.join(','))].join('\n');
}
