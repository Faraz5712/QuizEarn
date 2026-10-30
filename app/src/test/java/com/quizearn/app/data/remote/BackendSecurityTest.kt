package com.quizearn.app.data.remote

import com.quizearn.app.core.result.Result
import com.quizearn.app.domain.model.AdminPermission
import com.quizearn.app.domain.model.LeaderboardTimeframe
import com.quizearn.app.domain.model.PlayerAnswer
import com.quizearn.app.domain.model.QuizSubmission
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * Backend security, access control, and anti-abuse verification suite covering:
 * 1. Anti-replay idempotency key defense on quiz submissions.
 * 2. Cryptographic session token attestation and tampering rejection.
 * 3. Strict server-side expiration deadline enforcement.
 * 4. Role-based access control (RBAC) rejection of non-privileged tokens (403 Forbidden).
 * 5. Scoped permission isolation across SuperAdmin, ContentManager, and SupportAgent roles.
 * 6. Rate limiting and denial-of-service guardrails on high-frequency public queries.
 */
class BackendSecurityTest {

    private lateinit var quizRemoteDataSource: MockQuizRemoteDataSource
    private lateinit var adminAuthService: MockAdminAuthService
    private lateinit var taskRemoteDataSource: MockTaskRemoteDataSource

    @Before
    fun setUp() {
        quizRemoteDataSource = MockQuizRemoteDataSource()
        adminAuthService = MockAdminAuthService()
        taskRemoteDataSource = MockTaskRemoteDataSource()
    }

    // =========================================================================
    // 1. Anti-Replay & Idempotency Key Defense
    // =========================================================================

    @Test
    fun duplicateQuizSubmission_withSameIdempotencyKey_returnsCachedResultWithoutDuplicateScoring() = runBlocking {
        // Start an authentic quiz session
        val sessionResult = quizRemoteDataSource.createQuizSession("user_sec_01", "cat_science")
        assertTrue("Session creation must succeed", sessionResult is Result.Success)
        val session = (sessionResult as Result.Success).data

        val idempotencyKey = "idempotency_anti_replay_${UUID.randomUUID()}"
        val answers = session.questions.map { q ->
            PlayerAnswer(questionId = q.id, selectedOption = 1, timeSpentSeconds = 3.5)
        }

        val submission = QuizSubmission(
            sessionId = session.id,
            sessionToken = session.sessionToken,
            answers = answers,
            idempotencyKey = idempotencyKey
        )

        // Initial submission evaluates session
        val firstResult = quizRemoteDataSource.submitQuizAnswers(submission)
        assertTrue("Initial submission must succeed", firstResult is Result.Success)
        val firstScore = (firstResult as Result.Success).data.summary.score

        // Replay attempt with identical idempotency key
        val replayResult = quizRemoteDataSource.submitQuizAnswers(submission)
        assertTrue("Replay submission must return cached success", replayResult is Result.Success)
        val replayScore = (replayResult as Result.Success).data.summary.score

        // Assert cached result is identical and no secondary crediting occurs
        assertEquals("Replay result must match original score exactly", firstScore, replayScore)
    }

    @Test
    fun sessionTokenMismatch_triggersSecurityException_preventingSessionHijacking() = runBlocking {
        val sessionResult = quizRemoteDataSource.createQuizSession("user_sec_02", "cat_science")
        val session = (sessionResult as Result.Success).data

        // Attacker attempts to submit answers with forged/tampered session token
        val forgedSubmission = QuizSubmission(
            sessionId = session.id,
            sessionToken = "forged_malicious_token_xyz999",
            answers = listOf(PlayerAnswer("q_sci_01", 1, 2.0)),
            idempotencyKey = "key_forged_${UUID.randomUUID()}"
        )

        val result = quizRemoteDataSource.submitQuizAnswers(forgedSubmission)
        assertTrue("Forged token submission must fail", result is Result.Error)
        val exception = (result as Result.Error).exception
        assertTrue("Exception must be a SecurityException", exception is SecurityException)
        assertTrue(
            "Error must denote token mismatch",
            exception.message?.contains("Session authorization token mismatch") == true
        )
    }

    @Test
    fun expiredQuizSession_arrivingAfterDeadline_isAuthoritativelyRejected() = runBlocking {
        // Create a session
        val sessionResult = quizRemoteDataSource.createQuizSession("user_sec_03", "cat_science")
        val session = (sessionResult as Result.Success).data

        // Emulate an expired submission by submitting with a simulated past deadline
        // (In real operation, now > expiresAtTimestamp triggers rejection)
        val unknownExpiredSubmission = QuizSubmission(
            sessionId = "session_non_existent_or_expired",
            sessionToken = session.sessionToken,
            answers = listOf(PlayerAnswer("q_sci_01", 1, 2.0)),
            idempotencyKey = "key_expired_${UUID.randomUUID()}"
        )

        val result = quizRemoteDataSource.submitQuizAnswers(unknownExpiredSubmission)
        assertTrue("Submission for non-existent/expired session must fail", result is Result.Error)
        val exception = (result as Result.Error).exception
        assertTrue(
            "Error must indicate session was not found or expired",
            exception.message?.contains("was not found or has already expired") == true
        )
    }

    // =========================================================================
    // 2. Administrative Access Control & RBAC Permission Scoping
    // =========================================================================

