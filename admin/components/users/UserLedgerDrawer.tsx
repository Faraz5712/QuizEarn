'use client';

import React from 'react';
import { AdminPlayer } from '../../types/user';
import { Button } from '../ui/Button';
import {
  IconAlertCircle,
  IconBarChart,
  IconCheck,
  IconClock,
  IconShield,
  IconSpark,
  IconX,
} from '../ui/Icons';

interface UserLedgerDrawerProps {
  isOpen: boolean;
  user: AdminPlayer | null;
  onClose: () => void;
}

export function UserLedgerDrawer({
  isOpen,
  user,
  onClose,
}: UserLedgerDrawerProps) {
  if (!isOpen || !user) return null;

  const totalCredits = user.ledger
    .filter((t) => t.type === 'CREDIT')
    .reduce((sum, t) => sum + t.amount, 0);

  const totalDebits = user.ledger
    .filter((t) => t.type === 'DEBIT')
    .reduce((sum, t) => sum + t.amount, 0);

  const formatDateTime = (iso: string) => {
    try {
      const d = new Date(iso);
      return d.toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return iso;
    }
  };

  return (
    <div className="fixed inset-0 z-50 overflow-hidden bg-black/70 backdrop-blur-sm flex justify-end">
      <div className="w-full max-w-2xl bg-brand-dark-surface border-l border-brand-dark-border h-full flex flex-col shadow-2xl animate-in slide-in-from-right duration-300">
        {/* Drawer Header */}
        <div className="p-6 border-b border-brand-dark-border/80 flex items-center justify-between bg-slate-900/60">
          <div className="flex items-center space-x-3.5">
            <div
              className="w-11 h-11 rounded-2xl flex items-center justify-center font-bold text-white text-base shadow-lg border border-white/20"
              style={{ backgroundColor: user.avatarColor || '#5B5FEF' }}
            >
              {user.displayName.charAt(0).toUpperCase()}
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h2 className="text-lg font-bold text-white tracking-tight">{user.displayName}</h2>
                <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-brand-primary/20 text-brand-primary border border-brand-primary/30">
                  Level {user.level}
                </span>
                {user.status === 'SUSPENDED' && (
                  <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-rose-500/20 text-rose-300 border border-rose-500/30">
                    Suspended
                  </span>
                )}
              </div>
              <p className="text-xs text-slate-400 mt-0.5 font-mono">
                {user.email} • ID: {user.id}
              </p>
            </div>
          </div>

          <button
            onClick={onClose}
            className="p-2 text-slate-400 hover:text-white rounded-xl hover:bg-slate-800 transition-colors"
            aria-label="Close drawer"
          >
            <IconX className="w-5 h-5" />
          </button>
        </div>

        {/* Ledger Summary KPIs */}
        <div className="p-6 bg-slate-950/40 border-b border-brand-dark-border/60 grid grid-cols-3 gap-3">
          <div className="p-3.5 rounded-2xl bg-slate-900 border border-brand-dark-border">
            <span className="text-[10px] font-semibold text-slate-400 uppercase tracking-wider block">
              Current Balance
            </span>
            <div className="text-lg font-bold text-amber-400 mt-1 flex items-center gap-1 font-mono">
              <IconSpark className="w-4 h-4" />
              {user.pointsBalance.toLocaleString()} pts
            </div>
          </div>

          <div className="p-3.5 rounded-2xl bg-slate-900 border border-brand-dark-border">
            <span className="text-[10px] font-semibold text-slate-400 uppercase tracking-wider block">
              Lifetime Earned
            </span>
            <div className="text-lg font-bold text-teal-400 mt-1 font-mono">
              +{totalCredits.toLocaleString()} pts
            </div>
          </div>

          <div className="p-3.5 rounded-2xl bg-slate-900 border border-brand-dark-border">
            <span className="text-[10px] font-semibold text-slate-400 uppercase tracking-wider block">
              Lifetime Redeemed
            </span>
            <div className="text-lg font-bold text-rose-400 mt-1 font-mono">
              -{totalDebits.toLocaleString()} pts
            </div>
          </div>
        </div>

        {/* Reconciled Invariant Notice */}
        <div className="px-6 py-2.5 bg-teal-950/30 border-b border-teal-500/20 text-teal-300 text-[11px] flex items-center justify-between">
          <span className="flex items-center gap-1.5">
            <IconShield className="w-3.5 h-3.5 text-teal-400 shrink-0" />
            Double-Entry Ledger Verified: Sum(Credits) - Sum(Debits) = Current Balance
          </span>
          <span className="font-mono font-bold text-teal-400">
            {totalCredits - totalDebits === user.pointsBalance ? '✓ 100% Reconciled' : 'Discrepancy'}
          </span>
        </div>

        {/* Ledger Table Container */}
        <div className="flex-1 overflow-y-auto p-6 space-y-3">
          <h3 className="text-xs font-bold text-slate-300 uppercase tracking-wider flex items-center gap-2">
            <IconBarChart className="w-4 h-4 text-brand-gold" />
            Append-Only Transaction History ({user.ledger.length} events)
          </h3>

          <div className="bg-brand-dark-card border border-brand-dark-border rounded-2xl overflow-hidden shadow-lg">
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse text-xs">
                <thead>
                  <tr className="border-b border-brand-dark-border/80 bg-slate-900/80 text-[10px] font-semibold text-slate-400 uppercase tracking-wider">
                    <th className="py-2.5 px-3">Date & ID</th>
                    <th className="py-2.5 px-3">Reason / Description</th>
                    <th className="py-2.5 px-3 text-right">Amount</th>
                    <th className="py-2.5 px-3 text-right">Balance After</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-brand-dark-border/40 text-[11px]">
                  {user.ledger.map((txn) => (
                    <tr key={txn.id} className="hover:bg-slate-800/40 transition-colors">
                      <td className="py-3 px-3 whitespace-nowrap">
                        <div className="font-mono text-slate-300 font-semibold">{txn.id}</div>
                        <div className="text-[10px] text-slate-500 mt-0.5">
                          {formatDateTime(txn.timestampIso)}
                        </div>
                      </td>

                      <td className="py-3 px-3 max-w-[200px]">
                        <div className="flex items-center gap-1.5 font-semibold text-white">
                          <span
                            className={`px-1.5 py-0.2 rounded text-[9px] font-mono uppercase ${
                              txn.type === 'CREDIT'
                                ? 'bg-teal-500/10 text-teal-300 border border-teal-500/30'
                                : 'bg-rose-500/10 text-rose-300 border border-rose-500/30'
                            }`}
                          >
                            {txn.reason}
                          </span>
                        </div>
                        <div className="text-[10px] text-slate-400 mt-0.5 truncate">
                          {txn.description}
                        </div>
                        {txn.referenceId && (
                          <div className="font-mono text-[9px] text-slate-500 mt-0.5">
                            Ref: {txn.referenceId}
                          </div>
                        )}
                      </td>

                      <td className="py-3 px-3 whitespace-nowrap text-right font-mono font-bold">
                        <span
                          className={txn.type === 'CREDIT' ? 'text-teal-400' : 'text-rose-400'}
                        >
                          {txn.type === 'CREDIT' ? `+${txn.amount}` : `-${txn.amount}`} pts
                        </span>
                      </td>

                      <td className="py-3 px-3 whitespace-nowrap text-right font-mono font-bold text-white">
                        {txn.balanceAfter.toLocaleString()} pts
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        {/* Drawer Footer */}
        <div className="p-4 bg-slate-900 border-t border-brand-dark-border flex items-center justify-between text-xs text-slate-400">
          <span>Member since {formatDateTime(user.createdAtIso)}</span>
          <Button variant="outline" size="sm" onClick={onClose}>
            Close Ledger
          </Button>
        </div>
      </div>
    </div>
  );
}
