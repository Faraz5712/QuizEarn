'use client';

import React, { useEffect, useState } from 'react';
import { AdminCategory } from '../../types/question';
import { AdminTournament, AdminTournamentStatus } from '../../types/tournament';
import { Button } from '../ui/Button';
import { IconAlertCircle, IconCheck, IconSpark, IconTrophy, IconX } from '../ui/Icons';

interface TournamentConfigModalProps {
  isOpen: boolean;
  tournamentToEdit?: AdminTournament | null;
  categories: AdminCategory[];
  onClose: () => void;
  onSave: (
    tournament: Omit<AdminTournament, 'id' | 'createdAt' | 'registeredParticipants'> & {
      id?: string;
    }
  ) => void;
}

export function TournamentConfigModal({
  isOpen,
  tournamentToEdit,
  categories,
  onClose,
  onSave,
}: TournamentConfigModalProps) {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [categoryId, setCategoryId] = useState(categories[0]?.id || 'cat_science');
  const [status, setStatus] = useState<AdminTournamentStatus>('SCHEDULED');
  const [startTime, setStartTime] = useState('');
  const [endTime, setEndTime] = useState('');
  const [entryFeePoints, setEntryFeePoints] = useState(50);
  const [prizePoolPoints, setPrizePoolPoints] = useState(5000);
  const [maxParticipants, setMaxParticipants] = useState<number | undefined>(100);
  const [minLevelRequired, setMinLevelRequired] = useState(1);
  const [errors, setErrors] = useState<string[]>([]);

  // Calculate default future times
  useEffect(() => {
    if (tournamentToEdit) {
      setTitle(tournamentToEdit.title);
      setDescription(tournamentToEdit.description);
      setCategoryId(tournamentToEdit.categoryId);
      setStatus(tournamentToEdit.status);
      setStartTime(
        tournamentToEdit.startTimeIso ? tournamentToEdit.startTimeIso.slice(0, 16) : ''
      );
      setEndTime(
        tournamentToEdit.endTimeIso ? tournamentToEdit.endTimeIso.slice(0, 16) : ''
      );
      setEntryFeePoints(tournamentToEdit.entryFeePoints);
      setPrizePoolPoints(tournamentToEdit.prizePoolPoints);
      setMaxParticipants(tournamentToEdit.maxParticipants);
      setMinLevelRequired(tournamentToEdit.minLevelRequired);
    } else {
      const now = new Date();
      const tomorrow = new Date(now.getTime() + 24 * 60 * 60 * 1000);
      const dayAfter = new Date(tomorrow.getTime() + 12 * 60 * 60 * 1000);

      setTitle('');
      setDescription('');
      setCategoryId(categories[0]?.id || 'cat_science');
      setStatus('SCHEDULED');
      setStartTime(tomorrow.toISOString().slice(0, 16));
      setEndTime(dayAfter.toISOString().slice(0, 16));
      setEntryFeePoints(50);
      setPrizePoolPoints(5000);
      setMaxParticipants(100);
      setMinLevelRequired(1);
    }
    setErrors([]);
  }, [tournamentToEdit, categories, isOpen]);

  if (!isOpen) return null;

  // 50% / 30% / 20% podium breakdown
  const firstPrize = Math.round(prizePoolPoints * 0.5);
  const secondPrize = Math.round(prizePoolPoints * 0.3);
  const thirdPrize = Math.round(prizePoolPoints * 0.2);

  const validateForm = (): boolean => {
    const errs: string[] = [];

    if (!title.trim() || title.trim().length < 5) {
      errs.push('Tournament title must be at least 5 characters long.');
    }

    if (!description.trim() || description.trim().length < 10) {
      errs.push('Tournament description must be at least 10 characters long.');
    }

    if (!startTime || !endTime) {
      errs.push('Both start and end dates/times are required.');
    } else {
      const startDate = new Date(startTime);
      const endDate = new Date(endTime);
      if (endDate <= startDate) {
        errs.push('Tournament end time must be after the start time.');
      }
    }

    if (entryFeePoints < 0) {
      errs.push('Entry fee points cannot be negative.');
    }

    if (prizePoolPoints < 100) {
      errs.push('Prize pool points must be at least 100 points.');
    }

    if (minLevelRequired < 1 || minLevelRequired > 50) {
      errs.push('Minimum player level must be between 1 and 50.');
    }

    setErrors(errs);
    return errs.length === 0;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) return;

    const selectedCat = categories.find((c) => c.id === categoryId);

    onSave({
      id: tournamentToEdit?.id,
      title: title.trim(),
      description: description.trim(),
      categoryId,
      categoryName: selectedCat ? selectedCat.name : 'General',
      status,
      startTimeIso: new Date(startTime).toISOString(),
      endTimeIso: new Date(endTime).toISOString(),
      entryFeePoints,
      prizePoolPoints,
      maxParticipants: maxParticipants ? Number(maxParticipants) : undefined,
      minLevelRequired,
    });

    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm overflow-y-auto">
      <div className="relative w-full max-w-2xl bg-brand-dark-surface border border-brand-dark-border rounded-3xl shadow-2xl overflow-hidden my-8">
        {/* Header */}
        <div className="p-6 border-b border-brand-dark-border/60 flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-xl bg-brand-primary/10 border border-brand-primary/30 text-brand-primary flex items-center justify-center">
              <IconTrophy className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-lg font-bold text-white">
                {tournamentToEdit ? 'Configure Tournament Event' : 'Schedule New Tournament'}
              </h2>
              <p className="text-xs text-slate-400">
                Configure competition parameters, time windows, and prize allocations.
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-xl text-slate-400 hover:text-white hover:bg-white/5 transition"
          >
            <IconX className="w-5 h-5" />
          </button>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="p-6 space-y-5 max-h-[75vh] overflow-y-auto">
          {errors.length > 0 && (
            <div className="p-4 rounded-xl bg-rose-950/40 border border-rose-500/40 text-xs text-rose-300 space-y-1">
              <div className="flex items-center space-x-1.5 font-bold">
                <IconAlertCircle className="w-4 h-4 text-rose-400" />
                <span>Please correct the following:</span>
              </div>
              <ul className="list-disc list-inside pl-1 space-y-0.5">
                {errors.map((err, i) => (
                  <li key={i}>{err}</li>
                ))}
              </ul>
            </div>
          )}

          {/* Row 1: Title & Category */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <div className="sm:col-span-2">
              <label className="block text-xs font-semibold text-slate-300 mb-1">
                Tournament Title
              </label>
              <input
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="e.g. Science Sprint Championship"
                className="w-full px-3 py-2 rounded-xl bg-brand-dark-card border border-brand-dark-border text-white text-xs focus:outline-none focus:border-brand-primary"
                required
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">
                Trivia Category
              </label>
              <select
                value={categoryId}
                onChange={(e) => setCategoryId(e.target.value)}
                className="w-full px-3 py-2 rounded-xl bg-brand-dark-card border border-brand-dark-border text-white text-xs focus:outline-none focus:border-brand-primary cursor-pointer"
              >
                {categories.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* Description */}
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1">
              Description & Competition Rules
            </label>
            <textarea
              rows={2}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Detailed brief of competition scope and target audience..."
              className="w-full p-2.5 rounded-xl bg-brand-dark-card border border-brand-dark-border text-white text-xs focus:outline-none focus:border-brand-primary"
              required
            />
          </div>

          {/* Row 2: Time Window */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">
                Start Date & Time (UTC)
              </label>
              <input
                type="datetime-local"
                value={startTime}
                onChange={(e) => setStartTime(e.target.value)}
                className="w-full px-3 py-2 rounded-xl bg-brand-dark-card border border-brand-dark-border text-white text-xs focus:outline-none focus:border-brand-primary"
                required
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">
                End Date & Time (UTC)
              </label>
              <input
                type="datetime-local"
                value={endTime}
                onChange={(e) => setEndTime(e.target.value)}
                className="w-full px-3 py-2 rounded-xl bg-brand-dark-card border border-brand-dark-border text-white text-xs focus:outline-none focus:border-brand-primary"
                required
              />
            </div>
          </div>

          {/* Row 3: Economics & Requirements */}
          <div className="grid grid-cols-1 sm:grid-cols-4 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">
                Entry Fee (pts)
              </label>
              <input
                type="number"
                min="0"
                step="5"
                value={entryFeePoints}
                onChange={(e) => setEntryFeePoints(Number(e.target.value))}
                className="w-full px-3 py-2 rounded-xl bg-brand-dark-card border border-brand-dark-border text-white text-xs focus:outline-none focus:border-brand-primary"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">
                Total Prize Pool
              </label>
              <input
                type="number"
                min="100"
                step="100"
                value={prizePoolPoints}
                onChange={(e) => setPrizePoolPoints(Number(e.target.value))}
                className="w-full px-3 py-2 rounded-xl bg-brand-dark-card border border-brand-dark-border text-white text-xs focus:outline-none focus:border-brand-primary font-bold text-brand-gold"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">
                Max Players
              </label>
              <input
                type="number"
                min="10"
                placeholder="Unlimited"
                value={maxParticipants || ''}
                onChange={(e) =>
                  setMaxParticipants(e.target.value ? Number(e.target.value) : undefined)
                }
                className="w-full px-3 py-2 rounded-xl bg-brand-dark-card border border-brand-dark-border text-white text-xs focus:outline-none focus:border-brand-primary"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">
                Min Player Level
              </label>
              <input
                type="number"
                min="1"
                max="50"
                value={minLevelRequired}
                onChange={(e) => setMinLevelRequired(Number(e.target.value))}
                className="w-full px-3 py-2 rounded-xl bg-brand-dark-card border border-brand-dark-border text-white text-xs focus:outline-none focus:border-brand-primary"
              />
            </div>
          </div>

          {/* Prize Pool Allocation Card */}
          <div className="p-4 rounded-2xl bg-brand-dark-card border border-brand-dark-border/80 space-y-2">
            <div className="flex items-center justify-between text-xs">
              <span className="font-bold text-white flex items-center space-x-1.5">
                <IconSpark className="w-4 h-4 text-brand-gold" />
                <span>Authoritative Prize Distribution Scheme</span>
              </span>
              <span className="text-brand-gold font-mono font-bold">
                {prizePoolPoints.toLocaleString()} Total Points
              </span>
            </div>

            <div className="grid grid-cols-3 gap-2 pt-1 text-center text-xs">
              <div className="p-2.5 rounded-xl bg-amber-950/30 border border-amber-500/40">
                <p className="text-[10px] text-amber-300 font-bold uppercase">1st Place (50%)</p>
                <p className="text-sm font-bold text-white font-mono">{firstPrize.toLocaleString()} pts</p>
              </div>

              <div className="p-2.5 rounded-xl bg-slate-800 border border-slate-700">
                <p className="text-[10px] text-slate-300 font-bold uppercase">2nd Place (30%)</p>
                <p className="text-sm font-bold text-white font-mono">{secondPrize.toLocaleString()} pts</p>
              </div>

              <div className="p-2.5 rounded-xl bg-amber-900/20 border border-amber-800/40">
                <p className="text-[10px] text-amber-400 font-bold uppercase">3rd Place (20%)</p>
                <p className="text-sm font-bold text-white font-mono">{thirdPrize.toLocaleString()} pts</p>
              </div>
            </div>
          </div>

          {/* Status Override */}
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1">
              Event Status
            </label>
            <select
              value={status}
              onChange={(e) => setStatus(e.target.value as AdminTournamentStatus)}
              className="w-full px-3 py-2 rounded-xl bg-brand-dark-card border border-brand-dark-border text-white text-xs focus:outline-none focus:border-brand-primary cursor-pointer"
            >
              <option value="SCHEDULED">Scheduled (Visible to players as Upcoming)</option>
              <option value="DRAFT">Draft (Internal staging only)</option>
              <option value="LIVE">Live Now (Accepting player submissions)</option>
            </select>
          </div>

          {/* Modal Actions */}
          <div className="pt-4 border-t border-brand-dark-border/60 flex items-center justify-end space-x-3">
            <Button type="button" variant="outline" size="sm" onClick={onClose}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" size="sm">
              <IconCheck className="w-4 h-4 mr-1.5" />
              <span>{tournamentToEdit ? 'Save Changes' : 'Schedule Tournament'}</span>
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
