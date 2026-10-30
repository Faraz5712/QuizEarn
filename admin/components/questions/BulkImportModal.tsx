'use client';

import React, { useState } from 'react';
import { AdminCategory, AdminQuestion, BulkImportResult } from '../../types/question';
import { parseAndValidateBulkQuestions, SAMPLE_CSV_TEMPLATE, SAMPLE_JSON_TEMPLATE } from '../../lib/csv-parser';
import { Button } from '../ui/Button';
import {
  IconAlertCircle,
  IconCheck,
  IconDownload,
  IconUpload,
  IconX,
} from '../ui/Icons';

interface BulkImportModalProps {
  isOpen: boolean;
  categories: AdminCategory[];
  onClose: () => void;
  onCommitImport: (questions: Omit<AdminQuestion, 'id' | 'createdAt'>[]) => void;
}

export function BulkImportModal({
  isOpen,
  categories,
  onClose,
  onCommitImport,
}: BulkImportModalProps) {
  const [inputText, setInputText] = useState('');
  const [validationResult, setValidationResult] = useState<BulkImportResult | null>(null);
  const [hasValidated, setHasValidated] = useState(false);

  if (!isOpen) return null;

  const handleValidate = () => {
    const result = parseAndValidateBulkQuestions(inputText, categories);
    setValidationResult(result);
    setHasValidated(true);
  };

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (event) => {
      const content = event.target?.result as string;
      setInputText(content);
      const result = parseAndValidateBulkQuestions(content, categories);
      setValidationResult(result);
      setHasValidated(true);
    };
    reader.readAsText(file);
  };

  const handleDownloadTemplate = (type: 'csv' | 'json') => {
    const content = type === 'csv' ? SAMPLE_CSV_TEMPLATE : SAMPLE_JSON_TEMPLATE;
    const mime = type === 'csv' ? 'text/csv' : 'application/json';
    const filename = `quizearn_questions_template.${type}`;

    const blob = new Blob([content], { type: mime });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const handleCommit = () => {
    if (!validationResult || validationResult.validQuestions.length === 0) return;
    onCommitImport(validationResult.validQuestions);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm overflow-y-auto">
      <div className="relative w-full max-w-3xl bg-brand-dark-surface border border-brand-dark-border rounded-3xl shadow-2xl overflow-hidden my-8">
        {/* Header */}
        <div className="p-6 border-b border-brand-dark-border/60 flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-xl bg-brand-primary/10 border border-brand-primary/30 text-brand-primary flex items-center justify-center">
              <IconUpload className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-lg font-bold text-white">Bulk Question Import</h2>
              <p className="text-xs text-slate-400">
                Ingest large trivia batches via CSV or JSON with schema validation.
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

        {/* Modal Content */}
        <div className="p-6 space-y-6 max-h-[75vh] overflow-y-auto">
          {/* Template Download Shortcuts */}
          <div className="flex flex-col sm:flex-row sm:items-center justify-between p-4 rounded-2xl bg-brand-dark-card border border-brand-dark-border/80 gap-3">
            <div>
              <p className="text-xs font-bold text-white">Download Standard Templates</p>
              <p className="text-[11px] text-slate-400">
                Required columns: category, difficulty, question, optionA..D, correctOptionIndex, explanation
              </p>
            </div>
            <div className="flex items-center space-x-2">
              <button
                type="button"
                onClick={() => handleDownloadTemplate('csv')}
                className="inline-flex items-center space-x-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold bg-white/5 hover:bg-white/10 text-slate-200 border border-white/10 transition"
              >
                <IconDownload className="w-3.5 h-3.5 text-brand-primary" />
                <span>CSV Template</span>
              </button>
              <button
                type="button"
                onClick={() => handleDownloadTemplate('json')}
                className="inline-flex items-center space-x-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold bg-white/5 hover:bg-white/10 text-slate-200 border border-white/10 transition"
              >
                <IconDownload className="w-3.5 h-3.5 text-brand-mint" />
                <span>JSON Template</span>
              </button>
            </div>
          </div>

          {/* File Upload / Drag Area */}
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-2">
              Upload File (.csv or .json)
            </label>
            <div className="border-2 border-dashed border-brand-dark-border hover:border-brand-primary/60 rounded-2xl p-6 text-center transition cursor-pointer bg-brand-dark-card/50">
              <input
                type="file"
                accept=".csv, .json, text/csv, application/json"
                onChange={handleFileUpload}
                className="hidden"
                id="bulk-file-input"
              />
              <label htmlFor="bulk-file-input" className="cursor-pointer block">
                <IconUpload className="w-8 h-8 text-brand-primary mx-auto mb-2" />
                <p className="text-xs font-bold text-white">
                  Click to select a file or drag and drop
                </p>
                <p className="text-[11px] text-slate-500 mt-1">
                  Supports CSV with headers or JSON array format
                </p>
              </label>
            </div>
          </div>

          {/* Paste Raw Text Option */}
          <div>
            <div className="flex items-center justify-between mb-1.5">
              <label className="block text-xs font-semibold text-slate-300">
                Or Paste Raw CSV / JSON Text
              </label>
              {inputText && (
                <button
                  type="button"
                  onClick={() => {
                    setInputText('');
                    setValidationResult(null);
                    setHasValidated(false);
                  }}
                  className="text-xs text-rose-400 hover:underline"
                >
                  Clear
                </button>
              )}
            </div>
            <textarea
              rows={6}
              value={inputText}
              onChange={(e) => {
                setInputText(e.target.value);
                setHasValidated(false);
              }}
              placeholder="Paste raw CSV content with header row, or a JSON array of question objects..."
              className="w-full p-3 font-mono text-xs rounded-xl bg-brand-dark-card border border-brand-dark-border text-white placeholder-slate-600 focus:outline-none focus:border-brand-primary"
            />
          </div>

          {/* Validate Action */}
          <div className="flex items-center justify-between">
            <Button
              variant="outline"
              size="sm"
              disabled={!inputText.trim()}
              onClick={handleValidate}
            >
              Run Schema Validation
            </Button>
            {hasValidated && validationResult && (
              <span className="text-xs font-medium text-slate-300">
                Processed <strong className="text-white">{validationResult.totalRows}</strong> rows
              </span>
            )}
          </div>

          {/* Validation Feedback & Preview Section */}
          {hasValidated && validationResult && (
            <div className="space-y-4 pt-2">
              {/* Summary Stats */}
              <div className="grid grid-cols-2 gap-3">
                <div className="p-3.5 rounded-xl bg-teal-950/30 border border-teal-500/40 flex items-center space-x-3">
                  <div className="w-8 h-8 rounded-lg bg-teal-500/20 text-teal-300 flex items-center justify-center font-bold">
                    <IconCheck className="w-5 h-5" />
                  </div>
                  <div>
                    <p className="text-lg font-bold text-white">
                      {validationResult.validQuestions.length}
                    </p>
                    <p className="text-[11px] text-teal-300">Valid Questions Ready</p>
                  </div>
                </div>

                <div className="p-3.5 rounded-xl bg-rose-950/30 border border-rose-500/40 flex items-center space-x-3">
                  <div className="w-8 h-8 rounded-lg bg-rose-500/20 text-rose-300 flex items-center justify-center font-bold">
                    <IconAlertCircle className="w-5 h-5" />
                  </div>
                  <div>
                    <p className="text-lg font-bold text-white">
                      {validationResult.errors.length}
                    </p>
                    <p className="text-[11px] text-rose-300">Schema Validation Errors</p>
                  </div>
                </div>
              </div>

              {/* Error Details Table (if any) */}
              {validationResult.errors.length > 0 && (
                <div className="rounded-xl border border-rose-500/30 bg-rose-950/20 p-4 space-y-2 max-h-48 overflow-y-auto">
                  <p className="text-xs font-bold text-rose-300">Error Report:</p>
                  <ul className="space-y-1 text-xs text-rose-200">
                    {validationResult.errors.map((err, i) => (
                      <li key={i} className="flex items-start space-x-1.5 font-mono text-[11px]">
                        <span className="text-rose-400 font-bold">[Row {err.row}]</span>
                        <span className="text-slate-400">({err.field}):</span>
                        <span>{err.message}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              )}

              {/* Valid Questions Sample Preview */}
              {validationResult.validQuestions.length > 0 && (
                <div className="rounded-xl border border-brand-dark-border bg-brand-dark-card p-4 space-y-2">
                  <p className="text-xs font-bold text-white">
                    Parsed Questions Sample ({validationResult.validQuestions.length} total)
                  </p>
                  <div className="space-y-2 max-h-40 overflow-y-auto">
                    {validationResult.validQuestions.slice(0, 5).map((q, idx) => (
                      <div
                        key={idx}
                        className="p-2.5 rounded-lg bg-white/5 border border-white/5 text-xs"
                      >
                        <div className="flex items-center justify-between text-[11px] text-slate-400 mb-1">
                          <span className="text-brand-primary font-semibold">{q.categoryName}</span>
                          <span className="text-teal-400">Answer: {q.options[q.correctOptionIndex]}</span>
                        </div>
                        <p className="text-white font-medium line-clamp-1">{q.text}</p>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}
        </div>

        {/* Modal Footer */}
        <div className="p-6 bg-brand-dark-surface border-t border-brand-dark-border/60 flex items-center justify-between">
          <Button variant="outline" size="sm" onClick={onClose}>
            Cancel
          </Button>

          <Button
            variant="primary"
            size="sm"
            disabled={!validationResult || validationResult.validQuestions.length === 0}
            onClick={handleCommit}
          >
            <IconCheck className="w-4 h-4 mr-1.5" />
            <span>
              Commit Import ({validationResult ? validationResult.validQuestions.length : 0} Questions)
            </span>
          </Button>
        </div>
      </div>
    </div>
  );
}
