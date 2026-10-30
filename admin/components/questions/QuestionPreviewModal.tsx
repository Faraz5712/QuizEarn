'use client';

import React, { useState } from 'react';
import { AdminQuestion } from '../../types/question';
import { Button } from '../ui/Button';
import { IconCheck, IconClock, IconSpark, IconX } from '../ui/Icons';

interface QuestionPreviewModalProps {
  question: AdminQuestion | null;
  onClose: () => void;
}

export function QuestionPreviewModal({ question, onClose }: QuestionPreviewModalProps) {
  const [selectedOption, setSelectedOption] = useState<number | null>(null);
  const [showExplanation, setShowExplanation] = useState(false);

  if (!question) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm">
      <div className="relative w-full max-w-lg bg-brand-dark-surface border border-brand-dark-border rounded-3xl shadow-2xl overflow-hidden">
        {/* Header */}
        <div className="p-5 border-b border-brand-dark-border/60 flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <span className="w-2.5 h-2.5 rounded-full bg-brand-mint" />
            <h3 className="text-sm font-bold text-white">Player Mobile Preview</h3>
            <span className="text-[10px] text-slate-500 font-mono">[{question.id}]</span>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-slate-400 hover:text-white hover:bg-white/5 transition"
          >
            <IconX className="w-5 h-5" />
          </button>
        </div>

        {/* Mobile Device Viewport */}
        <div className="p-6 space-y-5 bg-brand-dark-bg/60">
          {/* Top Info Bar */}
          <div className="flex items-center justify-between text-xs">
            <span className="px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-brand-primary/20 text-brand-primary border border-brand-primary/30">
              {question.categoryName}
            </span>

            <div className="flex items-center space-x-2">
              <span className="flex items-center space-x-1 text-slate-400 font-mono text-xs">
                <IconClock className="w-3.5 h-3.5 text-brand-gold" />
                <span>{question.timeLimitSeconds}s</span>
              </span>
              <span className="flex items-center space-x-1 text-brand-gold font-bold text-xs">
                <IconSpark className="w-3.5 h-3.5" />
                <span>+{question.pointsReward} pts</span>
              </span>
            </div>
          </div>

          {/* Question Statement */}
          <div className="p-4 rounded-2xl bg-brand-dark-card border border-brand-dark-border/80">
            <p className="text-sm sm:text-base font-bold text-white leading-relaxed">
              {question.text}
            </p>
          </div>

          {/* Option Choices */}
          <div className="space-y-2.5">
            {question.options.map((option, idx) => {
              const isSelected = selectedOption === idx;
              const isCorrect = showExplanation && idx === question.correctOptionIndex;
              const isWrongSelected = showExplanation && isSelected && !isCorrect;

              let style = 'bg-brand-dark-card border-brand-dark-border text-slate-200 hover:border-slate-500';
              if (isCorrect) {
                style = 'bg-teal-950/40 border-teal-500 text-teal-200 shadow-md shadow-teal-900/20';
              } else if (isWrongSelected) {
                style = 'bg-rose-950/40 border-rose-500 text-rose-200';
              } else if (isSelected) {
                style = 'bg-brand-primary/20 border-brand-primary text-white';
              }

              const letters = ['A', 'B', 'C', 'D'];

              return (
                <button
                  key={idx}
                  onClick={() => setSelectedOption(idx)}
                  className={`w-full p-3.5 rounded-xl border text-left text-xs sm:text-sm font-medium flex items-center justify-between transition-all ${style}`}
                >
                  <div className="flex items-center space-x-3">
                    <span className="w-6 h-6 rounded-lg bg-white/10 font-bold text-xs flex items-center justify-center flex-shrink-0">
                      {letters[idx]}
                    </span>
                    <span>{option}</span>
                  </div>

                  {isCorrect && (
                    <span className="flex items-center space-x-1 text-teal-400 font-bold text-xs">
                      <IconCheck className="w-4 h-4" />
                      <span>Correct</span>
                    </span>
                  )}
                </button>
              );
            })}
          </div>

          {/* Explanation Reveal Box */}
          {showExplanation ? (
            <div className="p-4 rounded-2xl bg-teal-950/30 border border-teal-500/40 space-y-1">
              <p className="text-xs font-bold text-teal-300">Authoritative Explanation:</p>
              <p className="text-xs text-slate-300 leading-relaxed">{question.explanation}</p>
            </div>
          ) : (
            <button
              onClick={() => setShowExplanation(true)}
              className="w-full text-center text-xs text-brand-primary hover:underline py-1 font-semibold"
            >
              Reveal Answer Key & Explanation &rarr;
            </button>
          )}
        </div>

        {/* Footer */}
        <div className="p-4 bg-brand-dark-surface border-t border-brand-dark-border/60 flex items-center justify-between">
          <span className="text-[11px] text-slate-500">
            Difficulty: <strong className="text-white">{question.difficulty}</strong> • Status:{' '}
            <strong className="text-white">{question.status}</strong>
          </span>
          <Button variant="outline" size="sm" onClick={onClose}>
            Close Preview
          </Button>
        </div>
      </div>
    </div>
  );
}
