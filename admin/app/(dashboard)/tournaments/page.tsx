'use client';

import React, { useEffect, useState } from 'react';
import { RouteGuard } from '../../../components/layout/RouteGuard';
import { Button } from '../../../components/ui/Button';
import {
  IconAlertCircle,
  IconCheck,
  IconClock,
  IconPlus,
  IconSpark,
  IconTrophy,
  IconUsers,
} from '../../../components/ui/Icons';
import { TaskAuthoringModal } from '../../../components/tournaments/TaskAuthoringModal';
import { TaskManagementTable } from '../../../components/tournaments/TaskManagementTable';
import { TournamentCancelModal } from '../../../components/tournaments/TournamentCancelModal';
import { TournamentConfigModal } from '../../../components/tournaments/TournamentConfigModal';
import { TournamentTable } from '../../../components/tournaments/TournamentTable';
import {
  loadStoredCategories,
} from '../../../lib/questions-store';
import {
  loadStoredTasks,
  loadStoredTournaments,
  saveStoredTasks,
  saveStoredTournaments,
} from '../../../lib/tournaments-store';
import { AdminCategory } from '../../../types/question';
import { AdminTask, AdminTournament } from '../../../types/tournament';
import {
  cancelAdminTournament,
  deleteAdminRecord,
  listAdminRecords,
  saveAdminRecord,
} from '../../../lib/backend';
import { isFirebaseConfigured } from '../../../lib/firebase';

type ActiveTab = 'TOURNAMENTS' | 'TASKS';

