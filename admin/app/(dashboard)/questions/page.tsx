'use client';

import React, { useEffect, useState } from 'react';
import { RouteGuard } from '../../../components/layout/RouteGuard';
import {
  loadStoredCategories,
  loadStoredQuestions,
  saveStoredCategories,
  saveStoredQuestions,
} from '../../../lib/questions-store';
import { AdminCategory, AdminQuestion } from '../../../types/question';
import { QuestionTable } from '../../../components/questions/QuestionTable';
import { QuestionAuthoringModal } from '../../../components/questions/QuestionAuthoringModal';
import { QuestionPreviewModal } from '../../../components/questions/QuestionPreviewModal';
import { CategoryTaxonomyTable } from '../../../components/questions/CategoryTaxonomyTable';
import { BulkImportModal } from '../../../components/questions/BulkImportModal';
import { Button } from '../../../components/ui/Button';
import { IconCheck, IconPlus, IconQuiz, IconShield, IconUpload } from '../../../components/ui/Icons';
import { deleteAdminRecord, listAdminRecords, saveAdminRecord } from '../../../lib/backend';
import { isFirebaseConfigured } from '../../../lib/firebase';

type ActiveTab = 'QUESTIONS' | 'CATEGORIES';

export default function QuestionManagementPage() {
  const [activeTab, setActiveTab] = useState<ActiveTab>('QUESTIONS');
  const [questions, setQuestions] = useState<AdminQuestion[]>([]);
  const [categories, setCategories] = useState<AdminCategory[]>([]);
  const [isAuthoringOpen, setIsAuthoringOpen] = useState(false);
  const [isBulkImportOpen, setIsBulkImportOpen] = useState(false);
  const [questionToEdit, setQuestionToEdit] = useState<AdminQuestion | null>(null);
  const [questionToPreview, setQuestionToPreview] = useState<AdminQuestion | null>(null);
  const [notification, setNotification] = useState<string | null>(null);

  // Load questions and categories from storage on mount
  useEffect(() => {
    if (!isFirebaseConfigured) {
      setQuestions(loadStoredQuestions());
      setCategories(loadStoredCategories());
      return;
    }
    Promise.all([
      listAdminRecords<AdminQuestion>('questions'),
      listAdminRecords<AdminCategory>('categories'),
    ]).then(([loadedQuestions, loadedCategories]) => {
      setQuestions(loadedQuestions);
      setCategories(loadedCategories);
    }).catch((error: unknown) => {
      setNotification(error instanceof Error ? error.message : 'Failed to load backend content.');
    });
  }, []);

  const showToast = (message: string) => {
    setNotification(message);
    setTimeout(() => {
      setNotification(null);
    }, 4000);
  };

  // Recalculate question counts per category
  const syncCategoryCounts = (
    currentQuestions: AdminQuestion[],
    currentCategories: AdminCategory[]
  ) => {
    const updated = currentCategories.map((cat) => ({
      ...cat,
      questionCount: currentQuestions.filter((q) => q.categoryId === cat.id).length,
    }));
    setCategories(updated);
    if (!isFirebaseConfigured) saveStoredCategories(updated);
  };

  // Save new or edited question
  const handleSaveQuestion = async (
    payload: Omit<AdminQuestion, 'id' | 'createdAt'> & { id?: string }
  ) => {
    let updated: AdminQuestion[];

    if (payload.id) {
      // Edit existing
      updated = questions.map((q) =>
        q.id === payload.id
          ? {
              ...q,
              ...payload,
              id: q.id,
              createdAt: q.createdAt,
              updatedAt: new Date().toISOString(),
            }
          : q
      );
    } else {
      // Create new
      const newId = `QZ-${payload.categoryId.replace('cat_', '').substring(0, 3).toUpperCase()}-${String(
        questions.length + 1
      ).padStart(3, '0')}`;

      const newQuestion: AdminQuestion = {
        ...payload,
        id: newId,
        createdAt: new Date().toISOString(),
      };

      updated = [newQuestion, ...questions];
    }

    try {
      const saved = updated.find((question) => question.id === (payload.id || updated[0]?.id));
      if (!saved) throw new Error('Saved question could not be resolved.');
      await saveAdminRecord('questions', { ...saved });
    } catch (error) {
      setNotification(error instanceof Error ? error.message : 'Failed to save question.');
      return;
    }
    setQuestions(updated);
    if (!isFirebaseConfigured) saveStoredQuestions(updated);
    syncCategoryCounts(updated, categories);
    showToast(payload.id ? `Question [${payload.id}] updated successfully.` : `New question [${updated[0]!.id}] authored and added to bank.`);
    setIsAuthoringOpen(false);
    setQuestionToEdit(null);
  };

  // Delete question
  const handleDeleteQuestion = async (questionId: string) => {
    if (confirm(`Are you sure you want to permanently delete question ${questionId}?`)) {
      try {
        if (isFirebaseConfigured) await deleteAdminRecord('questions', questionId);
      } catch (error) {
        setNotification(error instanceof Error ? error.message : 'Failed to delete question.');
        return;
      }
      const updated = questions.filter((q) => q.id !== questionId);
      setQuestions(updated);
      if (!isFirebaseConfigured) saveStoredQuestions(updated);
      syncCategoryCounts(updated, categories);
      showToast(`Question [${questionId}] deleted from bank.`);
    }
  };

  // Bulk import commit
  const handleCommitBulkImport = async (
    importedQuestions: Omit<AdminQuestion, 'id' | 'createdAt'>[]
  ) => {
    const baseCount = questions.length;
    const timestamp = new Date().toISOString();

    const formatted: AdminQuestion[] = importedQuestions.map((q, idx) => ({
      ...q,
      id: `QZ-IMP-${String(baseCount + idx + 1).padStart(3, '0')}`,
      createdAt: timestamp,
    }));

    if (isFirebaseConfigured) {
      try {
        for (const question of formatted) await saveAdminRecord('questions', { ...question });
      } catch (error) {
        setNotification(error instanceof Error ? error.message : 'Bulk import failed.');
        return;
      }
    }
    const updated = [...formatted, ...questions];
    setQuestions(updated);
    if (!isFirebaseConfigured) saveStoredQuestions(updated);
    syncCategoryCounts(updated, categories);
    showToast(`Successfully imported ${formatted.length} questions into the bank.`);
  };

  // Toggle Category Status (Active vs Disabled)
  const handleToggleCategoryStatus = async (categoryId: string) => {
    const updated = categories.map((cat) =>
      cat.id === categoryId ? { ...cat, isActive: !cat.isActive } : cat
    );
    try {
      const target = updated.find((c) => c.id === categoryId);
      if (target) await saveAdminRecord('categories', { ...target });
    } catch (error) {
      setNotification(error instanceof Error ? error.message : 'Failed to update category.');
      return;
    }
    setCategories(updated);
    if (!isFirebaseConfigured) saveStoredCategories(updated);
    const target = updated.find((c) => c.id === categoryId);
    showToast(
      `Category "${target?.name}" ${target?.isActive ? 'enabled' : 'disabled'} in player app.`
    );
  };

  // Add new Category
  const handleAddCategory = async (
    newCat: Omit<AdminCategory, 'id' | 'questionCount'>
  ) => {
    const newId = `cat_${newCat.slug.replace(/[^a-zA-Z0-9]/g, '_').toLowerCase()}`;
    const category: AdminCategory = {
      ...newCat,
      id: newId,
      questionCount: 0,
    };
    const updated = [...categories, category];
    try {
      await saveAdminRecord('categories', { ...category });
    } catch (error) {
      setNotification(error instanceof Error ? error.message : 'Failed to create category.');
      return;
    }
    setCategories(updated);
    if (!isFirebaseConfigured) saveStoredCategories(updated);
    showToast(`New topic category "${category.name}" created.`);
  };

  return (
    <RouteGuard requiredPermission="MANAGE_QUESTIONS">
      <div className="space-y-6 max-w-7xl mx-auto">
        {/* Toast Notification */}
        {notification && (
          <div className="p-3.5 rounded-2xl bg-teal-950/80 border border-teal-500/50 text-teal-200 text-xs font-semibold flex items-center justify-between shadow-xl animate-fade-in">
            <div className="flex items-center space-x-2">
              <IconCheck className="w-4 h-4 text-brand-mint" />
              <span>{notification}</span>
            </div>
            <button
              onClick={() => setNotification(null)}
              className="text-slate-400 hover:text-white"
            >
              &times;
            </button>
          </div>
        )}

        {/* Top Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-6 border-b border-brand-dark-border/60">
          <div>
            <div className="flex items-center space-x-2 mb-1">
              <span className="text-xs font-bold uppercase tracking-wider text-brand-primary">
                Trivia Curation
              </span>
              <span className="text-[10px] px-2 py-0.5 rounded-full font-bold bg-brand-primary/20 text-brand-primary border border-brand-primary/30">
                {questions.length} Questions in Bank
              </span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
              Question Bank & Taxonomy
            </h1>
            <p className="text-xs text-slate-400 mt-1 max-w-xl">
              Author server-authoritative trivia questions, manage answer keys, audit difficulty balances, and import bulk sets.
            </p>
          </div>

          {/* Action CTAs */}
          <div className="flex items-center space-x-2.5 flex-shrink-0">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setIsBulkImportOpen(true)}
            >
              <IconUpload className="w-4 h-4 mr-1.5 text-brand-mint" />
              <span>Bulk Import</span>
            </Button>

            <Button
              variant="primary"
              size="sm"
              onClick={() => {
                setQuestionToEdit(null);
                setIsAuthoringOpen(true);
              }}
            >
              <IconPlus className="w-4 h-4 mr-1.5" />
              <span>+ Add Question</span>
            </Button>
          </div>
        </div>

        {/* Tab Navigation */}
        <div className="flex border-b border-brand-dark-border/80 space-x-6 text-xs font-bold">
          <button
            onClick={() => setActiveTab('QUESTIONS')}
            className={`pb-3 px-1 transition-all relative flex items-center space-x-2 ${
              activeTab === 'QUESTIONS'
                ? 'text-brand-primary'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <IconQuiz className="w-4 h-4" />
            <span>Question Bank ({questions.length})</span>
            {activeTab === 'QUESTIONS' && (
              <span className="absolute bottom-0 left-0 right-0 h-0.5 bg-brand-primary rounded-t-full shadow-sm shadow-brand-primary" />
            )}
          </button>

          <button
            onClick={() => setActiveTab('CATEGORIES')}
            className={`pb-3 px-1 transition-all relative flex items-center space-x-2 ${
              activeTab === 'CATEGORIES'
                ? 'text-brand-primary'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <IconShield className="w-4 h-4" />
            <span>Category Taxonomy ({categories.length})</span>
            {activeTab === 'CATEGORIES' && (
              <span className="absolute bottom-0 left-0 right-0 h-0.5 bg-brand-primary rounded-t-full shadow-sm shadow-brand-primary" />
            )}
          </button>
        </div>

        {/* Active Tab View */}
        {activeTab === 'QUESTIONS' ? (
          <QuestionTable
            questions={questions}
            categories={categories}
            onEditQuestion={(q) => {
              setQuestionToEdit(q);
              setIsAuthoringOpen(true);
            }}
            onPreviewQuestion={(q) => {
              setQuestionToPreview(q);
            }}
            onDeleteQuestion={handleDeleteQuestion}
          />
        ) : (
          <CategoryTaxonomyTable
            categories={categories}
            onToggleStatus={handleToggleCategoryStatus}
            onUpdateCategory={async (cat) => {
              const updated = categories.map((c) => (c.id === cat.id ? cat : c));
              try {
                await saveAdminRecord('categories', { ...cat });
              } catch (error) {
                setNotification(error instanceof Error ? error.message : 'Failed to update category.');
                return;
              }
              setCategories(updated);
              if (!isFirebaseConfigured) saveStoredCategories(updated);
            }}
            onAddCategory={handleAddCategory}
          />
        )}

        {/* Authoring Modal (Add/Edit) */}
        <QuestionAuthoringModal
          isOpen={isAuthoringOpen}
          questionToEdit={questionToEdit}
          categories={categories}
          onClose={() => {
            setIsAuthoringOpen(false);
            setQuestionToEdit(null);
          }}
          onSave={handleSaveQuestion}
        />

        {/* Player Device Preview Modal */}
        <QuestionPreviewModal
          question={questionToPreview}
          onClose={() => setQuestionToPreview(null)}
        />

        {/* Bulk Import Modal (CSV/JSON) */}
        <BulkImportModal
          isOpen={isBulkImportOpen}
          categories={categories}
          onClose={() => setIsBulkImportOpen(false)}
          onCommitImport={handleCommitBulkImport}
        />
      </div>
    </RouteGuard>
  );
}
