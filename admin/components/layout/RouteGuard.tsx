'use client';

import React, { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAdminAuth } from '../../lib/auth-context';
import { hasPermission } from '../../lib/rbac';
import { AdminPermission } from '../../types/admin';
import { IconAlertCircle } from '../ui/Icons';
import { Button } from '../ui/Button';

interface RouteGuardProps {
  children: React.ReactNode;
  requiredPermission?: AdminPermission;
}

export function RouteGuard({ children, requiredPermission }: RouteGuardProps) {
  const { user, isLoading } = useAdminAuth();
  const router = useRouter();

  useEffect(() => {
    if (!isLoading && !user) {
      router.push('/login');
    }
  }, [user, isLoading, router]);

  if (isLoading) {
    return (
      <div className="min-h-screen bg-brand-dark-bg flex items-center justify-center">
        <div className="flex flex-col items-center space-y-3">
          <div className="w-10 h-10 border-4 border-brand-primary border-t-transparent rounded-full animate-spin" />
          <p className="text-xs text-slate-400 font-medium">Authenticating Operator Session...</p>
        </div>
      </div>
    );
  }

  if (!user) {
    return null;
  }

  if (requiredPermission && !hasPermission(user.role, requiredPermission)) {
    return (
      <div className="p-8 max-w-xl mx-auto my-12 text-center bg-brand-dark-card rounded-2xl border border-brand-dark-border">
        <div className="w-12 h-12 mx-auto rounded-full bg-rose-900/30 text-rose-400 flex items-center justify-center mb-4">
          <IconAlertCircle className="w-6 h-6" />
        </div>
        <h2 className="text-lg font-bold text-white mb-2">Administrative Access Restricted</h2>
        <p className="text-xs text-slate-400 mb-6">
          Your current administrative role (<span className="text-white font-semibold">{user.role}</span>) does not possess the <code className="text-brand-primary font-mono text-xs">{requiredPermission}</code> capability required to access this screen.
        </p>
        <Button variant="outline" size="sm" onClick={() => router.push('/')}>
          Return to Dashboard Overview
        </Button>
      </div>
    );
  }

  return <>{children}</>;
}
