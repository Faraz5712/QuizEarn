package com.quizearn.app.data.repository

import android.util.Log
import com.google.firebase.firestore.ListenerRegistration
import com.quizearn.app.core.result.Result
import com.quizearn.app.data.remote.FirebaseBackend
import com.quizearn.app.domain.model.Redemption
import com.quizearn.app.domain.model.RedemptionStatus
import com.quizearn.app.domain.model.Reward
import com.quizearn.app.domain.model.Transaction
import com.quizearn.app.domain.model.TransactionReason
import com.quizearn.app.domain.model.TransactionType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map

class FirebaseWalletRepository private constructor() : WalletRepository {
    private val balanceState = MutableStateFlow(0L)
    private var profileListener: ListenerRegistration? = null
    @Volatile
    private var latestTransactions: List<Transaction> = emptyList()

    init {
        FirebaseBackend.auth.addAuthStateListener { auth ->
            profileListener?.remove()
            profileListener = auth.currentUser?.let { user ->
                FirebaseBackend.firestore.collection("users").document(user.uid)
                    .addSnapshotListener { snapshot, error ->
                        when {
                            error != null -> Log.e(TAG, "Failed to observe wallet profile.", error)
                            snapshot?.exists() == true -> {
                                val balance = snapshot.getLong("pointsBalance")
                                if (balance == null) {
                                    Log.e(TAG, "Wallet profile is missing pointsBalance.")
                                } else {
                                    balanceState.value = balance
                                }
                            }
                        }
                    }
            }
        }
    }

    override fun getBalance(): StateFlow<Long> = balanceState.asStateFlow()

    override fun getTransactions(): Flow<Result<List<Transaction>>> = collectionFlow("walletTransactions") { docs ->
        docs.map { document ->
            val data = document.data
            Transaction(
                id = document.id,
                userId = data.string("userId"),
                type = TransactionType.valueOf(data.string("type")),
                amount = data.long("amount"),
                balanceAfter = data.long("balanceAfter"),
                reason = TransactionReason.valueOf(data.string("reason")),
                referenceId = data["referenceId"] as? String,
                description = data.string("description"),
                timestampIso = data.string("timestampIso"),
                idempotencyKey = data.string("idempotencyKey")
            )
        }.also { latestTransactions = it }
    }

