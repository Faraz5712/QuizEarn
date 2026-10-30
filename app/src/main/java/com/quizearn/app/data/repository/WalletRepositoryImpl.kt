package com.quizearn.app.data.repository

import androidx.room.withTransaction
import com.quizearn.app.core.result.Result
import com.quizearn.app.data.local.room.QuizEarnDatabase
import com.quizearn.app.data.local.room.entity.RedemptionEntity
import com.quizearn.app.data.local.room.entity.TransactionEntity
import com.quizearn.app.data.remote.BackendConfig
import com.quizearn.app.domain.model.Redemption
import com.quizearn.app.domain.model.RedemptionStatus
import com.quizearn.app.domain.model.Reward
import com.quizearn.app.domain.model.Transaction
import com.quizearn.app.domain.model.TransactionReason
import com.quizearn.app.domain.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Concrete implementation of [WalletRepository] enforcing:
 * 1. Append-only immutable ledger: balances cannot mutate without a matching Transaction record.
 * 2. Mathematical double-entry invariant: CurrentBalance == sum(Credits) - sum(Debits).
 * 3. Anti-concurrency race condition defense via Mutex locking.
 * 4. Zero negative balances guarantee.
 * 5. Idempotency key deduplication across all mutations backed by Room SQLite unique constraints.
 * 6. Full redemption lifecycle (PENDING -> APPROVED / REJECTED) with automatic points refund on rejection.
 */
