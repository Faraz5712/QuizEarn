import React from 'react';
import { RecentActivityItem } from '../../types/admin';
import { StatusBadge } from '../ui/Badge';
import { IconClock, IconGift, IconHelp, IconQuiz, IconTrophy } from '../ui/Icons';

export function ActivityFeed({ activities }: { activities: RecentActivityItem[] }) {
  const getActivityIcon = (type: RecentActivityItem['type']) => {
    switch (type) {
      case 'QUIZ_COMPLETED':
        return <IconQuiz className="w-4 h-4 text-brand-primary" />;
      case 'REDEMPTION_REQUEST':
      case 'REDEMPTION_APPROVED':
      case 'REDEMPTION_REJECTED':
        return <IconGift className="w-4 h-4 text-brand-gold" />;
      case 'TOURNAMENT_FINALIZED':
        return <IconTrophy className="w-4 h-4 text-brand-mint" />;
      case 'SUPPORT_TICKET_OPENED':
      default:
        return <IconHelp className="w-4 h-4 text-slate-300" />;
    }
  };

  return (
    <div className="p-6 rounded-2xl bg-brand-dark-card border border-brand-dark-border shadow-xl">
      <div className="flex items-center justify-between pb-4 mb-4 border-b border-brand-dark-border/60">
        <div>
          <h3 className="text-base font-bold text-white">Recent Platform Activity</h3>
          <p className="text-xs text-slate-400 mt-0.5">Live audit stream of events and redemptions</p>
        </div>
        <div className="flex items-center space-x-1 text-xs text-slate-400">
          <IconClock className="w-3.5 h-3.5" />
          <span>Real-time</span>
        </div>
      </div>

      <div className="space-y-3.5">
        {activities.map((item) => (
          <div
            key={item.id}
            className="flex items-start justify-between p-3.5 rounded-xl bg-white/5 hover:bg-white/10 transition border border-white/5"
          >
            <div className="flex items-start space-x-3.5">
              <div className="w-9 h-9 rounded-xl bg-brand-dark-surface border border-brand-dark-border flex items-center justify-center flex-shrink-0 mt-0.5">
                {getActivityIcon(item.type)}
              </div>
              <div>
                <p className="text-xs font-semibold text-white">{item.title}</p>
                <p className="text-[11px] text-slate-400 mt-0.5">{item.subtitle}</p>
                <p className="text-[10px] text-slate-500 mt-1">{item.timestamp}</p>
              </div>
            </div>

            <div className="flex flex-col items-end space-y-1.5 flex-shrink-0">
              {item.status && <StatusBadge status={item.status} />}
              {item.amount && (
                <span className="text-xs font-mono font-semibold text-brand-gold">
                  {item.amount.toLocaleString()} pts
                </span>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
