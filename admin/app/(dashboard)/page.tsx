'use client';

import React from 'react';
import Link from 'next/link';
import { useAdminAuth } from '../../lib/auth-context';
import { MOCK_DASHBOARD_METRICS, MOCK_RECENT_ACTIVITIES } from '../../lib/mock-data';
import { MetricCard } from '../../components/dashboard/MetricCard';
import { EconomyCard } from '../../components/dashboard/EconomyCard';
import { ActivityFeed } from '../../components/dashboard/ActivityFeed';
import {
  IconGift,
  IconHelp,
  IconQuiz,
  IconShield,
  IconSpark,
  IconTrophy,
  IconUsers,
} from '../../components/ui/Icons';
import { Button } from '../../components/ui/Button';
import { RoleBadge } from '../../components/ui/Badge';
import { isFirebaseConfigured } from '../../lib/firebase';

export default function DashboardOverviewPage() {
  const { user } = useAdminAuth();
  const metrics = MOCK_DASHBOARD_METRICS;

  return (
    <div className="space-y-8 max-w-7xl mx-auto">
      {/* Welcome Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between p-6 sm:p-8 rounded-3xl bg-gradient-to-r from-brand-primary/20 via-brand-secondary/15 to-transparent border border-brand-primary/30 shadow-2xl relative overflow-hidden">
        <div className="relative z-10">
          <div className="flex items-center space-x-2.5 mb-2">
            <span className="text-xs font-bold uppercase tracking-wider text-brand-primary">
              Control Center
            </span>
            {user && <RoleBadge role={user.role} />}
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
            Welcome back, {user?.displayName || 'Administrator'}
          </h1>
          <p className="mt-1 text-xs sm:text-sm text-slate-300 max-w-2xl">
            {isFirebaseConfigured
              ? 'Firebase authentication is active. Overview metrics and activity are not yet backed by server data.'
              : 'Local preview metrics only; no live account, wallet, quiz, or redemption data is connected.'}
          </p>
        </div>

        <div className="mt-4 sm:mt-0 flex items-center space-x-3 relative z-10 flex-shrink-0">
          {user?.role === 'CONTENT_MANAGER' && (
            <Link href="/questions">
              <Button variant="primary" size="sm">
                Add Questions
              </Button>
            </Link>
          )}

          {user?.role === 'SUPPORT_AGENT' && (
            <Link href="/redemptions">
              <Button variant="primary" size="sm">
                Review Redemptions (14)
              </Button>
            </Link>
          )}

          {user?.role === 'SUPER_ADMIN' && (
            <div className="flex items-center space-x-2">
              <Link href="/redemptions">
                <Button variant="outline" size="sm">
                  Redemptions (14)
                </Button>
              </Link>
              <Link href="/audit-logs">
                <Button variant="primary" size="sm">
                  View Audit Trail
                </Button>
              </Link>
            </div>
          )}
        </div>

        {/* Decorative background glow */}
        <div className="absolute right-0 top-0 bottom-0 w-96 bg-gradient-to-l from-brand-secondary/20 to-transparent pointer-events-none" />
      </div>

      {!isFirebaseConfigured ? <div className="space-y-8">
      {/* Primary KPI Metric Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6">
        {/* Metric 1: Registered Players */}
        <MetricCard
          title="Total Registered Players"
          value={metrics.totalUsers.toLocaleString()}
          subtitle={`${metrics.activeUsersToday.toLocaleString()} active today`}
          growthPct={metrics.totalUsersGrowthPct}
          icon={<IconUsers className="w-5 h-5 text-brand-primary" />}
        />

        {/* Metric 2: Quizzes Completed */}
        <MetricCard
          title="Quizzes Completed Today"
          value={metrics.quizzesCompletedToday.toLocaleString()}
          subtitle={`${metrics.quizPassRatePct}% passing accuracy`}
          growthPct={metrics.activeUsersGrowthPct}
          icon={<IconQuiz className="w-5 h-5 text-brand-secondary" />}
          variant="success"
        />

        {/* Metric 3: Pending Redemptions Alert */}
        <MetricCard
          title="Pending Redemptions"
          value={metrics.pendingRedemptionsCount}
          subtitle="Awaiting manual code issuance"
          badge="Action Required"
          icon={<IconGift className="w-5 h-5 text-brand-gold" />}
          variant="alert"
        />

        {/* Metric 4: Open Support Tickets */}
        <MetricCard
          title="Support Tickets"
          value={metrics.openSupportTicketsCount}
          subtitle="Customer inquiries open"
          icon={<IconHelp className="w-5 h-5 text-teal-400" />}
        />
      </div>

      {/* Wallet Economy Hero Breakdown */}
      <EconomyCard
        totalPointsIssued={metrics.totalPointsIssued}
        totalPointsRedeemed={metrics.totalPointsRedeemed}
      />

      {/* Lower Split: Recent Activity Stream & Role-Specific Panels */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left 2 Cols: Activity Feed */}
        <div className="lg:col-span-2">
          <ActivityFeed activities={MOCK_RECENT_ACTIVITIES} />
        </div>

        {/* Right 1 Col: Quick Operational Links & System Health */}
        <div className="space-y-6">
          {/* Quick Operations Box */}
          <div className="p-6 rounded-2xl bg-brand-dark-card border border-brand-dark-border shadow-xl">
            <h3 className="text-base font-bold text-white mb-1">Quick Actions</h3>
            <p className="text-xs text-slate-400 mb-4">Role-authorized shortcuts</p>

            <div className="space-y-2">
              <Link
                href="/questions"
                className="flex items-center justify-between p-3 rounded-xl bg-white/5 hover:bg-white/10 transition border border-white/5 text-xs font-semibold text-white"
              >
                <div className="flex items-center space-x-2.5">
                  <IconQuiz className="w-4 h-4 text-brand-primary" />
                  <span>Question Bank (10,000+)</span>
                </div>
                <span className="text-[10px] text-slate-400">Manage &rarr;</span>
              </Link>

              <Link
                href="/tournaments"
                className="flex items-center justify-between p-3 rounded-xl bg-white/5 hover:bg-white/10 transition border border-white/5 text-xs font-semibold text-white"
              >
                <div className="flex items-center space-x-2.5">
                  <IconTrophy className="w-4 h-4 text-brand-mint" />
                  <span>Tournaments & Tasks</span>
                </div>
                <span className="text-[10px] text-slate-400">Configure &rarr;</span>
              </Link>

              <Link
                href="/redemptions"
                className="flex items-center justify-between p-3 rounded-xl bg-amber-950/20 hover:bg-amber-950/30 transition border border-amber-500/30 text-xs font-semibold text-white"
              >
                <div className="flex items-center space-x-2.5">
                  <IconGift className="w-4 h-4 text-brand-gold" />
                  <span>Redemption Queue</span>
                </div>
                <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-amber-900/40 text-amber-300">
                  14 Claims
                </span>
              </Link>

              <Link
                href="/audit-logs"
                className="flex items-center justify-between p-3 rounded-xl bg-white/5 hover:bg-white/10 transition border border-white/5 text-xs font-semibold text-white"
              >
                <div className="flex items-center space-x-2.5">
                  <IconShield className="w-4 h-4 text-purple-400" />
                  <span>Administrative Audit Trail</span>
                </div>
                <span className="text-[10px] text-slate-400">Inspect &rarr;</span>
              </Link>
            </div>
          </div>

          {/* System Security Status Box */}
          <div className="p-6 rounded-2xl bg-brand-dark-card border border-brand-dark-border shadow-xl">
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-base font-bold text-white">System Security</h3>
              <span className="w-2 h-2 rounded-full bg-brand-mint animate-pulse" />
            </div>

            <div className="space-y-3 text-xs">
              <div className="flex items-center justify-between pb-2 border-b border-brand-dark-border/40">
                <span className="text-slate-400">Play Integrity Attestation</span>
                <span className="text-brand-mint font-semibold">Active & Enforcing</span>
              </div>
              <div className="flex items-center justify-between pb-2 border-b border-brand-dark-border/40">
                <span className="text-slate-400">Firebase Custom Claims</span>
                <span className="text-brand-mint font-semibold">Verified Token RBAC</span>
              </div>
              <div className="flex items-center justify-between pb-2 border-b border-brand-dark-border/40">
                <span className="text-slate-400">Scoring Engine Authority</span>
                <span className="text-brand-mint font-semibold">Server-Side Secret Keys</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-slate-400">Double-Entry Ledger Status</span>
                <span className="text-brand-mint font-semibold">100% Reconciled</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      </div> : (
              <div className="rounded-2xl border border-amber-500/40 bg-amber-950/30 p-6 text-sm text-amber-100">
                Live operations can be managed in the connected question, task, tournament, reward, redemption, and user-list screens. Aggregate metrics, activity feed, support tickets, audit history, and account suspension are not yet integrated.
              </div>
            )}
    </div>
  );
}
