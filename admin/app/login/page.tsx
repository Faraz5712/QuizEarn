'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAdminAuth } from '../../lib/auth-context';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { IconLock, IconMail, IconShield, IconSpark } from '../../components/ui/Icons';
import { DEMO_ADMIN_USERS } from '../../lib/mock-data';

export default function AdminLoginPage() {
  const router = useRouter();
  const { signIn, user, isDemoMode } = useAdminAuth();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // If already logged in, provide a direct shortcut to dashboard
  const handleQuickDashboard = () => {
    router.push('/');
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);
    setIsLoading(true);

    const res = await signIn(email, password);
    setIsLoading(false);

    if (res.success) {
      router.push('/');
    } else {
      setErrorMessage(res.error || 'Authentication failed. Please verify credentials.');
    }
  };

  const handleSelectDemo = (roleKey: 'super' | 'content' | 'support') => {
    const demo = DEMO_ADMIN_USERS[roleKey];
    setEmail(demo.email);
    setPassword('adminDemo2026!');
    setErrorMessage(null);
  };

  return (
    <div className="min-h-screen bg-brand-dark-bg flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md text-center">
        {/* Brand Icon Header */}
        <div className="w-14 h-14 mx-auto rounded-2xl bg-gradient-to-br from-brand-primary to-brand-secondary flex items-center justify-center shadow-xl shadow-brand-primary/30 mb-4">
          <IconSpark className="w-8 h-8 text-white" />
        </div>
        <h2 className="text-2xl font-bold tracking-tight text-white">
          QuizEarn Back-Office
        </h2>
        <p className="mt-1 text-xs text-slate-400">
          Role-Based Access Control • Server-Authoritative Management
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md px-4 sm:px-0">
        <div className="bg-brand-dark-surface py-8 px-6 sm:px-10 rounded-3xl border border-brand-dark-border shadow-2xl space-y-6">
          {/* Active Session Notification (if already authenticated) */}
          {user && (
            <div className="p-3.5 rounded-xl bg-brand-primary/10 border border-brand-primary/30 flex items-center justify-between">
              <div>
                <p className="text-xs font-semibold text-white">Active session detected</p>
                <p className="text-[11px] text-slate-300">Logged in as {user.displayName} ({user.role})</p>
              </div>
              <Button size="sm" onClick={handleQuickDashboard}>
                Go to Dashboard
              </Button>
            </div>
          )}

          {/* Login Form */}
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-1">
              <Input
                label="Administrator Email"
                type="email"
                placeholder="admin@quizearn.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>

            <div className="space-y-1">
              <Input
                label="Secure Password"
                type="password"
                placeholder="••••••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>

            {errorMessage && (
              <div className="p-3 rounded-xl bg-rose-950/40 border border-rose-500/40 text-xs text-rose-300 flex items-start space-x-2">
                <IconShield className="w-4 h-4 text-rose-400 flex-shrink-0 mt-0.5" />
                <span>{errorMessage}</span>
              </div>
            )}

            <Button
              type="submit"
              variant="primary"
              className="w-full mt-2"
              isLoading={isLoading}
            >
              Sign In to Admin Portal
            </Button>
          </form>

          {/* Demo presets are available only in the non-production local preview. */}
          {isDemoMode && <div className="pt-4 border-t border-brand-dark-border/60">
            <div className="flex items-center justify-between mb-3">
              <span className="text-[11px] font-bold uppercase tracking-wider text-slate-400">
                Quick Demo Presets
              </span>
              <span className="text-[10px] text-brand-mint font-medium">Local Simulation</span>
            </div>
            {!isDemoMode && (
              <p className="pt-4 border-t border-brand-dark-border/60 text-[11px] text-slate-400">
                Access requires a Firebase Auth account with an administrator custom role.
              </p>
            )}

            <div className="grid grid-cols-3 gap-2">
              <button
                type="button"
                onClick={() => handleSelectDemo('super')}
                className="p-2 rounded-xl bg-purple-950/30 border border-purple-500/30 text-purple-300 hover:bg-purple-900/40 transition text-center"
              >
                <p className="text-[11px] font-bold">Super Admin</p>
                <p className="text-[9px] text-purple-400">Level 100</p>
              </button>

              <button
                type="button"
                onClick={() => handleSelectDemo('content')}
                className="p-2 rounded-xl bg-indigo-950/30 border border-indigo-500/30 text-indigo-300 hover:bg-indigo-900/40 transition text-center"
              >
                <p className="text-[11px] font-bold">Content Mgr</p>
                <p className="text-[9px] text-indigo-400">Level 50</p>
              </button>

              <button
                type="button"
                onClick={() => handleSelectDemo('support')}
                className="p-2 rounded-xl bg-teal-950/30 border border-teal-500/30 text-teal-300 hover:bg-teal-900/40 transition text-center"
              >
                <p className="text-[11px] font-bold">Support Agent</p>
                <p className="text-[9px] text-teal-400">Level 30</p>
              </button>
            </div>
            <p className="text-[10px] text-slate-500 text-center mt-2.5">
              Clicking a preset autofills verified credentials for role testing.
            </p>
          </div>}
        </div>

        {/* Security Warning Footer */}
        <div className="mt-6 text-center text-xs text-slate-500 flex items-center justify-center space-x-1.5">
          <IconLock className="w-3.5 h-3.5 text-slate-500" />
          <span>Restricted to authorized QuizEarn personnel. All sessions are audited.</span>
        </div>
      </div>
    </div>
  );
}
