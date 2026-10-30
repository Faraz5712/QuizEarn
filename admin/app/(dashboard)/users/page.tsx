'use client';

import React, { useEffect, useState } from 'react';
import { RouteGuard } from '../../../components/layout/RouteGuard';
import { Button } from '../../../components/ui/Button';
import {
  IconAlertCircle,
  IconCheck,
  IconLock,
  IconSpark,
  IconUsers,
} from '../../../components/ui/Icons';
import { UserLedgerDrawer } from '../../../components/users/UserLedgerDrawer';
import { UserStatusModal } from '../../../components/users/UserStatusModal';
import { UserTable } from '../../../components/users/UserTable';
import {
  loadStoredUsers,
  saveStoredUsers,
} from '../../../lib/users-store';
import { AdminPlayer, AdminUserAccountStatus } from '../../../types/user';
import { listAdminRecords } from '../../../lib/backend';
import { isFirebaseConfigured } from '../../../lib/firebase';

export default function UsersDirectoryPage() {
  const [users, setUsers] = useState<AdminPlayer[]>([]);
  const [isLoaded, setIsLoaded] = useState(false);

  // Drawer & Modal state
  const [selectedUserForLedger, setSelectedUserForLedger] = useState<AdminPlayer | null>(null);
  const [isLedgerOpen, setIsLedgerOpen] = useState(false);

  const [selectedUserForStatus, setSelectedUserForStatus] = useState<AdminPlayer | null>(null);
  const [isStatusModalOpen, setIsStatusModalOpen] = useState(false);

  // Toast
  const [toast, setToast] = useState<{
    message: string;
    type: 'success' | 'danger' | 'info';
  } | null>(null);

  useEffect(() => {
    if (!isFirebaseConfigured) {
      setUsers(loadStoredUsers());
      setIsLoaded(true);
      return;
    }
    listAdminRecords<AdminPlayer>('users').then(setUsers).catch((error: unknown) => {
      showToast(error instanceof Error ? error.message : 'Failed to load user directory.', 'danger');
    }).finally(() => setIsLoaded(true));
  }, []);

  const showToast = (message: string, type: 'success' | 'danger' | 'info' = 'success') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 4000);
  };

  const handleStatusChange = (
    userId: string,
    newStatus: AdminUserAccountStatus,
    reason: string
  ) => {
    if (isFirebaseConfigured) {
      showToast(
        'Account status changes are disabled until Firebase Auth user-disable and token-revocation workflows are configured.',
        'danger'
      );
      return;
    }
    const updated = users.map((u) =>
      u.id === userId
        ? {
            ...u,
            status: newStatus,
            statusReason: reason,
            statusUpdatedAtIso: new Date().toISOString(),
          }
        : u
    );

    setUsers(updated);
    if (!isFirebaseConfigured) saveStoredUsers(updated);

    const target = updated.find((u) => u.id === userId);
    showToast(
      `Account for ${target?.displayName} is now ${newStatus}.${
        reason ? ` Reason: ${reason}` : ''
      }`,
      newStatus === 'SUSPENDED' ? 'danger' : 'success'
    );
  };

  // Metrics
  const activeCount = users.filter((u) => u.status === 'ACTIVE').length;
  const suspendedCount = users.filter((u) => u.status === 'SUSPENDED').length;
  const totalPointsCirculation = users.reduce((sum, u) => sum + u.pointsBalance, 0);

  if (!isLoaded) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="w-8 h-8 border-4 border-brand-primary border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <RouteGuard requiredPermission="VIEW_USER_PROFILES">
      <div className="space-y-6 max-w-7xl mx-auto pb-12">
        {/* Toast Alert */}
        {toast && (
          <div
            className={`fixed bottom-6 right-6 z-50 flex items-center gap-3 px-4 py-3 rounded-2xl shadow-2xl text-xs font-semibold animate-in slide-in-from-bottom duration-300 ${
              toast.type === 'success'
                ? 'bg-teal-900 border border-teal-500/50 text-teal-200'
                : toast.type === 'danger'
                ? 'bg-rose-900 border border-rose-500/50 text-rose-200'
                : 'bg-slate-800 border border-slate-600 text-slate-200'
            }`}
          >
            {toast.type === 'success' ? (
              <IconCheck className="w-4 h-4 text-teal-400 shrink-0" />
            ) : (
              <IconAlertCircle className="w-4 h-4 text-rose-400 shrink-0" />
            )}
            <span>{toast.message}</span>
          </div>
        )}

        {/* Top Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-6 border-b border-brand-dark-border/60">
          <div>
            <h1 className="text-2xl font-bold text-white tracking-tight flex items-center gap-2.5">
              <IconUsers className="w-7 h-7 text-brand-primary" />
              Players & User Directory
            </h1>
            <p className="text-xs text-slate-400 mt-1">
              Search registered players, audit immutable wallet transaction ledgers, and manage account statuses.
            </p>
          </div>
        </div>

        {/* Metrics Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="bg-brand-dark-card border border-brand-dark-border p-4 rounded-2xl flex items-center space-x-3.5">
            <div className="p-3 rounded-xl bg-brand-primary/10 border border-brand-primary/20 text-brand-primary">
              <IconUsers className="w-5 h-5" />
            </div>
            <div>
              <div className="text-[11px] font-medium text-slate-400 uppercase tracking-wider">
                Total Players
              </div>
              <div className="text-xl font-bold text-white mt-0.5">
                {users.length} <span className="text-xs text-slate-400 font-normal">registered</span>
              </div>
            </div>
          </div>

          <div className="bg-brand-dark-card border border-brand-dark-border p-4 rounded-2xl flex items-center space-x-3.5">
            <div className="p-3 rounded-xl bg-teal-500/10 border border-teal-500/20 text-teal-400">
              <IconCheck className="w-5 h-5" />
            </div>
            <div>
              <div className="text-[11px] font-medium text-slate-400 uppercase tracking-wider">
                Active Accounts
              </div>
              <div className="text-xl font-bold text-teal-400 mt-0.5">
                {activeCount} <span className="text-xs font-normal">in good standing</span>
              </div>
            </div>
          </div>

          <div className="bg-brand-dark-card border border-brand-dark-border p-4 rounded-2xl flex items-center space-x-3.5">
            <div className="p-3 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400">
              <IconLock className="w-5 h-5" />
            </div>
            <div>
              <div className="text-[11px] font-medium text-slate-400 uppercase tracking-wider">
                Suspended Accounts
              </div>
              <div className="text-xl font-bold text-rose-400 mt-0.5">
                {suspendedCount} <span className="text-xs font-normal">restricted</span>
              </div>
            </div>
          </div>

          <div className="bg-brand-dark-card border border-brand-dark-border p-4 rounded-2xl flex items-center space-x-3.5">
            <div className="p-3 rounded-xl bg-amber-500/10 border border-amber-500/20 text-amber-400">
              <IconSpark className="w-5 h-5" />
            </div>
            <div>
              <div className="text-[11px] font-medium text-slate-400 uppercase tracking-wider">
                Total Points Held
              </div>
              <div className="text-xl font-bold text-amber-400 mt-0.5 font-mono">
                {totalPointsCirculation.toLocaleString()} <span className="text-xs font-normal">pts</span>
              </div>
            </div>
          </div>
        </div>

        {/* Users Table */}
        <UserTable
          users={users}
          onViewLedger={(user) => {
            setSelectedUserForLedger(user);
            setIsLedgerOpen(true);
          }}
          onToggleStatus={(user) => {
            setSelectedUserForStatus(user);
            setIsStatusModalOpen(true);
          }}
        />

        {/* Ledger Drawer */}
        <UserLedgerDrawer
          isOpen={isLedgerOpen}
          user={selectedUserForLedger}
          onClose={() => {
            setIsLedgerOpen(false);
            setSelectedUserForLedger(null);
          }}
        />

        {/* Status Confirmation Modal */}
        <UserStatusModal
          isOpen={isStatusModalOpen}
          user={selectedUserForStatus}
          onClose={() => {
            setIsStatusModalOpen(false);
            setSelectedUserForStatus(null);
          }}
          onConfirmStatusChange={handleStatusChange}
        />
      </div>
    </RouteGuard>
  );
}
