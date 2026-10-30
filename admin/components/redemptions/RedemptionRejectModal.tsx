'use client';

import React, { useEffect, useState } from 'react';
import { AdminRedemption } from '../../types/reward';
import { Button } from '../ui/Button';
import { IconAlertCircle, IconShield, IconSpark, IconX } from '../ui/Icons';

interface RedemptionRejectModalProps {
  isOpen: boolean;
  redemption: AdminRedemption | null;
  onClose: () => void;
  onConfirmReject: (redemptionId: string, reason: string) => void;
}

export function RedemptionRejectModal({
  isOpen,
  redemption,
  onClose,
  onConfirmReject,
}: RedemptionRejectModalProps) {
  const [reason, setReason] = useState('');
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setReason('');
    setError(null);
  }, [redemption, isOpen]);

  if (!isOpen || !redemption) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const trimmed = reason.trim();
    if (trimmed.length < 10) {
      setError('Please provide a substantive rejection reason (minimum 10 characters).');
      return;
    }

    onConfirmReject(redemption.id, trimmed);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="relative w-full max-w-lg bg-brand-dark-card border border-rose-500/40 rounded-3xl shadow-2xl p-6 sm:p-8 animate-in fade-in zoom-in duration-200">
        <button
          onClick={onClose}
          className="absolute top-6 right-6 text-slate-400 hover:text-white transition-colors"
          aria-label="Close modal"
        >
          <IconX className="w-5 h-5" />
        </button>

        <div className="flex items-center space-x-3 mb-4">
          <div className="p-3 bg-rose-500/10 border border-rose-500/30 rounded-2xl text-rose-400">
            <IconAlertCircle className="w-6 h-6" />
          </div>
          <div>
            <h2 className="text-xl font-bold text-white tracking-tight">Reject Redemption Claim</h2>
            <p className="text-xs text-slate-400">Decline claim & trigger automated point refund</p>
          </div>
        </div>

        {/* Financial Refund Notice */}
        <div className="p-4 mb-5 rounded-2xl bg-rose-950/30 border border-rose-500/30 text-rose-200 text-xs leading-relaxed space-y-2">
          <p className="font-semibold text-rose-300 flex items-center gap-1.5">
            <IconShield className="w-4 h-4 text-rose-400 shrink-0" />
            Automatic Double-Entry Ledger Refund Triggered
          </p>
          <p>
            Rejecting this redemption will automatically credit{' '}
            <strong className="text-amber-300 font-mono font-bold">
              +{redemption.pointsDeducted.toLocaleString()} pts
            </strong>{' '}
            back to player{' '}
            <strong className="text-white">{redemption.userName}</strong> as an immutable{' '}
            <code className="px-1.5 py-0.5 rounded bg-rose-900/60 font-mono text-[11px] text-rose-200">
              REDEMPTION_REFUND
            </code>{' '}
            ledger transaction and restore 1 unit to the reward stock.
          </p>
        </div>

        {/* Summary Card */}
        <div className="p-4 mb-5 rounded-2xl bg-slate-900/80 border border-brand-dark-border text-xs space-y-2.5">
          <div className="flex justify-between items-center pb-2 border-b border-brand-dark-border/60">
            <span className="text-slate-400">Claim ID:</span>
            <span className="font-mono text-white font-semibold">{redemption.id}</span>
          </div>
          <div className="flex justify-between items-center pb-2 border-b border-brand-dark-border/60">
            <span className="text-slate-400">Player:</span>
            <span className="text-white font-semibold">
              {redemption.userName} ({redemption.userEmail})
            </span>
          </div>
          <div className="flex justify-between items-center pb-2 border-b border-brand-dark-border/60">
            <span className="text-slate-400">Reward:</span>
            <span className="text-white font-semibold">{redemption.rewardTitle}</span>
          </div>
          <div className="flex justify-between items-center pt-1 font-bold">
            <span className="text-rose-400">Points to be Refunded:</span>
            <span className="text-amber-400 flex items-center gap-1 font-mono">
              <IconSpark className="w-4 h-4" />
              {redemption.pointsDeducted.toLocaleString()} pts
            </span>
          </div>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5">
              Reason for Rejection <span className="text-rose-400">*</span>
            </label>
            <textarea
              rows={3}
              value={reason}
              onChange={(e) => {
                setReason(e.target.value);
                if (error) setError(null);
              }}
              placeholder="e.g. Unverified PayPal account, suspected device spoofing, or invalid payout details..."
              className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-brand-dark-border text-xs text-white placeholder-slate-500 focus:outline-none focus:border-rose-500 transition-colors resize-none"
            />
            <p className="text-[11px] text-slate-400 mt-1">
              This reason will be recorded in the audit trail and displayed in the player&apos;s mobile notification.
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
              variant="danger"
              size="sm"
              disabled={reason.trim().length < 10}
              className="bg-rose-600 hover:bg-rose-700 text-white"
            >
              Confirm Rejection & Refund Points
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
