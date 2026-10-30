/**
 * QuizEarn Admin Panel — Tournament and Task Domain Types
 */

export type AdminTournamentStatus =
  | 'DRAFT'
  | 'SCHEDULED'
  | 'LIVE'
  | 'ENDED'
  | 'CANCELLED';

export interface AdminTournament {
  id: string;
  title: string;
  description: string;
  categoryId: string;
  categoryName: string;
  status: AdminTournamentStatus;
  startTimeIso: string;
  endTimeIso: string;
  entryFeePoints: number;
  prizePoolPoints: number;
  registeredParticipants: number;
  maxParticipants?: number;
  minLevelRequired: number;
  cancellationReason?: string;
  cancelledAtIso?: string;
  totalRefundedPoints?: number;
  createdAt: string;
}

export type AdminTaskType = 'DAILY' | 'WEEKLY';

export type AdminTaskCriterion =
  | 'QUIZ_COMPLETION'
  | 'ACCURACY_SCORE'
  | 'STREAK_MAINTAIN'
  | 'CATEGORY_QUIZ';

export interface AdminTask {
  id: string;
  title: string;
  description: string;
  type: AdminTaskType;
  criterion: AdminTaskCriterion;
  targetGoal: number; // e.g. 3 quizzes, 80% accuracy
  rewardPoints: number; // points credited upon completion
  categoryKey?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt?: string;
}
