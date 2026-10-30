'use client';

import React, { useEffect, useState } from 'react';
import { AdminTicket, AdminTicketStatus } from '../../types/support';
import { Button } from '../ui/Button';
import {
  IconAlertCircle,
  IconCheck,
  IconClock,
  IconHelp,
  IconMail,
  IconX,
} from '../ui/Icons';

interface TicketDetailModalProps {
  isOpen: boolean;
  ticket: AdminTicket | null;
  onClose: () => void;
  onSendReply: (
    ticketId: string,
    replyMessage: string,
    newStatus: AdminTicketStatus
  ) => void;
}

export function TicketDetailModal({
  isOpen,
  ticket,
  onClose,
  onSendReply,
}: TicketDetailModalProps) {
  const [replyText, setReplyText] = useState('');
  const [status, setStatus] = useState<AdminTicketStatus>('RESOLVED');
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (ticket) {
      setReplyText('');
      setStatus(ticket.status === 'OPEN' ? 'RESOLVED' : ticket.status);
      setError(null);
    }
  }, [ticket, isOpen]);

  if (!isOpen || !ticket) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const trimmed = replyText.trim();
    if (!trimmed) {
      setError('Please enter a response message to send to the player.');
      return;
    }

    onSendReply(ticket.id, trimmed, status);
    onClose();
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
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="relative w-full max-w-2xl bg-brand-dark-card border border-brand-dark-border rounded-3xl shadow-2xl flex flex-col max-h-[90vh] overflow-hidden animate-in fade-in zoom-in duration-200">
        {/* Modal Header */}
        <div className="p-6 border-b border-brand-dark-border/80 flex items-center justify-between bg-slate-900/60 shrink-0">
          <div className="flex items-center space-x-3.5">
            <div className="p-3 bg-brand-primary/10 border border-brand-primary/30 rounded-2xl text-brand-primary">
              <IconHelp className="w-6 h-6" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="font-mono text-xs font-bold text-brand-primary">{ticket.id}</span>
                <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-slate-800 text-slate-300 border border-slate-700">
                  {ticket.category}
                </span>
                <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-slate-800 text-slate-300 border border-slate-700">
                  {ticket.priority} PRIORITY
                </span>
              </div>
              <h2 className="text-base font-bold text-white tracking-tight mt-0.5 truncate max-w-md">
                {ticket.subject}
              </h2>
              <p className="text-xs text-slate-400">
                From: <strong className="text-white">{ticket.userName}</strong> ({ticket.userEmail})
              </p>
            </div>
          </div>

          <button
            onClick={onClose}
            className="text-slate-400 hover:text-white transition-colors"
            aria-label="Close modal"
          >
            <IconX className="w-5 h-5" />
          </button>
        </div>

        {/* Message Thread Scroll Area */}
        <div className="flex-1 overflow-y-auto p-6 space-y-4 bg-slate-950/40">
          {ticket.messages.map((msg) => {
            const isUser = msg.sender === 'USER';
            return (
              <div
                key={msg.id}
                className={`flex flex-col ${isUser ? 'items-start' : 'items-end'}`}
              >
                <div className="flex items-center gap-2 mb-1 px-1 text-[11px] text-slate-400">
                  <span className="font-semibold text-slate-300">{msg.senderName}</span>
                  <span>•</span>
                  <span>{formatDateTime(msg.timestampIso)}</span>
                </div>
                <div
                  className={`p-3.5 rounded-2xl text-xs max-w-[85%] leading-relaxed ${
                    isUser
                      ? 'bg-slate-900 border border-brand-dark-border text-slate-200 rounded-tl-sm'
                      : 'bg-brand-primary text-white shadow-lg shadow-brand-primary/20 rounded-tr-sm'
                  }`}
                >
                  {msg.message}
                </div>
              </div>
            );
          })}
        </div>

        {/* Reply Composer Form */}
        <form onSubmit={handleSubmit} className="p-6 border-t border-brand-dark-border/80 bg-slate-900/60 shrink-0 space-y-3">
          {error && (
            <div className="p-2.5 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-300 text-xs flex items-center gap-2">
              <IconAlertCircle className="w-4 h-4 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <div>
            <textarea
              rows={3}
              value={replyText}
              onChange={(e) => {
                setReplyText(e.target.value);
                if (error) setError(null);
              }}
              placeholder="Type your official administrative reply to the player..."
              className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-brand-dark-border text-xs text-white placeholder-slate-500 focus:outline-none focus:border-brand-primary transition-colors resize-none"
            />
          </div>

          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <div className="flex items-center space-x-2 text-xs">
              <span className="text-slate-400">Set Status On Reply:</span>
              <select
                value={status}
                onChange={(e) => setStatus(e.target.value as AdminTicketStatus)}
                className="px-2.5 py-1.5 rounded-xl bg-slate-900 border border-brand-dark-border text-xs text-white focus:outline-none focus:border-brand-primary"
              >
                <option value="IN_PROGRESS">Keep In Progress</option>
                <option value="RESOLVED">Mark as Resolved</option>
                <option value="CLOSED">Close Ticket</option>
                <option value="OPEN">Re-open Ticket</option>
              </select>
            </div>

            <div className="flex items-center space-x-2">
              <Button type="button" variant="outline" size="sm" onClick={onClose}>
                Cancel
              </Button>
              <Button type="submit" variant="primary" size="sm" className="flex items-center gap-1.5">
                <IconMail className="w-3.5 h-3.5" />
                Send Reply
              </Button>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
}
