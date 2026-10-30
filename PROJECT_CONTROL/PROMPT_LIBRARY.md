# QuizEarn — Prompt Library

> Complete catalog of all standardized prompt templates, status, and definitions for QuizEarn (QZ-00 through QZ-35, plus utility prompts).

---

## Utility Prompts

### PROMPT QZ-RESUME — Recover the Exact Project State
- **Trigger:** Start of any new session or after switching accounts/agents.
- **Text:**
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

### PROMPT QZ-EMERGENCY — Emergency Session Handoff
- **Trigger:** Session nearing quota limit or unexpectedly terminating.
- **Text:**
```text
STOP starting new features. Safely preserve the current QuizEarn project state now.

1. Save any valid completed code changes.
2. Inspect actual modified files and current build/test state.
3. Update PROJECT_CONTROL/CURRENT_STATE.md.
4. Update PROJECT_CONTROL/TASK_CHECKLIST.md.
5. Append an entry to PROJECT_CONTROL/SESSION_LOG.md.
6. Update PROJECT_CONTROL/FILE_CHANGE_LOG.md and any relevant error/test reports.
7. Write the exact next prompt in full into PROJECT_CONTROL/NEXT_PROMPT.md.
8. State the next prompt ID and exact first action.
9. Do not use Git. Do not claim unverified success.
10. Do not write any secrets into the reports.

If the current operation cannot be safely completed, document exactly what is unfinished and how the next session should inspect and recover it.
```

---

## Milestone Prompts Catalog

| Prompt ID | Title | Phase | Status |
| :--- | :--- | :--- | :--- |
| **QZ-00** | Create local control files and audit | Phase 1: Foundation | **COMPLETED** |
| **QZ-01** | Confirm product requirements and architecture | Phase 1: Foundation | **COMPLETED** |
| **QZ-02** | Stabilize Android build and dependencies | Phase 1: Foundation | **COMPLETED** |
| **QZ-03** | Create package structure and app navigation foundation | Phase 1: Foundation | **COMPLETED** |
| **QZ-04** | Create QuizEarn brand specification | Phase 2: Brand & Design | **COMPLETED** |
| **QZ-05** | Create logo and launcher asset plan/assets | Phase 2: Brand & Design | **COMPLETED** |
| **QZ-06** | Implement colors, typography, themes, reusable components | Phase 2: Brand & Design | **COMPLETED** |
| **QZ-07** | Implement splash and onboarding | Phase 3: Frontend | **COMPLETED** |
| **QZ-08** | Implement authentication screens and UI states | Phase 3: Frontend | **COMPLETED** |
| **QZ-09** | Implement Home dashboard | Phase 3: Frontend | **COMPLETED** |
| **QZ-10** | Implement quiz categories and quiz gameplay UI | Phase 3: Frontend | **COMPLETED** |
| **QZ-11** | Implement results and quiz history | Phase 3: Frontend | **COMPLETED** |
| **QZ-12** | Implement tasks and tournaments UI | Phase 3: Frontend | **COMPLETED** |
| **QZ-13** | Implement leaderboard, profile, settings, help | Phase 3: Frontend | **COMPLETED** |
| **QZ-14** | Implement rewards, wallet, transaction history, redemption UI | Phase 3: Frontend | **COMPLETED** |
| **QZ-15** | Choose/confirm backend and document setup | Phase 4: Backend | **COMPLETED** |
| **QZ-16** | Implement authentication/profile integration | Phase 4: Backend | **COMPLETED** |
| **QZ-17** | Implement quiz session and server-authoritative scoring | Phase 4: Backend | **COMPLETED** |
| **QZ-18** | Implement wallet ledger and transaction integrity | Phase 4: Backend | **COMPLETED** |
| **QZ-19** | Implement rewards and redemption workflow | Phase 4: Backend | **COMPLETED** |
| **QZ-20** | Implement tasks, tournaments, leaderboard backend | Phase 4: Backend | **COMPLETED** |
| **QZ-21** | Implement notifications and support workflow where configured | Phase 4: Backend | **COMPLETED** |
| **QZ-22** | Confirm admin architecture and authorization | Phase 5: Admin Panel | **COMPLETED** |
| **QZ-23** | Implement admin login and dashboard shell | Phase 5: Admin Panel | **COMPLETED** |
| **QZ-24** | Implement category/question management | Phase 5: Admin Panel | **COMPLETED** |
| **QZ-25** | Implement task/tournament management | Phase 5: Admin Panel | **COMPLETED** |
| **QZ-26** | Implement rewards/redemptions/user/support management | Phase 5: Admin Panel | **COMPLETED** |
| **QZ-27** | Implement audit logs and admin security verification | Phase 5: Admin Panel | **COMPLETED** |
| **QZ-28** | Add unit and UI tests for core Android flows | Phase 6: Quality | **COMPLETED** |
| **QZ-29** | Test backend security, duplicate requests, and wallet concurrency | Phase 6: Quality | **COMPLETED** |
| **QZ-30** | Accessibility, performance, error states, and polish | Phase 6: Quality | **COMPLETED** |
| **QZ-31** | End-to-end testing and bug-fix pass | Phase 6: Quality | **COMPLETED** |
| **QZ-32** | Build verification and environment documentation | Phase 7: Release | **COMPLETED** |
| **QZ-33** | Prepare release configuration and APK/AAB when possible | Phase 7: Release | **COMPLETED** |
| **QZ-34** | Prepare Play Store listing and release checklist | Phase 7: Release | **COMPLETED** |
| **QZ-35** | Final audit and handoff report | Phase 7: Release | **COMPLETED** |