export default function TournamentManagementPage() {
  const [activeTab, setActiveTab] = useState<ActiveTab>('TOURNAMENTS');
  const [tournaments, setTournaments] = useState<AdminTournament[]>([]);
  const [tasks, setTasks] = useState<AdminTask[]>([]);
  const [categories, setCategories] = useState<AdminCategory[]>([]);
  const [isLoaded, setIsLoaded] = useState(false);

  // Modals state
  const [isConfigModalOpen, setIsConfigModalOpen] = useState(false);
  const [tournamentToEdit, setTournamentToEdit] = useState<AdminTournament | null>(null);

  const [isCancelModalOpen, setIsCancelModalOpen] = useState(false);
  const [tournamentToCancel, setTournamentToCancel] = useState<AdminTournament | null>(null);

  const [isTaskModalOpen, setIsTaskModalOpen] = useState(false);
  const [taskToEdit, setTaskToEdit] = useState<AdminTask | null>(null);

  // Notification Toast
  const [toast, setToast] = useState<{
    message: string;
    type: 'success' | 'danger' | 'info';
  } | null>(null);

  useEffect(() => {
    if (!isFirebaseConfigured) {
      setTournaments(loadStoredTournaments());
      setTasks(loadStoredTasks());
      setCategories(loadStoredCategories());
      setIsLoaded(true);
      return;
    }
    Promise.all([
      listAdminRecords<AdminTournament>('tournaments'),
      listAdminRecords<AdminTask>('tasks'),
      listAdminRecords<AdminCategory>('categories'),
    ]).then(([loadedTournaments, loadedTasks, loadedCategories]) => {
      setTournaments(loadedTournaments);
      setTasks(loadedTasks);
      setCategories(loadedCategories);
    }).catch((error: unknown) => {
      showToast(error instanceof Error ? error.message : 'Failed to load backend records.', 'danger');
    }).finally(() => setIsLoaded(true));
  }, []);

  const showToast = (message: string, type: 'success' | 'danger' | 'info' = 'success') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 4000);
  };

  // Tournament Actions
  const handleSaveTournament = async (
    data: Omit<AdminTournament, 'id' | 'createdAt' | 'registeredParticipants'> & {
      id?: string;
    }
  ) => {
    let updatedTournaments: AdminTournament[];

    if (data.id) {
      updatedTournaments = tournaments.map((t) =>
        t.id === data.id
          ? {
              ...t,
              ...data,
            }
          : t
      );
    } else {
      const category = categories.find((c) => c.id === data.categoryId);
      const newTournament: AdminTournament = {
        id: `TRN-2026-${Date.now().toString().slice(-4)}`,
        ...data,
        categoryName: category?.name || data.categoryName || 'General',
        registeredParticipants: 0,
        createdAt: new Date().toISOString(),
      };
      updatedTournaments = [newTournament, ...tournaments];
    }

    const savedTournament = updatedTournaments.find((tournament) => tournament.id === data.id) ?? updatedTournaments[0]!;
    try {
      await saveAdminRecord('tournaments', { ...savedTournament });
    } catch (error) {
      showToast(error instanceof Error ? error.message : 'Failed to save tournament.', 'danger');
      return;
    }
    setTournaments(updatedTournaments);
    if (!isFirebaseConfigured) saveStoredTournaments(updatedTournaments);
    showToast(data.id ? `Tournament "${data.title}" updated successfully.` : `Tournament "${savedTournament.title}" scheduled successfully.`);
    setTournamentToEdit(null);
  };

  const handleOpenCancelModal = (tournament: AdminTournament) => {
    setTournamentToCancel(tournament);
    setIsCancelModalOpen(true);
  };

  const handleConfirmCancelTournament = async (tournamentId: string, reason: string) => {
    const target = tournaments.find((t) => t.id === tournamentId);
    if (!target) return;

    if (isFirebaseConfigured) {
      try {
        await cancelAdminTournament(tournamentId, reason);
      } catch (error) {
        showToast(error instanceof Error ? error.message : 'Tournament cancellation failed.', 'danger');
        return;
      }
    }
    const totalRefund = target.entryFeePoints * target.registeredParticipants;

    const updated = tournaments.map((t) =>
      t.id === tournamentId
        ? {
            ...t,
            status: 'CANCELLED' as const,
            cancellationReason: reason,
            cancelledAtIso: new Date().toISOString(),
            totalRefundedPoints: totalRefund,
          }
        : t
    );

    setTournaments(updated);
    if (!isFirebaseConfigured) saveStoredTournaments(updated);
    showToast(
      `Tournament "${target.title}" cancelled. ${totalRefund.toLocaleString()} pts refunded to ${
        target.registeredParticipants
      } players via ledger credits.`,
      'danger'
    );
  };

  const handleCloneTournament = (tournament: AdminTournament) => {
    setTournamentToEdit({
      ...tournament,
      id: '',
      title: `${tournament.title} (Copy)`,
      registeredParticipants: 0,
      status: 'SCHEDULED',
    });
    setIsConfigModalOpen(true);
  };

  // Task Actions
  const handleSaveTask = async (
    data: Omit<AdminTask, 'id' | 'createdAt'> & {
      id?: string;
    }
  ) => {
    let updatedTasks: AdminTask[];

    if (data.id) {
      updatedTasks = tasks.map((tsk) =>
        tsk.id === data.id
          ? {
              ...tsk,
              ...data,
            }
          : tsk
      );
    } else {
      const prefix = data.type === 'DAILY' ? 'DLY' : 'WKL';
      const newTask: AdminTask = {
        id: `TSK-${prefix}-${Date.now().toString().slice(-4)}`,
        ...data,
        createdAt: new Date().toISOString(),
      };
      updatedTasks = [newTask, ...tasks];
    }

    const savedTask = updatedTasks.find((task) => task.id === data.id) ?? updatedTasks[0]!;
    try {
      await saveAdminRecord('tasks', { ...savedTask });
    } catch (error) {
      showToast(error instanceof Error ? error.message : 'Failed to save task.', 'danger');
      return;
    }
    setTasks(updatedTasks);
    if (!isFirebaseConfigured) saveStoredTasks(updatedTasks);
    showToast(data.id ? `Challenge task "${data.title}" updated.` : `Challenge task "${savedTask.title}" added to rotation.`);
    setTaskToEdit(null);
  };

  const handleToggleTaskActive = async (taskId: string) => {
    const updated = tasks.map((t) =>
      t.id === taskId ? { ...t, isActive: !t.isActive } : t
    );
    const target = updated.find((t) => t.id === taskId);
    if (target) {
      try {
        await saveAdminRecord('tasks', { ...target });
      } catch (error) {
        showToast(error instanceof Error ? error.message : 'Failed to update task.', 'danger');
        return;
      }
    }
    setTasks(updated);
    if (!isFirebaseConfigured) saveStoredTasks(updated);
    showToast(
      `Task "${target?.title}" is now ${target?.isActive ? 'Active' : 'Paused'}.`,
      'info'
    );
  };

  const handleDeleteTask = async (taskId: string) => {
    const target = tasks.find((t) => t.id === taskId);
    if (!target) return;
    if (confirm(`Are you sure you want to delete challenge task "${target.title}"?`)) {
      try {
        if (isFirebaseConfigured) await deleteAdminRecord('tasks', taskId);
      } catch (error) {
        showToast(error instanceof Error ? error.message : 'Failed to delete task.', 'danger');
        return;
      }
      const updated = tasks.filter((t) => t.id !== taskId);
      setTasks(updated);
      if (!isFirebaseConfigured) saveStoredTasks(updated);
      showToast(`Task "${target.title}" removed.`, 'info');
    }
  };

  // Metrics calculation
  const liveCount = tournaments.filter((t) => t.status === 'LIVE').length;
  const scheduledCount = tournaments.filter((t) => t.status === 'SCHEDULED').length;
  const totalPrizePoolCommitted = tournaments
    .filter((t) => t.status === 'LIVE' || t.status === 'SCHEDULED')
    .reduce((sum, t) => sum + t.prizePoolPoints, 0);
  const totalParticipantsInFlight = tournaments
    .filter((t) => t.status === 'LIVE' || t.status === 'SCHEDULED')
    .reduce((sum, t) => sum + t.registeredParticipants, 0);
  const activeChallengesCount = tasks.filter((t) => t.isActive).length;

  if (!isLoaded) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="w-8 h-8 border-4 border-brand-primary border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <RouteGuard requiredPermission="MANAGE_TOURNAMENTS">
      <div className="space-y-6 max-w-7xl mx-auto pb-12">
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
              <IconTrophy className="w-7 h-7 text-brand-gold" />
              Tournaments & Daily Challenges
            </h1>
            <p className="text-xs text-slate-400 mt-1">
              Configure scheduled tournaments, dynamic prize pool distribution, and gamified retention challenges.
            </p>
          </div>
          <div className="mt-4 sm:mt-0 flex items-center space-x-3">
            {activeTab === 'TOURNAMENTS' ? (
              <Button
                variant="primary"
                size="sm"
                onClick={() => {
                  setTournamentToEdit(null);
                  setIsConfigModalOpen(true);
                }}
                className="flex items-center gap-1.5 shadow-lg shadow-brand-primary/20"
              >
                <IconPlus className="w-4 h-4" />
                Schedule Tournament
              </Button>
            ) : (
              <Button
                variant="primary"
                size="sm"
                onClick={() => {
                  setTaskToEdit(null);
                  setIsTaskModalOpen(true);
                }}
                className="flex items-center gap-1.5 shadow-lg shadow-brand-primary/20"
              >
                <IconPlus className="w-4 h-4" />
                Create Challenge Task
              </Button>
            )}
          </div>
        </div>

        {/* Operational Metrics Bar */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="bg-brand-dark-card border border-brand-dark-border p-4 rounded-2xl flex items-center space-x-3.5">
            <div className="p-3 rounded-xl bg-teal-500/10 border border-teal-500/20 text-teal-400">
              <IconTrophy className="w-5 h-5" />
            </div>
            <div>
              <div className="text-[11px] font-medium text-slate-400 uppercase tracking-wider">
                Live & Scheduled
              </div>
              <div className="text-xl font-bold text-white mt-0.5">
                {liveCount} <span className="text-xs text-teal-400 font-normal">live</span> / {scheduledCount}{' '}
                <span className="text-xs text-slate-400 font-normal">upcoming</span>
              </div>
            </div>
          </div>

          <div className="bg-brand-dark-card border border-brand-dark-border p-4 rounded-2xl flex items-center space-x-3.5">
            <div className="p-3 rounded-xl bg-amber-500/10 border border-amber-500/20 text-amber-400">
              <IconSpark className="w-5 h-5" />
            </div>
            <div>
              <div className="text-[11px] font-medium text-slate-400 uppercase tracking-wider">
                Committed Prize Pools
              </div>
              <div className="text-xl font-bold text-amber-400 mt-0.5">
                {totalPrizePoolCommitted.toLocaleString()} <span className="text-xs font-normal">pts</span>
              </div>
            </div>
          </div>

          <div className="bg-brand-dark-card border border-brand-dark-border p-4 rounded-2xl flex items-center space-x-3.5">
            <div className="p-3 rounded-xl bg-purple-500/10 border border-purple-500/20 text-purple-400">
              <IconUsers className="w-5 h-5" />
            </div>
            <div>
              <div className="text-[11px] font-medium text-slate-400 uppercase tracking-wider">
                Registered Players
              </div>
              <div className="text-xl font-bold text-white mt-0.5">
                {totalParticipantsInFlight.toLocaleString()}{' '}
                <span className="text-xs text-slate-400 font-normal">active</span>
              </div>
            </div>
          </div>

          <div className="bg-brand-dark-card border border-brand-dark-border p-4 rounded-2xl flex items-center space-x-3.5">
            <div className="p-3 rounded-xl bg-blue-500/10 border border-blue-500/20 text-blue-400">
              <IconClock className="w-5 h-5" />
            </div>
            <div>
              <div className="text-[11px] font-medium text-slate-400 uppercase tracking-wider">
                Active Challenges
              </div>
              <div className="text-xl font-bold text-white mt-0.5">
                {activeChallengesCount}{' '}
                <span className="text-xs text-slate-400 font-normal">in rotation</span>
              </div>
            </div>
          </div>
        </div>

        {/* Navigation Tabs */}
        <div className="flex border-b border-brand-dark-border/80 space-x-6 text-sm">
          <button
            onClick={() => setActiveTab('TOURNAMENTS')}
            className={`pb-3 font-semibold transition-all relative flex items-center gap-2 ${
              activeTab === 'TOURNAMENTS'
                ? 'text-white border-b-2 border-brand-primary'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <IconTrophy className="w-4 h-4" />
            Tournaments
            <span className="px-2 py-0.5 text-[10px] rounded-full bg-slate-800 text-slate-300 font-mono">
              {tournaments.length}
            </span>
          </button>

          <button
            onClick={() => setActiveTab('TASKS')}
            className={`pb-3 font-semibold transition-all relative flex items-center gap-2 ${
              activeTab === 'TASKS'
                ? 'text-white border-b-2 border-brand-primary'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <IconClock className="w-4 h-4" />
            Daily & Weekly Challenges
            <span className="px-2 py-0.5 text-[10px] rounded-full bg-slate-800 text-slate-300 font-mono">
              {tasks.length}
            </span>
          </button>
        </div>

        {/* Tab Content */}
        {activeTab === 'TOURNAMENTS' ? (
          <TournamentTable
            tournaments={tournaments}
            onEditTournament={(t) => {
              setTournamentToEdit(t);
              setIsConfigModalOpen(true);
            }}
            onCancelTournament={handleOpenCancelModal}
            onCloneTournament={handleCloneTournament}
          />
        ) : (
          <TaskManagementTable
            tasks={tasks}
            onToggleActive={handleToggleTaskActive}
            onEditTask={(tsk) => {
              setTaskToEdit(tsk);
              setIsTaskModalOpen(true);
            }}
            onDeleteTask={handleDeleteTask}
          />
        )}

        {/* Modals */}
        <TournamentConfigModal
          isOpen={isConfigModalOpen}
          tournamentToEdit={tournamentToEdit}
          categories={categories}
          onClose={() => {
            setIsConfigModalOpen(false);
            setTournamentToEdit(null);
          }}
          onSave={handleSaveTournament}
        />

        <TournamentCancelModal
          isOpen={isCancelModalOpen}
          tournament={tournamentToCancel}
          onClose={() => {
            setIsCancelModalOpen(false);
            setTournamentToCancel(null);
          }}
          onConfirmCancel={handleConfirmCancelTournament}
        />

        <TaskAuthoringModal
          isOpen={isTaskModalOpen}
          taskToEdit={taskToEdit}
          categories={categories}
          onClose={() => {
            setIsTaskModalOpen(false);
            setTaskToEdit(null);
          }}
          onSave={handleSaveTask}
        />
      </div>
    </RouteGuard>
  );
}
