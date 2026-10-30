/**
 * QuizEarn Admin Panel — Customer Support Desk Types
 */

export type AdminTicketPriority = 'LOW' | 'MEDIUM' | 'HIGH';

export type AdminTicketStatus =
  | 'OPEN'
  | 'IN_PROGRESS'
  | 'RESOLVED'
  | 'CLOSED';

export type AdminTicketCategory =
  | 'ACCOUNT'
  | 'WALLET_POINTS'
  | 'QUIZ_TECHNICAL'
  | 'REDEMPTION'
  | 'OTHER';

export interface AdminTicketMessage {
  id: string;
  sender: 'USER' | 'SUPPORT';
  senderName: string;
  message: string;
  timestampIso: string;
}

export interface AdminTicket {
  id: string;
  userId: string;
  userName: string;
  userEmail: string;
  subject: string;
  category: AdminTicketCategory;
  priority: AdminTicketPriority;
  status: AdminTicketStatus;
  createdAtIso: string;
  updatedAtIso: string;
  messages: AdminTicketMessage[];
}
