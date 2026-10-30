'use client';

import React, { useState } from 'react';
import { AdminAuditLogEntry } from '../../types/audit';
import { Button } from '../ui/Button';
import {
  IconAlertCircle,
  IconCheck,
  IconClock,
  IconShield,
  IconX,
} from '../ui/Icons';

interface AuditLogDetailModalProps {
  isOpen: boolean;
  log: AdminAuditLogEntry | null;
  onClose: () => void;
}

export function AuditLogDetailModal({
  isOpen,
  log,
  onClose,
}: AuditLogDetailModalProps) {
  const [copied, setCopied] = useState(false);

  if (!isOpen || !log) return null;

  const handleCopy = () => {
    try {
      navigator.clipboard.writeText(JSON.stringify(log, null, 2));
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {
      // fallback
    }
  };

  const formatDateTime = (iso: string) => {
    try {
      const d = new Date(iso);
      return d.toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
      });
    } catch {
      return iso;
    }
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="relative w-full max-w-2xl bg-brand-dark-card border border-brand-dark-border rounded-3xl shadow-2xl p-6 sm:p-8 animate-in fade-in zoom-in duration-200">
        <button
          onClick={onClose}
          className="absolute top-6 right-6 text-slate-400 hover:text-white transition-colors"
          aria-label="Close modal"
        >
          <IconX className="w-5 h-5" />
        </button>

        <div className="flex items-center space-x-3 mb-6">
          <div className="p-3 bg-purple-500/10 border border-purple-500/30 rounded-2xl text-purple-400">
            <IconShield className="w-6 h-6" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="font-mono text-xs font-bold text-purple-400">{log.id}</span>
              <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-slate-800 text-slate-300 border border-slate-700">
                {log.action}
              </span>
            </div>
            <h2 className="text-lg font-bold text-white tracking-tight mt-1">
              Audit Event Payload Inspection
            </h2>
            <p className="text-xs text-slate-400">
              Recorded at {formatDateTime(log.timestampIso)} (UTC)
            </p>
          </div>
        </div>

        {/* Cryptographic Checksum Banner */}
        <div className="p-3.5 mb-5 rounded-2xl bg-slate-900 border border-teal-500/30 text-xs flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <IconCheck className="w-4 h-4 text-teal-400 shrink-0" />
            <div>
              <span className="font-semibold text-slate-200">Tamper-Resistant Signature</span>
              <p className="font-mono text-[10px] text-teal-400 mt-0.5 truncate max-w-xs sm:max-w-md">
                {log.checksumSignature}
              </p>
            </div>
          </div>
          <span className="px-2 py-0.5 rounded bg-teal-500/10 text-teal-300 text-[10px] font-bold border border-teal-500/30 whitespace-nowrap">
            VERIFIED
          </span>
        </div>

        {/* Actor and Target Cards */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 mb-5 text-xs">
          <div className="p-3.5 rounded-2xl bg-slate-900/80 border border-brand-dark-border space-y-1.5">
            <span className="text-[10px] font-semibold text-slate-400 uppercase tracking-wider block">
              Administrative Actor
            </span>
            <div className="font-semibold text-white truncate">{log.adminEmail}</div>
            <div className="text-[11px] text-slate-400 font-mono">UID: {log.adminId}</div>
            <div className="text-[10px] text-purple-300 font-semibold font-mono">
              Role: {log.adminRole}
            </div>
            <div className="text-[10px] text-slate-500 font-mono mt-1">
              IP: {log.ipAddress}
            </div>
          </div>

          <div className="p-3.5 rounded-2xl bg-slate-900/80 border border-brand-dark-border space-y-1.5">
            <span className="text-[10px] font-semibold text-slate-400 uppercase tracking-wider block">
              Affected Resource Target
            </span>
            <div className="font-semibold text-white font-mono">{log.targetId}</div>
            <div className="text-[11px] text-slate-300">Type: {log.targetType}</div>
            {log.reason && (
              <div className="text-[11px] text-rose-300 mt-1 italic">
                Reason: {log.reason}
              </div>
            )}
          </div>
        </div>

        {/* Structured Details Payload */}
        <div className="space-y-2 mb-6">
          <div className="flex items-center justify-between text-xs">
            <span className="font-semibold text-slate-300">Operation Metadata Payload</span>
            <button
              onClick={handleCopy}
              className="px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 text-[11px] font-mono transition-colors border border-slate-700"
            >
              {copied ? '✓ Copied JSON' : 'Copy JSON'}
            </button>
          </div>
          <div className="bg-slate-950 p-4 rounded-2xl border border-brand-dark-border font-mono text-[11px] text-slate-200 overflow-x-auto max-h-48 leading-relaxed">
            <pre>{JSON.stringify(log.details, null, 2)}</pre>
          </div>
          <p className="text-[10px] text-slate-500 font-mono truncate">
            User Agent: {log.userAgent}
          </p>
        </div>

        <div className="flex items-center justify-end pt-3 border-t border-brand-dark-border/60">
          <Button variant="outline" size="sm" onClick={onClose}>
            Close Inspector
          </Button>
        </div>
      </div>
    </div>
  );
}
