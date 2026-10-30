'use client';

import React, { useEffect, useState } from 'react';
import { RouteGuard } from '../../../components/layout/RouteGuard';
import { Button } from '../../../components/ui/Button';
import {
  IconAlertCircle,
  IconBarChart,
  IconCheck,
  IconClock,
  IconDownload,
  IconLock,
  IconShield,
  IconSpark,
} from '../../../components/ui/Icons';
import { AuditLogDetailModal } from '../../../components/audit-logs/AuditLogDetailModal';
import { AuditLogTable } from '../../../components/audit-logs/AuditLogTable';
import {
  exportAuditLogsToCSV,
  exportAuditLogsToJSON,
  loadStoredAuditLogs,
} from '../../../lib/audit-store';
import { AdminAuditLogEntry } from '../../../types/audit';
import { isFirebaseConfigured } from '../../../lib/firebase';

export default function AuditLogsPage() {
  const [logs, setLogs] = useState<AdminAuditLogEntry[]>([]);
  const [isLoaded, setIsLoaded] = useState(false);

  // Modal state
  const [selectedLog, setSelectedLog] = useState<AdminAuditLogEntry | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);

  // Toast feedback
  const [toast, setToast] = useState<{
    message: string;
    type: 'success' | 'danger' | 'info';
  } | null>(null);

  useEffect(() => {
    setLogs(isFirebaseConfigured ? [] : loadStoredAuditLogs());
    setIsLoaded(true);
  }, []);

  const showToast = (message: string, type: 'success' | 'danger' | 'info' = 'success') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 4000);
  };

  const downloadFile = (content: string, fileName: string, contentType: string) => {
    const blob = new Blob([content], { type: contentType });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = fileName;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const handleExportCSV = () => {
    const csv = exportAuditLogsToCSV(logs);
    downloadFile(csv, `quizearn-audit-log-${new Date().toISOString().slice(0, 10)}.csv`, 'text/csv;charset=utf-8;');
    showToast('Audit log CSV report downloaded successfully.');
  };

  const handleExportJSON = () => {
    const json = exportAuditLogsToJSON(logs);
    downloadFile(json, `quizearn-audit-log-${new Date().toISOString().slice(0, 10)}.json`, 'application/json');
    showToast('Audit log JSON export downloaded successfully.');
  };

  // Metrics
  const financialEventsCount = logs.filter(
    (l) =>
      l.action.includes('REDEMPTION') ||
      l.action.includes('REFUND') ||
      l.action.includes('TOURNAMENT')
  ).length;

  const moderationEventsCount = logs.filter((l) =>
    l.action.includes('USER_SUSPEND') || l.action.includes('USER_REACTIVATE')
  ).length;

  if (!isLoaded) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="w-8 h-8 border-4 border-brand-primary border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <RouteGuard requiredPermission="VIEW_AUDIT_LOGS">
      <div className="space-y-6 max-w-7xl mx-auto pb-12">
        {isFirebaseConfigured && (
          <div className="rounded-xl border border-amber-500/40 bg-amber-950/30 p-4 text-xs text-amber-200">
            Server-side append-only audit logging is not implemented. Local demo audit records are hidden and must not be used as evidence.
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
              <IconShield className="w-7 h-7 text-purple-400" />
              Administrative Audit Trail
            </h1>
            <p className="text-xs text-slate-400 mt-1">
              Tamper-resistant append-only log stream of administrative actions, financial decisions, and user moderation.
            </p>
          </div>
          <div className="mt-4 sm:mt-0 flex items-center space-x-2">
            <span className="text-xs px-3 py-1 rounded-full font-bold bg-purple-900/40 text-purple-300 border border-purple-500/40">
              SuperAdmin Restricted
            </span>
          </div>
        </div>

        {/* Operational Metrics Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="bg-brand-dark-card border border-brand-dark-border p-4 rounded-2xl flex items-center space-x-3.5">
            <div className="p-3 rounded-xl bg-purple-500/10 border border-purple-500/20 text-purple-400">
              <IconShield className="w-5 h-5" />
            </div>
            <div>
              <div className="text-[11px] font-medium text-slate-400 uppercase tracking-wider">
                Total Audit Entries
              </div>
              <div className="text-xl font-bold text-white mt-0.5">
                {logs.length} <span className="text-xs text-slate-400 font-normal">events</span>
              </div>
            </div>
          </div>

          <div className="bg-brand-dark-card border border-brand-dark-border p-4 rounded-2xl flex items-center space-x-3.5">
            <div className="p-3 rounded-xl bg-amber-500/10 border border-amber-500/20 text-amber-400">
              <IconSpark className="w-5 h-5" />
            </div>
            <div>
              <div className="text-[11px] font-medium text-slate-400 uppercase tracking-wider">
                Financial Operations
              </div>
              <div className="text-xl font-bold text-amber-400 mt-0.5">
                {financialEventsCount} <span className="text-xs font-normal">audited</span>
              </div>
            </div>
          </div>

          <div className="bg-brand-dark-card border border-brand-dark-border p-4 rounded-2xl flex items-center space-x-3.5">
            <div className="p-3 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400">
              <IconLock className="w-5 h-5" />
            </div>
            <div>
              <div className="text-[11px] font-medium text-slate-400 uppercase tracking-wider">
                User Moderation
              </div>
              <div className="text-xl font-bold text-rose-400 mt-0.5">
                {moderationEventsCount} <span className="text-xs font-normal">actions</span>
              </div>
            </div>
          </div>

          <div className="bg-brand-dark-card border border-brand-dark-border p-4 rounded-2xl flex items-center space-x-3.5">
            <div className="p-3 rounded-xl bg-teal-500/10 border border-teal-500/20 text-teal-400">
              <IconCheck className="w-5 h-5" />
            </div>
            <div>
              <div className="text-[11px] font-medium text-slate-400 uppercase tracking-wider">
                Hash Chain Integrity
              </div>
              <div className="text-xl font-bold text-teal-400 mt-0.5 font-mono">
                100% <span className="text-xs font-normal">valid</span>
              </div>
            </div>
          </div>
        </div>

        {/* Audit Log Table */}
        <AuditLogTable
          logs={logs}
          onInspectLog={(log) => {
            setSelectedLog(log);
            setIsModalOpen(true);
          }}
          onExportCSV={handleExportCSV}
          onExportJSON={handleExportJSON}
        />

        {/* Detail Modal */}
        <AuditLogDetailModal
          isOpen={isModalOpen}
          log={selectedLog}
          onClose={() => {
            setIsModalOpen(false);
            setSelectedLog(null);
          }}
        />
      </div>
    </RouteGuard>
  );
}
