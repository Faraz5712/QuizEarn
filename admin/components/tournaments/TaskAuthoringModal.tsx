'use client';

import React, { useEffect, useState } from 'react';
import { AdminCategory } from '../../types/question';
import {
  AdminTask,
  AdminTaskCriterion,
  AdminTaskType,
} from '../../types/tournament';
import { Button } from '../ui/Button';
import { IconAlertCircle, IconSpark, IconTrophy, IconX } from '../ui/Icons';

interface TaskAuthoringModalProps {
  isOpen: boolean;
  taskToEdit?: AdminTask | null;
  categories: AdminCategory[];
  onClose: () => void;
  onSave: (
    task: Omit<AdminTask, 'id' | 'createdAt'> & {
      id?: string;
    }
  ) => void;
}

export function TaskAuthoringModal({
  isOpen,
  taskToEdit,
  categories,
  onClose,
  onSave,
}: TaskAuthoringModalProps) {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [type, setType] = useState<AdminTaskType>('DAILY');
  const [criterion, setCriterion] = useState<AdminTaskCriterion>('QUIZ_COMPLETION');
  const [categoryKey, setCategoryKey] = useState<string>('cat_science');
  const [targetGoal, setTargetGoal] = useState<number>(3);
  const [rewardPoints, setRewardPoints] = useState<number>(25);
  const [isActive, setIsActive] = useState<boolean>(true);
  const [errors, setErrors] = useState<string[]>([]);

  useEffect(() => {
    if (taskToEdit) {
      setTitle(taskToEdit.title);
      setDescription(taskToEdit.description);
      setType(taskToEdit.type);
      setCriterion(taskToEdit.criterion);
      setCategoryKey(taskToEdit.categoryKey || categories[0]?.id || 'cat_science');
      setTargetGoal(taskToEdit.targetGoal);
      setRewardPoints(taskToEdit.rewardPoints);
      setIsActive(taskToEdit.isActive);
    } else {
      setTitle('');
      setDescription('');
      setType('DAILY');
      setCriterion('QUIZ_COMPLETION');
      setCategoryKey(categories[0]?.id || 'cat_science');
      setTargetGoal(3);
      setRewardPoints(25);
      setIsActive(true);
    }
    setErrors([]);
  }, [taskToEdit, isOpen, categories]);

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const errs: string[] = [];

    if (!title.trim()) errs.push('Task title is required.');
    if (!description.trim()) errs.push('Task description is required.');
    if (targetGoal <= 0) errs.push('Target goal must be greater than zero.');
    if (rewardPoints <= 0) errs.push('Reward points must be greater than zero.');
    if (criterion === 'CATEGORY_QUIZ' && !categoryKey) {
      errs.push('A valid category must be selected for Category Quiz criterion.');
    }

    if (errs.length > 0) {
      setErrors(errs);
      return;
    }

    onSave({
      id: taskToEdit?.id,
      title: title.trim(),
      description: description.trim(),
      type,
      criterion,
      categoryKey: criterion === 'CATEGORY_QUIZ' ? categoryKey : undefined,
      targetGoal,
      rewardPoints,
      isActive,
      updatedAt: new Date().toISOString(),
    });

    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="relative w-full max-w-xl bg-brand-dark-card border border-brand-dark-border rounded-3xl shadow-2xl p-6 sm:p-8 animate-in fade-in zoom-in duration-200">
        <button
          onClick={onClose}
          className="absolute top-6 right-6 text-slate-400 hover:text-white transition-colors"
          aria-label="Close modal"
        >
          <IconX className="w-5 h-5" />
        </button>

        <div className="flex items-center space-x-3 mb-6">
          <div className="p-3 bg-brand-primary/10 border border-brand-primary/30 rounded-2xl text-brand-primary">
            <IconTrophy className="w-6 h-6" />
          </div>
          <div>
            <h2 className="text-xl font-bold text-white tracking-tight">
              {taskToEdit ? 'Edit Challenge Task' : 'Create New Challenge Task'}
            </h2>
            <p className="text-xs text-slate-400">
              Configure gamified daily milestones and weekly retention goals
            </p>
          </div>
        </div>

        {errors.length > 0 && (
          <div className="mb-5 p-4 rounded-2xl bg-rose-500/10 border border-rose-500/30 text-rose-300 text-xs space-y-1">
            <div className="font-semibold flex items-center gap-1.5">
              <IconAlertCircle className="w-4 h-4" />
              <span>Please resolve the following errors:</span>
            </div>
            <ul className="list-disc list-inside pl-1 space-y-0.5">
              {errors.map((err, idx) => (
                <li key={idx}>{err}</li>
              ))}
            </ul>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Title */}
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1">
              Task Title <span className="text-brand-primary">*</span>
            </label>
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="e.g. Science Scholar, Daily Accuracy Sprint"
              className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-brand-dark-border text-xs text-white placeholder-slate-500 focus:outline-none focus:border-brand-primary transition-colors"
            />
          </div>

          {/* Description */}
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1">
              Description <span className="text-brand-primary">*</span>
            </label>
            <textarea
              rows={2}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="e.g. Complete 3 quizzes in Science & Nature category today."
              className="w-full px-3.5 py-2 rounded-xl bg-slate-900 border border-brand-dark-border text-xs text-white placeholder-slate-500 focus:outline-none focus:border-brand-primary transition-colors resize-none"
            />
          </div>

          {/* Cadence Type & Criterion Selection */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">
                Cadence Type
              </label>
              <select
                value={type}
                onChange={(e) => setType(e.target.value as AdminTaskType)}
                className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-brand-dark-border text-xs text-white focus:outline-none focus:border-brand-primary transition-colors"
              >
                <option value="DAILY">Daily Challenge (resets 00:00 UTC)</option>
                <option value="WEEKLY">Weekly Goal (resets Monday 00:00 UTC)</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">
                Evaluation Criterion
              </label>
              <select
                value={criterion}
                onChange={(e) => setCriterion(e.target.value as AdminTaskCriterion)}
                className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-brand-dark-border text-xs text-white focus:outline-none focus:border-brand-primary transition-colors"
              >
                <option value="QUIZ_COMPLETION">Quiz Completions Count</option>
                <option value="ACCURACY_SCORE">Score Accuracy Percentage</option>
                <option value="CATEGORY_QUIZ">Category Specific Performance</option>
                <option value="STREAK_MAINTAIN">Daily Streak Check-in</option>
              </select>
            </div>
          </div>

          {/* Category Selector (conditional) */}
          {criterion === 'CATEGORY_QUIZ' && (
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">
                Target Category <span className="text-brand-primary">*</span>
              </label>
              <select
                value={categoryKey}
                onChange={(e) => setCategoryKey(e.target.value)}
                className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-brand-dark-border text-xs text-white focus:outline-none focus:border-brand-primary transition-colors"
              >
                {categories.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name} ({c.id})
                  </option>
                ))}
              </select>
            </div>
          )}

          {/* Target Goal & Reward Points */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">
                Target Threshold Goal{' '}
                <span className="text-slate-400 font-normal">
                  ({criterion === 'ACCURACY_SCORE' ? 'Percentage %' : 'Units count'})
                </span>
              </label>
              <input
                type="number"
                min={1}
                max={criterion === 'ACCURACY_SCORE' ? 100 : 1000}
                value={targetGoal}
                onChange={(e) => setTargetGoal(Math.max(1, parseInt(e.target.value) || 1))}
                className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-brand-dark-border text-xs text-white font-mono focus:outline-none focus:border-brand-primary transition-colors"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">
                Points Reward
              </label>
              <div className="relative">
                <input
                  type="number"
                  min={5}
                  step={5}
                  value={rewardPoints}
                  onChange={(e) => setRewardPoints(Math.max(1, parseInt(e.target.value) || 5))}
                  className="w-full pl-3.5 pr-14 py-2.5 rounded-xl bg-slate-900 border border-brand-dark-border text-xs text-white font-mono focus:outline-none focus:border-brand-primary transition-colors"
                />
                <span className="absolute right-3 top-1/2 -translate-y-1/2 text-xs font-bold text-amber-400 flex items-center gap-1 pointer-events-none">
                  <IconSpark className="w-3.5 h-3.5" />
                  pts
                </span>
              </div>
            </div>
          </div>

          {/* Active Switch */}
          <div className="pt-2">
            <label className="flex items-center space-x-3 cursor-pointer">
              <input
                type="checkbox"
                checked={isActive}
                onChange={(e) => setIsActive(e.target.checked)}
                className="w-4 h-4 rounded text-brand-primary bg-slate-900 border-brand-dark-border focus:ring-brand-primary"
              />
              <div>
                <span className="text-xs font-semibold text-white">Active in rotation</span>
                <p className="text-[11px] text-slate-400">
                  Players can see and track this task in their daily challenges tab.
                </p>
              </div>
            </label>
          </div>

          {/* Action Buttons */}
          <div className="flex items-center justify-end space-x-3 pt-4 border-t border-brand-dark-border/60">
            <Button type="button" variant="outline" size="sm" onClick={onClose}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" size="sm">
              {taskToEdit ? 'Save Changes' : 'Create Challenge Task'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
