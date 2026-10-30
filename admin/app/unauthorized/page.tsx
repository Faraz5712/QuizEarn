'use client';

import React from 'react';
import Link from 'next/link';
import { IconShield } from '../../components/ui/Icons';
import { Button } from '../../components/ui/Button';
import { useAdminAuth } from '../../lib/auth-context';

export default function UnauthorizedPage() {
  const { user, signOut } = useAdminAuth();

  return (
    <div className="min-h-screen bg-brand-dark-bg flex items-center justify-center p-4">
      <div className="max-w-md w-full text-center p-8 rounded-3xl bg-brand-dark-surface border border-brand-dark-border shadow-2xl">
        <div className="w-16 h-16 mx-auto rounded-2xl bg-rose-950/40 border border-rose-500/40 text-rose-400 flex items-center justify-center mb-5">
          <IconShield className="w-8 h-8" />
        </div>

        <h1 className="text-xl font-bold text-white mb-2">403 — Unauthorized</h1>
        <p className="text-xs text-slate-400 mb-6">
          You do not have the required administrative permissions to access the QuizEarn back-office. Administrative accounts must have verified custom claims assigned.
        </p>

        {user && (
          <div className="p-3 mb-6 rounded-xl bg-brand-dark-card text-left text-xs text-slate-400">
            <p>Signed in as: <span className="text-white font-medium">{user.email}</span></p>
            <p>Role: <span className="text-white font-medium">{user.role}</span></p>
          </div>
        )}

        <div className="flex flex-col sm:flex-row items-center justify-center gap-3">
          <Link href="/login" className="w-full sm:w-auto">
            <Button variant="primary" size="sm" className="w-full sm:w-auto">
              Switch Account
            </Button>
          </Link>
          <Button variant="outline" size="sm" onClick={signOut} className="w-full sm:w-auto">
            Sign Out
          </Button>
        </div>
      </div>
    </div>
  );
}
