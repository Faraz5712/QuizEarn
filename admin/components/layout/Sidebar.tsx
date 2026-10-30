'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useAdminAuth } from '../../lib/auth-context';
import { hasPermission, NAVIGATION_ITEMS } from '../../lib/rbac';
import { DynamicIcon, IconSpark, IconX } from '../ui/Icons';
import { RoleBadge } from '../ui/Badge';
import { AdminRole } from '../../types/admin';

interface SidebarProps {
  isOpen: boolean;
  onClose: () => void;
}

export function Sidebar({ isOpen, onClose }: SidebarProps) {
  const pathname = usePathname();
  const { user, isDemoMode, switchRoleDemo } = useAdminAuth();

  // Filter navigation items based on current operator's RBAC permissions
  const authorizedItems = NAVIGATION_ITEMS.filter((item) => {
    if (!item.requiredPermission) return true;
    if (!user) return false;
    return hasPermission(user.role, item.requiredPermission);
  });

  return (
    <>
      {/* Mobile Backdrop */}
      {isOpen && (
        <div
          className="fixed inset-0 z-40 bg-black/60 backdrop-blur-sm lg:hidden transition-opacity"
          onClick={onClose}
        />
      )}

      {/* Sidebar Container */}
      <aside
        className={`fixed top-0 bottom-0 left-0 z-50 w-72 bg-brand-dark-surface border-r border-brand-dark-border flex flex-col transition-transform duration-300 ease-in-out lg:translate-x-0 ${
          isOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        {/* Brand Header */}
        <div className="h-18 px-6 flex items-center justify-between border-b border-brand-dark-border/60">
          <Link href="/" className="flex items-center space-x-3 group">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-brand-primary to-brand-secondary flex items-center justify-center shadow-lg shadow-brand-primary/25 group-hover:scale-105 transition-transform">
              <IconSpark className="w-5 h-5 text-white" />
            </div>
            <div>
              <div className="flex items-center space-x-1.5">
                <span className="font-bold text-lg text-white tracking-tight">QuizEarn</span>
                <span className="text-[10px] font-bold uppercase tracking-wider px-1.5 py-0.5 rounded bg-brand-primary/20 text-brand-primary border border-brand-primary/30">
                  Admin
                </span>
              </div>
              <p className="text-xs text-slate-400">Back-Office Suite</p>
            </div>
          </Link>

          {/* Mobile close button */}
          <button
            onClick={onClose}
            className="lg:hidden p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-white/5"
            aria-label="Close sidebar"
          >
            <IconX className="w-5 h-5" />
          </button>
        </div>

        {/* Current Operator Role Banner */}
        {user && (
          <div className="px-5 py-3.5 mx-3 mt-3 rounded-xl bg-brand-dark-card/70 border border-brand-dark-border/80">
            <div className="flex items-center justify-between mb-1">
              <span className="text-[11px] font-medium text-slate-400 uppercase tracking-wider">
                Active Session
              </span>
              <RoleBadge role={user.role} />
            </div>
            <p className="text-xs font-semibold text-white truncate">{user.displayName}</p>
            <p className="text-[11px] text-slate-400 truncate">{user.email}</p>
          </div>
        )}

        {/* Navigation Section */}
        <div className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
          <div className="px-3 pb-2 text-[10px] font-bold uppercase tracking-wider text-slate-500">
            Operations & Management
          </div>

          {authorizedItems.map((item) => {
            const isActive =
              item.href === '/' ? pathname === '/' : pathname.startsWith(item.href);

            return (
              <Link
                key={item.id}
                href={item.href}
                onClick={() => onClose()}
                className={`flex items-center justify-between px-3.5 py-2.5 rounded-xl text-sm font-medium transition-all duration-150 ${
                  isActive
                    ? 'bg-gradient-to-r from-brand-primary/20 to-brand-secondary/10 text-white border border-brand-primary/30 shadow-sm'
                    : 'text-slate-400 hover:text-slate-200 hover:bg-white/5'
                }`}
              >
                <div className="flex items-center space-x-3">
                  <DynamicIcon
                    name={item.iconName}
                    className={`w-5 h-5 transition-colors ${
                      isActive ? 'text-brand-primary' : 'text-slate-400'
                    }`}
                  />
                  <span>{item.label}</span>
                </div>

                {item.badge && (
                  <span
                    className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                      item.badgeVariant === 'warning'
                        ? 'bg-amber-900/40 text-amber-300 border border-amber-500/40'
                        : 'bg-brand-primary/20 text-brand-primary border border-brand-primary/30'
                    }`}
                  >
                    {item.badge}
                  </span>
                )}
              </Link>
            );
          })}
        </div>

        {/* Interactive Demo Role Switcher (Facilitates RBAC verification) */}
        {isDemoMode && <div className="p-4 mx-3 mb-3 rounded-xl bg-brand-dark-card border border-brand-dark-border/80">
          <div className="flex items-center justify-between mb-2">
            <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400">
              Demo Role Preview
            </span>
            <span className="text-[10px] text-brand-mint font-semibold">RBAC Live</span>
          </div>
          <div className="grid grid-cols-3 gap-1 text-[11px]">
            {(['SUPER_ADMIN', 'CONTENT_MANAGER', 'SUPPORT_AGENT'] as AdminRole[]).map((r) => {
              const isSelected = user?.role === r;
              const label = r === 'SUPER_ADMIN' ? 'Super' : r === 'CONTENT_MANAGER' ? 'Content' : 'Support';
              return (
                <button
                  key={r}
                  onClick={() => switchRoleDemo(r)}
                  className={`py-1.5 px-1 rounded-lg text-center font-medium transition-all ${
                    isSelected
                      ? 'bg-brand-primary text-white shadow-sm'
                      : 'bg-white/5 text-slate-400 hover:text-white hover:bg-white/10'
                  }`}
                  title={`Switch to ${r} mode`}
                >
                  {label}
                </button>
              );
            })}
          </div>
        </div>}

        {/* Footer System Version */}
        <div className="px-6 py-3 border-t border-brand-dark-border/60 flex items-center justify-between text-[11px] text-slate-500">
          <span>QuizEarn Admin v1.0.0</span>
          <span className="flex items-center space-x-1.5">
            <span className="w-1.5 h-1.5 rounded-full bg-brand-mint animate-pulse" />
            <span>Online</span>
          </span>
        </div>
      </aside>
    </>
  );
}
