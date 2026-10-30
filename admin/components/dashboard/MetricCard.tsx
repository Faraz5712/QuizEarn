import React from 'react';
import { IconTrendingUp } from '../ui/Icons';

interface MetricCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  growthPct?: number;
  icon: React.ReactNode;
  badge?: string;
  variant?: 'default' | 'alert' | 'success';
}

export function MetricCard({
  title,
  value,
  subtitle,
  growthPct,
  icon,
  badge,
  variant = 'default',
}: MetricCardProps) {
  const borderStyles = {
    default: 'border-brand-dark-border',
    alert: 'border-amber-500/50 bg-gradient-to-br from-amber-950/20 to-brand-dark-card',
    success: 'border-teal-500/40 bg-gradient-to-br from-teal-950/20 to-brand-dark-card',
  };

  return (
    <div
      className={`p-5 rounded-2xl bg-brand-dark-card border ${borderStyles[variant]} shadow-lg transition-all hover:border-slate-600 duration-200`}
    >
      <div className="flex items-center justify-between mb-3">
        <span className="text-xs font-medium text-slate-400">{title}</span>
        <div className="w-9 h-9 rounded-xl bg-white/5 flex items-center justify-center text-brand-primary">
          {icon}
        </div>
      </div>

      <div className="flex items-baseline justify-between">
        <h3 className="text-2xl font-bold text-white tracking-tight">{value}</h3>
        {growthPct !== undefined && (
          <span className="flex items-center space-x-1 text-xs font-semibold text-brand-mint">
            <IconTrendingUp className="w-3.5 h-3.5" />
            <span>+{growthPct}%</span>
          </span>
        )}
      </div>

      {(subtitle || badge) && (
        <div className="mt-2.5 flex items-center justify-between text-[11px] text-slate-400">
          <span>{subtitle}</span>
          {badge && (
            <span className="px-2 py-0.5 rounded-full font-bold bg-amber-900/40 text-amber-300 border border-amber-500/40 text-[10px]">
              {badge}
            </span>
          )}
        </div>
      )}
    </div>
  );
}
