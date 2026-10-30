'use client';

import { useAdminAuth } from '../../lib/auth-context';

export function BackendModeNotice() {
  const { isDemoMode } = useAdminAuth();
  return (
    <div
      role="status"
      className={`mx-4 mt-4 rounded-xl border px-4 py-2 text-xs ${
        isDemoMode
          ? 'border-amber-500/40 bg-amber-950/30 text-amber-200'
          : 'border-teal-500/40 bg-teal-950/30 text-teal-200'
      }`}
    >
      {isDemoMode
        ? 'Local demo mode: changes are browser-only and are not authenticated or persisted to Firebase.'
        : 'Firebase Auth is active. Question, category, task, tournament, reward, redemption, and user-list operations use authenticated callables. Dashboard metrics, support tickets, audit history, and account suspension remain unintegrated.'}
    </div>
  );
}
