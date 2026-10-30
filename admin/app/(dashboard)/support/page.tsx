'use client';

import React, { useEffect, useState } from 'react';
import { RouteGuard } from '../../../components/layout/RouteGuard';
import { Button } from '../../../components/ui/Button';
import {
  IconAlertCircle,
  IconCheck,
  IconClock,
  IconHelp,
  IconMail,
} from '../../../components/ui/Icons';
import { TicketDetailModal } from '../../../components/support/TicketDetailModal';
import { TicketTable } from '../../../components/support/TicketTable';
import {
  loadStoredTickets,
  saveStoredTickets,
} from '../../../lib/support-store';
import { AdminTicket, AdminTicketStatus } from '../../../types/support';
import { isFirebaseConfigured } from '../../../lib/firebase';

export default function SupportDeskPage() {
  const [tickets, setTickets] = useState<AdminTicket[]>([]);
  const [isLoaded, setIsLoaded] = useState(false);

  // Modal state
  const [selectedTicket, setSelectedTicket] = useState<AdminTicket | null>(null);
  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);

  // Toast
  const [toast, setToast] = useState<{
    message: string;
    type: 'success' | 'danger' | 'info';
  } | null>(null);

  useEffect(() => {
    setTickets(isFirebaseConfigured ? [] : loadStoredTickets());
    setIsLoaded(true);
  }, []);

  const showToast = (message: string, type: 'success' | 'danger' | 'info' = 'success') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 4000);
  };

  const handleSendReply = (
    ticketId: string,
    replyMessage: string,
    newStatus: AdminTicketStatus
  ) => {
    if (isFirebaseConfigured) {
      showToast('Support tickets are not connected to a server-side ticket service.', 'danger');
      return;
    }
    const updated = tickets.map((t) => {
      if (t.id === ticketId) {
        const newMsg = {
          id: `MSG-${Date.now().toString().slice(-4)}`,
          sender: 'SUPPORT' as const,
          senderName: 'Support Agent (Miller)',
          message: replyMessage,
          timestampIso: new Date().toISOString(),
        };
        return {
          ...t,
          status: newStatus,
          updatedAtIso: new Date().toISOString(),
          messages: [...t.messages, newMsg],
        };
      }
      return t;
    });

    setTickets(updated);
    if (!isFirebaseConfigured) saveStoredTickets(updated);

    const target = updated.find((t) => t.id === ticketId);
    showToast(
      `Reply dispatched to ${target?.userName} for ticket ${ticketId}. Status: ${newStatus}.`
    );
  };

  // Metrics
  const openCount = tickets.filter((t) => t.status === 'OPEN').length;
  const inProgressCount = tickets.filter((t) => t.status === 'IN_PROGRESS').length;
  const resolvedCount = tickets.filter((t) => t.status === 'RESOLVED').length;

  if (!isLoaded) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="w-8 h-8 border-4 border-brand-primary border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <RouteGuard requiredPermission="VIEW_SUPPORT_TICKETS">
      <div className="space-y-6 max-w-7xl mx-auto pb-12">
        {isFirebaseConfigured && (
          <div className="rounded-xl border border-amber-500/40 bg-amber-950/30 p-4 text-xs text-amber-200">
            Firebase support-ticket storage and server-authorized replies are not implemented. Demo tickets are hidden.
          </div>
        )}
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
              <IconHelp className="w-7 h-7 text-brand-mint" />
              Customer Support Desk
            </h1>
            <p className="text-xs text-slate-400 mt-1">
              Inspect player inquiries across gameplay, wallet redemptions, and accounts. Post agent replies and track resolution status.
            </p>
          </div>
        </div>

        {/* Metrics Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="bg-brand-dark-card border border-brand-dark-border p-4 rounded-2xl flex items-center space-x-3.5">
            <div className="p-3 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400">
              <IconAlertCircle className="w-5 h-5" />
            </div>
            <div>
              <div className="text-[11px] font-medium text-slate-400 uppercase tracking-wider">
                Open Inquiries
              </div>
              <div className="text-xl font-bold text-rose-400 mt-0.5">
                {openCount} <span className="text-xs font-normal">awaiting reply</span>
              </div>
            </div>
          </div>

          <div className="bg-brand-dark-card border border-brand-dark-border p-4 rounded-2xl flex items-center space-x-3.5">
            <div className="p-3 rounded-xl bg-amber-500/10 border border-amber-500/20 text-amber-400">
              <IconClock className="w-5 h-5" />
            </div>
            <div>
              <div className="text-[11px] font-medium text-slate-400 uppercase tracking-wider">
                In Progress
              </div>
              <div className="text-xl font-bold text-amber-400 mt-0.5">
                {inProgressCount} <span className="text-xs font-normal">being handled</span>
              </div>
            </div>
          </div>

          <div className="bg-brand-dark-card border border-brand-dark-border p-4 rounded-2xl flex items-center space-x-3.5">
            <div className="p-3 rounded-xl bg-teal-500/10 border border-teal-500/20 text-teal-400">
              <IconCheck className="w-5 h-5" />
            </div>
            <div>
              <div className="text-[11px] font-medium text-slate-400 uppercase tracking-wider">
                Resolved Tickets
              </div>
              <div className="text-xl font-bold text-teal-400 mt-0.5">
                {resolvedCount} <span className="text-xs font-normal">closed/resolved</span>
              </div>
            </div>
          </div>

          <div className="bg-brand-dark-card border border-brand-dark-border p-4 rounded-2xl flex items-center space-x-3.5">
            <div className="p-3 rounded-xl bg-purple-500/10 border border-purple-500/20 text-purple-400">
              <IconMail className="w-5 h-5" />
            </div>
            <div>
              <div className="text-[11px] font-medium text-slate-400 uppercase tracking-wider">
                Total Inquiries
              </div>
              <div className="text-xl font-bold text-white mt-0.5">
                {tickets.length} <span className="text-xs text-slate-400 font-normal">all time</span>
              </div>
            </div>
          </div>
        </div>

        {/* Tickets Table */}
        <TicketTable
          tickets={tickets}
          onOpenTicket={(t) => {
            setSelectedTicket(t);
            setIsDetailModalOpen(true);
          }}
        />

        {/* Ticket Detail & Thread Modal */}
        <TicketDetailModal
          isOpen={isDetailModalOpen}
          ticket={selectedTicket}
          onClose={() => {
            setIsDetailModalOpen(false);
            setSelectedTicket(null);
          }}
          onSendReply={handleSendReply}
        />
      </div>
    </RouteGuard>
  );
}
