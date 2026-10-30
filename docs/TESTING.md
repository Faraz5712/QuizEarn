# QuizEarn — Testing Strategy & Verification Guide

> Testing framework, test cases, and quality assurance methodology for QuizEarn.

---

## 1. Testing Pyramid & Verification Levels

```
               / \
              /   \
             / E2E \       Level 4: End-to-End User Flow (QZ-31)
            /-------\
           / Concur. \     Level 3: Security & Ledger Concurrency (QZ-29)
          /-----------\
         /  Compose UI \   Level 2: Screen Semantics & Interactions (QZ-28)
        /---------------\
       /    Unit Tests   \ Level 1: ViewModels, Repositories, Reducers (QZ-28)
      /-------------------\
```

---

## 2. Unit Testing Strategy (Android)

### Frameworks & Libraries
- **Test Runner:** JUnit 4 / AndroidX Test.
- **Mocking:** MockK or Fake implementations.
- **Coroutines & Flow:** `kotlinx-coroutines-test`, `Turbine` (for testing `StateFlow` and `SharedFlow`).
- **Assertions:** Google `Truth` or `AssertJ`.

### Critical Test Targets
1. **`QuizViewModelTest`**:
   - Verify initial state is `Loading` then `Ready`.
   - Verify timer countdown decrements correctly.
   - Verify answer selection updates `selectedOptionIndex` and enables submit button.
   - Verify answer timeout triggers auto-lock or forfeit according to category rules.
2. **`WalletViewModelTest`**:
   - Verify ledger entries format properly (credits formatted with `+` sign and gold badge).
   - Verify balance calculations match remote server response.
3. **`AuthViewModelTest`**:
   - Verify email validation regex rejects invalid formats.
   - Verify password strength rules (min 8 chars, numbers, uppercase).

---

## 3. Jetpack Compose UI Testing

- Uses `androidx.compose.ui:ui-test-junit4`.
- Tests verify accessibility semantics (`contentDescription`), button clickability, and visible text without relying on brittle pixel matching.
- Previews are verified via Compose Preview rendering in Android Studio / sidecar.

---

## 4. Backend Concurrency & Security Testing (Phase 6 / QZ-29)

### Wallet Double-Spend Stress Test
- **Scenario:** Issue 10 simultaneous redemption requests for 1,000 points from an account with only 1,000 points.
- **Expected Outcome:** Exactly 1 request succeeds; 9 requests fail with `400 INSUFFICIENT_BALANCE`. Final user balance must be 0 points.

### Quiz Session Replay Test
- **Scenario:** Submit the same completed quiz session ID twice.
- **Expected Outcome:** First request succeeds and awards points. Second request returns `409 CONFLICT` or cached result with 0 additional points credited.

---

## 5. Verification Command Cheatsheet

```powershell
# Run all local JVM unit tests
.\gradlew testDebugUnitTest

# Run specific ViewModel test class
.\gradlew testDebugUnitTest --tests "com.quizearn.app.ui.screens.quiz.QuizViewModelTest"

# Run Android lint
.\gradlew lintDebug
```
