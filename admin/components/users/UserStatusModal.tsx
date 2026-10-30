'use client';

import React, { useEffect, useState } from 'react';
import { AdminPlayer, AdminUserAccountStatus } from '../../types/user';
import { Button } from '../ui/Button';
import { IconAlertCircle, IconCheck, IconLock, IconShield, IconX } from '../ui/Icons';

interface UserStatusModalProps {
  isOpen: boolean;
  user: AdminPlayer | null;
  onClose: () => void;
  onConfirmStatusChange: (
    userId: string,
    newStatus: AdminUserAccountStatus,
    reason: string
  ) => void;
}

export function UserStatusModal({
  isOpen,
  user,
  onClose,
  onConfirmStatusChange,
}: UserStatusModalProps) {
  const [reason, setReason] = useState('');
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setReason('');
    setError(null);
  }, [user, isOpen]);

  if (!isOpen || !user) return null;

  const isSuspending = user.status === 'ACTIVE';
  const newStatus: AdminUserAccountStatus = isSuspending ? 'SUSPENDED' : 'ACTIVE';

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const trimmed = reason.trim();
    if (isSuspending && trimmed.length < 10) {
      setError('Please provide a substantive suspension reason (minimum 10 characters).');
      return;
    }

    onConfirmStatusChange(user.id, newStatus, trimmed);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="relative w-full max-w-lg bg-brand-dark-card border border-brand-dark-border rounded-3xl shadow-2xl p-6 sm:p-8 animate-in fade-in zoom-in duration-200">
        <button
          onClick={onClose}
          className="absolute top-6 right-6 text-slate-400 hover:text-white transition-colors"
          aria-label="Close modal"
        >
          <IconX className="w-5 h-5" />
        </button>

        <div className="flex items-center space-x-3 mb-4">
          <div
            className={`p-3 rounded-2xl ${
              isSuspending
                ? 'bg-rose-500/10 border border-rose-500/30 text-rose-400'
                : 'bg-teal-500/10 border border-teal-500/30 text-teal-400'
            }`}
          >
            {isSuspending ? <IconLock className="w-6 h-6" /> : <IconCheck className="w-6 h-6" />}
          </div>
          <div>
            <h2 className="text-xl font-bold text-white tracking-tight">
              {isSuspending ? 'Suspend Player Account' : 'Reactivate Player Account'}
            </h2>
            <p className="text-xs text-slate-400">
              {isSuspending
                ? 'Restrict gameplay, matchmaking, and redemption permissions'
                : 'Restore full player privileges and wallet access'}
            </p>
          </div>
        </div>

        {/* Warning Callout */}
        <div
          className={`p-4 mb-5 rounded-2xl text-xs leading-relaxed space-y-1.5 border ${
            isSuspending
              ? 'bg-rose-950/30 border-rose-500/30 text-rose-200'
              : 'bg-teal-950/30 border-teal-500/30 text-teal-200'
          }`}
        >
          <p className="font-semibold flex items-center gap-1.5">
            <IconShield className="w-4 h-4 shrink-0" />
            {isSuspending
              ? 'Player Account Will Be Immediately Restricted'
              : 'Player Access Restored'}
          </p>
          <p>
            {isSuspending
              ? 'Suspending this player invalidates active session tokens, blocks quiz matchmaking, and prevents any pending redemptions from processing.'
              : 'Reactivating this player re-enables login, tournament entry, and wallet redemptions.'}
          </p>
        </div>

        {/* User Details */}
        <div className="p-4 mb-5 rounded-2xl bg-slate-900/80 border border-brand-dark-border text-xs space-y-2">
          <div className="flex justify-between items-center pb-2 border-b border-brand-dark-border/60">
            <span className="text-slate-400">Player:</span>
            <span className="text-white font-semibold">{user.displayName}</span>
          </div>
          <div className="flex justify-between items-center pb-2 border-b border-brand-dark-border/60">
            <span className="text-slate-400">Email:</span>
            <span className="text-slate-200 font-mono">{user.email}</span>
          </div>
          <div className="flex justify-between items-center pt-0.5">
            <span className="text-slate-400">Wallet Balance:</span>
            <span className="font-mono text-amber-400 font-bold">{user.pointsBalance.toLocaleString()} pts</span>
          </div>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5">
              Reason for {isSuspending ? 'Suspension' : 'Reactivation'}{' '}
              {isSuspending && <span className="text-rose-400">*</span>}
            </label>
            <textarea
              rows={3}
              value={reason}
              onChange={(e) => {
                setReason(e.target.value);
                if (error) setError(null);
              }}
              placeholder={
                isSuspending
                  ? 'e.g. Terms of service violation, automated answer scripting, or multiple accounts...'
                  : 'e.g. Identity verified upon support review, false positive resolved...'
              }
              className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-brand-dark-border text-xs text-white placeholder-slate-500 focus:outline-none focus:border-brand-primary transition-colors resize-none"
            />
            <p className="text-[11px] text-slate-400 mt-1">
              Logged to immutable admin audit trail for administrative record.
            </p>
          </div>

          {error && (
            <div className="p-3 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-300 text-xs flex items-center gap-2">
              <IconAlertCircle className="w-4 h-4 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <div className="flex items-center justify-end space-x-3 pt-3 border-t border-brand-dark-border/60">
            <Button type="button" variant="outline" size="sm" onClick={onClose}>
              Cancel
            </Button>
            <Button
              type="submit"
              variant={isSuspending ? 'danger' : 'primary'}
              size="sm"
              className={
                isSuspending
                  ? 'bg-rose-600 hover:bg-rose-700 text-white'
                  : 'bg-teal-600 hover:bg-teal-500 text-white'
              }
            >
              {isSuspending ? 'Confirm Account Suspension' : 'Confirm Reactivation'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