---

## Detailed Prompt Definitions

### Prompt QZ-00 — Create Local Control Files and Audit
```text
We are building QuizEarn, an original Android quiz-and-rewards application. My development sessions may end or move to another authorized Antigravity session. I do NOT want Git-based progress tracking. All handoff reports must be saved inside this project folder.

Your task is ONLY to establish a reliable local project-control system and audit the existing project. Do not begin building every feature yet.

1. Inspect the current project folder and identify the actual Android/frontend/backend technologies, if present.
2. Create PROJECT_CONTROL/ and the files listed in the local project kit:
   00_START_HERE.md, CURRENT_STATE.md, MASTER_ROADMAP.md, TASK_CHECKLIST.md,
   SESSION_LOG.md, NEXT_PROMPT.md, DECISIONS.md, BLOCKERS_AND_ERRORS.md,
   FILE_CHANGE_LOG.md, TEST_RESULTS.md, CONFIGURATION_STATUS.md, PROMPT_LIBRARY.md.
3. Create the docs/ files listed in the kit if missing, without deleting useful existing documentation.
4. Inspect the repository and report what actually exists: source files, screens, build system, backend, database, tests, configuration, and known build issues.
5. Do not guess. Mark unknown information as unknown and unverified.
6. Put the complete QuizEarn roadmap and acceptance criteria into MASTER_ROADMAP.md.
7. Put the current actual status into CURRENT_STATE.md and TASK_CHECKLIST.md.
8. Put a concise start/resume guide into 00_START_HERE.md.
9. Put the exact next prompt (QZ-01) into NEXT_PROMPT.md.
10. Record this work in SESSION_LOG.md and FILE_CHANGE_LOG.md.

Do not initialize Git. Do not add secrets. Do not claim a build passed unless you run it successfully.
At the end, summarize the files created and the next prompt ID.
```

### Prompt QZ-01 — Confirm Product Requirements and Architecture
```text
Read the QuizEarn project control files first. Inspect the actual project before deciding anything.

Task: confirm the product requirements and technical architecture only. Document:
- Android application scope
- Existing versus missing features
- Recommended architecture based on the actual repository
- Whether a backend/admin panel exists
- Key dependencies between milestones
- Important unknowns and external configuration requirements

Do not implement all screens. Do not replace the existing stack without a clear technical reason. Update docs/ARCHITECTURE.md, MASTER_ROADMAP.md, CURRENT_STATE.md, TASK_CHECKLIST.md, SESSION_LOG.md, and NEXT_PROMPT.md. Set the next prompt to QZ-02. Do not use Git.
```

### Prompt QZ-02 — Stabilize the Android Build
```text
Read the local project state and inspect the existing Android build.

Task: make the smallest necessary changes to get the existing project building. Check Gradle configuration, Android plugin/Kotlin compatibility, dependencies, manifest, SDK configuration, and source errors. Do not implement new product features in this task.

Run the appropriate build command available in the project. Record the exact command and actual result. If blocked by missing SDK, network, credentials, or environment, document the exact blocker rather than pretending success.

Update CURRENT_STATE.md, TASK_CHECKLIST.md, SESSION_LOG.md, FILE_CHANGE_LOG.md, TEST_RESULTS.md, BLOCKERS_AND_ERRORS.md, and NEXT_PROMPT.md. Set the next prompt to QZ-03. Do not use Git.
```

### Prompt QZ-03 — Package Structure and Navigation Foundation
```text
Read all relevant local reports. Implement only the Android app foundation and navigation.

Use the existing project conventions where practical. If this is a new Compose project, establish a maintainable structure for ui, navigation, domain, data, and core modules only as needed. Add the QuizEarn app entry point and a working navigation shell. Avoid fake business functionality.

Verify the app compiles if possible. Record all changed files and actual verification results. Update the control files and set NEXT_PROMPT.md to QZ-04. Do not use Git.
```

*(See MASTER_ROADMAP.md for detailed acceptance criteria for QZ-04 through QZ-35).*
