'use client';

import React, { useEffect, useState } from 'react';
import { AdminRedemption } from '../../types/reward';
import { Button } from '../ui/Button';
import { IconAlertCircle, IconCheck, IconGift, IconSpark, IconX } from '../ui/Icons';

interface RedemptionApproveModalProps {
  isOpen: boolean;
  redemption: AdminRedemption | null;
  onClose: () => void;
  onConfirmApprove: (redemptionId: string, digitalCode: string) => void;
}

export function RedemptionApproveModal({
  isOpen,
  redemption,
  onClose,
  onConfirmApprove,
}: RedemptionApproveModalProps) {
  const [digitalCode, setDigitalCode] = useState('');
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setDigitalCode('');
    setError(null);
  }, [redemption, isOpen]);

  if (!isOpen || !redemption) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const trimmed = digitalCode.trim();
    if (!trimmed) {
      setError('Please provide a valid voucher claim code or transaction reference ID.');
      return;
    }

    onConfirmApprove(redemption.id, trimmed);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="relative w-full max-w-lg bg-brand-dark-card border border-teal-500/40 rounded-3xl shadow-2xl p-6 sm:p-8 animate-in fade-in zoom-in duration-200">
        <button
          onClick={onClose}
          className="absolute top-6 right-6 text-slate-400 hover:text-white transition-colors"
          aria-label="Close modal"
        >
          <IconX className="w-5 h-5" />
        </button>

        <div className="flex items-center space-x-3 mb-5">
          <div className="p-3 bg-teal-500/10 border border-teal-500/30 rounded-2xl text-teal-400">
            <IconCheck className="w-6 h-6" />
          </div>
          <div>
            <h2 className="text-xl font-bold text-white tracking-tight">Approve Redemption Claim</h2>
            <p className="text-xs text-slate-400">Issue voucher code and confirm reward fulfillment</p>
          </div>
        </div>

        {/* Claim Details Card */}
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
          <div className="flex justify-between items-center pb-2 border-b border-brand-dark-border/60">
            <span className="text-slate-400">Points Cost:</span>
            <span className="text-amber-400 font-bold flex items-center gap-1 font-mono">
              <IconSpark className="w-3.5 h-3.5" />
              {redemption.pointsDeducted.toLocaleString()} pts
            </span>
          </div>
          <div className="flex justify-between items-center pt-1">
            <span className="text-slate-400">Payout Destination:</span>
            <span className="font-mono text-teal-300 font-semibold">{redemption.payoutDetails}</span>
          </div>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5">
              Digital Voucher Code / Payment Reference ID <span className="text-teal-400">*</span>
            </label>
            <input
              type="text"
              value={digitalCode}
              onChange={(e) => {
                setDigitalCode(e.target.value);
                if (error) setError(null);
              }}
              placeholder="e.g. AMZN-9021-4412-KV89 or TXN-PAYPAL-99120"
              className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-brand-dark-border text-xs text-white font-mono placeholder-slate-500 focus:outline-none focus:border-teal-500 transition-colors"
            />
            <p className="text-[11px] text-slate-400 mt-1">
              This code will be immediately displayed in the player&apos;s mobile redemption history and dispatched via email.
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
              variant="primary"
              size="sm"
              className="bg-teal-600 hover:bg-teal-500 text-white"
            >
              Confirm & Issue Voucher
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
