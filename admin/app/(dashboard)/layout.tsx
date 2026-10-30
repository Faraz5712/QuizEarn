'use client';

import React, { useState } from 'react';
import { Sidebar } from '../../components/layout/Sidebar';
import { TopNav } from '../../components/layout/TopNav';
import { RouteGuard } from '../../components/layout/RouteGuard';
import { BackendModeNotice } from '../../components/layout/BackendModeNotice';

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <RouteGuard>
      <div className="min-h-screen bg-brand-dark-bg text-slate-100 flex">
        {/* Responsive Sidebar */}
        <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

        {/* Main Content Area */}
        <div className="flex-1 flex flex-col min-w-0 lg:pl-72">
          {/* Top Navigation Bar */}
          <TopNav onOpenSidebar={() => setSidebarOpen(true)} />
          <BackendModeNotice />

          {/* Main Content Viewport */}
          <main className="flex-1 p-4 sm:p-6 lg:p-8 overflow-y-auto">
            {children}
          </main>
        </div>
      </div>
    </RouteGuard>
  );
}
