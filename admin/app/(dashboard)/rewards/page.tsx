'use client';

import React, { useEffect, useState } from 'react';
import Link from 'next/link';
import { RouteGuard } from '../../../components/layout/RouteGuard';
import { Button } from '../../../components/ui/Button';
import {
  IconAlertCircle,
  IconCheck,
  IconChevronRight,
  IconGift,
  IconPlus,
  IconSpark,
} from '../../../components/ui/Icons';
import { RewardConfigModal } from '../../../components/rewards/RewardConfigModal';
import { RewardTable } from '../../../components/rewards/RewardTable';
import { StockAdjustModal } from '../../../components/rewards/StockAdjustModal';
import {
  loadStoredRewards,
  saveStoredRewards,
} from '../../../lib/rewards-store';
import { AdminReward } from '../../../types/reward';
import {
  adjustAdminRewardStock,
  deleteAdminRecord,
  listAdminRecords,
  saveAdminRecord,
} from '../../../lib/backend';
import { isFirebaseConfigured } from '../../../lib/firebase';

export default function RewardsCatalogPage() {
  const [rewards, setRewards] = useState<AdminReward[]>([]);
  const [isLoaded, setIsLoaded] = useState(false);

  // Modals state
  const [isConfigModalOpen, setIsConfigModalOpen] = useState(false);
  const [rewardToEdit, setRewardToEdit] = useState<AdminReward | null>(null);

  const [isStockModalOpen, setIsStockModalOpen] = useState(false);
  const [rewardToAdjust, setRewardToAdjust] = useState<AdminReward | null>(null);

  // Toast feedback
  const [toast, setToast] = useState<{
    message: string;
    type: 'success' | 'danger' | 'info';
  } | null>(null);

  useEffect(() => {
    if (!isFirebaseConfigured) {
      setRewards(loadStoredRewards());
      setIsLoaded(true);
      return;
    }
    listAdminRecords<AdminReward>('rewards').then(setRewards).catch((error: unknown) => {
      showToast(error instanceof Error ? error.message : 'Failed to load rewards.', 'danger');
    }).finally(() => setIsLoaded(true));
  }, []);

  const showToast = (message: string, type: 'success' | 'danger' | 'info' = 'success') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 4000);
  };

  const handleSaveReward = async (
    data: Omit<AdminReward, 'id' | 'createdAt'> & { id?: string }
  ) => {
    let updated: AdminReward[];
    if (data.id) {
      updated = rewards.map((r) =>
        r.id === data.id ? { ...r, ...data } : r
      );
    } else {
      const newReward: AdminReward = {
        id: `RWD-NEW-${Date.now().toString().slice(-4)}`,
        ...data,
        createdAt: new Date().toISOString(),
      };
      updated = [newReward, ...rewards];
    }

    const savedReward = updated.find((reward) => reward.id === data.id) ?? updated[0]!;
    try {
      await saveAdminRecord('rewards', { ...savedReward });
    } catch (error) {
      showToast(error instanceof Error ? error.message : 'Failed to save reward.', 'danger');
      return;
    }
    setRewards(updated);
    if (!isFirebaseConfigured) saveStoredRewards(updated);
    showToast(data.id ? `Reward "${data.title}" updated.` : `Reward "${savedReward.title}" added to catalog.`);
    setRewardToEdit(null);
  };

  const handleAdjustStock = async (rewardId: string, newStock: number, note: string) => {
    try {
      if (isFirebaseConfigured) await adjustAdminRewardStock(rewardId, newStock);
    } catch (error) {
      showToast(error instanceof Error ? error.message : 'Failed to adjust stock.', 'danger');
      return;
    }
    const updated = rewards.map((r) =>
      r.id === rewardId ? { ...r, stockCount: newStock, updatedAt: new Date().toISOString() } : r
    );
    setRewards(updated);
    if (!isFirebaseConfigured) saveStoredRewards(updated);
    const target = updated.find((r) => r.id === rewardId);
    showToast(
      `Inventory for "${target?.title}" adjusted to ${newStock} units.${note ? ` (${note})` : ''}`,
      'info'
    );
  };

  const handleToggleActive = async (rewardId: string) => {
    const updated = rewards.map((r) =>
      r.id === rewardId ? { ...r, isActive: !r.isActive } : r
    );
    const target = updated.find((reward) => reward.id === rewardId);
    try {
      if (target) await saveAdminRecord('rewards', { ...target });
    } catch (error) {
      showToast(error instanceof Error ? error.message : 'Failed to update reward.', 'danger');
      return;
    }
    setRewards(updated);
    if (!isFirebaseConfigured) saveStoredRewards(updated);
    showToast(
      `Reward "${target?.title}" is now ${target?.isActive ? 'Active' : 'Disabled'}.`,
      'info'
    );
  };

  const handleDeleteReward = async (rewardId: string) => {
    const target = rewards.find((r) => r.id === rewardId);
    if (!target) return;
    if (confirm(`Remove reward "${target.title}" from catalog?`)) {
      try {
        if (isFirebaseConfigured) await deleteAdminRecord('rewards', rewardId);
      } catch (error) {
        showToast(error instanceof Error ? error.message : 'Failed to delete reward.', 'danger');
        return;
      }
      const updated = rewards.filter((r) => r.id !== rewardId);
      setRewards(updated);
      if (!isFirebaseConfigured) saveStoredRewards(updated);
      showToast(`Reward "${target.title}" deleted.`, 'info');
    }
  };

  // Metrics
  const totalStock = rewards.reduce((sum, r) => sum + r.stockCount, 0);
  const lowStockCount = rewards.filter((r) => r.stockCount <= 10).length;
  const activeCount = rewards.filter((r) => r.isActive).length;

  if (!isLoaded) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="w-8 h-8 border-4 border-brand-primary border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <RouteGuard requiredPermission="MANAGE_REWARDS">
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
              <IconGift className="w-7 h-7 text-brand-gold" />
              Rewards Inventory Management
            </h1>
            <p className="text-xs text-slate-400 mt-1">
              Curate gift card catalog, real-money cashouts, points thresholds, and real-time voucher inventory stock.
            </p>
          </div>
          <div className="mt-4 sm:mt-0 flex items-center space-x-3">
            <Link
              href="/redemptions"
              className="px-3.5 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 text-xs font-semibold transition-colors flex items-center gap-1.5"
            >
              <span>Redemption Queue</span>
              <IconChevronRight className="w-3.5 h-3.5 text-slate-400" />
            </Link>
            <Button
              variant="primary"
              size="sm"
              onClick={() => {
                setRewardToEdit(null);
                setIsConfigModalOpen(true);
              }}
              className="flex items-center gap-1.5 shadow-lg shadow-brand-primary/20"
            >
              <IconPlus className="w-4 h-4" />
              Add New Reward
            </Button>
          </div>
        </div>

        {/* Metrics Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="bg-brand-dark-card border border-brand-dark-border p-4 rounded-2xl flex items-center space-x-3.5">
            <div className="p-3 rounded-xl bg-indigo-500/10 border border-indigo-500/20 text-indigo-400">
              <IconGift className="w-5 h-5" />
            </div>
            <div>
              <div className="text-[11px] font-medium text-slate-400 uppercase tracking-wider">
                Catalog Items
              </div>
              <div className="text-xl font-bold text-white mt-0.5">
                {rewards.length} <span className="text-xs text-slate-400 font-normal">rewards</span>
              </div>
            </div>
          </div>

          <div className="bg-brand-dark-card border border-brand-dark-border p-4 rounded-2xl flex items-center space-x-3.5">
            <div className="p-3 rounded-xl bg-teal-500/10 border border-teal-500/20 text-teal-400">
              <IconCheck className="w-5 h-5" />
            </div>
            <div>
              <div className="text-[11px] font-medium text-slate-400 uppercase tracking-wider">
                Active in Store
              </div>
              <div className="text-xl font-bold text-teal-400 mt-0.5">
                {activeCount} <span className="text-xs font-normal">active</span>
              </div>
            </div>
          </div>

          <div className="bg-brand-dark-card border border-brand-dark-border p-4 rounded-2xl flex items-center space-x-3.5">
            <div className="p-3 rounded-xl bg-amber-500/10 border border-amber-500/20 text-amber-400">
              <IconSpark className="w-5 h-5" />
            </div>
            <div>
              <div className="text-[11px] font-medium text-slate-400 uppercase tracking-wider">
                Total Stock Units
              </div>
              <div className="text-xl font-bold text-amber-400 mt-0.5 font-mono">
                {totalStock.toLocaleString()} <span className="text-xs font-normal">units</span>
              </div>
            </div>
          </div>

          <div className="bg-brand-dark-card border border-brand-dark-border p-4 rounded-2xl flex items-center space-x-3.5">
            <div className="p-3 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400">
              <IconAlertCircle className="w-5 h-5" />
            </div>
            <div>
              <div className="text-[11px] font-medium text-slate-400 uppercase tracking-wider">
                Low Stock Warning
              </div>
              <div className="text-xl font-bold text-rose-400 mt-0.5 font-mono">
                {lowStockCount} <span className="text-xs font-normal">items ≤ 10</span>
              </div>
            </div>
          </div>
        </div>

        {/* Table */}
        <RewardTable
          rewards={rewards}
          onEditReward={(r) => {
            setRewardToEdit(r);
            setIsConfigModalOpen(true);
          }}
          onAdjustStock={(r) => {
            setRewardToAdjust(r);
            setIsStockModalOpen(true);
          }}
          onToggleActive={handleToggleActive}
          onDeleteReward={handleDeleteReward}
        />

        {/* Modals */}
        <RewardConfigModal
          isOpen={isConfigModalOpen}
          rewardToEdit={rewardToEdit}
          onClose={() => {
            setIsConfigModalOpen(false);
            setRewardToEdit(null);
          }}
          onSave={handleSaveReward}
        />

        <StockAdjustModal
          isOpen={isStockModalOpen}
          reward={rewardToAdjust}
          onClose={() => {
            setIsStockModalOpen(false);
            setRewardToAdjust(null);
          }}
          onSave={handleAdjustStock}
        />
      </div>
    </RouteGuard>
  );
}
