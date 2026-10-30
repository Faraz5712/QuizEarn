package com.quizearn.app.data.local.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.quizearn.app.domain.model.Redemption
import com.quizearn.app.domain.model.RedemptionStatus

/**
 * Room entity representing a points redemption record.
 * Unique constraint on [idempotencyKey] prevents duplicate debits at the database level.
 */
@Entity(
    tableName = "redemptions",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["idempotencyKey"], unique = true)
    ]
)
data class RedemptionEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val rewardId: String,
    val rewardTitle: String,
    val pointsDeducted: Long,
    val payoutDetails: String,
    val status: String,
    val rejectionReason: String?,
    val digitalCode: String?,
    val createdAtIso: String,
    val processedAtIso: String?,
    val idempotencyKey: String
) {
    fun toDomain(): Redemption = Redemption(
        id = id,
        userId = userId,
        rewardId = rewardId,
        rewardTitle = rewardTitle,
        pointsDeducted = pointsDeducted,
        payoutDetails = payoutDetails,
        status = try { RedemptionStatus.valueOf(status) } catch (_: Exception) { RedemptionStatus.PENDING },
        rejectionReason = rejectionReason,
        digitalCode = digitalCode,
        createdAtIso = createdAtIso,
        processedAtIso = processedAtIso
    )

    companion object {
        fun fromDomain(domain: Redemption, idempotencyKey: String = ""): RedemptionEntity = RedemptionEntity(
            id = domain.id,
            userId = domain.userId,
            rewardId = domain.rewardId,
            rewardTitle = domain.rewardTitle,
            pointsDeducted = domain.pointsDeducted,
            payoutDetails = domain.payoutDetails,
            status = domain.status.name,
            rejectionReason = domain.rejectionReason,
            digitalCode = domain.digitalCode,
            createdAtIso = domain.createdAtIso,
            processedAtIso = domain.processedAtIso,
            idempotencyKey = if (idempotencyKey.isNotBlank()) idempotencyKey else "redemption_${domain.id}"
        )
    }
}
