'use client';

import React, { useMemo, useState } from 'react';
import { AdminTournament, AdminTournamentStatus } from '../../types/tournament';
import { Button } from '../ui/Button';
import {
  IconAlertCircle,
  IconCheck,
  IconClock,
  IconEdit,
  IconSearch,
  IconSpark,
  IconTrophy,
  IconX,
} from '../ui/Icons';

interface TournamentTableProps {
  tournaments: AdminTournament[];
  onEditTournament: (tournament: AdminTournament) => void;
  onCancelTournament: (tournament: AdminTournament) => void;
  onCloneTournament: (tournament: AdminTournament) => void;
}

export function TournamentTable({
  tournaments,
  onEditTournament,
  onCancelTournament,
  onCloneTournament,
}: TournamentTableProps) {
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 6;

  const filteredTournaments = useMemo(() => {
    return tournaments.filter((t) => {
      const matchesSearch =
        searchTerm === '' ||
        t.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
        t.id.toLowerCase().includes(searchTerm.toLowerCase()) ||
        t.categoryName.toLowerCase().includes(searchTerm.toLowerCase());

      const matchesStatus =
        statusFilter === 'ALL' || t.status === statusFilter;

      return matchesSearch && matchesStatus;
    });
  }, [tournaments, searchTerm, statusFilter]);

  const totalPages = Math.max(1, Math.ceil(filteredTournaments.length / itemsPerPage));
  const paginatedTournaments = useMemo(() => {
    const startIdx = (currentPage - 1) * itemsPerPage;
    return filteredTournaments.slice(startIdx, startIdx + itemsPerPage);
  }, [filteredTournaments, currentPage, itemsPerPage]);

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

  const renderStatusBadge = (status: AdminTournamentStatus) => {
    switch (status) {
      case 'LIVE':
        return (
          <span className="inline-flex items-center space-x-1.5 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-950/40 text-emerald-300 border border-emerald-500/40 animate-pulse">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-400" />
            <span>LIVE NOW</span>
          </span>
        );
      case 'SCHEDULED':
        return (
          <span className="inline-flex items-center space-x-1 px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-brand-primary/20 text-brand-primary border border-brand-primary/30">
            <IconClock className="w-3 h-3" />
            <span>Scheduled</span>
          </span>
        );
      case 'DRAFT':
        return (
          <span className="px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-slate-800 text-slate-300 border border-slate-700">
            Draft
          </span>
        );
      case 'ENDED':
        return (
          <span className="inline-flex items-center space-x-1 px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-teal-950/30 text-teal-300 border border-teal-500/30">
            <IconCheck className="w-3 h-3" />
            <span>Completed</span>
          </span>
        );
      case 'CANCELLED':
        return (
          <span className="inline-flex items-center space-x-1 px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-rose-950/30 text-rose-300 border border-rose-500/30">
            <IconX className="w-3 h-3" />
            <span>Cancelled</span>
          </span>
        );
    }
  };

  return (
    <div className="space-y-4">
      {/* Search and Status Filter Chips */}
      <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-3 p-4 rounded-2xl bg-brand-dark-card border border-brand-dark-border">
        {/* Search Input */}
        <div className="relative flex-1">
          <IconSearch className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder="Search tournaments by title, category, or ID..."
            value={searchTerm}
            onChange={(e) => {
              setSearchTerm(e.target.value);
              setCurrentPage(1);
            }}
            className="w-full pl-10 pr-4 py-2 rounded-xl bg-brand-dark-surface border border-brand-dark-border text-white placeholder-slate-500 text-xs focus:outline-none focus:border-brand-primary"
          />
        </div>

        {/* Status Filter Chips */}
        <div className="flex flex-wrap items-center gap-1.5">
          {[
            { id: 'ALL', label: 'All' },
            { id: 'LIVE', label: 'Live Now' },
            { id: 'SCHEDULED', label: 'Upcoming' },
            { id: 'ENDED', label: 'Completed' },
            { id: 'CANCELLED', label: 'Cancelled' },
          ].map((chip) => {
            const isSelected = statusFilter === chip.id;
            return (
              <button
                key={chip.id}
                onClick={() => {
                  setStatusFilter(chip.id);
                  setCurrentPage(1);
                }}
                className={`px-3 py-1.5 rounded-xl text-xs font-semibold transition-all ${
                  isSelected
                    ? 'bg-brand-primary text-white shadow-sm shadow-brand-primary/20'
                    : 'bg-white/5 text-slate-400 hover:text-white hover:bg-white/10'
                }`}
              >
                {chip.label}
              </button>
            );
          })}
        </div>
      </div>

      {/* Tournaments Data Table */}
      <div className="rounded-2xl bg-brand-dark-card border border-brand-dark-border overflow-hidden shadow-xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-xs">
            <thead>
              <tr className="bg-brand-dark-surface/80 border-b border-brand-dark-border/80 text-[11px] font-bold uppercase tracking-wider text-slate-400">
                <th className="py-3.5 px-4">Event ID</th>
                <th className="py-3.5 px-4 min-w-[240px]">Tournament Title</th>
                <th className="py-3.5 px-4">Category</th>
                <th className="py-3.5 px-4">Status</th>
                <th className="py-3.5 px-4">Time Window</th>
                <th className="py-3.5 px-4">Prize Pool / Fee</th>
                <th className="py-3.5 px-4">Participants</th>
                <th className="py-3.5 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-brand-dark-border/40">
              {paginatedTournaments.length === 0 ? (
                <tr>
                  <td colSpan={8} className="py-12 text-center text-slate-400">
                    <div className="flex flex-col items-center justify-center space-y-2">
                      <IconTrophy className="w-8 h-8 text-slate-500" />
                      <p className="text-sm font-semibold text-white">No tournaments found</p>
                      <p className="text-xs text-slate-500">
                        Adjust filters or schedule a new competition.
                      </p>
                    </div>
                  </td>
                </tr>
              ) : (
                paginatedTournaments.map((t) => (
                  <tr key={t.id} className="hover:bg-white/5 transition-colors group">
                    <td className="py-3.5 px-4 font-mono font-medium text-slate-400 text-[11px]">
                      {t.id}
                    </td>

                    <td className="py-3.5 px-4">
                      <p className="font-bold text-white group-hover:text-brand-primary transition-colors">
                        {t.title}
                      </p>
                      <p className="text-[11px] text-slate-400 line-clamp-1 mt-0.5">
                        {t.description}
                      </p>
                      {t.cancellationReason && (
                        <p className="text-[10px] text-rose-400 mt-1 flex items-center space-x-1 font-medium">
                          <IconAlertCircle className="w-3 h-3 flex-shrink-0" />
                          <span>{t.cancellationReason}</span>
                        </p>
                      )}
                    </td>

                    <td className="py-3.5 px-4 whitespace-nowrap text-slate-300 font-medium">
                      {t.categoryName}
                    </td>

                    <td className="py-3.5 px-4 whitespace-nowrap">
                      {renderStatusBadge(t.status)}
                    </td>

                    <td className="py-3.5 px-4 whitespace-nowrap text-[11px] text-slate-400">
                      <div>
                        <span className="text-slate-300 font-medium">{formatDateTime(t.startTimeIso)}</span>
                        <span className="text-slate-600 mx-1">&rarr;</span>
                        <span className="text-slate-300 font-medium">{formatDateTime(t.endTimeIso)}</span>
                      </div>
                    </td>

                    <td className="py-3.5 px-4 whitespace-nowrap">
                      <div className="flex items-center space-x-1 text-brand-gold font-bold">
                        <IconSpark className="w-3.5 h-3.5" />
                        <span>{t.prizePoolPoints.toLocaleString()} pts</span>
                      </div>
                      <p className="text-[10px] text-slate-500">
                        Fee: {t.entryFeePoints} pts • Min Lvl {t.minLevelRequired}
                      </p>
                    </td>

                    <td className="py-3.5 px-4 whitespace-nowrap">
                      <span className="font-semibold text-white">
                        {t.registeredParticipants}
                      </span>
                      {t.maxParticipants && (
                        <span className="text-slate-500 text-[11px]"> / {t.maxParticipants}</span>
                      )}
                      <div className="w-20 bg-brand-dark-surface rounded-full h-1.5 mt-1 overflow-hidden border border-brand-dark-border/50">
                        <div
                          className="bg-brand-primary h-full rounded-full"
                          style={{
                            width: `${Math.min(
                              100,
                              (t.registeredParticipants / (t.maxParticipants || 100)) * 100
                            )}%`,
                          }}
                        />
                      </div>
                    </td>

                    <td className="py-3.5 px-4 whitespace-nowrap text-right">
                      <div className="inline-flex items-center space-x-1">
                        <button
                          onClick={() => onEditTournament(t)}
                          className="p-1.5 rounded-lg text-slate-400 hover:text-brand-primary hover:bg-brand-primary/10 transition"
                          title="Edit Tournament"
                        >
                          <IconEdit className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => onCloneTournament(t)}
                          className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-white/10 transition text-[11px] font-mono"
                          title="Clone Tournament"
                        >
                          Clone
                        </button>
                        {t.status !== 'CANCELLED' && t.status !== 'ENDED' && (
                          <button
                            onClick={() => onCancelTournament(t)}
                            className="p-1.5 rounded-lg text-rose-400 hover:text-rose-300 hover:bg-rose-950/30 transition text-[11px] font-semibold"
                            title="Cancel & Refund Participants"
                          >
                            Cancel
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Table Footer */}
        <div className="p-4 bg-brand-dark-surface/60 border-t border-brand-dark-border/60 flex flex-col sm:flex-row items-center justify-between gap-3 text-xs text-slate-400">
          <div>
            Showing{' '}
            <span className="font-semibold text-white">
              {filteredTournaments.length === 0 ? 0 : (currentPage - 1) * itemsPerPage + 1}
            </span>{' '}
            to{' '}
            <span className="font-semibold text-white">
              {Math.min(currentPage * itemsPerPage, filteredTournaments.length)}
            </span>{' '}
            of <span className="font-semibold text-white">{filteredTournaments.length}</span> tournaments
          </div>

          <div className="flex items-center space-x-2">
            <Button
              variant="outline"
              size="sm"
              disabled={currentPage <= 1}
              onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
            >
              Previous
            </Button>
            <span className="px-2 text-xs font-semibold text-white">
              Page {currentPage} of {totalPages}
            </span>
            <Button
              variant="outline"
              size="sm"
              disabled={currentPage >= totalPages}
              onClick={() => setCurrentPage((p) => Math.min(totalPages, p + 1))}
            >
              Next
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}