    override fun getRewardCatalog(): Flow<Result<List<Reward>>> = callbackFlow {
        val registration = FirebaseBackend.firestore.collection("rewards")
            .whereEqualTo("active", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.Error(error))
                } else if (snapshot == null) {
                    trySend(Result.Error(IllegalStateException("Firebase returned no rewards snapshot.")))
                } else {
                    trySend(try {
                        Result.Success(snapshot.documents.map { document ->
                                val data = document.data ?: error("Reward ${document.id} has no data.")
                                Reward(
                                    id = document.id,
                                    title = data.string("title"),
                                    description = data.string("description"),
                                    pointsCost = data.long("pointsCost"),
                                    imageUrl = data["imageUrl"] as? String,
                                    category = data["category"] as? String ?: "GIFT_CARD",
                                    stockQuantity = data.long("stockQuantity").toInt(),
                                    isActive = data["active"] as? Boolean ?: false,
                                    minLevelRequired = data.long("minLevelRequired").toInt()
                                )
                            })
                    } catch (exception: Exception) {
                        Result.Error(exception)
                    })
                }
            }
        awaitClose { registration.remove() }
    }

    override fun getRedemptions(): Flow<Result<List<Redemption>>> = collectionFlow("redemptions") { docs ->
        docs.map { document ->
            val data = document.data
            Redemption(
                id = document.id,
                userId = data.string("userId"),
                rewardId = data.string("rewardId"),
                rewardTitle = data.string("rewardTitle"),
                pointsDeducted = data.long("pointsDeducted"),
                payoutDetails = data.string("payoutDetails"),
                status = RedemptionStatus.valueOf(data.string("status")),
                rejectionReason = data["rejectionReason"] as? String,
                digitalCode = data["digitalCode"] as? String,
                createdAtIso = data.string("createdAtIso"),
                processedAtIso = data["processedAtIso"] as? String
            )
        }
    }

    override suspend fun credit(
        userId: String,
        amount: Long,
        reason: TransactionReason,
        referenceId: String?,
        description: String,
        idempotencyKey: String
    ): Result<Transaction> = serverManagedMutation()

    override suspend fun debit(
        userId: String,
        amount: Long,
        reason: TransactionReason,
        referenceId: String?,
        description: String,
        idempotencyKey: String
    ): Result<Transaction> = serverManagedMutation()

    override suspend fun requestRedemption(
        rewardId: String,
        payoutDetails: String,
        idempotencyKey: String
    ): Result<Redemption> = callRedemption(
        "requestRedemption",
        mapOf("rewardId" to rewardId, "payoutDetails" to payoutDetails, "idempotencyKey" to idempotencyKey)
    )

    override suspend fun approveRedemption(redemptionId: String, digitalCode: String): Result<Redemption> =
        callRedemption(
            "processRedemption",
            mapOf("redemptionId" to redemptionId, "decision" to "APPROVE", "digitalCode" to digitalCode)
        )

    override suspend fun rejectRedemption(redemptionId: String, reason: String): Result<Redemption> =
        callRedemption(
            "processRedemption",
            mapOf("redemptionId" to redemptionId, "decision" to "REJECT", "reason" to reason)
        )

    override fun reconcileLedger(): LedgerReconciliationResult {
        val credits = latestTransactions.filter { it.type == TransactionType.CREDIT }.sumOf { it.amount }
        val debits = latestTransactions.filter { it.type == TransactionType.DEBIT }.sumOf { it.amount }
        val calculated = credits - debits
        val recorded = balanceState.value
        return LedgerReconciliationResult(
            calculatedBalance = calculated,
            recordedBalance = recorded,
            totalCredits = credits,
            totalDebits = debits,
            isBalanced = calculated == recorded,
            transactionCount = latestTransactions.size
        )
    }

    private fun serverManagedMutation(): Result<Transaction> = Result.Error(
        SecurityException("Wallet amounts can only be changed by trusted Firebase server workflows.")
    )

    private suspend fun callRedemption(
        function: String,
        payload: Map<String, Any?>
    ): Result<Redemption> = try {
        val response = FirebaseBackend.call(function, payload)
        val data = response["redemption"] as? Map<*, *>
            ?: error("Firebase callable '$function' returned no redemption.")
        Result.Success(
            Redemption(
                id = data.string("id"),
                userId = data.string("userId"),
                rewardId = data.string("rewardId"),
                rewardTitle = data.string("rewardTitle"),
                pointsDeducted = data.long("pointsDeducted"),
                payoutDetails = data.string("payoutDetails"),
                status = RedemptionStatus.valueOf(data.string("status")),
                rejectionReason = data["rejectionReason"] as? String,
                digitalCode = data["digitalCode"] as? String,
                createdAtIso = data.string("createdAtIso"),
                processedAtIso = data["processedAtIso"] as? String
            )
        )
    } catch (exception: Exception) {
        Result.Error(exception)
    }

    private fun <T> collectionFlow(
        collectionName: String,
        transform: (List<com.google.firebase.firestore.QueryDocumentSnapshot>) -> T
    ): Flow<Result<T>> = callbackFlow {
        val user = FirebaseBackend.auth.currentUser
        if (user == null) {
            trySend(Result.Error(IllegalStateException("Sign in to access wallet data.")))
            close()
            return@callbackFlow
        }
        val registration = FirebaseBackend.firestore.collection("users").document(user.uid)
            .collection(collectionName)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.Error(error))
                } else {
                    trySend(
                        try {
                            Result.Success(
                                transform(
                                    snapshot?.documents.orEmpty()
                                        .filterIsInstance<com.google.firebase.firestore.QueryDocumentSnapshot>()
                                )
                            )
                        } catch (exception: Exception) {
                            Result.Error(exception)
                        }
                    )
                }
            }
        awaitClose { registration.remove() }
    }

    private fun Map<String, Any?>.string(key: String): String =
        this[key] as? String ?: error("Missing field '$key'.")

    private fun Map<String, Any?>.long(key: String): Long =
        (this[key] as? Number)?.toLong() ?: error("Missing numeric field '$key'.")

    private fun Any?.string(key: String): String =
        (this as? Map<*, *>)?.get(key) as? String ?: error("Missing field '$key'.")

    private fun Any?.long(key: String): Long =
        ((this as? Map<*, *>)?.get(key) as? Number)?.toLong() ?: error("Missing numeric field '$key'.")

    companion object {
        private const val TAG = "FirebaseWalletRepo"

        @Volatile
        private var instance: FirebaseWalletRepository? = null

        fun getInstance(): FirebaseWalletRepository = instance ?: synchronized(this) {
            instance ?: FirebaseWalletRepository().also { instance = it }
        }
    }
}
