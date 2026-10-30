'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useAdminAuth } from '../../lib/auth-context';
import { IconBell, IconChevronRight, IconLogOut, IconMenu } from '../ui/Icons';
import { RoleBadge } from '../ui/Badge';

interface TopNavProps {
  onOpenSidebar: () => void;
}

export function TopNav({ onOpenSidebar }: TopNavProps) {
  const pathname = usePathname();
  const { user, signOut } = useAdminAuth();
  const [profileDropdownOpen, setProfileDropdownOpen] = useState(false);

  // Generate dynamic breadcrumb segments from pathname
  const segments = pathname.split('/').filter(Boolean);
  const breadcrumbs = [
    { label: 'Admin', href: '/' },
    ...segments.map((seg, idx) => {
      const href = '/' + segments.slice(0, idx + 1).join('/');
      const label =
        seg.charAt(0).toUpperCase() + seg.slice(1).replace(/-/g, ' ');
      return { label, href };
    }),
  ];

  return (
    <header className="sticky top-0 z-30 h-18 bg-brand-dark-surface/90 backdrop-blur-md border-b border-brand-dark-border px-4 lg:px-8 flex items-center justify-between">
      {/* Left: Mobile Toggle & Breadcrumbs */}
      <div className="flex items-center space-x-3">
        <button
          onClick={onOpenSidebar}
          className="lg:hidden p-2 rounded-xl text-slate-400 hover:text-white hover:bg-white/5 transition"
          aria-label="Open navigation menu"
        >
          <IconMenu className="w-5 h-5" />
        </button>

        {/* Breadcrumb Trail */}
        <nav className="hidden sm:flex items-center space-x-2 text-xs">
          {breadcrumbs.map((crumb, idx) => {
            const isLast = idx === breadcrumbs.length - 1;
            return (
              <React.Fragment key={crumb.href}>
                {idx > 0 && <IconChevronRight className="w-3.5 h-3.5 text-slate-600" />}
                {isLast ? (
                  <span className="font-semibold text-white">{crumb.label}</span>
                ) : (
                  <Link
                    href={crumb.href}
                    className="text-slate-400 hover:text-slate-200 transition"
                  >
                    {crumb.label}
                  </Link>
                )}
              </React.Fragment>
            );
          })}
        </nav>
      </div>

      {/* Right: Environment, Notifications & Profile */}
      <div className="flex items-center space-x-4">
        {/* Environment Pill */}
        <div className="hidden md:flex items-center space-x-2 px-3 py-1 rounded-full bg-brand-dark-card border border-brand-dark-border text-xs text-slate-300">
          <span className="w-2 h-2 rounded-full bg-brand-mint animate-pulse" />
          <span className="font-medium">Local / Emulator</span>
        </div>

        {/* Quick Pending Alert / Bell */}
        <Link
          href="/redemptions"
          className="relative p-2 rounded-xl text-slate-400 hover:text-white hover:bg-white/5 transition"
          title="14 Pending Redemptions"
        >
          <IconBell className="w-5 h-5" />
          <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-brand-crimson ring-2 ring-brand-dark-surface" />
        </Link>

        {/* Operator Profile Dropdown */}
        {user && (
          <div className="relative">
            <button
              onClick={() => setProfileDropdownOpen(!profileDropdownOpen)}
              className="flex items-center space-x-3 p-1.5 rounded-xl hover:bg-white/5 transition focus:outline-none"
            >
              <div className="w-8 h-8 rounded-full bg-gradient-to-tr from-brand-primary to-brand-secondary text-white font-bold text-xs flex items-center justify-center shadow-md">
                {user.displayName.charAt(0)}
              </div>
              <div className="hidden sm:block text-left">
                <p className="text-xs font-semibold text-white leading-tight">
                  {user.displayName}
                </p>
                <p className="text-[10px] text-slate-400">{user.role}</p>
              </div>
            </button>

            {/* Dropdown Menu */}
            {profileDropdownOpen && (
              <>
                <div
                  className="fixed inset-0 z-40"
                  onClick={() => setProfileDropdownOpen(false)}
                />
                <div className="absolute right-0 mt-2 w-64 rounded-2xl bg-brand-dark-surface border border-brand-dark-border shadow-2xl z-50 p-3 space-y-3">
                  <div className="p-2 rounded-xl bg-brand-dark-card">
                    <p className="text-xs font-bold text-white">{user.displayName}</p>
                    <p className="text-[11px] text-slate-400 truncate">{user.email}</p>
                    <div className="mt-2">
                      <RoleBadge role={user.role} />
                    </div>
                  </div>

                  <div className="pt-1 border-t border-brand-dark-border/60">
                    <button
                      onClick={() => {
                        setProfileDropdownOpen(false);
                        signOut();
                      }}
                      className="w-full flex items-center space-x-2.5 px-3 py-2 rounded-xl text-xs font-medium text-rose-400 hover:bg-rose-900/20 transition"
                    >
                      <IconLogOut className="w-4 h-4" />
                      <span>Sign Out Operator Session</span>
                    </button>
                  </div>
                </div>
              </>
            )}
          </div>
        )}
      </div>
    </header>
  );
}
