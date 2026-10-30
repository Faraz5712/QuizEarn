'use client';

import React, { useMemo, useState } from 'react';
import { AdminReward, RewardCategory } from '../../types/reward';
import { Button } from '../ui/Button';
import {
  IconCheck,
  IconEdit,
  IconGift,
  IconPlus,
  IconSearch,
  IconSpark,
  IconTrash,
  IconX,
} from '../ui/Icons';

interface RewardTableProps {
  rewards: AdminReward[];
  onEditReward: (reward: AdminReward) => void;
  onAdjustStock: (reward: AdminReward) => void;
  onToggleActive: (rewardId: string) => void;
  onDeleteReward: (rewardId: string) => void;
}

export function RewardTable({
  rewards,
  onEditReward,
  onAdjustStock,
  onToggleActive,
  onDeleteReward,
}: RewardTableProps) {
  const [searchTerm, setSearchTerm] = useState('');
  const [categoryFilter, setCategoryFilter] = useState<string>('ALL');
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'ACTIVE' | 'INACTIVE'>('ALL');

  const filteredRewards = useMemo(() => {
    return rewards.filter((r) => {
      const matchesSearch =
        searchTerm === '' ||
        r.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
        r.id.toLowerCase().includes(searchTerm.toLowerCase()) ||
        r.provider.toLowerCase().includes(searchTerm.toLowerCase());

      const matchesCategory =
        categoryFilter === 'ALL' || r.category === categoryFilter;

      const matchesStatus =
        statusFilter === 'ALL' ||
        (statusFilter === 'ACTIVE' && r.isActive) ||
        (statusFilter === 'INACTIVE' && !r.isActive);

      return matchesSearch && matchesCategory && matchesStatus;
    });
  }, [rewards, searchTerm, categoryFilter, statusFilter]);

  const formatCategoryBadge = (category: RewardCategory) => {
    switch (category) {
      case 'GIFT_CARD':
        return (
          <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-semibold bg-indigo-500/10 text-indigo-300 border border-indigo-500/30">
            Gift Card
          </span>
        );
      case 'CASHOUT':
        return (
          <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-semibold bg-emerald-500/10 text-emerald-300 border border-emerald-500/30">
            Cashout
          </span>
        );
      case 'DIGITAL_VOUCHER':
        return (
          <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-semibold bg-purple-500/10 text-purple-300 border border-purple-500/30">
            Digital Voucher
          </span>
        );
      default:
        return category;
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
            placeholder="Search rewards by title, provider, or ID..."
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

        <div className="flex flex-wrap items-center gap-2">
          {/* Category Filter */}
          <div className="flex items-center bg-slate-900 p-1 rounded-xl border border-brand-dark-border text-xs">
            <button
              onClick={() => setCategoryFilter('ALL')}
              className={`px-3 py-1 rounded-lg font-medium transition-colors ${
                categoryFilter === 'ALL'
                  ? 'bg-brand-primary text-white'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              All Types
            </button>
            <button
              onClick={() => setCategoryFilter('GIFT_CARD')}
              className={`px-3 py-1 rounded-lg font-medium transition-colors ${
                categoryFilter === 'GIFT_CARD'
                  ? 'bg-brand-primary text-white'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              Gift Cards
            </button>
            <button
              onClick={() => setCategoryFilter('CASHOUT')}
              className={`px-3 py-1 rounded-lg font-medium transition-colors ${
                categoryFilter === 'CASHOUT'
                  ? 'bg-brand-primary text-white'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              Cashout
            </button>
            <button
              onClick={() => setCategoryFilter('DIGITAL_VOUCHER')}
              className={`px-3 py-1 rounded-lg font-medium transition-colors ${
                categoryFilter === 'DIGITAL_VOUCHER'
                  ? 'bg-brand-primary text-white'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              Vouchers
            </button>
          </div>

          {/* Status Filter */}
          <div className="flex items-center bg-slate-900 p-1 rounded-xl border border-brand-dark-border text-xs">
            <button
              onClick={() => setStatusFilter('ALL')}
              className={`px-2.5 py-1 rounded-lg font-medium transition-colors ${
                statusFilter === 'ALL'
                  ? 'bg-slate-700 text-white'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              All
            </button>
            <button
              onClick={() => setStatusFilter('ACTIVE')}
              className={`px-2.5 py-1 rounded-lg font-medium transition-colors ${
                statusFilter === 'ACTIVE'
                  ? 'bg-teal-600 text-white'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              Active
            </button>
            <button
              onClick={() => setStatusFilter('INACTIVE')}
              className={`px-2.5 py-1 rounded-lg font-medium transition-colors ${
                statusFilter === 'INACTIVE'
                  ? 'bg-rose-700 text-white'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              Disabled
            </button>
          </div>
        </div>
      </div>

      {/* Rewards Catalog Table */}
      <div className="bg-brand-dark-card border border-brand-dark-border rounded-2xl overflow-hidden shadow-xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-brand-dark-border/80 bg-slate-900/60 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
                <th className="py-3.5 px-4">Reward Item</th>
                <th className="py-3.5 px-4">Category</th>
                <th className="py-3.5 px-4">Points Cost</th>
                <th className="py-3.5 px-4">Inventory Stock</th>
                <th className="py-3.5 px-4">Min Level</th>
                <th className="py-3.5 px-4 text-center">Status</th>
                <th className="py-3.5 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-brand-dark-border/40 text-xs">
              {filteredRewards.length === 0 ? (
                <tr>
                  <td colSpan={7} className="py-12 text-center text-slate-400">
                    <div className="flex flex-col items-center justify-center space-y-2">
                      <IconGift className="w-8 h-8 text-slate-600" />
                      <p className="font-semibold text-slate-300">No rewards match your filter criteria</p>
                      <p className="text-[11px] text-slate-500">
                        Adjust filters or add a new reward to the catalog.
                      </p>
                    </div>
                  </td>
                </tr>
              ) : (
                filteredRewards.map((reward) => (
                  <tr
                    key={reward.id}
                    className={`hover:bg-slate-800/40 transition-colors ${
                      !reward.isActive ? 'opacity-60' : ''
                    }`}
                  >
                    {/* Title & Desc */}
                    <td className="py-4 px-4 max-w-xs">
                      <div className="font-semibold text-white">{reward.title}</div>
                      <div className="text-[11px] text-slate-400 truncate mt-0.5">
                        {reward.description}
                      </div>
                      <div className="font-mono text-[10px] text-slate-500 mt-1">
                        {reward.id} • Provider: {reward.provider}
                      </div>
                    </td>

                    {/* Category */}
                    <td className="py-4 px-4 whitespace-nowrap">
                      {formatCategoryBadge(reward.category)}
                    </td>

                    {/* Points Cost */}
                    <td className="py-4 px-4 whitespace-nowrap">
                      <span className="inline-flex items-center gap-1 font-bold text-amber-400 bg-amber-500/10 border border-amber-500/20 px-2 py-0.5 rounded-lg text-xs">
                        <IconSpark className="w-3.5 h-3.5" />
                        {reward.pointsCost.toLocaleString()} pts
                      </span>
                    </td>

                    {/* Stock Count */}
                    <td className="py-4 px-4 whitespace-nowrap">
                      <div className="flex items-center gap-2">
                        <span
                          className={`font-mono font-bold text-sm ${
                            reward.stockCount === 0
                              ? 'text-rose-400'
                              : reward.stockCount <= 10
                              ? 'text-amber-400'
                              : 'text-teal-400'
                          }`}
                        >
                          {reward.stockCount} units
                        </span>
                        <button
                          onClick={() => onAdjustStock(reward)}
                          className="px-2 py-0.5 rounded bg-slate-800 hover:bg-slate-700 text-[10px] text-slate-300 border border-slate-700 transition-colors"
                          title="Quick stock adjust"
                        >
                          Adjust
                        </button>
                      </div>
                    </td>

                    {/* Min Level */}
                    <td className="py-4 px-4 whitespace-nowrap">
                      <span className="inline-flex items-center px-2 py-0.5 rounded bg-slate-800 text-slate-300 text-[11px] font-mono border border-slate-700">
                        Level {reward.minLevelRequired}+
                      </span>
                    </td>

                    {/* Status & Active Toggle */}
                    <td className="py-4 px-4 whitespace-nowrap text-center">
                      <button
                        onClick={() => onToggleActive(reward.id)}
                        className={`inline-flex items-center px-2.5 py-1 rounded-full text-[11px] font-semibold transition-all ${
                          reward.isActive
                            ? 'bg-teal-500/20 text-teal-300 border border-teal-500/30 hover:bg-teal-500/30'
                            : 'bg-slate-800 text-slate-400 border border-slate-700 hover:bg-slate-700'
                        }`}
                        title={reward.isActive ? 'Click to disable' : 'Click to enable'}
                      >
                        {reward.isActive ? (
                          <>
                            <IconCheck className="w-3 h-3 mr-1" />
                            Active
                          </>
                        ) : (
                          'Disabled'
                        )}
                      </button>
                    </td>

                    {/* Actions */}
                    <td className="py-4 px-4 whitespace-nowrap text-right">
                      <div className="flex items-center justify-end space-x-1.5">
                        <button
                          onClick={() => onEditReward(reward)}
                          className="p-1.5 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition-colors"
                          title="Edit reward details"
                        >
                          <IconEdit className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => onDeleteReward(reward.id)}
                          className="p-1.5 text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 rounded-lg transition-colors"
                          title="Remove reward"
                        >
                          <IconTrash className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Table Footer */}
        <div className="p-3 bg-slate-900/40 border-t border-brand-dark-border text-[11px] text-slate-400 flex items-center justify-between">
          <span>
            Showing <strong className="text-white">{filteredRewards.length}</strong> of{' '}
            <strong className="text-white">{rewards.length}</strong> catalog rewards
          </span>
          <span className="text-slate-500">
            Real-time inventory deduction & automated stock restoration on reject
          </span>
        </div>
      </div>
    </div>
  );
}
