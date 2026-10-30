import React from 'react';
import type { Metadata } from 'next';
import './globals.css';
import { AdminAuthProvider } from '../lib/auth-context';

export const metadata: Metadata = {
  title: 'QuizEarn Admin Panel — Role-Based Operations & Management',
  description: 'Enterprise administration dashboard for QuizEarn: Question Bank, Tournaments, Wallet Ledger, and Redemptions.',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en" className="dark">
      <body className="min-h-screen bg-brand-dark-bg text-slate-100 antialiased">
        <AdminAuthProvider>{children}</AdminAuthProvider>
      </body>
    </html>
  );
}
