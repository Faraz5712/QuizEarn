'use client';

import React, { useMemo, useState } from 'react';
import {
  AdminTicket,
  AdminTicketCategory,
  AdminTicketPriority,
  AdminTicketStatus,
} from '../../types/support';
import { Button } from '../ui/Button';
import {
  IconAlertCircle,
  IconCheck,
  IconClock,
  IconHelp,
  IconSearch,
  IconX,
} from '../ui/Icons';

interface TicketTableProps {
  tickets: AdminTicket[];
  onOpenTicket: (ticket: AdminTicket) => void;
}

export function TicketTable({ tickets, onOpenTicket }: TicketTableProps) {
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<'ALL' | AdminTicketStatus>('ALL');
  const [categoryFilter, setCategoryFilter] = useState<string>('ALL');
  const [priorityFilter, setPriorityFilter] = useState<string>('ALL');

  const filteredTickets = useMemo(() => {
    return tickets.filter((t) => {
      const matchesSearch =
        searchTerm === '' ||
        t.id.toLowerCase().includes(searchTerm.toLowerCase()) ||
        t.subject.toLowerCase().includes(searchTerm.toLowerCase()) ||
        t.userName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        t.userEmail.toLowerCase().includes(searchTerm.toLowerCase());

      const matchesStatus =
        statusFilter === 'ALL' || t.status === statusFilter;

      const matchesCategory =
        categoryFilter === 'ALL' || t.category === categoryFilter;

      const matchesPriority =
        priorityFilter === 'ALL' || t.priority === priorityFilter;

      return matchesSearch && matchesStatus && matchesCategory && matchesPriority;
    });
  }, [tickets, searchTerm, statusFilter, categoryFilter, priorityFilter]);

  const formatPriorityBadge = (priority: AdminTicketPriority) => {
    switch (priority) {
      case 'HIGH':
        return (
          <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-rose-500/20 text-rose-300 border border-rose-500/30">
            HIGH
          </span>
        );
      case 'MEDIUM':
        return (
          <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-500/20 text-amber-300 border border-amber-500/30">
            MEDIUM
          </span>
        );
      case 'LOW':
        return (
          <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-slate-700 text-slate-300 border border-slate-600">
            LOW
          </span>
        );
    }
  };

  const formatStatusBadge = (status: AdminTicketStatus) => {
    switch (status) {
      case 'OPEN':
        return (
          <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-semibold bg-rose-500/10 text-rose-300 border border-rose-500/30">
            <IconAlertCircle className="w-3 h-3 mr-1" />
            Open
          </span>
        );
      case 'IN_PROGRESS':
        return (
          <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-semibold bg-amber-500/10 text-amber-300 border border-amber-500/30">
            <IconClock className="w-3 h-3 mr-1" />
            In Progress
          </span>
        );
      case 'RESOLVED':
        return (
          <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-semibold bg-teal-500/10 text-teal-300 border border-teal-500/30">
            <IconCheck className="w-3 h-3 mr-1" />
            Resolved
          </span>
        );
      case 'CLOSED':
        return (
          <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-semibold bg-slate-800 text-slate-400 border border-slate-700">
            Closed
          </span>
        );
    }
  };

  const formatDateTime = (iso: string) => {
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
      {/* Controls Bar */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-3 bg-brand-dark-card p-4 rounded-2xl border border-brand-dark-border">
        <div className="relative flex-1 max-w-md">
          <IconSearch className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Search tickets by subject, user, or ID..."
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

        {/* Filters */}
        <div className="flex flex-wrap items-center gap-2 text-xs">
          {/* Status Tabs */}
          <div className="flex items-center bg-slate-900 p-1 rounded-xl border border-brand-dark-border">
            <button
              onClick={() => setStatusFilter('ALL')}
              className={`px-2.5 py-1 rounded-lg font-medium transition-colors ${
                statusFilter === 'ALL'
                  ? 'bg-brand-primary text-white'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              All
            </button>
            <button
              onClick={() => setStatusFilter('OPEN')}
              className={`px-2.5 py-1 rounded-lg font-medium transition-colors ${
                statusFilter === 'OPEN'
                  ? 'bg-rose-700 text-white'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              Open
            </button>
            <button
              onClick={() => setStatusFilter('IN_PROGRESS')}
              className={`px-2.5 py-1 rounded-lg font-medium transition-colors ${
                statusFilter === 'IN_PROGRESS'
                  ? 'bg-amber-600 text-white'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              In Progress
            </button>
            <button
              onClick={() => setStatusFilter('RESOLVED')}
              className={`px-2.5 py-1 rounded-lg font-medium transition-colors ${
                statusFilter === 'RESOLVED'
                  ? 'bg-teal-600 text-white'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              Resolved
            </button>
          </div>

          {/* Category Dropdown */}
          <select
            value={categoryFilter}
            onChange={(e) => setCategoryFilter(e.target.value)}
            className="px-3 py-1.5 rounded-xl bg-slate-900 border border-brand-dark-border text-xs text-white focus:outline-none focus:border-brand-primary"
          >
            <option value="ALL">All Categories</option>
            <option value="WALLET_POINTS">Wallet & Points</option>
            <option value="REDEMPTION">Redemptions</option>
            <option value="QUIZ_TECHNICAL">Quiz Technical</option>
            <option value="ACCOUNT">Account</option>
          </select>
        </div>
      </div>

      {/* Ticket Table */}
      <div className="bg-brand-dark-card border border-brand-dark-border rounded-2xl overflow-hidden shadow-xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-brand-dark-border/80 bg-slate-900/60 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
                <th className="py-3.5 px-4">Ticket ID & Date</th>
                <th className="py-3.5 px-4">Player</th>
                <th className="py-3.5 px-4">Subject</th>
                <th className="py-3.5 px-4">Category</th>
                <th className="py-3.5 px-4">Priority</th>
                <th className="py-3.5 px-4">Status</th>
                <th className="py-3.5 px-4 text-center">Messages</th>
                <th className="py-3.5 px-4 text-right">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-brand-dark-border/40 text-xs">
              {filteredTickets.length === 0 ? (
                <tr>
                  <td colSpan={8} className="py-12 text-center text-slate-400">
                    <div className="flex flex-col items-center justify-center space-y-2">
                      <IconHelp className="w-8 h-8 text-slate-600" />
                      <p className="font-semibold text-slate-300">No support tickets match your filter</p>
                      <p className="text-[11px] text-slate-500">
                        Adjust search filters or check back later.
                      </p>
                    </div>
                  </td>
                </tr>
              ) : (
                filteredTickets.map((ticket) => (
                  <tr key={ticket.id} className="hover:bg-slate-800/40 transition-colors">
                    {/* ID & Date */}
                    <td className="py-4 px-4 whitespace-nowrap">
                      <div className="font-mono font-bold text-white text-xs">{ticket.id}</div>
                      <div className="text-[10px] text-slate-500 mt-0.5">
                        {formatDateTime(ticket.createdAtIso)}
                      </div>
                    </td>

                    {/* Player */}
                    <td className="py-4 px-4 whitespace-nowrap">
                      <div className="font-semibold text-white">{ticket.userName}</div>
                      <div className="text-[11px] text-slate-400 truncate max-w-[140px]">
                        {ticket.userEmail}
                      </div>
                    </td>

                    {/* Subject */}
                    <td className="py-4 px-4 max-w-xs">
                      <div className="font-medium text-white truncate">{ticket.subject}</div>
                      <div className="text-[11px] text-slate-400 truncate mt-0.5">
                        {ticket.messages[ticket.messages.length - 1]?.message || ''}
                      </div>
                    </td>

                    {/* Category */}
                    <td className="py-4 px-4 whitespace-nowrap">
                      <span className="inline-flex items-center px-2 py-0.5 rounded bg-slate-800 text-slate-300 text-[10px] font-mono border border-slate-700">
                        {ticket.category}
                      </span>
                    </td>

                    {/* Priority */}
                    <td className="py-4 px-4 whitespace-nowrap">
                      {formatPriorityBadge(ticket.priority)}
                    </td>

                    {/* Status */}
                    <td className="py-4 px-4 whitespace-nowrap">
                      {formatStatusBadge(ticket.status)}
                    </td>

                    {/* Messages Count */}
                    <td className="py-4 px-4 whitespace-nowrap text-center font-mono font-semibold text-slate-300">
                      {ticket.messages.length}
                    </td>

                    {/* Action */}
                    <td className="py-4 px-4 whitespace-nowrap text-right">
                      <button
                        onClick={() => onOpenTicket(ticket)}
                        className="px-3 py-1 rounded-lg bg-brand-primary/20 hover:bg-brand-primary text-brand-primary hover:text-white font-semibold text-xs border border-brand-primary/30 transition-colors"
                      >
                        Reply & Thread
                      </button>
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
            Showing <strong className="text-white">{filteredTickets.length}</strong> of{' '}
            <strong className="text-white">{tickets.length}</strong> customer inquiries
          </span>
          <span className="text-slate-500">
            Real-time multi-agent customer support responses
          </span>
        </div>
      </div>
    </div>
  );
}