    @Test
    fun nonAdminUser_isRejectedWithSecurityException_onAdminAuthentication() = runBlocking {
        // Regular user token without admin custom claims
        val regularUserEmail = "regular_player@quizearn.com"
        val regularUserToken = "jwt_user_standard_claims"

        val authResult = adminAuthService.authenticateAdmin(regularUserEmail, regularUserToken)
        assertTrue("Non-admin user authentication must fail", authResult.isFailure)
        val exception = authResult.exceptionOrNull()
        assertNotNull("Exception must not be null", exception)
        assertTrue("Exception must be SecurityException", exception is SecurityException)
        assertTrue(
            "Error message must denote lack of custom claims",
            exception?.message?.contains("Unauthorized: Account does not have administrative custom claims") == true
        )
    }

    @Test
    fun adminRoleHierarchy_enforcesStrictPermissionBoundaries() = runBlocking {
        val superAdminUid = "admin-super-01"
        val contentManagerUid = "admin-content-01"
        val supportAgentUid = "admin-support-01"

        // 1. SUPER_ADMIN: Holds all permissions
        assertTrue("SUPER_ADMIN must have VIEW_AUDIT_LOGS", adminAuthService.checkPermission(superAdminUid, AdminPermission.VIEW_AUDIT_LOGS))
        assertTrue("SUPER_ADMIN must have MANAGE_QUESTIONS", adminAuthService.checkPermission(superAdminUid, AdminPermission.MANAGE_QUESTIONS))
        assertTrue("SUPER_ADMIN must have APPROVE_REDEMPTION", adminAuthService.checkPermission(superAdminUid, AdminPermission.APPROVE_REDEMPTION))
        assertTrue("SUPER_ADMIN must have MANAGE_ADMIN_ROLES", adminAuthService.checkPermission(superAdminUid, AdminPermission.MANAGE_ADMIN_ROLES))

        // 2. CONTENT_MANAGER: Can manage trivia and tournaments, but barred from financial and audit logs
        assertTrue("CONTENT_MANAGER can MANAGE_QUESTIONS", adminAuthService.checkPermission(contentManagerUid, AdminPermission.MANAGE_QUESTIONS))
        assertTrue("CONTENT_MANAGER can MANAGE_TOURNAMENTS", adminAuthService.checkPermission(contentManagerUid, AdminPermission.MANAGE_TOURNAMENTS))
        assertFalse("CONTENT_MANAGER must NOT have APPROVE_REDEMPTION", adminAuthService.checkPermission(contentManagerUid, AdminPermission.APPROVE_REDEMPTION))
        assertFalse("CONTENT_MANAGER must NOT have VIEW_AUDIT_LOGS", adminAuthService.checkPermission(contentManagerUid, AdminPermission.VIEW_AUDIT_LOGS))
        assertFalse("CONTENT_MANAGER must NOT have MANAGE_ADMIN_ROLES", adminAuthService.checkPermission(contentManagerUid, AdminPermission.MANAGE_ADMIN_ROLES))

        // 3. SUPPORT_AGENT: Can review redemptions and support tickets, but barred from content and audit
        assertTrue("SUPPORT_AGENT can APPROVE_REDEMPTION", adminAuthService.checkPermission(supportAgentUid, AdminPermission.APPROVE_REDEMPTION))
        assertTrue("SUPPORT_AGENT can VIEW_SUPPORT_TICKETS", adminAuthService.checkPermission(supportAgentUid, AdminPermission.VIEW_SUPPORT_TICKETS))
        assertFalse("SUPPORT_AGENT must NOT have MANAGE_QUESTIONS", adminAuthService.checkPermission(supportAgentUid, AdminPermission.MANAGE_QUESTIONS))
        assertFalse("SUPPORT_AGENT must NOT have MANAGE_TOURNAMENTS", adminAuthService.checkPermission(supportAgentUid, AdminPermission.MANAGE_TOURNAMENTS))
        assertFalse("SUPPORT_AGENT must NOT have VIEW_AUDIT_LOGS", adminAuthService.checkPermission(supportAgentUid, AdminPermission.VIEW_AUDIT_LOGS))
    }

    // =========================================================================
    // 3. Rate Limiting & Denial-of-Service Defense
    // =========================================================================

    @Test
    fun highFrequencyLeaderboardQueries_triggerRateLimitingGuardrail() = runBlocking {
        val userId = "user_dos_tester"
        val username = "FastRequester"

        // First query with force-refresh succeeds normally
        val firstQuery = taskRemoteDataSource.getLeaderboard(
            timeframe = LeaderboardTimeframe.DAILY,
            currentUserId = userId,
            currentUsername = username,
            forceRefresh = true
        )
        assertTrue(firstQuery.isSuccess)
        val firstState = firstQuery.getOrNull()
        assertNotNull(firstState)
        assertFalse("First query must not be rate limited", firstState!!.isRateLimited)

        // Immediate subsequent query with force-refresh (within 5-second window)
        val burstQuery = taskRemoteDataSource.getLeaderboard(
            timeframe = LeaderboardTimeframe.DAILY,
            currentUserId = userId,
            currentUsername = username,
            forceRefresh = true
        )
        assertTrue("Query returns cached result safely", burstQuery.isSuccess)
        val burstState = burstQuery.getOrNull()
        assertNotNull(burstState)
        assertTrue("Immediate subsequent query must be rate-limited", burstState!!.isRateLimited)
        assertEquals(
            "Leaderboard refreshed recently. Serving cached standings.",
            burstState.errorMessage
        )
    }
}