class WalletRepositoryImpl(
    val database: QuizEarnDatabase? = null,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : WalletRepository {

    private val ledgerMutex = Mutex()
    private val initializationMutex = Mutex()

    @Volatile
    private var databaseInitialized = false

    // Starting balanced state: 10,450 credits - 5,000 debits = 5,450 current balance
    private val initialTransactions = listOf(
        Transaction(
            id = "tx_init_07",
            userId = "user_001",
            type = TransactionType.CREDIT,
            amount = 4300L,
            balanceAfter = 5450L,
            reason = TransactionReason.TOURNAMENT_PRIZE,
            referenceId = "tourn_grand_01",
            description = "Weekend Grand Championship 1st Place",
            timestampIso = "2026-10-29T10:30:00Z",
            idempotencyKey = "init_key_07"
        ),
        Transaction(
            id = "tx_init_06",
            userId = "user_001",
            type = TransactionType.DEBIT,
            amount = 5000L,
            balanceAfter = 1150L,
            reason = TransactionReason.REDEMPTION_HOLD,
            referenceId = "rew_amz_05",
            description = "Amazon $5 Digital Gift Card",
            timestampIso = "2026-10-28T16:20:00Z",
            idempotencyKey = "init_key_06"
        ),
        Transaction(
            id = "tx_init_05",
            userId = "user_001",
            type = TransactionType.CREDIT,
            amount = 1000L,
            balanceAfter = 6150L,
            reason = TransactionReason.TOURNAMENT_PRIZE,
            referenceId = "tourn_weekly_01",
            description = "Weekly Trivia Qualifier 2nd Place",
            timestampIso = "2026-10-28T12:00:00Z",
            idempotencyKey = "init_key_05"
        ),
        Transaction(
            id = "tx_init_04",
            userId = "user_001",
            type = TransactionType.CREDIT,
            amount = 75L,
            balanceAfter = 5150L,
            reason = TransactionReason.TASK_COMPLETED,
            referenceId = "task_wk_01",
            description = "Weekly Goal: Complete 5 Quizzes",
            timestampIso = "2026-10-27T19:40:00Z",
            idempotencyKey = "init_key_04"
        ),
        Transaction(
            id = "tx_init_03",
            userId = "user_001",
            type = TransactionType.CREDIT,
            amount = 50L,
            balanceAfter = 5075L,
            reason = TransactionReason.QUIZ_REWARD,
            referenceId = "cat_science",
            description = "Science & Nature Quiz Mastered (5/5)",
            timestampIso = "2026-10-27T14:15:00Z",
            idempotencyKey = "init_key_03"
        ),
        Transaction(
            id = "tx_init_02",
            userId = "user_001",
            type = TransactionType.CREDIT,
            amount = 25L,
            balanceAfter = 5025L,
            reason = TransactionReason.DAILY_CHECKIN,
            referenceId = null,
            description = "7-Day Streak Check-in Bonus",
            timestampIso = "2026-10-26T08:00:00Z",
            idempotencyKey = "init_key_02"
        ),
        Transaction(
            id = "tx_init_01",
            userId = "user_001",
            type = TransactionType.CREDIT,
            amount = 5000L,
            balanceAfter = 5000L,
            reason = TransactionReason.WELCOME_BONUS,
            referenceId = null,
            description = "QuizEarn Welcome Account Bonus",
            timestampIso = "2026-10-25T00:00:00Z",
            idempotencyKey = "init_key_01"
        )
    )

    private val _balance = MutableStateFlow(5450L)
    private val _transactions = MutableStateFlow<List<Transaction>>(initialTransactions)

    // Initial past redemptions matching tx_init_06
    private val _redemptions = MutableStateFlow<List<Redemption>>(
        listOf(
            Redemption(
                id = "red_init_01",
                userId = "user_001",
                rewardId = "rew_amz_05",
                rewardTitle = "Amazon $5 Gift Card",
                pointsDeducted = 5000L,
                payoutDetails = "user@example.com",
                status = RedemptionStatus.APPROVED,
                rejectionReason = null,
                digitalCode = "AMZN-8921-XKQW-4412",
                createdAtIso = "2026-10-28T16:20:00Z",
                processedAtIso = "2026-10-28T16:22:00Z"
            )
        )
    )

    private val processedTransactionsByIdempotency = ConcurrentHashMap<String, Transaction>()
    private val processedRedemptionsByIdempotency = ConcurrentHashMap<String, Redemption>()

    init {
        // Seed initial idempotency keys to prevent replay of initial ledger entries.
        initialTransactions.forEach { tx ->
            if (tx.idempotencyKey.isNotBlank()) {
                processedTransactionsByIdempotency[cacheKey(tx.userId, tx.idempotencyKey)] = tx
            }
        }
        _redemptions.value.forEach { red ->
            processedRedemptionsByIdempotency["red_key_${red.id}"] = red
        }

        if (database != null) {
            coroutineScope.launch {
                ensureDatabaseInitialized()
                database.transactionDao().getTransactionsFlow("user_001").collect { entities ->
                    val transactions = entities.map { it.toDomain() }
                    _transactions.value = transactions
                    _balance.value = database.transactionDao().calculateBalance("user_001")
                    transactions.forEach { transaction ->
                        processedTransactionsByIdempotency[
                            cacheKey(transaction.userId, transaction.idempotencyKey)
                        ] = transaction
                    }
                }
            }
            coroutineScope.launch {
                ensureDatabaseInitialized()
                database.redemptionDao().getRedemptionsFlow("user_001").collect { entities ->
                    _redemptions.value = entities.map { it.toDomain() }
                }
            }
        }
    }

    private val _rewards = MutableStateFlow<List<Reward>>(
        listOf(
            Reward(
                id = "rew_amz_05",
                title = "Amazon $5 Gift Card",
                description = "Digital code redeemable on amazon.com",
                pointsCost = 5000L,
                category = "GIFT_CARD",
                stockQuantity = 50,
                isActive = true,
                minLevelRequired = 1
            ),
            Reward(
                id = "rew_gplay_05",
                title = "Google Play $5 Card",
                description = "Apps, games, movies on Google Play",
                pointsCost = 5000L,
                category = "GIFT_CARD",
                stockQuantity = 45,
                isActive = true,
                minLevelRequired = 1
            ),
            Reward(
                id = "rew_topup_05",
                title = "Mobile Airtime $5",
                description = "Instant prepaid airtime top-up",
                pointsCost = 5000L,
                category = "AIRTIME",
                stockQuantity = 100,
                isActive = true,
                minLevelRequired = 1
            ),
            Reward(
                id = "rew_amz_10",
                title = "Amazon $10 Gift Card",
                description = "Digital code redeemable on amazon.com",
                pointsCost = 10000L,
                category = "GIFT_CARD",
                stockQuantity = 25,
                isActive = true,
                minLevelRequired = 2
            ),
            Reward(
                id = "rew_apple_10",
                title = "Apple $10 Gift Card",
                description = "App Store, iTunes, Apple services",
                pointsCost = 10000L,
                category = "GIFT_CARD",
                stockQuantity = 30,
                isActive = true,
                minLevelRequired = 2
            )
        )
    )

    override fun getBalance(): StateFlow<Long> = _balance.asStateFlow()

    override fun getTransactions(): Flow<Result<List<Transaction>>> =
        _transactions.map { Result.Success(it) }

    override fun getRewardCatalog(): Flow<Result<List<Reward>>> =
        _rewards.map { Result.Success(it) }

    override fun getRedemptions(): Flow<Result<List<Redemption>>> =
        _redemptions.map { Result.Success(it) }

    private fun cacheKey(userId: String, idempotencyKey: String): String = "$userId:$idempotencyKey"

    private fun matchesRequest(
        transaction: Transaction,
        userId: String,
        type: TransactionType,
        amount: Long,
        reason: TransactionReason,
        referenceId: String?
    ): Boolean =
        transaction.userId == userId &&
            transaction.type == type &&
            transaction.amount == amount &&
            transaction.reason == reason &&
            transaction.referenceId == referenceId

    private fun replayResult(
        transaction: Transaction,
        userId: String,
        type: TransactionType,
        amount: Long,
        reason: TransactionReason,
        referenceId: String?
    ): Result<Transaction> =
        if (matchesRequest(transaction, userId, type, amount, reason, referenceId)) {
            Result.Success(transaction)
        } else {
            Result.Error(IllegalArgumentException("Idempotency key was already used for a different transaction."))
        }

    private suspend fun ensureDatabaseInitialized() {
        val db = database ?: return
        if (databaseInitialized) return

        initializationMutex.withLock {
            if (databaseInitialized) return
            db.withTransaction {
                val transactionDao = db.transactionDao()
                if (transactionDao.getTransactionCount("user_001") == 0) {
                    initialTransactions.asReversed().forEach { transaction ->
                        transactionDao.insertTransaction(TransactionEntity.fromDomain(transaction))
                    }
                }

                val redemptionDao = db.redemptionDao()
                if (redemptionDao.getRedemptions("user_001").isEmpty()) {
                    _redemptions.value.forEach { redemption ->
                        redemptionDao.insertRedemption(
                            RedemptionEntity.fromDomain(redemption, "red_key_${redemption.id}")
                        )
                    }
                }
            }
            databaseInitialized = true
        }
    }

    private suspend fun mutateLedger(
        userId: String,
        amount: Long,
        type: TransactionType,
        reason: TransactionReason,
        referenceId: String?,
        description: String,
        idempotencyKey: String
    ): Result<Transaction> {
        if (userId.isBlank()) return Result.Error(IllegalArgumentException("User ID cannot be blank."))
        if (idempotencyKey.isBlank()) {
            return Result.Error(IllegalArgumentException("Idempotency key cannot be blank."))
        }
        if (amount <= 0L) {
            return Result.Error(IllegalArgumentException("${type.name.lowercase().replaceFirstChar { it.uppercase() }} amount must be greater than zero."))
        }

        val key = cacheKey(userId, idempotencyKey)
        processedTransactionsByIdempotency[key]?.let {
            return replayResult(it, userId, type, amount, reason, referenceId)
        }

        val db = database
        if (db != null) ensureDatabaseInitialized()

        val mutation: Result<Transaction> = if (db == null) {
            val existing = processedTransactionsByIdempotency[key]
            if (existing != null) {
                replayResult(existing, userId, type, amount, reason, referenceId)
            } else {
                val currentBalance = _balance.value
                if (type == TransactionType.DEBIT && currentBalance < amount) {
                    Result.Error(
                        IllegalStateException("Insufficient points balance: $currentBalance available, $amount required.")
                    )
                } else if (type == TransactionType.CREDIT && currentBalance > Long.MAX_VALUE - amount) {
                    Result.Error(ArithmeticException("Credit would exceed the supported balance range."))
                } else {
                    val newBalance = if (type == TransactionType.CREDIT) currentBalance + amount else currentBalance - amount
                    Result.Success(
                        Transaction(
                            id = "tx_${UUID.randomUUID().toString().take(8)}",
                            userId = userId,
                            type = type,
                            amount = amount,
                            balanceAfter = newBalance,
                            reason = reason,
                            referenceId = referenceId,
                            description = description,
                            timestampIso = "2026-10-29T12:55:00Z",
                            idempotencyKey = idempotencyKey
                        )
                    )
                }
            }
        } else {
            try {
                db.withTransaction {
                    val dao = db.transactionDao()
                    val existing = dao.getByIdempotencyKey(idempotencyKey)
                    if (existing != null) {
                        return@withTransaction replayResult(
                            existing.toDomain(), userId, type, amount, reason, referenceId
                        )
                    }

                    val currentBalance = dao.calculateBalance(userId)
                    if (type == TransactionType.DEBIT && currentBalance < amount) {
                        return@withTransaction Result.Error(
                            IllegalStateException(
                                "Insufficient points balance: $currentBalance available, $amount required."
                            )
                        )
                    }
                    if (type == TransactionType.CREDIT && currentBalance > Long.MAX_VALUE - amount) {
                        return@withTransaction Result.Error(
                            ArithmeticException("Credit would exceed the supported balance range.")
                        )
                    }

                    val newBalance =
                        if (type == TransactionType.CREDIT) currentBalance + amount else currentBalance - amount
                    val transaction = Transaction(
                        id = "tx_${UUID.randomUUID().toString().take(8)}",
                        userId = userId,
                        type = type,
                        amount = amount,
                        balanceAfter = newBalance,
                        reason = reason,
                        referenceId = referenceId,
                        description = description,
                        timestampIso = "2026-10-29T12:55:00Z",
                        idempotencyKey = idempotencyKey
                    )
                    dao.insertTransaction(TransactionEntity.fromDomain(transaction))
                    Result.Success(transaction)
                }
            } catch (exception: Exception) {
                val existing = db.transactionDao().getByIdempotencyKey(idempotencyKey)
                if (existing != null) {
                    replayResult(existing.toDomain(), userId, type, amount, reason, referenceId)
                } else {
                    Result.Error(exception)
                }
            }
        }

        if (mutation is Result.Success) {
            val transaction = mutation.data
            processedTransactionsByIdempotency[key] = transaction
            _balance.value = if (db == null) {
                transaction.balanceAfter
            } else {
                db.transactionDao().calculateBalance(userId)
            }
            _transactions.value = if (db == null) {
                listOf(transaction) + _transactions.value
            } else {
                db.transactionDao().getTransactions(userId).map { it.toDomain() }
            }
        }
        return mutation
    }

    override suspend fun credit(
        userId: String,
        amount: Long,
        reason: TransactionReason,
        referenceId: String?,
        description: String,
        idempotencyKey: String
    ): Result<Transaction> = ledgerMutex.withLock {
        val result = mutateLedger(
            userId, amount, TransactionType.CREDIT, reason, referenceId, description, idempotencyKey
        )
        if (result is Result.Success) {
            try {
                NotificationRepositoryImpl.getInstance().triggerNotification(
                    title = "Points Credited! +$amount Pts",
                    body = description.ifBlank { "You received $amount points ($reason)" },
                    type = com.quizearn.app.domain.model.NotificationType.WALLET_CREDIT,
                    actionRoute = "wallet"
                )
            } catch (_: Exception) {
                // A notification failure must not invalidate an already committed ledger entry.
            }
        }
        result
    }

    override suspend fun debit(
        userId: String,
        amount: Long,
        reason: TransactionReason,
        referenceId: String?,
        description: String,
        idempotencyKey: String
    ): Result<Transaction> = ledgerMutex.withLock {
        mutateLedger(
            userId, amount, TransactionType.DEBIT, reason, referenceId, description, idempotencyKey
        )
    }

    override suspend fun requestRedemption(
        rewardId: String,
        payoutDetails: String,
        idempotencyKey: String
    ): Result<Redemption> {
        // Enforce anti-fraud idempotency check
        processedRedemptionsByIdempotency[idempotencyKey]?.let { cached ->
            return Result.Success(cached)
        }

        if (payoutDetails.isBlank()) {
            return Result.Error(IllegalArgumentException("Payout details (email or phone) cannot be blank."))
        }

        val reward = _rewards.value.find { it.id == rewardId }
            ?: return Result.Error(IllegalArgumentException("Reward '$rewardId' not found in catalog."))

        if (!reward.isActive) {
            return Result.Error(IllegalStateException("Reward '${reward.title}' is currently disabled."))
        }

        if (reward.stockQuantity <= 0) {
            return Result.Error(IllegalStateException("Reward '${reward.title}' is currently out of stock."))
        }

        if (reward.pointsCost < BackendConfig.MIN_REDEMPTION_POINTS) {
            return Result.Error(
                IllegalArgumentException("Redemption amount does not meet the minimum threshold of ${BackendConfig.MIN_REDEMPTION_POINTS} points.")
            )
        }

        // Execute atomic debit through ledger
        val debitResult = debit(
            userId = "user_001",
            amount = reward.pointsCost,
            reason = TransactionReason.REDEMPTION_HOLD,
            referenceId = rewardId,
            description = "Redemption Hold: ${reward.title} ($payoutDetails)",
            idempotencyKey = "tx_redeem_$idempotencyKey"
        )

        when (debitResult) {
            is Result.Success -> {
                // Decrement stock in catalog
                _rewards.value = _rewards.value.map {
                    if (it.id == rewardId) it.copy(stockQuantity = (it.stockQuantity - 1).coerceAtLeast(0)) else it
                }

                val redemption = Redemption(
                    id = "red_${UUID.randomUUID().toString().take(8)}",
                    userId = "user_001",
                    rewardId = rewardId,
                    rewardTitle = reward.title,
                    pointsDeducted = reward.pointsCost,
                    payoutDetails = payoutDetails,
                    status = RedemptionStatus.PENDING,
                    rejectionReason = null,
                    digitalCode = null,
                    createdAtIso = "2026-10-29T12:55:00Z",
                    processedAtIso = null
                )

                if (database != null) {
                    try {
                        val redEntity = RedemptionEntity.fromDomain(redemption, idempotencyKey)
                        database.redemptionDao().insertRedemption(redEntity)
                    } catch (e: Exception) {
                        val existing = database.redemptionDao().getByIdempotencyKey(idempotencyKey)
                        if (existing != null) {
                            val dom = existing.toDomain()
                            processedRedemptionsByIdempotency[idempotencyKey] = dom
                            return Result.Success(dom)
                        }
                    }
                }

                _redemptions.value = listOf(redemption) + _redemptions.value
                processedRedemptionsByIdempotency[idempotencyKey] = redemption
                return Result.Success(redemption)
            }
            is Result.Error -> {
                return Result.Error(debitResult.exception)
            }
            is Result.Loading -> {
                return Result.Loading
            }
        }
    }

    override suspend fun approveRedemption(
        redemptionId: String,
        digitalCode: String
    ): Result<Redemption> {
        if (digitalCode.isBlank()) {
            return Result.Error(IllegalArgumentException("Fulfillment digital voucher code cannot be blank upon approval."))
        }

        val currentList = _redemptions.value
        val target = currentList.find { it.id == redemptionId }
            ?: return Result.Error(IllegalArgumentException("Redemption '$redemptionId' not found."))

        if (target.status == RedemptionStatus.APPROVED || target.status == RedemptionStatus.REJECTED) {
            return Result.Error(IllegalStateException("Cannot modify finalized redemption with status '${target.status}'."))
        }

        val updated = target.copy(
            status = RedemptionStatus.APPROVED,
            digitalCode = digitalCode,
            processedAtIso = "2026-10-29T12:56:00Z"
        )

        if (database != null) {
            try {
                database.redemptionDao().updateRedemption(RedemptionEntity.fromDomain(updated, "red_key_${target.id}"))
            } catch (_: Exception) {}
        }

        _redemptions.value = currentList.map { if (it.id == redemptionId) updated else it }

        try {
            NotificationRepositoryImpl.getInstance().triggerNotification(
                title = "Redemption Approved! 🎉",
                body = "Your ${target.rewardTitle} is approved! Digital voucher code is ready.",
                type = com.quizearn.app.domain.model.NotificationType.WALLET_CREDIT,
                actionRoute = "reward_catalog"
            )
        } catch (_: Exception) {
        }

        return Result.Success(updated)
    }

    override suspend fun rejectRedemption(
        redemptionId: String,
        reason: String
    ): Result<Redemption> {
        if (reason.isBlank()) {
            return Result.Error(IllegalArgumentException("Rejection reason is mandatory and cannot be blank."))
        }

        val currentList = _redemptions.value
        val target = currentList.find { it.id == redemptionId }
            ?: return Result.Error(IllegalArgumentException("Redemption '$redemptionId' not found."))

        if (target.status == RedemptionStatus.APPROVED || target.status == RedemptionStatus.REJECTED) {
            return Result.Error(IllegalStateException("Cannot modify finalized redemption with status '${target.status}'."))
        }

        // 1. Issue automatic ledger refund
        val refundResult = credit(
            userId = target.userId,
            amount = target.pointsDeducted,
            reason = TransactionReason.REDEMPTION_REFUND,
            referenceId = target.id,
            description = "Redemption Refund: ${target.rewardTitle} - $reason",
            idempotencyKey = "tx_refund_${target.id}"
        )

        if (refundResult is Result.Error) {
            return Result.Error(refundResult.exception)
        }

        // 2. Restore reward stock
        _rewards.value = _rewards.value.map {
            if (it.id == target.rewardId) it.copy(stockQuantity = it.stockQuantity + 1) else it
        }

        // 3. Mark redemption as REJECTED
        val updated = target.copy(
            status = RedemptionStatus.REJECTED,
            rejectionReason = reason,
            processedAtIso = "2026-10-29T12:56:00Z"
        )

        if (database != null) {
            try {
                database.redemptionDao().updateRedemption(RedemptionEntity.fromDomain(updated, "red_key_${target.id}"))
            } catch (_: Exception) {}
        }

        _redemptions.value = currentList.map { if (it.id == redemptionId) updated else it }
        return Result.Success(updated)
    }

    /**
     * Atomically credit points to the user's ledger for server-awarded events
     * (e.g. quiz session rewards, daily streak bonuses, tournament prizes).
     */
    suspend fun creditPoints(
        userId: String,
        amount: Long,
        reason: TransactionReason,
        referenceId: String?,
        description: String,
        idempotencyKey: String = referenceId?.let { "${reason.name}:$it" } ?: UUID.randomUUID().toString()
    ): Result<Long> {
        return when (
            val result = credit(
                userId = userId,
                amount = amount,
                reason = reason,
                referenceId = referenceId,
                description = description,
                idempotencyKey = idempotencyKey
            )
        ) {
            is Result.Success -> Result.Success(result.data.balanceAfter)
            is Result.Error -> Result.Error(result.exception)
            is Result.Loading -> Result.Loading
        }
    }

    override fun reconcileLedger(): LedgerReconciliationResult {
        val txs = _transactions.value
        val credits = txs.filter { it.type == TransactionType.CREDIT }.sumOf { it.amount }
        val debits = txs.filter { it.type == TransactionType.DEBIT }.sumOf { it.amount }
        val calculated = credits - debits
        val recorded = _balance.value
        return LedgerReconciliationResult(
            calculatedBalance = calculated,
            recordedBalance = recorded,
            totalCredits = credits,
            totalDebits = debits,
            isBalanced = (calculated == recorded),
            transactionCount = txs.size
        )
    }

    companion object {
        @Volatile
        private var instance: WalletRepositoryImpl? = null

        fun getMockInstance(database: QuizEarnDatabase? = null): WalletRepositoryImpl =
            instance ?: synchronized(this) {
                instance ?: WalletRepositoryImpl(database = database).also { instance = it }
            }

        fun getInstance(database: QuizEarnDatabase? = null): WalletRepository {
            if (com.quizearn.app.data.remote.FirebaseBackend.isConfigured) {
                return FirebaseWalletRepository.getInstance()
            }
            val existing = getMockInstance(database)
            if (database != null && existing.database == null) {
                return synchronized(this) {
                    if (instance?.database == null) {
                        instance = WalletRepositoryImpl(database = database)
                    }
                    instance ?: existing
                }
            }
            return existing
        }

        fun resetInstance() {
            synchronized(this) {
                instance = null
            }
        }
    }
}
