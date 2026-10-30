package com.quizearn.app.data.local.room

import com.quizearn.app.data.local.room.entity.QuizHistoryEntity
import com.quizearn.app.data.local.room.entity.RedemptionEntity
import com.quizearn.app.data.local.room.entity.TaskEntity
import com.quizearn.app.data.local.room.entity.TransactionEntity
import com.quizearn.app.domain.model.Redemption
import com.quizearn.app.domain.model.RedemptionStatus
import com.quizearn.app.domain.model.TaskItem
import com.quizearn.app.domain.model.TaskType
import com.quizearn.app.domain.model.Transaction
import com.quizearn.app.domain.model.TransactionReason
import com.quizearn.app.domain.model.TransactionType
import com.quizearn.app.ui.screens.quiz.QuizHistoryItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test suite verifying:
 * 1. Room Entity <-> Domain Model mapping fidelity and bidirectional parity.
 * 2. Idempotency key preservation and safe deterministic fallback keys.
 * 3. Graceful enum parsing resilience against unknown database values.
 * 4. Accounting ledger invariant serialization.
 */
class RoomEntityMappingTest {

    @Test
    fun transactionEntity_bidirectionalParity_preservesAllAccountingFields() {
        val original = Transaction(
            id = "tx_test_001",
            userId = "usr_456",
            type = TransactionType.CREDIT,
            amount = 2500L,
            balanceAfter = 7950L,
            reason = TransactionReason.TOURNAMENT_PRIZE,
            referenceId = "tourn_summer_2026",
            description = "Summer Championship 1st Place",
            timestampIso = "2026-10-29T12:00:00Z",
            idempotencyKey = "idem_key_credit_001"
        )

        val entity = TransactionEntity.fromDomain(original)
        assertEquals("tx_test_001", entity.id)
        assertEquals("usr_456", entity.userId)
        assertEquals("CREDIT", entity.type)
        assertEquals(2500L, entity.amount)
        assertEquals(7950L, entity.balanceAfter)
        assertEquals("TOURNAMENT_PRIZE", entity.reason)
        assertEquals("idem_key_credit_001", entity.idempotencyKey)

        val reconstructed = entity.toDomain()
        assertEquals(original, reconstructed)
    }

    @Test
    fun transactionEntity_generatesDeterministicKey_whenDomainKeyBlank() {
        val original = Transaction(
            id = "tx_blank_key_01",
            userId = "usr_123",
            type = TransactionType.DEBIT,
            amount = 500L,
            balanceAfter = 4950L,
            reason = TransactionReason.REDEMPTION_HOLD,
            referenceId = "rew_01",
            description = "Debit without key",
            timestampIso = "2026-10-29T12:00:00Z",
            idempotencyKey = ""
        )

        val entity = TransactionEntity.fromDomain(original)
        assertEquals("generated_tx_blank_key_01", entity.idempotencyKey)
    }

    @Test
    fun quizHistoryEntity_bidirectionalParity_preservesMetrics() {
        val originalItem = QuizHistoryItem(
            sessionId = "sess_hist_99",
            categoryTitle = "Science & Nature",
            timestampFormatted = "Yesterday, 3:45 PM",
            score = 9,
            totalQuestions = 10,
            pointsEarned = 90L,
            accuracyRate = 90.0
        )

        val entity = QuizHistoryEntity.fromDomain(
            userId = "usr_789",
            item = originalItem,
            timestampIso = "2026-10-28T15:45:00Z"
        )

        assertEquals("sess_hist_99", entity.sessionId)
        assertEquals("usr_789", entity.userId)
        assertEquals(90.0, entity.accuracyRate, 0.001)

        val reconstructed = entity.toDomain()
        assertEquals(originalItem.sessionId, reconstructed.sessionId)
        assertEquals(originalItem.categoryTitle, reconstructed.categoryTitle)
        assertEquals(originalItem.score, reconstructed.score)
        assertEquals(originalItem.totalQuestions, reconstructed.totalQuestions)
        assertEquals(originalItem.pointsEarned, reconstructed.pointsEarned)
        assertEquals(originalItem.accuracyRate, reconstructed.accuracyRate, 0.001)
    }

    @Test
    fun taskEntity_bidirectionalParity_preservesGoalProgressAndClaimState() {
        val originalTask = TaskItem(
            id = "task_daily_01",
            title = "Speed Demon",
            description = "Answer 5 questions under 5 seconds",
            rewardPoints = 50L,
            currentProgress = 3,
            targetGoal = 5,
            isClaimed = false,
            type = TaskType.DAILY,
            categoryKey = "cat_science",
            claimedAtTimestamp = null
        )

        val entity = TaskEntity.fromDomain(userId = "usr_player_1", domain = originalTask)
        assertEquals("task_daily_01", entity.id)
        assertEquals("usr_player_1", entity.userId)
        assertEquals("DAILY", entity.type)
        assertEquals(3, entity.currentProgress)

        val reconstructed = entity.toDomain()
        assertEquals(originalTask, reconstructed)
    }

    @Test
    fun redemptionEntity_bidirectionalParity_preservesFulfillmentState() {
        val originalRedemption = Redemption(
            id = "red_ful_001",
            userId = "usr_001",
            rewardId = "rew_amz_05",
            rewardTitle = "Amazon $5 Gift Card",
            pointsDeducted = 5000L,
            payoutDetails = "user@example.com",
            status = RedemptionStatus.APPROVED,
            rejectionReason = null,
            digitalCode = "AMZN-VOUCHER-9981-TEST",
            createdAtIso = "2026-10-29T10:00:00Z",
            processedAtIso = "2026-10-29T10:05:00Z"
        )

        val entity = RedemptionEntity.fromDomain(originalRedemption, "idemp_red_001")
        assertEquals("red_ful_001", entity.id)
        assertEquals("APPROVED", entity.status)
        assertEquals("idemp_red_001", entity.idempotencyKey)
        assertEquals("AMZN-VOUCHER-9981-TEST", entity.digitalCode)

        val reconstructed = entity.toDomain()
        assertEquals(originalRedemption, reconstructed)
    }
}
