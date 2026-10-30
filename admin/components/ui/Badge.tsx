import React from 'react';
import { AdminRole } from '../../types/admin';
import { ROLE_CONFIG } from '../../lib/rbac';

export function RoleBadge({ role }: { role: AdminRole }) {
  const config = ROLE_CONFIG[role];
  return (
    <span
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border ${config.badgeBg} ${config.badgeText} ${config.border}`}
    >
      {config.displayName}
    </span>
  );
}

export function StatusBadge({
  status,
  label,
}: {
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'RESOLVED' | 'INFO' | 'OPEN';
  label?: string;
}) {
  const text = label || status;

  switch (status) {
    case 'APPROVED':
    case 'RESOLVED':
      return (
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-teal-900/30 text-teal-300 border border-teal-500/30">
          {text}
        </span>
      );
    case 'PENDING':
    case 'OPEN':
      return (
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-amber-900/30 text-amber-300 border border-amber-500/30">
          {text}
        </span>
      );
    case 'REJECTED':
      return (
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-rose-900/30 text-rose-300 border border-rose-500/30">
          {text}
        </span>
      );
    case 'INFO':
    default:
      return (
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-slate-800 text-slate-300 border border-slate-700">
          {text}
        </span>
      );
  }
}
