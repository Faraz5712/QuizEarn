/**
 * QuizEarn Admin Panel — Question and Category Domain Types
 */

export type DifficultyLevel = 'EASY' | 'MEDIUM' | 'HARD';

export type QuestionStatus = 'ACTIVE' | 'DRAFT';

export interface AdminQuestion {
  id: string;
  categoryId: string;
  categoryName: string;
  text: string;
  options: [string, string, string, string];
  correctOptionIndex: number; // 0, 1, 2, or 3
  explanation: string;
  difficulty: DifficultyLevel;
  timeLimitSeconds: number; // 10 to 30s
  pointsReward: number; // default 10
  status: QuestionStatus;
  createdAt: string;
  updatedAt?: string;
}

export interface AdminCategory {
  id: string;
  name: string;
  slug: string;
  iconName: string;
  colorHex: string;
  displayOrder: number;
  isActive: boolean;
  questionCount: number;
  description?: string;
}

export interface BulkImportError {
  row: number;
  field: string;
  message: string;
}

export interface BulkImportResult {
  totalRows: number;
  validQuestions: Omit<AdminQuestion, 'id' | 'createdAt'>[];
  errors: BulkImportError[];
}
