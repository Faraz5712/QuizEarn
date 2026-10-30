'use client';

import React, { useMemo, useState } from 'react';
import { AdminTask, AdminTaskCriterion, AdminTaskType } from '../../types/tournament';
import { Button } from '../ui/Button';
import {
  IconCheck,
  IconClock,
  IconEdit,
  IconQuiz,
  IconSearch,
  IconSpark,
  IconTrash,
  IconTrophy,
  IconX,
} from '../ui/Icons';

interface TaskManagementTableProps {
  tasks: AdminTask[];
  onToggleActive: (taskId: string) => void;
  onEditTask: (task: AdminTask) => void;
  onDeleteTask: (taskId: string) => void;
}

export function TaskManagementTable({
  tasks,
  onToggleActive,
  onEditTask,
  onDeleteTask,
}: TaskManagementTableProps) {
  const [searchTerm, setSearchTerm] = useState('');
  const [typeFilter, setTypeFilter] = useState<'ALL' | AdminTaskType>('ALL');
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'ACTIVE' | 'INACTIVE'>('ALL');

  const filteredTasks = useMemo(() => {
    return tasks.filter((task) => {
      const matchesSearch =
        searchTerm === '' ||
        task.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
        task.description.toLowerCase().includes(searchTerm.toLowerCase()) ||
        task.id.toLowerCase().includes(searchTerm.toLowerCase());

      const matchesType = typeFilter === 'ALL' || task.type === typeFilter;

      const matchesStatus =
        statusFilter === 'ALL' ||
        (statusFilter === 'ACTIVE' && task.isActive) ||
        (statusFilter === 'INACTIVE' && !task.isActive);

      return matchesSearch && matchesType && matchesStatus;
    });
  }, [tasks, searchTerm, typeFilter, statusFilter]);

  const formatCriterion = (criterion: AdminTaskCriterion, categoryKey?: string) => {
    switch (criterion) {
      case 'QUIZ_COMPLETION':
        return 'Quizzes Completed';
      case 'ACCURACY_SCORE':
        return 'Score Accuracy %';
      case 'STREAK_MAINTAIN':
        return 'Daily Streak Maintain';
      case 'CATEGORY_QUIZ':
        return categoryKey ? `Category Specific (${categoryKey})` : 'Category Quiz';
      default:
        return criterion;
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
            placeholder="Search tasks by title, goal, or ID..."
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

        {/* Filter Chips */}
        <div className="flex flex-wrap items-center gap-2">
          {/* Type Filter */}
          <div className="flex items-center bg-slate-900 p-1 rounded-xl border border-brand-dark-border text-xs">
            <button
              onClick={() => setTypeFilter('ALL')}
              className={`px-3 py-1 rounded-lg font-medium transition-colors ${
                typeFilter === 'ALL'
                  ? 'bg-brand-primary text-white'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              All Types
            </button>
            <button
              onClick={() => setTypeFilter('DAILY')}
              className={`px-3 py-1 rounded-lg font-medium transition-colors ${
                typeFilter === 'DAILY'
                  ? 'bg-brand-primary text-white'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              Daily
            </button>
            <button
              onClick={() => setTypeFilter('WEEKLY')}
              className={`px-3 py-1 rounded-lg font-medium transition-colors ${
                typeFilter === 'WEEKLY'
                  ? 'bg-brand-primary text-white'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              Weekly
            </button>
          </div>

          {/* Status Filter */}
          <div className="flex items-center bg-slate-900 p-1 rounded-xl border border-brand-dark-border text-xs">
            <button
              onClick={() => setStatusFilter('ALL')}
              className={`px-3 py-1 rounded-lg font-medium transition-colors ${
                statusFilter === 'ALL'
                  ? 'bg-slate-700 text-white'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              All Status
            </button>
            <button
              onClick={() => setStatusFilter('ACTIVE')}
              className={`px-3 py-1 rounded-lg font-medium transition-colors ${
                statusFilter === 'ACTIVE'
                  ? 'bg-teal-600 text-white'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              Active
            </button>
            <button
              onClick={() => setStatusFilter('INACTIVE')}
              className={`px-3 py-1 rounded-lg font-medium transition-colors ${
                statusFilter === 'INACTIVE'
                  ? 'bg-rose-700 text-white'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              Paused
            </button>
          </div>
        </div>
      </div>

      {/* Task Cards & Table */}
      <div className="bg-brand-dark-card border border-brand-dark-border rounded-2xl overflow-hidden shadow-xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-brand-dark-border/80 bg-slate-900/60 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
                <th className="py-3.5 px-4">Task / Challenge</th>
                <th className="py-3.5 px-4">Cadence</th>
                <th className="py-3.5 px-4">Evaluation Criterion</th>
                <th className="py-3.5 px-4">Target Goal</th>
                <th className="py-3.5 px-4">Reward</th>
                <th className="py-3.5 px-4 text-center">Status</th>
                <th className="py-3.5 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-brand-dark-border/40 text-xs">
              {filteredTasks.length === 0 ? (
                <tr>
                  <td colSpan={7} className="py-12 text-center text-slate-400">
                    <div className="flex flex-col items-center justify-center space-y-2">
                      <IconQuiz className="w-8 h-8 text-slate-600" />
                      <p className="font-semibold text-slate-300">No challenge tasks match your criteria</p>
                      <p className="text-[11px] text-slate-500">
                        Adjust search filters or create a new daily/weekly task.
                      </p>
                    </div>
                  </td>
                </tr>
              ) : (
                filteredTasks.map((task) => (
                  <tr
                    key={task.id}
                    className={`hover:bg-slate-800/40 transition-colors ${
                      !task.isActive ? 'opacity-65' : ''
                    }`}
                  >
                    {/* Task Title & Desc */}
                    <td className="py-4 px-4 max-w-xs">
                      <div className="font-semibold text-white truncate">{task.title}</div>
                      <div className="text-[11px] text-slate-400 truncate mt-0.5">
                        {task.description}
                      </div>
                      <div className="font-mono text-[10px] text-slate-500 mt-1">{task.id}</div>
                    </td>

                    {/* Cadence Type */}
                    <td className="py-4 px-4 whitespace-nowrap">
                      {task.type === 'DAILY' ? (
                        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-amber-500/10 text-amber-300 border border-amber-500/30">
                          <IconClock className="w-3 h-3 mr-1" />
                          Daily Challenge
                        </span>
                      ) : (
                        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-purple-500/10 text-purple-300 border border-purple-500/30">
                          <IconTrophy className="w-3 h-3 mr-1" />
                          Weekly Goal
                        </span>
                      )}
                    </td>

                    {/* Criterion */}
                    <td className="py-4 px-4 whitespace-nowrap text-slate-300">
                      <div className="font-medium">
                        {formatCriterion(task.criterion, task.categoryKey)}
                      </div>
                      {task.categoryKey && (
                        <span className="inline-block mt-0.5 text-[10px] px-1.5 py-0.2 rounded bg-slate-800 text-slate-400 border border-slate-700 font-mono">
                          {task.categoryKey}
                        </span>
                      )}
                    </td>

                    {/* Target Goal */}
                    <td className="py-4 px-4 whitespace-nowrap">
                      <span className="font-mono font-bold text-white text-sm">
                        {task.targetGoal}
                      </span>{' '}
                      <span className="text-[11px] text-slate-400">
                        {task.criterion === 'ACCURACY_SCORE' ? '%' : 'units'}
                      </span>
                    </td>

                    {/* Reward */}
                    <td className="py-4 px-4 whitespace-nowrap">
                      <span className="inline-flex items-center gap-1 font-bold text-amber-400 bg-amber-500/10 border border-amber-500/20 px-2 py-0.5 rounded-lg text-xs">
                        <IconSpark className="w-3.5 h-3.5" />
                        +{task.rewardPoints} pts
                      </span>
                    </td>

                    {/* Status & Active Toggle */}
                    <td className="py-4 px-4 whitespace-nowrap text-center">
                      <button
                        onClick={() => onToggleActive(task.id)}
                        className={`inline-flex items-center px-2.5 py-1 rounded-full text-[11px] font-semibold transition-all ${
                          task.isActive
                            ? 'bg-teal-500/20 text-teal-300 border border-teal-500/30 hover:bg-teal-500/30'
                            : 'bg-slate-800 text-slate-400 border border-slate-700 hover:bg-slate-700'
                        }`}
                        title={task.isActive ? 'Click to Pause' : 'Click to Activate'}
                      >
                        {task.isActive ? (
                          <>
                            <IconCheck className="w-3 h-3 mr-1" />
                            Active
                          </>
                        ) : (
                          'Paused'
                        )}
                      </button>
                    </td>

                    {/* Actions */}
                    <td className="py-4 px-4 whitespace-nowrap text-right">
                      <div className="flex items-center justify-end space-x-1.5">
                        <button
                          onClick={() => onEditTask(task)}
                          className="p-1.5 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition-colors"
                          title="Edit task"
                        >
                          <IconEdit className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => onDeleteTask(task.id)}
                          className="p-1.5 text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 rounded-lg transition-colors"
                          title="Delete task"
                        >
                          <IconTrash className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Footer Summary */}
        <div className="p-3 bg-slate-900/40 border-t border-brand-dark-border text-[11px] text-slate-400 flex items-center justify-between">
          <span>
            Showing <strong className="text-white">{filteredTasks.length}</strong> of{' '}
            <strong className="text-white">{tasks.length}</strong> challenge tasks
          </span>
          <span className="text-slate-500">
            Daily reset deterministic at 00:00 UTC | Double-entry TASK_COMPLETED ledger credits
          </span>
        </div>
      </div>
    </div>
  );
}
