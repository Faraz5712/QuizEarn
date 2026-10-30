'use client';

import React, { useMemo, useState } from 'react';
import { AdminAuditAction, AdminAuditLogEntry, AdminAuditTargetType } from '../../types/audit';
import { RoleBadge } from '../ui/Badge';
import { Button } from '../ui/Button';
import {
  IconAlertCircle,
  IconBarChart,
  IconCheck,
  IconClock,
  IconDownload,
  IconSearch,
  IconShield,
  IconX,
} from '../ui/Icons';

interface AuditLogTableProps {
  logs: AdminAuditLogEntry[];
  onInspectLog: (log: AdminAuditLogEntry) => void;
  onExportCSV: () => void;
  onExportJSON: () => void;
}

export function AuditLogTable({
  logs,
  onInspectLog,
  onExportCSV,
  onExportJSON,
}: AuditLogTableProps) {
  const [searchTerm, setSearchTerm] = useState('');
  const [targetTypeFilter, setTargetTypeFilter] = useState<string>('ALL');
  const [roleFilter, setRoleFilter] = useState<string>('ALL');

  const filteredLogs = useMemo(() => {
    return logs.filter((log) => {
      const matchesSearch =
        searchTerm === '' ||
        log.id.toLowerCase().includes(searchTerm.toLowerCase()) ||
        log.adminEmail.toLowerCase().includes(searchTerm.toLowerCase()) ||
        log.targetId.toLowerCase().includes(searchTerm.toLowerCase()) ||
        log.summary.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (log.reason && log.reason.toLowerCase().includes(searchTerm.toLowerCase())) ||
        log.action.toLowerCase().includes(searchTerm.toLowerCase());

      const matchesTarget =
        targetTypeFilter === 'ALL' || log.targetType === targetTypeFilter;

      const matchesRole =
        roleFilter === 'ALL' || log.adminRole === roleFilter;

      return matchesSearch && matchesTarget && matchesRole;
    });
  }, [logs, searchTerm, targetTypeFilter, roleFilter]);

  const formatActionBadge = (action: AdminAuditAction) => {
    if (action.includes('REJECT') || action.includes('SUSPEND') || action.includes('CANCEL')) {
      return (
        <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-rose-500/10 text-rose-300 border border-rose-500/30">
          {action}
        </span>
      );
    }
    if (action.includes('APPROVE') || action.includes('REACTIVATE') || action.includes('PASS')) {
      return (
        <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-teal-500/10 text-teal-300 border border-teal-500/30">
          {action}
        </span>
      );
    }
    if (action.includes('REWARD') || action.includes('IMPORT') || action.includes('CREATE')) {
      return (
        <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-indigo-500/10 text-indigo-300 border border-indigo-500/30">
          {action}
        </span>
      );
    }
    return (
      <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-purple-500/10 text-purple-300 border border-purple-500/30">
        {action}
      </span>
    );
  };

  const formatDateTime = (iso: string) => {
    try {
      const d = new Date(iso);
      return d.toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
      });
    } catch {
      return iso;
    }
  };

  return (
    <div className="space-y-4">
      {/* Controls Bar */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-3 bg-brand-dark-card p-4 rounded-2xl border border-brand-dark-border">
        {/* Search */}
        <div className="relative flex-1 max-w-md">
          <IconSearch className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Search audit trail by operator, target ID, action..."
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

        {/* Filters and Export CTAs */}
        <div className="flex flex-wrap items-center gap-2 text-xs">
          {/* Target Type Filter */}
          <select
            value={targetTypeFilter}
            onChange={(e) => setTargetTypeFilter(e.target.value)}
            className="px-3 py-1.5 rounded-xl bg-slate-900 border border-brand-dark-border text-xs text-white focus:outline-none focus:border-brand-primary"
          >
            <option value="ALL">All Resource Types</option>
            <option value="USER">User / Player</option>
            <option value="REDEMPTION">Redemption</option>
            <option value="TOURNAMENT">Tournament</option>
            <option value="QUESTION">Question Bank</option>
            <option value="REWARD">Reward Catalog</option>
            <option value="SUPPORT_TICKET">Support Ticket</option>
            <option value="SYSTEM">System Config</option>
          </select>

          {/* Role Filter */}
          <select
            value={roleFilter}
            onChange={(e) => setRoleFilter(e.target.value)}
            className="px-3 py-1.5 rounded-xl bg-slate-900 border border-brand-dark-border text-xs text-white focus:outline-none focus:border-brand-primary"
          >
            <option value="ALL">All Roles</option>
            <option value="SUPER_ADMIN">Super Administrator</option>
            <option value="CONTENT_MANAGER">Content Manager</option>
            <option value="SUPPORT_AGENT">Support Agent</option>
          </select>

          {/* Export Buttons */}
          <div className="flex items-center space-x-1.5 border-l border-brand-dark-border/80 pl-2">
            <button
              onClick={onExportCSV}
              className="px-2.5 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 font-semibold text-xs transition-colors flex items-center gap-1"
              title="Download CSV report"
            >
              <IconDownload className="w-3.5 h-3.5 text-slate-400" />
              CSV
            </button>
            <button
              onClick={onExportJSON}
              className="px-2.5 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 font-semibold text-xs transition-colors flex items-center gap-1"
              title="Download JSON export"
            >
              <IconDownload className="w-3.5 h-3.5 text-slate-400" />
              JSON
            </button>
          </div>
        </div>
      </div>

      {/* Audit Log Table */}
      <div className="bg-brand-dark-card border border-brand-dark-border rounded-2xl overflow-hidden shadow-xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-brand-dark-border/80 bg-slate-900/60 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
                <th className="py-3.5 px-4">Date & Log ID</th>
                <th className="py-3.5 px-4">Action</th>
                <th className="py-3.5 px-4">Operator</th>
                <th className="py-3.5 px-4">Target Resource</th>
                <th className="py-3.5 px-4">Summary Description</th>
                <th className="py-3.5 px-4">Provenance</th>
                <th className="py-3.5 px-4 text-right">Details</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-brand-dark-border/40 text-xs">
              {filteredLogs.length === 0 ? (
                <tr>
                  <td colSpan={7} className="py-12 text-center text-slate-400">
                    <div className="flex flex-col items-center justify-center space-y-2">
                      <IconShield className="w-8 h-8 text-slate-600" />
                      <p className="font-semibold text-slate-300">No audit logs match your search</p>
                      <p className="text-[11px] text-slate-500">
                        Adjust search filters or clear the target type query.
                      </p>
                    </div>
                  </td>
                </tr>
              ) : (
                filteredLogs.map((log) => (
                  <tr key={log.id} className="hover:bg-slate-800/40 transition-colors">
                    {/* Timestamp & ID */}
                    <td className="py-4 px-4 whitespace-nowrap">
                      <div className="font-mono text-white font-bold text-xs">{log.id}</div>
                      <div className="text-[10px] text-slate-400 mt-0.5">
                        {formatDateTime(log.timestampIso)}
                      </div>
                    </td>

                    {/* Action */}
                    <td className="py-4 px-4 whitespace-nowrap">
                      {formatActionBadge(log.action)}
                    </td>

                    {/* Operator */}
                    <td className="py-4 px-4 whitespace-nowrap">
                      <div className="font-semibold text-white">{log.adminEmail}</div>
                      <div className="mt-0.5">
                        <RoleBadge role={log.adminRole} />
                      </div>
                    </td>

                    {/* Target */}
                    <td className="py-4 px-4 whitespace-nowrap">
                      <div className="font-mono font-semibold text-slate-200">{log.targetId}</div>
                      <span className="inline-block text-[9px] px-1.5 py-0.2 rounded bg-slate-800 text-slate-400 border border-slate-700 font-mono mt-0.5">
                        {log.targetType}
                      </span>
                    </td>

                    {/* Summary & Reason */}
                    <td className="py-4 px-4 max-w-xs">
                      <div className="text-slate-200 truncate">{log.summary}</div>
                      {log.reason && (
                        <div className="text-[10px] text-rose-300/90 truncate mt-0.5 italic">
                          Reason: {log.reason}
                        </div>
                      )}
                    </td>

                    {/* Provenance */}
                    <td className="py-4 px-4 whitespace-nowrap text-slate-400 font-mono text-[11px]">
                      {log.ipAddress}
                    </td>

                    {/* Actions */}
                    <td className="py-4 px-4 whitespace-nowrap text-right">
                      <button
                        onClick={() => onInspectLog(log)}
                        className="px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 font-medium text-xs border border-slate-700 transition-colors"
                      >
                        Inspect
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
            Showing <strong className="text-white">{filteredLogs.length}</strong> of{' '}
            <strong className="text-white">{logs.length}</strong> immutable audit entries
          </span>
          <span className="text-slate-500">
            Cryptographic SHA-256 signatures enforce non-repudiation
          </span>
        </div>
      </div>
    </div>
  );
}
