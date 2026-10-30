'use client';

import React, { useEffect, useState } from 'react';
import { AdminReward } from '../../types/reward';
import { Button } from '../ui/Button';
import { IconGift, IconX } from '../ui/Icons';

interface StockAdjustModalProps {
  isOpen: boolean;
  reward: AdminReward | null;
  onClose: () => void;
  onSave: (rewardId: string, newStock: number, note: string) => void;
}

export function StockAdjustModal({
  isOpen,
  reward,
  onClose,
  onSave,
}: StockAdjustModalProps) {
  const [stock, setStock] = useState(0);
  const [note, setNote] = useState('');

  useEffect(() => {
    if (reward) {
      setStock(reward.stockCount);
      setNote('');
    }
  }, [reward, isOpen]);

  if (!isOpen || !reward) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSave(reward.id, stock, note.trim());
    onClose();
  };

  const delta = stock - reward.stockCount;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="relative w-full max-w-md bg-brand-dark-card border border-brand-dark-border rounded-3xl shadow-2xl p-6 animate-in fade-in zoom-in duration-200">
        <button
          onClick={onClose}
          className="absolute top-6 right-6 text-slate-400 hover:text-white transition-colors"
          aria-label="Close modal"
        >
          <IconX className="w-5 h-5" />
        </button>

        <div className="flex items-center space-x-3 mb-5">
          <div className="p-3 bg-teal-500/10 border border-teal-500/30 rounded-2xl text-teal-400">
            <IconGift className="w-6 h-6" />
          </div>
          <div>
            <h2 className="text-lg font-bold text-white tracking-tight">Adjust Inventory Stock</h2>
            <p className="text-xs text-slate-400 truncate max-w-[240px]">{reward.title}</p>
          </div>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="p-3 rounded-2xl bg-slate-900 border border-brand-dark-border flex items-center justify-between text-xs">
            <span className="text-slate-400">Current Stock:</span>
            <span className="font-mono font-bold text-white text-sm">{reward.stockCount} units</span>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5">
              New Stock Count
            </label>
            <div className="flex items-center space-x-2">
              <button
                type="button"
                onClick={() => setStock((prev) => Math.max(0, prev - 5))}
                className="px-3 py-2 bg-slate-800 hover:bg-slate-700 text-white rounded-xl text-xs font-bold transition-colors"
              >
                -5
              </button>
              <button
                type="button"
                onClick={() => setStock((prev) => Math.max(0, prev - 1))}
                className="px-3 py-2 bg-slate-800 hover:bg-slate-700 text-white rounded-xl text-xs font-bold transition-colors"
              >
                -1
              </button>
              <input
                type="number"
                min={0}
                value={stock}
                onChange={(e) => setStock(Math.max(0, parseInt(e.target.value) || 0))}
                className="flex-1 px-3 py-2 rounded-xl bg-slate-900 border border-brand-dark-border text-center text-sm text-white font-mono font-bold focus:outline-none focus:border-brand-primary"
              />
              <button
                type="button"
                onClick={() => setStock((prev) => prev + 1)}
                className="px-3 py-2 bg-slate-800 hover:bg-slate-700 text-white rounded-xl text-xs font-bold transition-colors"
              >
                +1
              </button>
              <button
                type="button"
                onClick={() => setStock((prev) => prev + 5)}
                className="px-3 py-2 bg-slate-800 hover:bg-slate-700 text-white rounded-xl text-xs font-bold transition-colors"
              >
                +5
              </button>
            </div>
            {delta !== 0 && (
              <p className="text-[11px] mt-1.5 text-right font-mono">
                Adjustment:{' '}
                <span className={delta > 0 ? 'text-teal-400' : 'text-rose-400'}>
                  {delta > 0 ? `+${delta}` : delta} units
                </span>
              </p>
            )}
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5">
              Adjustment Note / Source (Optional)
            </label>
            <input
              type="text"
              value={note}
              onChange={(e) => setNote(e.target.value)}
              placeholder="e.g. Bulk voucher restock from Amazon vendor"
              className="w-full px-3 py-2 rounded-xl bg-slate-900 border border-brand-dark-border text-xs text-white placeholder-slate-500 focus:outline-none focus:border-brand-primary"
            />
          </div>

          <div className="flex items-center justify-end space-x-3 pt-3 border-t border-brand-dark-border/60">
            <Button type="button" variant="outline" size="sm" onClick={onClose}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" size="sm">
              Confirm Stock
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
