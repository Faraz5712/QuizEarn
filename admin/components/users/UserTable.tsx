'use client';

import React, { useMemo, useState } from 'react';
import { AdminPlayer, AdminUserAccountStatus } from '../../types/user';
import { Button } from '../ui/Button';
import {
  IconAlertCircle,
  IconBarChart,
  IconCheck,
  IconLock,
  IconSearch,
  IconShield,
  IconSpark,
  IconUsers,
  IconX,
} from '../ui/Icons';

interface UserTableProps {
  users: AdminPlayer[];
  onViewLedger: (user: AdminPlayer) => void;
  onToggleStatus: (user: AdminPlayer) => void;
}

export function UserTable({
  users,
  onViewLedger,
  onToggleStatus,
}: UserTableProps) {
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<'ALL' | AdminUserAccountStatus>('ALL');

  const filteredUsers = useMemo(() => {
    return users.filter((u) => {
      const matchesSearch =
        searchTerm === '' ||
        u.id.toLowerCase().includes(searchTerm.toLowerCase()) ||
        u.displayName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        u.email.toLowerCase().includes(searchTerm.toLowerCase());

      const matchesStatus =
        statusFilter === 'ALL' || u.status === statusFilter;

      return matchesSearch && matchesStatus;
    });
  }, [users, searchTerm, statusFilter]);

  const formatDate = (iso: string) => {
    try {
      return new Date(iso).toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
      });
    } catch {
      return iso;
    }
  };

  return (
    <div className="space-y-4">
      {/* Search and Filters */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-3 bg-brand-dark-card p-4 rounded-2xl border border-brand-dark-border">
        <div className="relative flex-1 max-w-md">
          <IconSearch className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Search players by name, email, or user ID..."
            className="w-full pl-10 pr-4 py-2 rounded-xl bg-slate-900 border border-brand-dark-border text-xs text-white placeholder-slate-500 focus:outline-none focus:border-brand-primary transition-colors"
          />
          {searchTerm && (
            <button
              onClick={() => setSearchTerm('')}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500 hover:text-white"
            >
              <IconX className="w-3.5 h-3.5" />
            </button>
          )}
        </div>

        <div className="flex items-center bg-slate-900 p-1 rounded-xl border border-brand-dark-border text-xs">
          <button
            onClick={() => setStatusFilter('ALL')}
            className={`px-3 py-1 rounded-lg font-medium transition-colors ${
              statusFilter === 'ALL'
                ? 'bg-brand-primary text-white'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            All Players
          </button>
          <button
            onClick={() => setStatusFilter('ACTIVE')}
            className={`px-3 py-1 rounded-lg font-medium transition-colors ${
              statusFilter === 'ACTIVE'
                ? 'bg-teal-600 text-white'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            Active
          </button>
          <button
            onClick={() => setStatusFilter('SUSPENDED')}
            className={`px-3 py-1 rounded-lg font-medium transition-colors ${
              statusFilter === 'SUSPENDED'
                ? 'bg-rose-700 text-white'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            Suspended
          </button>
        </div>
      </div>

      {/* Players Table */}
      <div className="bg-brand-dark-card border border-brand-dark-border rounded-2xl overflow-hidden shadow-xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-brand-dark-border/80 bg-slate-900/60 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
                <th className="py-3.5 px-4">Player Profile</th>
                <th className="py-3.5 px-4">Tier</th>
                <th className="py-3.5 px-4">Wallet Balance</th>
                <th className="py-3.5 px-4">Performance</th>
                <th className="py-3.5 px-4">Status</th>
                <th className="py-3.5 px-4">Joined</th>
                <th className="py-3.5 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-brand-dark-border/40 text-xs">
              {filteredUsers.length === 0 ? (
                <tr>
                  <td colSpan={7} className="py-12 text-center text-slate-400">
                    <div className="flex flex-col items-center justify-center space-y-2">
                      <IconUsers className="w-8 h-8 text-slate-600" />
                      <p className="font-semibold text-slate-300">No players match your search</p>
                      <p className="text-[11px] text-slate-500">
                        Try searching by a different name, email, or reset filters.
                      </p>
                    </div>
                  </td>
                </tr>
              ) : (
                filteredUsers.map((user) => (
                  <tr key={user.id} className="hover:bg-slate-800/40 transition-colors">
                    {/* Profile avatar & name */}
                    <td className="py-4 px-4 whitespace-nowrap">
                      <div className="flex items-center space-x-3">
                        <div
                          className="w-9 h-9 rounded-full flex items-center justify-center font-bold text-white text-xs shadow-md border border-white/20"
                          style={{ backgroundColor: user.avatarColor || '#5B5FEF' }}
                        >
                          {user.displayName.charAt(0).toUpperCase()}
                        </div>
                        <div>
                          <div className="font-semibold text-white flex items-center gap-1.5">
                            {user.displayName}
                            {user.status === 'SUSPENDED' && (
                              <span className="p-0.5 rounded bg-rose-500/20 text-rose-400" title="Account Suspended">
                                <IconLock className="w-3 h-3" />
                              </span>
                            )}
                          </div>
                          <div className="text-[11px] text-slate-400">{user.email}</div>
                          <div className="text-[10px] text-slate-500 font-mono mt-0.5">{user.id}</div>
                        </div>
                      </div>
                    </td>

                    {/* Level */}
                    <td className="py-4 px-4 whitespace-nowrap">
                      <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-semibold bg-brand-primary/10 text-brand-primary border border-brand-primary/30">
                        Level {user.level}
                      </span>
                    </td>

                    {/* Points Balance */}
                    <td className="py-4 px-4 whitespace-nowrap">
                      <span className="inline-flex items-center gap-1 font-bold text-amber-400 font-mono text-sm">
                        <IconSpark className="w-4 h-4" />
                        {user.pointsBalance.toLocaleString()} pts
                      </span>
                    </td>

                    {/* Performance */}
                    <td className="py-4 px-4 whitespace-nowrap">
                      <div className="text-slate-300 font-medium">
                        {user.quizzesPlayed} quizzes • {user.accuracyRate}% acc
                      </div>
                      <div className="text-[10px] text-amber-400/90 flex items-center gap-1 mt-0.5">
                        🔥 {user.winStreak} streak
                      </div>
                    </td>

                    {/* Status */}
                    <td className="py-4 px-4 whitespace-nowrap">
                      {user.status === 'ACTIVE' ? (
                        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-teal-500/10 text-teal-300 border border-teal-500/30">
                          <IconCheck className="w-3 h-3 mr-1" />
                          Active
                        </span>
                      ) : (
                        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-rose-500/10 text-rose-300 border border-rose-500/30">
                          <IconLock className="w-3 h-3 mr-1" />
                          Suspended
                        </span>
                      )}
                    </td>

                    {/* Joined */}
                    <td className="py-4 px-4 whitespace-nowrap text-slate-400 text-[11px]">
                      {formatDate(user.createdAtIso)}
                    </td>

                    {/* Actions */}
                    <td className="py-4 px-4 whitespace-nowrap text-right">
                      <div className="flex items-center justify-end space-x-2">
                        <button
                          onClick={() => onViewLedger(user)}
                          className="px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 font-medium text-xs border border-slate-700 transition-colors flex items-center gap-1"
                        >
                          <IconBarChart className="w-3.5 h-3.5 text-brand-gold" />
                          Ledger
                        </button>
                        <button
                          onClick={() => onToggleStatus(user)}
                          className={`px-2.5 py-1 rounded-lg font-medium text-xs transition-colors flex items-center gap-1 ${
                            user.status === 'ACTIVE'
                              ? 'bg-rose-500/10 hover:bg-rose-500/20 text-rose-300 border border-rose-500/30'
                              : 'bg-teal-500/10 hover:bg-teal-500/20 text-teal-300 border border-teal-500/30'
                          }`}
                        >
                          {user.status === 'ACTIVE' ? 'Suspend' : 'Reactivate'}
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Footer */}
        <div className="p-3 bg-slate-900/40 border-t border-brand-dark-border text-[11px] text-slate-400 flex items-center justify-between">
          <span>
            Showing <strong className="text-white">{filteredUsers.length}</strong> of{' '}
            <strong className="text-white">{users.length}</strong> registered players
          </span>
          <span className="text-slate-500">
            Immutable audit logs & double-entry wallet reconciliation
          </span>
        </div>
      </div>
    </div>
  );
}
