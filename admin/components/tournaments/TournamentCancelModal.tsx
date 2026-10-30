'use client';

import React, { useState } from 'react';
import { AdminTournament } from '../../types/tournament';
import { Button } from '../ui/Button';
import { IconAlertCircle, IconShield, IconSpark, IconX } from '../ui/Icons';

interface TournamentCancelModalProps {
  isOpen: boolean;
  tournament: AdminTournament | null;
  onClose: () => void;
  onConfirmCancel: (tournamentId: string, reason: string) => void;
}

export function TournamentCancelModal({
  isOpen,
  tournament,
  onClose,
  onConfirmCancel,
}: TournamentCancelModalProps) {
  const [reason, setReason] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  if (!isOpen || !tournament) return null;

  const totalRefund = tournament.entryFeePoints * tournament.registeredParticipants;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const trimmed = reason.trim();
    if (trimmed.length < 10) {
      setError('Please provide a substantive cancellation reason (minimum 10 characters).');
      return;
    }

    setIsSubmitting(true);
    setError(null);

    try {
      onConfirmCancel(tournament.id, trimmed);
      setReason('');
      onClose();
    } catch {
      setError('Failed to process tournament cancellation.');
    } finally {
      setIsSubmitting(false);
    }
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
            <h2 className="text-xl font-bold text-white tracking-tight">Cancel Tournament</h2>
            <p className="text-xs text-slate-400">Emergency cancellation & automated ledger refund</p>
          </div>
        </div>

        {/* Warning Callout */}
        <div className="p-4 mb-5 rounded-2xl bg-rose-950/30 border border-rose-500/30 text-rose-200 text-xs leading-relaxed space-y-2">
          <p className="font-semibold text-rose-300 flex items-center gap-1.5">
            <IconShield className="w-4 h-4 text-rose-400 shrink-0" />
            Warning: Cancellation is an authoritative, irreversible action.
          </p>
          <p>
            Cancelling this competition terminates matchmaking immediately and triggers simulated{' '}
            <code className="px-1.5 py-0.5 rounded bg-rose-900/60 font-mono text-[11px] text-rose-200">
              TOURNAMENT_REFUND
            </code>{' '}
            ledger credits to all registered participant wallets.
          </p>
        </div>

        {/* Tournament Summary Card */}
        <div className="p-4 mb-5 rounded-2xl bg-slate-900/80 border border-brand-dark-border text-xs space-y-2.5">
          <div className="flex justify-between items-center pb-2 border-b border-brand-dark-border/60">
            <span className="text-slate-400">Tournament ID:</span>
            <span className="font-mono text-slate-200 font-semibold">{tournament.id}</span>
          </div>
          <div className="flex justify-between items-center pb-2 border-b border-brand-dark-border/60">
            <span className="text-slate-400">Title:</span>
            <span className="text-white font-semibold text-right truncate max-w-[240px]">{tournament.title}</span>
          </div>
          <div className="flex justify-between items-center pb-2 border-b border-brand-dark-border/60">
            <span className="text-slate-400">Registered Players:</span>
            <span className="text-slate-200 font-semibold">{tournament.registeredParticipants} players</span>
          </div>
          <div className="flex justify-between items-center pb-2 border-b border-brand-dark-border/60">
            <span className="text-slate-400">Entry Fee:</span>
            <span className="text-slate-200 font-semibold">{tournament.entryFeePoints} pts / player</span>
          </div>
          <div className="flex justify-between items-center pt-1 text-sm font-bold">
            <span className="text-rose-400">Total Points to Refund:</span>
            <span className="text-amber-400 flex items-center gap-1">
              <IconSpark className="w-4 h-4" />
              {totalRefund.toLocaleString()} pts
            </span>
          </div>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5">
              Reason for Cancellation <span className="text-rose-400">*</span>
            </label>
            <textarea
              rows={3}
              value={reason}
              onChange={(e) => {
                setReason(e.target.value);
                if (error) setError(null);
              }}
              placeholder="e.g. Question bank integrity update, scheduled maintenance, or technical conflict..."
              className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-brand-dark-border text-xs text-white placeholder-slate-500 focus:outline-none focus:border-rose-500 transition-colors resize-none"
            />
            <p className="text-[11px] text-slate-400 mt-1">
              This message will be permanently logged in audit trails and displayed in player notifications.
            </p>
          </div>

          {error && (
            <div className="p-3 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-300 text-xs flex items-center gap-2">
              <IconAlertCircle className="w-4 h-4 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <div className="flex items-center justify-end space-x-3 pt-3 border-t border-brand-dark-border/60">
            <Button type="button" variant="outline" size="sm" onClick={onClose} disabled={isSubmitting}>
              Keep Tournament
            </Button>
            <Button
              type="submit"
              variant="danger"
              size="sm"
              disabled={isSubmitting || reason.trim().length < 10}
              className="bg-rose-600 hover:bg-rose-700 text-white"
            >
              {isSubmitting ? 'Cancelling & Refunding...' : 'Confirm Emergency Cancellation'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
