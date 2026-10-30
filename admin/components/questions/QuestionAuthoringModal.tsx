'use client';

import React, { useEffect, useState } from 'react';
import { AdminCategory, AdminQuestion, DifficultyLevel, QuestionStatus } from '../../types/question';
import { Button } from '../ui/Button';
import { IconAlertCircle, IconCheck, IconX } from '../ui/Icons';

interface QuestionAuthoringModalProps {
  isOpen: boolean;
  questionToEdit?: AdminQuestion | null;
  categories: AdminCategory[];
  onClose: () => void;
  onSave: (question: Omit<AdminQuestion, 'id' | 'createdAt'> & { id?: string }) => void;
}

export function QuestionAuthoringModal({
  isOpen,
  questionToEdit,
  categories,
  onClose,
  onSave,
}: QuestionAuthoringModalProps) {
  const [categoryId, setCategoryId] = useState(categories[0]?.id || 'cat_science');
  const [text, setText] = useState('');
  const [optionA, setOptionA] = useState('');
  const [optionB, setOptionB] = useState('');
  const [optionC, setOptionC] = useState('');
  const [optionD, setOptionD] = useState('');
  const [correctOptionIndex, setCorrectOptionIndex] = useState(0);
  const [explanation, setExplanation] = useState('');
  const [difficulty, setDifficulty] = useState<DifficultyLevel>('MEDIUM');
  const [timeLimitSeconds, setTimeLimitSeconds] = useState(15);
  const [pointsReward, setPointsReward] = useState(10);
  const [status, setStatus] = useState<QuestionStatus>('ACTIVE');
  const [errors, setErrors] = useState<string[]>([]);

  useEffect(() => {
    if (questionToEdit) {
      setCategoryId(questionToEdit.categoryId);
      setText(questionToEdit.text);
      setOptionA(questionToEdit.options[0] || '');
      setOptionB(questionToEdit.options[1] || '');
      setOptionC(questionToEdit.options[2] || '');
      setOptionD(questionToEdit.options[3] || '');
      setCorrectOptionIndex(questionToEdit.correctOptionIndex);
      setExplanation(questionToEdit.explanation);
      setDifficulty(questionToEdit.difficulty);
      setTimeLimitSeconds(questionToEdit.timeLimitSeconds);
      setPointsReward(questionToEdit.pointsReward);
      setStatus(questionToEdit.status);
    } else {
      // Defaults for new question
      setCategoryId(categories[0]?.id || 'cat_science');
      setText('');
      setOptionA('');
      setOptionB('');
      setOptionC('');
      setOptionD('');
      setCorrectOptionIndex(0);
      setExplanation('');
      setDifficulty('MEDIUM');
      setTimeLimitSeconds(15);
      setPointsReward(10);
      setStatus('ACTIVE');
    }
    setErrors([]);
  }, [questionToEdit, categories, isOpen]);

  if (!isOpen) return null;

  const validateForm = (): boolean => {
    const errs: string[] = [];

    if (!text.trim() || text.trim().length < 8) {
      errs.push('Question statement must be at least 8 characters long.');
    }

    if (!optionA.trim() || !optionB.trim() || !optionC.trim() || !optionD.trim()) {
      errs.push('All 4 answer options (A, B, C, D) are required.');
    } else {
      const unique = new Set([
        optionA.trim().toLowerCase(),
        optionB.trim().toLowerCase(),
        optionC.trim().toLowerCase(),
        optionD.trim().toLowerCase(),
      ]);
      if (unique.size < 4) {
        errs.push('All 4 options must be distinct from each other.');
      }
    }

    if (!explanation.trim() || explanation.trim().length < 10) {
      errs.push('Educational explanation must be at least 10 characters long.');
    }

    if (timeLimitSeconds < 10 || timeLimitSeconds > 30) {
      errs.push('Time limit must be between 10 and 30 seconds.');
    }

    setErrors(errs);
    return errs.length === 0;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) return;

    const selectedCat = categories.find((c) => c.id === categoryId);

    onSave({
      id: questionToEdit?.id,
      categoryId,
      categoryName: selectedCat ? selectedCat.name : 'General',
      text: text.trim(),
      options: [optionA.trim(), optionB.trim(), optionC.trim(), optionD.trim()],
      correctOptionIndex,
      explanation: explanation.trim(),
      difficulty,
      timeLimitSeconds,
      pointsReward,
      status,
    });

    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm overflow-y-auto">
      <div className="relative w-full max-w-2xl bg-brand-dark-surface border border-brand-dark-border rounded-3xl shadow-2xl overflow-hidden my-8">
        {/* Header */}
        <div className="p-6 border-b border-brand-dark-border/60 flex items-center justify-between">
          <div>
            <h2 className="text-lg font-bold text-white">
              {questionToEdit ? 'Edit Trivia Question' : 'Create Trivia Question'}
            </h2>
            <p className="text-xs text-slate-400 mt-0.5">
              Server-authoritative question authoring and answer key assignment.
            </p>
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
          {/* Validation Errors */}
          {errors.length > 0 && (
            <div className="p-4 rounded-xl bg-rose-950/40 border border-rose-500/40 text-xs text-rose-300 space-y-1">
              <div className="flex items-center space-x-1.5 font-bold">
                <IconAlertCircle className="w-4 h-4 text-rose-400" />
                <span>Please correct the following errors:</span>
              </div>
              <ul className="list-disc list-inside pl-1 space-y-0.5">
                {errors.map((err, i) => (
                  <li key={i}>{err}</li>
                ))}
              </ul>
            </div>
          )}

          {/* Row 1: Category, Difficulty & Status */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                Category
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

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                Difficulty
              </label>
              <select
                value={difficulty}
                onChange={(e) => setDifficulty(e.target.value as DifficultyLevel)}
                className="w-full px-3 py-2 rounded-xl bg-brand-dark-card border border-brand-dark-border text-white text-xs focus:outline-none focus:border-brand-primary cursor-pointer"
              >
                <option value="EASY">Easy</option>
                <option value="MEDIUM">Medium</option>
                <option value="HARD">Hard</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                Status
              </label>
              <select
                value={status}
                onChange={(e) => setStatus(e.target.value as QuestionStatus)}
                className="w-full px-3 py-2 rounded-xl bg-brand-dark-card border border-brand-dark-border text-white text-xs focus:outline-none focus:border-brand-primary cursor-pointer"
              >
                <option value="ACTIVE">Active (Live in Quizzes)</option>
                <option value="DRAFT">Draft (Staged)</option>
              </select>
            </div>
          </div>

          {/* Question Statement */}
          <div>
            <div className="flex items-center justify-between mb-1.5">
              <label className="block text-xs font-semibold text-slate-300">
                Question Statement
              </label>
              <span className="text-[11px] text-slate-500 font-mono">
                {text.length} characters
              </span>
            </div>
            <textarea
              rows={3}
              value={text}
              onChange={(e) => setText(e.target.value)}
              placeholder="e.g. Which planet in our solar system has the most moons?"
              className="w-full p-3 rounded-xl bg-brand-dark-card border border-brand-dark-border text-white placeholder-slate-500 text-xs focus:outline-none focus:border-brand-primary transition"
              required
            />
          </div>

          {/* 4 Options with Correct Answer Selector */}
          <div>
            <div className="flex items-center justify-between mb-2">
              <label className="block text-xs font-semibold text-slate-300">
                Answer Options & Authoritative Answer Key
              </label>
              <span className="text-[11px] text-brand-mint">
                Select the radio button for the correct answer
              </span>
            </div>

            <div className="space-y-2.5">
              {[
                { label: 'A', value: optionA, setter: setOptionA, index: 0 },
                { label: 'B', value: optionB, setter: setOptionB, index: 1 },
                { label: 'C', value: optionC, setter: setOptionC, index: 2 },
                { label: 'D', value: optionD, setter: setOptionD, index: 3 },
              ].map((opt) => (
                <div
                  key={opt.label}
                  className={`flex items-center space-x-3 p-2.5 rounded-xl border transition-all ${
                    correctOptionIndex === opt.index
                      ? 'bg-teal-950/30 border-teal-500/50'
                      : 'bg-brand-dark-card border-brand-dark-border'
                  }`}
                >
                  <label className="flex items-center space-x-2 cursor-pointer flex-shrink-0">
                    <input
                      type="radio"
                      name="correctOption"
                      checked={correctOptionIndex === opt.index}
                      onChange={() => setCorrectOptionIndex(opt.index)}
                      className="w-4 h-4 text-brand-primary focus:ring-brand-primary accent-brand-mint"
                    />
                    <span
                      className={`w-6 h-6 rounded-lg text-xs font-bold flex items-center justify-center ${
                        correctOptionIndex === opt.index
                          ? 'bg-brand-mint text-brand-dark-bg'
                          : 'bg-white/10 text-slate-300'
                      }`}
                    >
                      {opt.label}
                    </span>
                  </label>

                  <input
                    type="text"
                    value={opt.value}
                    onChange={(e) => opt.setter(e.target.value)}
                    placeholder={`Option ${opt.label} text`}
                    className="flex-1 bg-transparent border-0 text-white placeholder-slate-500 text-xs focus:outline-none focus:ring-0"
                    required
                  />

                  {correctOptionIndex === opt.index && (
                    <span className="text-[10px] font-bold text-teal-300 bg-teal-900/40 px-2 py-0.5 rounded-md flex-shrink-0">
                      Correct Key
                    </span>
                  )}
                </div>
              ))}
            </div>
          </div>

          {/* Educational Explanation */}
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5">
              Educational Explanation
            </label>
            <textarea
              rows={2}
              value={explanation}
              onChange={(e) => setExplanation(e.target.value)}
              placeholder="Explanation revealed to players after submitting answers..."
              className="w-full p-3 rounded-xl bg-brand-dark-card border border-brand-dark-border text-white placeholder-slate-500 text-xs focus:outline-none focus:border-brand-primary transition"
              required
            />
          </div>

          {/* Row: Timer & Points Reward */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-2">
            <div>
              <div className="flex items-center justify-between mb-1">
                <label className="block text-xs font-semibold text-slate-300">
                  Timer Limit ({timeLimitSeconds} seconds)
                </label>
              </div>
              <input
                type="range"
                min="10"
                max="30"
                step="5"
                value={timeLimitSeconds}
                onChange={(e) => setTimeLimitSeconds(Number(e.target.value))}
                className="w-full accent-brand-primary"
              />
              <div className="flex justify-between text-[10px] text-slate-500 mt-1">
                <span>10s (Fast)</span>
                <span>15s (Default)</span>
                <span>30s (Complex)</span>
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                Points Reward
              </label>
              <input
                type="number"
                min="5"
                max="50"
                value={pointsReward}
                onChange={(e) => setPointsReward(Number(e.target.value))}
                className="w-full px-3 py-2 rounded-xl bg-brand-dark-card border border-brand-dark-border text-white text-xs focus:outline-none focus:border-brand-primary"
              />
            </div>
          </div>

          {/* Modal Actions */}
          <div className="pt-4 border-t border-brand-dark-border/60 flex items-center justify-end space-x-3">
            <Button type="button" variant="outline" size="sm" onClick={onClose}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" size="sm">
              <IconCheck className="w-4 h-4 mr-1.5" />
              <span>{questionToEdit ? 'Save Changes' : 'Create Question'}</span>
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
