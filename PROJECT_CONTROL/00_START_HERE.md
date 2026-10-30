# QuizEarn — Project Control: Start Here

Welcome to **QuizEarn**, an original Android quiz-and-rewards application.

This project uses a **local-folder progress control system**. Progress and handoff reports are tracked directly inside this project repository. **Git is NOT used for progress tracking**. All handoff state, decisions, and task statuses are kept in `PROJECT_CONTROL/` and `docs/`.

---

## 1. Quick Resume Protocol (For Every New Session)

Whenever a new development session begins, run the **Resume Protocol**:

```text
Resume QuizEarn from the local project reports. Do not restart the project.

1. Read PROJECT_CONTROL/00_START_HERE.md, CURRENT_STATE.md, TASK_CHECKLIST.md,
   NEXT_PROMPT.md, and the latest entries in SESSION_LOG.md.
2. Inspect the actual source files and compare them with the reports.
3. If reports and source disagree, trust verified source/build evidence, correct the reports, and explain the discrepancy.
4. Identify the exact next prompt ID and unfinished task.
5. Perform only that next manageable task. If NEXT_PROMPT.md is empty or stale, choose the smallest unfinished task in TASK_CHECKLIST.md and write a corrected next prompt.
6. Run a relevant verification step if possible.
7. Update CURRENT_STATE.md, TASK_CHECKLIST.md, SESSION_LOG.md, NEXT_PROMPT.md, FILE_CHANGE_LOG.md, and any affected test/error reports.
8. Do not redo completed work, do not use Git, and do not claim unverified success.
```

---

## 2. Reading Order for Incoming Agents / Developers

Before writing or modifying any code, read files in this exact order:

1. **`PROJECT_CONTROL/00_START_HERE.md`** (This file) — Ground rules, resumption protocol, and index.
2. **`PROJECT_CONTROL/CURRENT_STATE.md`** — The single source of truth for the project's actual status.
3. **`PROJECT_CONTROL/NEXT_PROMPT.md`** — The exact task and prompt queued for immediate execution.
4. **`PROJECT_CONTROL/TASK_CHECKLIST.md`** — Breakdown of completed vs. pending tasks.
5. **`PROJECT_CONTROL/SESSION_LOG.md`** — Historical record of recent work sessions and commands.
6. **`PROJECT_CONTROL/BLOCKERS_AND_ERRORS.md`** — Active blockers, environmental issues, or known errors.
7. **`PROJECT_CONTROL/CONFIGURATION_STATUS.md`** — Status of external services and environment tools.

---

## 3. Core Working Rules

1. **Work Locally**: All source code, assets, and documentation live under the root folder `E:\Faraz\App\QuizEarn`.
2. **No Git Progress Tracking**: Do not initialize Git or create Git commits unless the user explicitly requests it.
3. **Inspect Before Assuming**: Always check actual directory contents and source files before assuming code exists.
4. **No Guesses or Fabricated Success**: If a build has not run, mark it as unverified. If a service is not configured, mark it as unverified. Never pretend mock data is a production backend.
5. **No Secret Leaks**: Never write API keys, passwords, keystore passwords, private keys, or service-account JSON contents into project control or documentation files.
6. **Server-Authoritative Scoring & Ledger**: Quiz scoring, wallet balances, and redemption eligibility must never trust client-submitted calculations.
7. **Originality**: QuizEarn is an original application. Do not copy proprietary code, copyrighted artwork, or assets from other quiz apps.
8. **Update Reports After Every Task**: Before concluding any prompt, update `CURRENT_STATE.md`, `TASK_CHECKLIST.md`, `SESSION_LOG.md`, `FILE_CHANGE_LOG.md`, `TEST_RESULTS.md`, and write the single next actionable prompt into `NEXT_PROMPT.md`.
9. **Never Auto-Start the Next Task**: Save the prompt in `NEXT_PROMPT.md`, present the summary to the user, and wait for confirmation.

---

## 4. Directory Map

```text
QuizEarn/
├── PROJECT_CONTROL/
│   ├── 00_START_HERE.md           <- You are here
│   ├── CURRENT_STATE.md           <- Single source of truth for state
│   ├── MASTER_ROADMAP.md          <- All phases and prompt acceptance criteria
│   ├── TASK_CHECKLIST.md          <- Granular task items with checkmarks
│   ├── SESSION_LOG.md             <- Chronological session history
│   ├── NEXT_PROMPT.md             <- Exact prompt ready to run next
│   ├── DECISIONS.md               <- Architectural Decision Records (ADRs)
│   ├── BLOCKERS_AND_ERRORS.md     <- Unresolved issues and error log
│   ├── FILE_CHANGE_LOG.md         <- File modification audit trail
│   ├── TEST_RESULTS.md            <- Verified commands and test outputs
│   ├── CONFIGURATION_STATUS.md    <- Toolchain & credential status
│   └── PROMPT_LIBRARY.md          <- Catalog of all prompts (QZ-00 to QZ-35)
├── docs/
│   ├── ARCHITECTURE.md            <- System architecture and component design
│   ├── DATABASE_SCHEMA.md         <- Relational/NoSQL database models
│   ├── API_REFERENCE.md           <- Server endpoints & contracts
│   ├── SECURITY.md                <- Threat model & authorization rules
│   ├── PRIVACY_DATA_MAP.md        <- User data & Google Play safety
│   ├── ENVIRONMENT_SETUP.md       <- Toolchain, JDK, SDK instructions
│   ├── TESTING.md                 <- Test guidelines & verification plans
│   ├── BUILD_AND_RELEASE.md       <- Build scripts, variants, Play Store checklist
│   └── KNOWN_LIMITATIONS.md       <- Current known technical debt & limits
└── [Android source files and assets to be placed here in upcoming phases]
```

---

## 5. Current Active Task

- **Current Completed Milestone:** QZ-35 (Phase 7: Final Audit and Handoff Report completed — 100% Project Lifecycle Complete).
- **Status:** **ALL 35 MILESTONES COMPLETED (QZ-00 through QZ-35)**.
- **Next Task:** Maintenance and production deployment mode. See `PROJECT_CONTROL/NEXT_PROMPT.md` and `docs/KNOWN_LIMITATIONS.md` for handoff details.
