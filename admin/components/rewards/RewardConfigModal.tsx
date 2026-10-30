'use client';

import React, { useEffect, useState } from 'react';
import { AdminReward, RewardCategory } from '../../types/reward';
import { Button } from '../ui/Button';
import { IconAlertCircle, IconGift, IconSpark, IconX } from '../ui/Icons';

interface RewardConfigModalProps {
  isOpen: boolean;
  rewardToEdit?: AdminReward | null;
  onClose: () => void;
  onSave: (
    reward: Omit<AdminReward, 'id' | 'createdAt'> & {
      id?: string;
    }
  ) => void;
}

export function RewardConfigModal({
  isOpen,
  rewardToEdit,
  onClose,
  onSave,
}: RewardConfigModalProps) {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [pointsCost, setPointsCost] = useState(5000);
  const [stockCount, setStockCount] = useState(25);
  const [category, setCategory] = useState<RewardCategory>('GIFT_CARD');
  const [provider, setProvider] = useState('Amazon');
  const [minLevelRequired, setMinLevelRequired] = useState(1);
  const [terms, setTerms] = useState('');
  const [isActive, setIsActive] = useState(true);
  const [errors, setErrors] = useState<string[]>([]);

  useEffect(() => {
    if (rewardToEdit) {
      setTitle(rewardToEdit.title);
      setDescription(rewardToEdit.description);
      setPointsCost(rewardToEdit.pointsCost);
      setStockCount(rewardToEdit.stockCount);
      setCategory(rewardToEdit.category);
      setProvider(rewardToEdit.provider);
      setMinLevelRequired(rewardToEdit.minLevelRequired);
      setTerms(rewardToEdit.terms || '');
      setIsActive(rewardToEdit.isActive);
    } else {
      setTitle('');
      setDescription('');
      setPointsCost(5000);
      setStockCount(25);
      setCategory('GIFT_CARD');
      setProvider('Amazon');
      setMinLevelRequired(1);
      setTerms('');
      setIsActive(true);
    }
    setErrors([]);
  }, [rewardToEdit, isOpen]);

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const errs: string[] = [];

    if (!title.trim()) errs.push('Reward title is required.');
    if (!description.trim()) errs.push('Reward description is required.');
    if (pointsCost < 5000) errs.push('Points cost must be at least 5,000 points.');
    if (stockCount < 0) errs.push('Stock count cannot be negative.');
    if (!provider.trim()) errs.push('Provider name is required.');

    if (errs.length > 0) {
      setErrors(errs);
      return;
    }

    onSave({
      id: rewardToEdit?.id,
      title: title.trim(),
      description: description.trim(),
      pointsCost,
      stockCount,
      category,
      provider: provider.trim(),
      minLevelRequired,
      terms: terms.trim() || undefined,
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
            <IconGift className="w-6 h-6" />
          </div>
          <div>
            <h2 className="text-xl font-bold text-white tracking-tight">
              {rewardToEdit ? 'Edit Reward Item' : 'Add New Reward to Catalog'}
            </h2>
            <p className="text-xs text-slate-400">
              Configure inventory stock, points pricing, and redemption requirements
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
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1">
              Reward Title <span className="text-brand-primary">*</span>
            </label>
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="e.g. $10 Amazon Gift Card"
              className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-brand-dark-border text-xs text-white placeholder-slate-500 focus:outline-none focus:border-brand-primary transition-colors"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1">
              Description <span className="text-brand-primary">*</span>
            </label>
            <textarea
              rows={2}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="e.g. Digital gift card claim code redeemable on Amazon.com..."
              className="w-full px-3.5 py-2 rounded-xl bg-slate-900 border border-brand-dark-border text-xs text-white placeholder-slate-500 focus:outline-none focus:border-brand-primary transition-colors resize-none"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">
                Category Type
              </label>
              <select
                value={category}
                onChange={(e) => setCategory(e.target.value as RewardCategory)}
                className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-brand-dark-border text-xs text-white focus:outline-none focus:border-brand-primary transition-colors"
              >
                <option value="GIFT_CARD">Gift Card</option>
                <option value="CASHOUT">Cashout (PayPal, Direct Transfer)</option>
                <option value="DIGITAL_VOUCHER">Digital Voucher / Subscription</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">
                Provider / Brand <span className="text-brand-primary">*</span>
              </label>
              <input
                type="text"
                value={provider}
                onChange={(e) => setProvider(e.target.value)}
                placeholder="e.g. Amazon, Google Play, PayPal"
                className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-brand-dark-border text-xs text-white placeholder-slate-500 focus:outline-none focus:border-brand-primary transition-colors"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">
                Points Cost <span className="text-brand-primary">*</span>
              </label>
              <div className="relative">
                <input
                  type="number"
                  min={5000}
                  step={1000}
                  value={pointsCost}
                  onChange={(e) => setPointsCost(Math.max(1, parseInt(e.target.value) || 50))}
                  className="w-full pl-3.5 pr-12 py-2.5 rounded-xl bg-slate-900 border border-brand-dark-border text-xs text-white font-mono focus:outline-none focus:border-brand-primary transition-colors"
                />
                <span className="absolute right-3 top-1/2 -translate-y-1/2 text-xs font-bold text-amber-400 pointer-events-none">
                  pts
                </span>
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">
                Stock Quantity
              </label>
              <input
                type="number"
                min={0}
                value={stockCount}
                onChange={(e) => setStockCount(Math.max(0, parseInt(e.target.value) || 0))}
                className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-brand-dark-border text-xs text-white font-mono focus:outline-none focus:border-brand-primary transition-colors"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">
                Min Player Level
              </label>
              <input
                type="number"
                min={1}
                max={20}
                value={minLevelRequired}
                onChange={(e) => setMinLevelRequired(Math.max(1, parseInt(e.target.value) || 1))}
                className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-brand-dark-border text-xs text-white font-mono focus:outline-none focus:border-brand-primary transition-colors"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1">
              Terms & Delivery Instructions
            </label>
            <input
              type="text"
              value={terms}
              onChange={(e) => setTerms(e.target.value)}
              placeholder="e.g. Valid in US accounts. Delivered instantly to registered email."
              className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-brand-dark-border text-xs text-white placeholder-slate-500 focus:outline-none focus:border-brand-primary transition-colors"
            />
          </div>

          <div className="pt-2">
            <label className="flex items-center space-x-3 cursor-pointer">
              <input
                type="checkbox"
                checked={isActive}
                onChange={(e) => setIsActive(e.target.checked)}
                className="w-4 h-4 rounded text-brand-primary bg-slate-900 border-brand-dark-border focus:ring-brand-primary"
              />
              <div>
                <span className="text-xs font-semibold text-white">Active in store catalog</span>
                <p className="text-[11px] text-slate-400">
                  Visible to players on the Android app rewards screen.
                </p>
              </div>
            </label>
          </div>

          <div className="flex items-center justify-end space-x-3 pt-4 border-t border-brand-dark-border/60">
            <Button type="button" variant="outline" size="sm" onClick={onClose}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" size="sm">
              {rewardToEdit ? 'Save Changes' : 'Add Reward'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
