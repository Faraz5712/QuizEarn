'use client';

import React, { createContext, useContext, useEffect, useState } from 'react';
import {
  onIdTokenChanged,
  signInWithEmailAndPassword,
  signOut as firebaseSignOut,
} from 'firebase/auth';
import { DEMO_ADMIN_USERS } from './mock-data';
import { firebaseAuth, isFirebaseConfigured } from './firebase';
import { AdminRole, AdminUser } from '../types/admin';

interface AuthContextType {
  user: AdminUser | null;
  isLoading: boolean;
  isDemoMode: boolean;
  signIn: (email: string, password: string) => Promise<{ success: boolean; error?: string }>;
  signOut: () => void;
  switchRoleDemo: (role: AdminRole) => void;
}

const STORAGE_KEY = 'quizearn_admin_session';
const ADMIN_ROLES: AdminRole[] = ['SUPER_ADMIN', 'CONTENT_MANAGER', 'SUPPORT_AGENT'];
const localDemoEnabled = !isFirebaseConfigured && process.env.NODE_ENV !== 'production';

const AuthContext = createContext<AuthContextType>({
  user: null,
  isLoading: true,
  isDemoMode: localDemoEnabled,
  signIn: async () => ({ success: false }),
  signOut: () => {},
  switchRoleDemo: () => {},
});

export function AdminAuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<AdminUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (firebaseAuth) {
      const auth = firebaseAuth;
      return onIdTokenChanged(auth, async (firebaseUser) => {
        if (!firebaseUser) {
          setUser(null);
          setIsLoading(false);
          return;
        }
        if (!localDemoEnabled) {
          setUser(null);
          setIsLoading(false);
          return;
        }
        try {
          const token = await firebaseUser.getIdTokenResult();
          const role = token.claims.role;
          if (typeof role !== 'string' || !ADMIN_ROLES.includes(role as AdminRole)) {
            await firebaseSignOut(auth);
            setUser(null);
            setIsLoading(false);
            return;
          }
          setUser({
            uid: firebaseUser.uid,
            email: firebaseUser.email || '',
            displayName: firebaseUser.displayName || firebaseUser.email || 'Administrator',
            role: role as AdminRole,
            assignedAt: '',
            isActive: true,
          });
        } catch {
          await firebaseSignOut(auth);
          setUser(null);
        } finally {
          setIsLoading(false);
        }
      });
    }

    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      if (stored) {
        setUser(JSON.parse(stored) as AdminUser);
      } else {
        const demoUser = DEMO_ADMIN_USERS.super;
        setUser(demoUser);
        localStorage.setItem(STORAGE_KEY, JSON.stringify(demoUser));
      }
    } catch {
      setUser(DEMO_ADMIN_USERS.super);
    } finally {
      setIsLoading(false);
    }
  }, []);

  const signIn = async (
    email: string,
    password: string
  ): Promise<{ success: boolean; error?: string }> => {
    setIsLoading(true);
    if (firebaseAuth) {
      const auth = firebaseAuth;
      try {
        const credential = await signInWithEmailAndPassword(auth, email.trim(), password);
        const token = await credential.user.getIdTokenResult(true);
        const role = token.claims.role;
        if (typeof role !== 'string' || !ADMIN_ROLES.includes(role as AdminRole)) {
          await firebaseSignOut(auth);
          return {
            success: false,
            error: 'This account does not have an authorized administrator role.',
          };
        }
        return { success: true };
      } catch (error) {
        return {
          success: false,
          error: error instanceof Error ? error.message : 'Firebase sign-in failed.',
        };
      } finally {
        setIsLoading(false);
      }
    }

    if (!localDemoEnabled) {
      setIsLoading(false);
      return { success: false, error: 'Firebase Auth must be configured before production sign-in.' };
    }
    await new Promise((resolve) => setTimeout(resolve, 300));
    const matched = Object.values(DEMO_ADMIN_USERS).find(
      (candidate) => candidate.email.toLowerCase() === email.toLowerCase().trim()
    );
    if (!matched || password.length < 6) {
      setIsLoading(false);
      return { success: false, error: 'Invalid local demo credentials.' };
    }
    const updatedUser = { ...matched, lastLogin: new Date().toISOString() };
    setUser(updatedUser);
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(updatedUser));
    } catch {
      setUser(null);
      setIsLoading(false);
      return { success: false, error: 'Unable to persist the local demo session.' };
    }
    setIsLoading(false);
    return { success: true };
  };

  const signOut = () => {
    setUser(null);
    if (firebaseAuth) {
      void firebaseSignOut(firebaseAuth);
      return;
    }
    localStorage.removeItem(STORAGE_KEY);
  };

  const switchRoleDemo = (role: AdminRole) => {
    if (firebaseAuth || !localDemoEnabled) return;
    const key = role === 'SUPER_ADMIN' ? 'super' : role === 'CONTENT_MANAGER' ? 'content' : 'support';
    const demoUser = DEMO_ADMIN_USERS[key];
    setUser(demoUser);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(demoUser));
  };

  return (
    <AuthContext.Provider
      value={{ user, isLoading, isDemoMode: localDemoEnabled, signIn, signOut, switchRoleDemo }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAdminAuth() {
  return useContext(AuthContext);
}
