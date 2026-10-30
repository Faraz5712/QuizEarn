package com.quizearn.app.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.quizearn.app.data.local.room.entity.RedemptionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for points redemption records.
 * Enforces hardware-level anti-replay via unique idempotency constraints.
 */
@Dao
interface RedemptionDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRedemption(item: RedemptionEntity): Long

    @Update
    suspend fun updateRedemption(item: RedemptionEntity)

    @Query("SELECT * FROM redemptions WHERE userId = :userId ORDER BY createdAtIso DESC")
    fun getRedemptionsFlow(userId: String): Flow<List<RedemptionEntity>>

    @Query("SELECT * FROM redemptions WHERE userId = :userId ORDER BY createdAtIso DESC")
    suspend fun getRedemptions(userId: String): List<RedemptionEntity>

    @Query("SELECT * FROM redemptions WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): RedemptionEntity?

    @Query("SELECT * FROM redemptions WHERE idempotencyKey = :key LIMIT 1")
    suspend fun getByIdempotencyKey(key: String): RedemptionEntity?
}
