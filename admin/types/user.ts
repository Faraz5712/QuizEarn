/**
 * QuizEarn Admin Panel — Player Moderation & Transaction Ledger Types
 */

export type AdminUserAccountStatus = 'ACTIVE' | 'SUSPENDED';

export type AdminLedgerTransactionType = 'CREDIT' | 'DEBIT';

export interface AdminLedgerTransaction {
  id: string;
  userId: string;
  type: AdminLedgerTransactionType;
  amount: number;
  balanceAfter: number;
  reason: string; // WELCOME_BONUS, QUIZ_REWARD, TASK_COMPLETED, TOURNAMENT_PRIZE, REDEMPTION_HOLD, REDEMPTION_REFUND
  referenceId?: string;
  description: string;
  timestampIso: string;
}

export interface AdminPlayer {
  id: string;
  displayName: string;
  email: string;
  avatarColor: string;
  level: number;
  pointsBalance: number;
  quizzesPlayed: number;
  accuracyRate: number; // percentage (0-100)
  winStreak: number;
  status: AdminUserAccountStatus;
  statusReason?: string;
  statusUpdatedAtIso?: string;
  createdAtIso: string;
  lastActiveIso: string;
  ledger: AdminLedgerTransaction[];
}
