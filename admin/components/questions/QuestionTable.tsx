'use client';

import React, { useMemo, useState } from 'react';
import { AdminCategory, AdminQuestion, DifficultyLevel, QuestionStatus } from '../../types/question';
import {
  IconAlertCircle,
  IconChevronRight,
  IconEdit,
  IconEye,
  IconFilter,
  IconSearch,
  IconTrash,
} from '../ui/Icons';
import { Button } from '../ui/Button';

interface QuestionTableProps {
  questions: AdminQuestion[];
  categories: AdminCategory[];
  onEditQuestion: (question: AdminQuestion) => void;
  onPreviewQuestion: (question: AdminQuestion) => void;
  onDeleteQuestion: (questionId: string) => void;
}

export function QuestionTable({
  questions,
  categories,
  onEditQuestion,
  onPreviewQuestion,
  onDeleteQuestion,
}: QuestionTableProps) {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string>('ALL');
  const [selectedDifficulty, setSelectedDifficulty] = useState<string>('ALL');
  const [selectedStatus, setSelectedStatus] = useState<string>('ALL');
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 8;

  // Filtered dataset
  const filteredQuestions = useMemo(() => {
    return questions.filter((q) => {
      // Keyword search
      const matchesSearch =
        searchTerm === '' ||
        q.text.toLowerCase().includes(searchTerm.toLowerCase()) ||
        q.id.toLowerCase().includes(searchTerm.toLowerCase()) ||
        q.options.some((opt) => opt.toLowerCase().includes(searchTerm.toLowerCase()));

      // Category filter
      const matchesCategory =
        selectedCategory === 'ALL' || q.categoryId === selectedCategory;

      // Difficulty filter
      const matchesDifficulty =
        selectedDifficulty === 'ALL' || q.difficulty === selectedDifficulty;

      // Status filter
      const matchesStatus =
        selectedStatus === 'ALL' || q.status === selectedStatus;

      return matchesSearch && matchesCategory && matchesDifficulty && matchesStatus;
    });
  }, [questions, searchTerm, selectedCategory, selectedDifficulty, selectedStatus]);

  // Pagination calculation
  const totalPages = Math.max(1, Math.ceil(filteredQuestions.length / itemsPerPage));
  const paginatedQuestions = useMemo(() => {
    const startIdx = (currentPage - 1) * itemsPerPage;
    return filteredQuestions.slice(startIdx, startIdx + itemsPerPage);
  }, [filteredQuestions, currentPage, itemsPerPage]);

  const handleDifficultyBadge = (diff: DifficultyLevel) => {
    switch (diff) {
      case 'EASY':
        return (
          <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-teal-900/40 text-teal-300 border border-teal-500/40">
            EASY
          </span>
        );
      case 'MEDIUM':
        return (
          <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-900/40 text-amber-300 border border-amber-500/40">
            MEDIUM
          </span>
        );
      case 'HARD':
        return (
          <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-rose-900/40 text-rose-300 border border-rose-500/40">
            HARD
          </span>
        );
    }
  };

  const handleStatusBadge = (status: QuestionStatus) => {
    return status === 'ACTIVE' ? (
      <span className="inline-flex items-center space-x-1 px-2 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-950/40 text-emerald-300 border border-emerald-500/40">
        <span className="w-1.5 h-1.5 rounded-full bg-emerald-400" />
        <span>Active</span>
      </span>
    ) : (
      <span className="inline-flex items-center space-x-1 px-2 py-0.5 rounded-full text-[10px] font-semibold bg-slate-800 text-slate-300 border border-slate-700">
        <span className="w-1.5 h-1.5 rounded-full bg-slate-400" />
        <span>Draft</span>
      </span>
    );
  };

  return (
    <div className="space-y-4">
      {/* Search and Filters Bar */}
      <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-3 p-4 rounded-2xl bg-brand-dark-card border border-brand-dark-border">
        {/* Search Input */}
        <div className="relative flex-1">
          <IconSearch className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder="Search questions by keyword, ID, or option text..."
            value={searchTerm}
            onChange={(e) => {
              setSearchTerm(e.target.value);
              setCurrentPage(1);
            }}
            className="w-full pl-10 pr-4 py-2 rounded-xl bg-brand-dark-surface border border-brand-dark-border text-white placeholder-slate-500 text-xs focus:outline-none focus:border-brand-primary"
          />
        </div>

        {/* Filter Controls */}
        <div className="flex flex-wrap items-center gap-2">
          {/* Category Dropdown */}
          <select
            value={selectedCategory}
            onChange={(e) => {
              setSelectedCategory(e.target.value);
              setCurrentPage(1);
            }}
            className="px-3 py-2 rounded-xl bg-brand-dark-surface border border-brand-dark-border text-xs text-white focus:outline-none focus:border-brand-primary cursor-pointer"
          >
            <option value="ALL">All Categories</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>

          {/* Difficulty Dropdown */}
          <select
            value={selectedDifficulty}
            onChange={(e) => {
              setSelectedDifficulty(e.target.value);
              setCurrentPage(1);
            }}
            className="px-3 py-2 rounded-xl bg-brand-dark-surface border border-brand-dark-border text-xs text-white focus:outline-none focus:border-brand-primary cursor-pointer"
          >
            <option value="ALL">All Difficulties</option>
            <option value="EASY">Easy</option>
            <option value="MEDIUM">Medium</option>
            <option value="HARD">Hard</option>
          </select>

          {/* Status Dropdown */}
          <select
            value={selectedStatus}
            onChange={(e) => {
              setSelectedStatus(e.target.value);
              setCurrentPage(1);
            }}
            className="px-3 py-2 rounded-xl bg-brand-dark-surface border border-brand-dark-border text-xs text-white focus:outline-none focus:border-brand-primary cursor-pointer"
          >
            <option value="ALL">All Statuses</option>
            <option value="ACTIVE">Active</option>
            <option value="DRAFT">Draft</option>
          </select>

          {/* Reset Filters button */}
          {(searchTerm || selectedCategory !== 'ALL' || selectedDifficulty !== 'ALL' || selectedStatus !== 'ALL') && (
            <button
              onClick={() => {
                setSearchTerm('');
                setSelectedCategory('ALL');
                setSelectedDifficulty('ALL');
                setSelectedStatus('ALL');
                setCurrentPage(1);
              }}
              className="text-xs text-brand-primary hover:underline px-2"
            >
              Reset
            </button>
          )}
        </div>
      </div>

      {/* Questions Data Table */}
      <div className="rounded-2xl bg-brand-dark-card border border-brand-dark-border overflow-hidden shadow-xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-xs">
            <thead>
              <tr className="bg-brand-dark-surface/80 border-b border-brand-dark-border/80 text-[11px] font-bold uppercase tracking-wider text-slate-400">
                <th className="py-3.5 px-4">ID</th>
                <th className="py-3.5 px-4 min-w-[280px]">Question Statement</th>
                <th className="py-3.5 px-4">Category</th>
                <th className="py-3.5 px-4">Difficulty</th>
                <th className="py-3.5 px-4">Timer / Pts</th>
                <th className="py-3.5 px-4">Status</th>
                <th className="py-3.5 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-brand-dark-border/40">
              {paginatedQuestions.length === 0 ? (
                <tr>
                  <td colSpan={7} className="py-12 text-center text-slate-400">
                    <div className="flex flex-col items-center justify-center space-y-2">
                      <IconAlertCircle className="w-8 h-8 text-slate-500" />
                      <p className="text-sm font-semibold text-white">No questions found</p>
                      <p className="text-xs text-slate-500">
                        Try adjusting your search criteria or add a new question.
                      </p>
                    </div>
                  </td>
                </tr>
              ) : (
                paginatedQuestions.map((q) => (
                  <tr
                    key={q.id}
                    className="hover:bg-white/5 transition-colors group"
                  >
                    <td className="py-3.5 px-4 font-mono font-medium text-slate-400 text-[11px]">
                      {q.id}
                    </td>

                    <td className="py-3.5 px-4">
                      <p className="font-medium text-white line-clamp-2 max-w-md group-hover:text-brand-primary transition-colors">
                        {q.text}
                      </p>
                      <p className="text-[10px] text-slate-500 mt-0.5 line-clamp-1">
                        Answer: <span className="text-teal-400 font-semibold">{q.options[q.correctOptionIndex]}</span>
                      </p>
                    </td>

                    <td className="py-3.5 px-4 whitespace-nowrap text-slate-300 font-medium">
                      {q.categoryName}
                    </td>

                    <td className="py-3.5 px-4 whitespace-nowrap">
                      {handleDifficultyBadge(q.difficulty)}
                    </td>

                    <td className="py-3.5 px-4 whitespace-nowrap font-mono text-slate-400">
                      <span>{q.timeLimitSeconds}s</span>
                      <span className="text-slate-600 mx-1">•</span>
                      <span className="text-brand-gold font-semibold">+{q.pointsReward} pts</span>
                    </td>

                    <td className="py-3.5 px-4 whitespace-nowrap">
                      {handleStatusBadge(q.status)}
                    </td>

                    <td className="py-3.5 px-4 whitespace-nowrap text-right">
                      <div className="inline-flex items-center space-x-1">
                        <button
                          onClick={() => onPreviewQuestion(q)}
                          className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-white/10 transition"
                          title="Preview Question"
                        >
                          <IconEye className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => onEditQuestion(q)}
                          className="p-1.5 rounded-lg text-slate-400 hover:text-brand-primary hover:bg-brand-primary/10 transition"
                          title="Edit Question"
                        >
                          <IconEdit className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => onDeleteQuestion(q.id)}
                          className="p-1.5 rounded-lg text-slate-400 hover:text-rose-400 hover:bg-rose-950/20 transition"
                          title="Delete Question"
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

        {/* Pagination & Summary Footer */}
        <div className="p-4 bg-brand-dark-surface/60 border-t border-brand-dark-border/60 flex flex-col sm:flex-row items-center justify-between gap-3 text-xs text-slate-400">
          <div>
            Showing{' '}
            <span className="font-semibold text-white">
              {filteredQuestions.length === 0 ? 0 : (currentPage - 1) * itemsPerPage + 1}
            </span>{' '}
            to{' '}
            <span className="font-semibold text-white">
              {Math.min(currentPage * itemsPerPage, filteredQuestions.length)}
            </span>{' '}
            of <span className="font-semibold text-white">{filteredQuestions.length}</span> questions
          </div>

          <div className="flex items-center space-x-2">
            <Button
              variant="outline"
              size="sm"
              disabled={currentPage <= 1}
              onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
            >
              Previous
            </Button>
            <span className="px-2 text-xs font-semibold text-white">
              Page {currentPage} of {totalPages}
            </span>
            <Button
              variant="outline"
              size="sm"
              disabled={currentPage >= totalPages}
              onClick={() => setCurrentPage((p) => Math.min(totalPages, p + 1))}
            >
              Next
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}
