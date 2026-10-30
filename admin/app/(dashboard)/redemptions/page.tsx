'use client';

import React, { useEffect, useState } from 'react';
import Link from 'next/link';
import { RouteGuard } from '../../../components/layout/RouteGuard';
import { Button } from '../../../components/ui/Button';
import {
  IconAlertCircle,
  IconCheck,
  IconChevronRight,
  IconClock,
  IconGift,
  IconSpark,
  IconX,
} from '../../../components/ui/Icons';
import { RedemptionApproveModal } from '../../../components/redemptions/RedemptionApproveModal';
import { RedemptionRejectModal } from '../../../components/redemptions/RedemptionRejectModal';
import { RedemptionTable } from '../../../components/redemptions/RedemptionTable';
import {
  loadStoredRedemptions,
  loadStoredRewards,
  saveStoredRedemptions,
  saveStoredRewards,
} from '../../../lib/rewards-store';
import { refundUserRedemption } from '../../../lib/users-store';
import { AdminRedemption } from '../../../types/reward';
import { listAdminRecords, processAdminRedemption } from '../../../lib/backend';
import { isFirebaseConfigured } from '../../../lib/firebase';

export default function RedemptionsQueuePage() {
  const [redemptions, setRedemptions] = useState<AdminRedemption[]>([]);
  const [isLoaded, setIsLoaded] = useState(false);

  // Modals state
  const [isApproveModalOpen, setIsApproveModalOpen] = useState(false);
  const [claimToApprove, setClaimToApprove] = useState<AdminRedemption | null>(null);

  const [isRejectModalOpen, setIsRejectModalOpen] = useState(false);
  const [claimToReject, setClaimToReject] = useState<AdminRedemption | null>(null);

  // Toast
  const [toast, setToast] = useState<{
    message: string;
    type: 'success' | 'danger' | 'info';
  } | null>(null);

  useEffect(() => {
    if (!isFirebaseConfigured) {
      setRedemptions(loadStoredRedemptions());
      setIsLoaded(true);
      return;
    }
    listAdminRecords<AdminRedemption>('redemptions').then(setRedemptions).catch((error: unknown) => {
      showToast(error instanceof Error ? error.message : 'Failed to load redemption queue.', 'danger');
    }).finally(() => setIsLoaded(true));
  }, []);

  const showToast = (message: string, type: 'success' | 'danger' | 'info' = 'success') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 4000);
  };

  const handleApprove = async (redemptionId: string, digitalCode: string) => {
    if (isFirebaseConfigured) {
      try {
        await processAdminRedemption(redemptionId, 'APPROVE', { digitalCode });
        setRedemptions(await listAdminRecords<AdminRedemption>('redemptions'));
        showToast(`Claim ${redemptionId} approved and recorded by the backend.`);
      } catch (error) {
        showToast(error instanceof Error ? error.message : 'Redemption approval failed.', 'danger');
      }
      return;
    }
    const updated = redemptions.map((r) =>
      r.id === redemptionId
        ? {
            ...r,
            status: 'APPROVED' as const,
            digitalCodeOrReference: digitalCode,
            processedAtIso: new Date().toISOString(),
            processedByAdminId: 'adm_support_agent',
          }
        : r
    );

    setRedemptions(updated);
    saveStoredRedemptions(updated);
    const target = updated.find((r) => r.id === redemptionId);
    showToast(
      `Claim ${redemptionId} approved! Digital voucher code "${digitalCode}" issued to ${target?.userName}.`
    );
  };

  const handleReject = async (redemptionId: string, reason: string) => {
    const target = redemptions.find((r) => r.id === redemptionId);
    if (!target) return;

    if (isFirebaseConfigured) {
      try {
        await processAdminRedemption(redemptionId, 'REJECT', { reason });
        setRedemptions(await listAdminRecords<AdminRedemption>('redemptions'));
        showToast(`Claim ${redemptionId} rejected; refund and stock handling completed by the backend.`, 'danger');
      } catch (error) {
        showToast(error instanceof Error ? error.message : 'Redemption rejection failed.', 'danger');
      }
      return;
    }

    const updated = redemptions.map((r) =>
      r.id === redemptionId
        ? {
            ...r,
            status: 'REJECTED' as const,
            rejectionReason: reason,
            processedAtIso: new Date().toISOString(),
            processedByAdminId: 'adm_support_agent',
          }
        : r
    );

    setRedemptions(updated);
    saveStoredRedemptions(updated);

    // Atomically refund points to the user's wallet ledger
    refundUserRedemption(
      target.userId,
      target.pointsDeducted,
      target.id,
      reason
    );

    // Restore stock in rewards inventory
    const rewards = loadStoredRewards();
    const updatedRewards = rewards.map((rw) =>
      rw.id === target.rewardId ? { ...rw, stockCount: rw.stockCount + 1 } : rw
    );
    saveStoredRewards(updatedRewards);

    showToast(
      `Claim ${redemptionId} rejected. ${target.pointsDeducted.toLocaleString()} pts refunded to ${
        target.userName
      }'s wallet via REDEMPTION_REFUND ledger credit.`,
      'danger'
    );
  };

  // Metrics
  const pendingCount = redemptions.filter((r) => r.status === 'PENDING').length;
  const approvedCount = redemptions.filter((r) => r.status === 'APPROVED').length;
  const rejectedCount = redemptions.filter((r) => r.status === 'REJECTED').length;
  const pendingPointsHeld = redemptions
    .filter((r) => r.status === 'PENDING')
    .reduce((sum, r) => sum + r.pointsDeducted, 0);

  if (!isLoaded) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="w-8 h-8 border-4 border-brand-primary border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <RouteGuard requiredPermission="REVIEW_REDEMPTIONS">
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
              Reward Redemption Queue
            </h1>
            <p className="text-xs text-slate-400 mt-1">
              Review player redemption claims, dispatch gift card claim codes, or reject with automatic points refund.
            </p>
          </div>
          <div className="mt-4 sm:mt-0 flex items-center space-x-3">
            <Link
              href="/rewards"
              className="px-3.5 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 text-xs font-semibold transition-colors flex items-center gap-1.5"
            >
              <span>Manage Rewards Inventory</span>
              <IconChevronRight className="w-3.5 h-3.5 text-slate-400" />
            </Link>
          </div>
        </div>

        {/* Metrics Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="bg-brand-dark-card border border-brand-dark-border p-4 rounded-2xl flex items-center space-x-3.5">
            <div className="p-3 rounded-xl bg-amber-500/10 border border-amber-500/20 text-amber-400">
              <IconClock className="w-5 h-5" />
            </div>
            <div>
              <div className="text-[11px] font-medium text-slate-400 uppercase tracking-wider">
                Pending Review
              </div>
              <div className="text-xl font-bold text-amber-400 mt-0.5">
                {pendingCount} <span className="text-xs font-normal">claims</span>
              </div>
            </div>
          </div>

          <div className="bg-brand-dark-card border border-brand-dark-border p-4 rounded-2xl flex items-center space-x-3.5">
            <div className="p-3 rounded-xl bg-teal-500/10 border border-teal-500/20 text-teal-400">
              <IconCheck className="w-5 h-5" />
            </div>
            <div>
              <div className="text-[11px] font-medium text-slate-400 uppercase tracking-wider">
                Approved & Fulfilled
              </div>
              <div className="text-xl font-bold text-teal-400 mt-0.5">
                {approvedCount} <span className="text-xs font-normal">claims</span>
              </div>
            </div>
          </div>

          <div className="bg-brand-dark-card border border-brand-dark-border p-4 rounded-2xl flex items-center space-x-3.5">
            <div className="p-3 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400">
              <IconX className="w-5 h-5" />
            </div>
            <div>
              <div className="text-[11px] font-medium text-slate-400 uppercase tracking-wider">
                Rejected Claims
              </div>
              <div className="text-xl font-bold text-rose-400 mt-0.5">
                {rejectedCount} <span className="text-xs font-normal">refunded</span>
              </div>
            </div>
          </div>

          <div className="bg-brand-dark-card border border-brand-dark-border p-4 rounded-2xl flex items-center space-x-3.5">
            <div className="p-3 rounded-xl bg-purple-500/10 border border-purple-500/20 text-purple-400">
              <IconSpark className="w-5 h-5" />
            </div>
            <div>
              <div className="text-[11px] font-medium text-slate-400 uppercase tracking-wider">
                Points in Escrow Hold
              </div>
              <div className="text-xl font-bold text-white mt-0.5 font-mono">
                {pendingPointsHeld.toLocaleString()} <span className="text-xs text-slate-400 font-normal">pts</span>
              </div>
            </div>
          </div>
        </div>

        {/* Redemptions Table */}
        <RedemptionTable
          redemptions={redemptions}
          onApprove={(claim) => {
            setClaimToApprove(claim);
            setIsApproveModalOpen(true);
          }}
          onReject={(claim) => {
            setClaimToReject(claim);
            setIsRejectModalOpen(true);
          }}
        />

        {/* Modals */}
        <RedemptionApproveModal
          isOpen={isApproveModalOpen}
          redemption={claimToApprove}
          onClose={() => {
            setIsApproveModalOpen(false);
            setClaimToApprove(null);
          }}
          onConfirmApprove={handleApprove}
        />

        <RedemptionRejectModal
          isOpen={isRejectModalOpen}
          redemption={claimToReject}
          onClose={() => {
            setIsRejectModalOpen(false);
            setClaimToReject(null);
          }}
          onConfirmReject={handleReject}
        />
      </div>
    </RouteGuard>
  );
}
