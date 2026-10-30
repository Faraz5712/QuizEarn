/**
 * QuizEarn Admin Panel — Reward Catalog & Redemption Queue Types
 */

export type RewardCategory = 'GIFT_CARD' | 'CASHOUT' | 'DIGITAL_VOUCHER';

export interface AdminReward {
  id: string;
  title: string;
  description: string;
  pointsCost: number;
  stockCount: number;
  category: RewardCategory;
  minLevelRequired: number;
  provider: string; // e.g. Amazon, Google Play, PayPal, Steam
  terms?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt?: string;
}

export type AdminRedemptionStatus =
  | 'PENDING'
  | 'PROCESSING'
  | 'APPROVED'
  | 'REJECTED';

export interface AdminRedemption {
  id: string;
  userId: string;
  userName: string;
  userEmail: string;
  rewardId: string;
  rewardTitle: string;
  rewardCategory: RewardCategory;
  pointsDeducted: number;
  payoutDetails: string; // e.g. PayPal email, mobile phone, gift card recipient email
  status: AdminRedemptionStatus;
  digitalCodeOrReference?: string;
  rejectionReason?: string;
  requestedAtIso: string;
  processedAtIso?: string;
  processedByAdminId?: string;
}
