import React from 'react';
import { IconGift, IconSpark, IconTrophy } from '../ui/Icons';

interface EconomyCardProps {
  totalPointsIssued: number;
  totalPointsRedeemed: number;
}

export function EconomyCard({
  totalPointsIssued,
  totalPointsRedeemed,
}: EconomyCardProps) {
  const pointsInCirculation = totalPointsIssued - totalPointsRedeemed;
  const redemptionRatio = ((totalPointsRedeemed / totalPointsIssued) * 100).toFixed(1);
  const estimatedCashLiability = (pointsInCirculation / 1000).toFixed(2); // 1,000 pts = $1.00 USD

  return (
    <div className="p-6 rounded-2xl bg-gradient-to-br from-brand-dark-card to-brand-dark-surface border border-brand-dark-border shadow-xl">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-4 mb-5 border-b border-brand-dark-border/60">
        <div>
          <div className="flex items-center space-x-2">
            <IconSpark className="w-5 h-5 text-brand-gold" />
            <h3 className="text-base font-bold text-white">Wallet Economy & Point Ledger</h3>
          </div>
          <p className="text-xs text-slate-400 mt-0.5">
            Real-time reconciliation of double-entry transaction ledgers
          </p>
        </div>
        <div className="mt-3 sm:mt-0 flex items-center space-x-2">
          <span className="text-xs px-2.5 py-1 rounded-full font-semibold bg-brand-primary/20 text-brand-primary border border-brand-primary/30">
            Redemption Ratio: {redemptionRatio}%
          </span>
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="p-4 rounded-xl bg-white/5 border border-white/5">
          <div className="flex items-center space-x-2 text-xs text-slate-400 mb-1">
            <IconTrophy className="w-4 h-4 text-brand-mint" />
            <span>Lifetime Points Issued</span>
          </div>
          <p className="text-xl font-bold text-white">
            {totalPointsIssued.toLocaleString()} <span className="text-xs font-normal text-slate-400">pts</span>
          </p>
          <p className="text-[11px] text-slate-500 mt-1">Quiz wins, daily streaks, tournaments</p>
        </div>

        <div className="p-4 rounded-xl bg-white/5 border border-white/5">
          <div className="flex items-center space-x-2 text-xs text-slate-400 mb-1">
            <IconGift className="w-4 h-4 text-brand-gold" />
            <span>Lifetime Redeemed</span>
          </div>
          <p className="text-xl font-bold text-brand-gold">
            {totalPointsRedeemed.toLocaleString()} <span className="text-xs font-normal text-slate-400">pts</span>
          </p>
          <p className="text-[11px] text-slate-500 mt-1">Digital vouchers & top-ups fulfilled</p>
        </div>

        <div className="p-4 rounded-xl bg-brand-primary/10 border border-brand-primary/30">
          <div className="flex items-center space-x-2 text-xs text-brand-primary mb-1">
            <IconSpark className="w-4 h-4 text-brand-primary" />
            <span>Active Point Liability</span>
          </div>
          <p className="text-xl font-bold text-white">
            {pointsInCirculation.toLocaleString()} <span className="text-xs font-normal text-slate-400">pts</span>
          </p>
          <p className="text-[11px] text-slate-300 mt-1">
            Est. ~${estimatedCashLiability} USD reserve required
          </p>
        </div>
      </div>
    </div>
  );
}
