package com.quizearn.app.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quizearn.app.data.local.room.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for wallet transactions.
 * Enforces atomic append-only ledger operations, mathematical balance calculations,
 * and unique idempotency constraint protection.
 */
@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTransaction(tx: TransactionEntity): Long

    @Query("SELECT * FROM wallet_transactions WHERE userId = :userId ORDER BY timestampIso DESC")
    fun getTransactionsFlow(userId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM wallet_transactions WHERE userId = :userId ORDER BY timestampIso DESC")
    suspend fun getTransactions(userId: String): List<TransactionEntity>

    @Query("SELECT * FROM wallet_transactions WHERE idempotencyKey = :key LIMIT 1")
    suspend fun getByIdempotencyKey(key: String): TransactionEntity?

    @Query("SELECT COALESCE(SUM(CASE WHEN type = 'CREDIT' THEN amount ELSE -amount END), 0) FROM wallet_transactions WHERE userId = :userId")
    suspend fun calculateBalance(userId: String): Long

    @Query("SELECT COALESCE(SUM(CASE WHEN type = 'CREDIT' THEN amount ELSE -amount END), 0) FROM wallet_transactions WHERE userId = :userId")
    fun calculateBalanceFlow(userId: String): Flow<Long>

    @Query("SELECT COUNT(*) FROM wallet_transactions WHERE userId = :userId")
    suspend fun getTransactionCount(userId: String): Int

    @Query("SELECT COALESCE(SUM(amount), 0) FROM wallet_transactions WHERE userId = :userId AND type = 'CREDIT'")
    suspend fun getTotalCredits(userId: String): Long

    @Query("SELECT COALESCE(SUM(amount), 0) FROM wallet_transactions WHERE userId = :userId AND type = 'DEBIT'")
    suspend fun getTotalDebits(userId: String): Long
}
