'use client';

import React, { useMemo, useState } from 'react';
import { AdminRedemption, AdminRedemptionStatus } from '../../types/reward';
import { Button } from '../ui/Button';
import {
  IconAlertCircle,
  IconCheck,
  IconClock,
  IconGift,
  IconSearch,
  IconSpark,
  IconX,
} from '../ui/Icons';

interface RedemptionTableProps {
  redemptions: AdminRedemption[];
  onApprove: (redemption: AdminRedemption) => void;
  onReject: (redemption: AdminRedemption) => void;
}

export function RedemptionTable({
  redemptions,
  onApprove,
  onReject,
}: RedemptionTableProps) {
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<'ALL' | AdminRedemptionStatus>('ALL');

  const filteredRedemptions = useMemo(() => {
    return redemptions.filter((r) => {
      const matchesSearch =
        searchTerm === '' ||
        r.id.toLowerCase().includes(searchTerm.toLowerCase()) ||
        r.userName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        r.userEmail.toLowerCase().includes(searchTerm.toLowerCase()) ||
        r.rewardTitle.toLowerCase().includes(searchTerm.toLowerCase()) ||
        r.payoutDetails.toLowerCase().includes(searchTerm.toLowerCase());

      const matchesStatus =
        statusFilter === 'ALL' || r.status === statusFilter;

      return matchesSearch && matchesStatus;
    });
  }, [redemptions, searchTerm, statusFilter]);

  const formatStatusBadge = (status: AdminRedemptionStatus) => {
    switch (status) {
      case 'APPROVED':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-teal-500/10 text-teal-300 border border-teal-500/30">
            <IconCheck className="w-3 h-3 mr-1" />
            Approved
          </span>
        );
      case 'PENDING':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-amber-500/10 text-amber-300 border border-amber-500/30 animate-pulse">
            <IconClock className="w-3 h-3 mr-1" />
            Pending Review
          </span>
        );
      case 'PROCESSING':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-blue-500/10 text-blue-300 border border-blue-500/30">
            Processing
          </span>
        );
      case 'REJECTED':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-rose-500/10 text-rose-300 border border-rose-500/30">
            <IconX className="w-3 h-3 mr-1" />
            Rejected & Refunded
          </span>
        );
      default:
        return status;
    }
  };

  const formatDateTime = (iso: string) => {
    if (!iso) return 'Not Set';
    try {
      const d = new Date(iso);
      return d.toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return iso;
    }
  };

  return (
    <div className="space-y-4">
      {/* Search and Status Chips */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-3 bg-brand-dark-card p-4 rounded-2xl border border-brand-dark-border">
        <div className="relative flex-1 max-w-md">
          <IconSearch className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Search claims by ID, user, reward, or payout..."
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
            All Claims
          </button>
          <button
            onClick={() => setStatusFilter('PENDING')}
            className={`px-3 py-1 rounded-lg font-medium transition-colors ${
              statusFilter === 'PENDING'
                ? 'bg-amber-600 text-white'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            Pending Review
          </button>
          <button
            onClick={() => setStatusFilter('APPROVED')}
            className={`px-3 py-1 rounded-lg font-medium transition-colors ${
              statusFilter === 'APPROVED'
                ? 'bg-teal-600 text-white'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            Approved
          </button>
          <button
            onClick={() => setStatusFilter('REJECTED')}
            className={`px-3 py-1 rounded-lg font-medium transition-colors ${
              statusFilter === 'REJECTED'
                ? 'bg-rose-700 text-white'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            Rejected
          </button>
        </div>
      </div>

      {/* Redemptions Data Table */}
      <div className="bg-brand-dark-card border border-brand-dark-border rounded-2xl overflow-hidden shadow-xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-brand-dark-border/80 bg-slate-900/60 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
                <th className="py-3.5 px-4">Claim ID & Date</th>
                <th className="py-3.5 px-4">Player</th>
                <th className="py-3.5 px-4">Reward Item</th>
                <th className="py-3.5 px-4">Points</th>
                <th className="py-3.5 px-4">Payout Destination</th>
                <th className="py-3.5 px-4">Status</th>
                <th className="py-3.5 px-4">Fulfillment Details</th>
                <th className="py-3.5 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-brand-dark-border/40 text-xs">
              {filteredRedemptions.length === 0 ? (
                <tr>
                  <td colSpan={8} className="py-12 text-center text-slate-400">
                    <div className="flex flex-col items-center justify-center space-y-2">
                      <IconGift className="w-8 h-8 text-slate-600" />
                      <p className="font-semibold text-slate-300">No redemption claims match your filter</p>
                      <p className="text-[11px] text-slate-500">
                        Adjust search filters or select a different status.
                      </p>
                    </div>
                  </td>
                </tr>
              ) : (
                filteredRedemptions.map((claim) => (
                  <tr key={claim.id} className="hover:bg-slate-800/40 transition-colors">
                    {/* ID & Date */}
                    <td className="py-4 px-4 whitespace-nowrap">
                      <div className="font-mono font-bold text-white text-xs">{claim.id}</div>
                      <div className="text-[10px] text-slate-400 mt-0.5">
                        {formatDateTime(claim.requestedAtIso)}
                      </div>
                    </td>

                    {/* Player */}
                    <td className="py-4 px-4 whitespace-nowrap">
                      <div className="font-semibold text-white">{claim.userName}</div>
                      <div className="text-[11px] text-slate-400 truncate max-w-[140px]">
                        {claim.userEmail}
                      </div>
                    </td>

                    {/* Reward */}
                    <td className="py-4 px-4 max-w-[160px]">
                      <div className="font-medium text-white truncate">{claim.rewardTitle}</div>
                      <div className="text-[10px] text-slate-400 font-mono mt-0.5">
                        {claim.rewardCategory}
                      </div>
                    </td>

                    {/* Points */}
                    <td className="py-4 px-4 whitespace-nowrap">
                      <span className="inline-flex items-center gap-1 font-bold text-amber-400 font-mono">
                        <IconSpark className="w-3.5 h-3.5" />
                        {claim.pointsDeducted.toLocaleString()} pts
                      </span>
                    </td>

                    {/* Payout Details */}
                    <td className="py-4 px-4 max-w-xs">
                      <div className="font-mono text-[11px] text-slate-200 truncate bg-slate-900/80 px-2 py-1 rounded border border-slate-800">
                        {claim.payoutDetails}
                      </div>
                    </td>

                    {/* Status Badge */}
                    <td className="py-4 px-4 whitespace-nowrap">
                      {formatStatusBadge(claim.status)}
                    </td>

                    {/* Fulfillment Details / Code */}
                    <td className="py-4 px-4 max-w-[180px]">
                      {claim.status === 'APPROVED' && claim.digitalCodeOrReference && (
                        <div>
                          <div className="font-mono text-[11px] font-bold text-teal-300 bg-teal-950/40 px-2 py-0.5 rounded border border-teal-500/30 truncate">
                            {claim.digitalCodeOrReference}
                          </div>
                          <div className="text-[10px] text-slate-500 mt-0.5">
                            Dispatched {formatDateTime(claim.processedAtIso || '')}
                          </div>
                        </div>
                      )}
                      {claim.status === 'REJECTED' && claim.rejectionReason && (
                        <div
                          className="text-[11px] text-rose-300 truncate max-w-[170px]"
                          title={claim.rejectionReason}
                        >
                          {claim.rejectionReason}
                        </div>
                      )}
                      {claim.status === 'PENDING' && (
                        <span className="text-[11px] text-slate-500 italic">
                          Awaiting agent approval
                        </span>
                      )}
                    </td>

                    {/* Actions */}
                    <td className="py-4 px-4 whitespace-nowrap text-right">
                      {claim.status === 'PENDING' ? (
                        <div className="flex items-center justify-end space-x-2">
                          <button
                            onClick={() => onApprove(claim)}
                            className="px-2.5 py-1 rounded-lg bg-teal-600 hover:bg-teal-500 text-white font-semibold text-xs transition-colors flex items-center gap-1"
                          >
                            <IconCheck className="w-3 h-3" />
                            Approve
                          </button>
                          <button
                            onClick={() => onReject(claim)}
                            className="px-2.5 py-1 rounded-lg bg-rose-600 hover:bg-rose-500 text-white font-semibold text-xs transition-colors flex items-center gap-1"
                          >
                            <IconX className="w-3 h-3" />
                            Reject
                          </button>
                        </div>
                      ) : (
                        <span className="text-[11px] text-slate-500 font-mono">
                          Completed
                        </span>
                      )}
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
            Showing <strong className="text-white">{filteredRedemptions.length}</strong> of{' '}
            <strong className="text-white">{redemptions.length}</strong> total redemption requests
          </span>
          <span className="text-slate-500">
            Atomic REDEMPTION_HOLD points reservation on claim creation
          </span>
        </div>
      </div>
    </div>
  );
}
