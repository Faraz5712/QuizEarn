'use client';

import React, { useState } from 'react';
import { AdminCategory } from '../../types/question';
import { Button } from '../ui/Button';
import { IconCheck, IconPlus, IconX } from '../ui/Icons';

interface CategoryTaxonomyTableProps {
  categories: AdminCategory[];
  onToggleStatus: (categoryId: string) => void;
  onUpdateCategory: (category: AdminCategory) => void;
  onAddCategory: (category: Omit<AdminCategory, 'id' | 'questionCount'>) => void;
}

export function CategoryTaxonomyTable({
  categories,
  onToggleStatus,
  onUpdateCategory,
  onAddCategory,
}: CategoryTaxonomyTableProps) {
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [newCatName, setNewCatName] = useState('');
  const [newCatSlug, setNewCatSlug] = useState('');
  const [newCatColor, setNewCatColor] = useState('#5B5FEF');
  const [newCatDesc, setNewCatDesc] = useState('');

  const handleCreateCategory = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newCatName.trim()) return;

    onAddCategory({
      name: newCatName.trim(),
      slug: newCatSlug.trim() || newCatName.toLowerCase().replace(/\s+/g, '-'),
      iconName: 'quiz',
      colorHex: newCatColor,
      displayOrder: categories.length + 1,
      isActive: true,
      description: newCatDesc.trim(),
    });

    setNewCatName('');
    setNewCatSlug('');
    setNewCatDesc('');
    setIsAddModalOpen(false);
  };

  return (
    <div className="space-y-4">
      {/* Category Overview Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 p-4 rounded-2xl bg-brand-dark-card border border-brand-dark-border">
        <div>
          <h3 className="text-sm font-bold text-white">Trivia Topic Categories</h3>
          <p className="text-xs text-slate-400">
            Configure the 6 core quiz domains, active status visibility, and question allocation.
          </p>
        </div>
        <Button variant="primary" size="sm" onClick={() => setIsAddModalOpen(true)}>
          <IconPlus className="w-4 h-4 mr-1.5" />
          <span>New Category</span>
        </Button>
      </div>

      {/* Category Table */}
      <div className="rounded-2xl bg-brand-dark-card border border-brand-dark-border overflow-hidden shadow-xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-xs">
            <thead>
              <tr className="bg-brand-dark-surface/80 border-b border-brand-dark-border/80 text-[11px] font-bold uppercase tracking-wider text-slate-400">
                <th className="py-3.5 px-4">Order</th>
                <th className="py-3.5 px-4">Category Name</th>
                <th className="py-3.5 px-4">Slug Identifier</th>
                <th className="py-3.5 px-4">Accent Color</th>
                <th className="py-3.5 px-4">Bank Size</th>
                <th className="py-3.5 px-4">App Status</th>
                <th className="py-3.5 px-4 text-right">Visibility Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-brand-dark-border/40">
              {categories.map((cat) => (
                <tr key={cat.id} className="hover:bg-white/5 transition-colors">
                  <td className="py-3.5 px-4 font-mono font-bold text-slate-400">
                    #{cat.displayOrder}
                  </td>

                  <td className="py-3.5 px-4">
                    <div className="flex items-center space-x-2.5">
                      <span
                        className="w-3.5 h-3.5 rounded-full flex-shrink-0"
                        style={{ backgroundColor: cat.colorHex }}
                      />
                      <div>
                        <p className="font-bold text-white">{cat.name}</p>
                        {cat.description && (
                          <p className="text-[10px] text-slate-400 line-clamp-1">
                            {cat.description}
                          </p>
                        )}
                      </div>
                    </div>
                  </td>

                  <td className="py-3.5 px-4 font-mono text-slate-400 text-[11px]">
                    {cat.slug}
                  </td>

                  <td className="py-3.5 px-4 font-mono text-[11px]">
                    <span
                      className="px-2 py-0.5 rounded text-[10px] font-semibold border"
                      style={{
                        backgroundColor: `${cat.colorHex}20`,
                        color: cat.colorHex,
                        borderColor: `${cat.colorHex}50`,
                      }}
                    >
                      {cat.colorHex}
                    </span>
                  </td>

                  <td className="py-3.5 px-4 font-semibold text-white">
                    <span className="px-2 py-0.5 rounded-full text-[11px] bg-white/10 text-white font-mono">
                      {cat.questionCount} questions
                    </span>
                  </td>

                  <td className="py-3.5 px-4 whitespace-nowrap">
                    {cat.isActive ? (
                      <span className="inline-flex items-center space-x-1 px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-950/40 text-emerald-300 border border-emerald-500/40">
                        <span className="w-1.5 h-1.5 rounded-full bg-emerald-400" />
                        <span>Enabled in App</span>
                      </span>
                    ) : (
                      <span className="inline-flex items-center space-x-1 px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-rose-950/40 text-rose-300 border border-rose-500/40">
                        <span className="w-1.5 h-1.5 rounded-full bg-rose-400" />
                        <span>Disabled</span>
                      </span>
                    )}
                  </td>

                  <td className="py-3.5 px-4 text-right whitespace-nowrap">
                    <button
                      onClick={() => onToggleStatus(cat.id)}
                      className={`px-3 py-1 rounded-xl text-xs font-semibold transition ${
                        cat.isActive
                          ? 'bg-rose-950/30 text-rose-300 hover:bg-rose-900/40 border border-rose-500/30'
                          : 'bg-emerald-950/30 text-emerald-300 hover:bg-emerald-900/40 border border-emerald-500/30'
                      }`}
                    >
                      {cat.isActive ? 'Disable Category' : 'Enable Category'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Add Category Modal */}
      {isAddModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm">
          <div className="w-full max-w-md bg-brand-dark-surface border border-brand-dark-border rounded-3xl p-6 shadow-2xl">
            <div className="flex items-center justify-between pb-4 border-b border-brand-dark-border/60 mb-4">
              <h3 className="text-base font-bold text-white">Create New Category</h3>
              <button
                onClick={() => setIsAddModalOpen(false)}
                className="p-1 rounded-lg text-slate-400 hover:text-white"
              >
                <IconX className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleCreateCategory} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">
                  Category Name
                </label>
                <input
                  type="text"
                  value={newCatName}
                  onChange={(e) => setNewCatName(e.target.value)}
                  placeholder="e.g. World Literature"
                  className="w-full px-3 py-2 rounded-xl bg-brand-dark-card border border-brand-dark-border text-white text-xs focus:outline-none focus:border-brand-primary"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">
                  Slug / URL Identifier
                </label>
                <input
                  type="text"
                  value={newCatSlug}
                  onChange={(e) => setNewCatSlug(e.target.value)}
                  placeholder="e.g. world-literature"
                  className="w-full px-3 py-2 rounded-xl bg-brand-dark-card border border-brand-dark-border text-white text-xs focus:outline-none focus:border-brand-primary"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">
                  Accent Color Hex
                </label>
                <div className="flex items-center space-x-2">
                  <input
                    type="color"
                    value={newCatColor}
                    onChange={(e) => setNewCatColor(e.target.value)}
                    className="w-8 h-8 rounded-lg cursor-pointer bg-transparent border-0"
                  />
                  <input
                    type="text"
                    value={newCatColor}
                    onChange={(e) => setNewCatColor(e.target.value)}
                    className="w-full px-3 py-1.5 rounded-xl bg-brand-dark-card border border-brand-dark-border text-white font-mono text-xs"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">
                  Description
                </label>
                <textarea
                  rows={2}
                  value={newCatDesc}
                  onChange={(e) => setNewCatDesc(e.target.value)}
                  placeholder="Brief description of the subject area..."
                  className="w-full p-2.5 rounded-xl bg-brand-dark-card border border-brand-dark-border text-white text-xs focus:outline-none focus:border-brand-primary"
                />
              </div>

              <div className="pt-3 border-t border-brand-dark-border/60 flex items-center justify-end space-x-2">
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() => setIsAddModalOpen(false)}
                >
                  Cancel
                </Button>
                <Button type="submit" variant="primary" size="sm">
                  <IconCheck className="w-4 h-4 mr-1.5" />
                  <span>Create Category</span>
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
