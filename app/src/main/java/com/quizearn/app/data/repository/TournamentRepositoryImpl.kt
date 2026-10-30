package com.quizearn.app.data.repository

import com.quizearn.app.core.result.exceptionOrNull
import com.quizearn.app.core.result.isFailure
import com.quizearn.app.data.local.AuthSessionManager
import com.quizearn.app.data.remote.MockTaskRemoteDataSource
import com.quizearn.app.data.remote.FirebaseTaskRemoteDataSource
import com.quizearn.app.data.remote.TaskRemoteDataSource
import com.quizearn.app.domain.model.TournamentEvent
import com.quizearn.app.domain.model.TournamentParticipant
import com.quizearn.app.domain.model.TournamentStatus
import com.quizearn.app.domain.model.TransactionReason
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Production implementation of [TournamentRepository] coordinating entry validation,
 * atomic wallet fee deductions, authoritative scoring, and prize disbursements.
 */
class TournamentRepositoryImpl(
    private val remoteDataSource: TaskRemoteDataSource = MockTaskRemoteDataSource(),
    private val walletRepository: WalletRepository = WalletRepositoryImpl.getInstance(),
    private val sessionManager: AuthSessionManager = AuthSessionManager.getInstance(),
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : TournamentRepository {

    private val _tournaments = MutableStateFlow<List<TournamentEvent>>(emptyList())

    init {
        coroutineScope.launch {
            refreshTournaments()
        }
    }

    private suspend fun getActiveUserId(): String {
        return sessionManager.currentSession.firstOrNull()?.userId ?: "mock_user_123"
    }

    private suspend fun getActiveUserLevel(): Int {
        return sessionManager.currentSession.firstOrNull()?.level ?: 1
    }

    override fun getTournaments(): Flow<List<TournamentEvent>> = _tournaments.asStateFlow()

    override fun getTournamentDetails(tournamentId: String): Flow<TournamentEvent?> {
        return _tournaments.map { list -> list.find { it.id == tournamentId } }
    }

    override fun getTournamentLeaderboard(tournamentId: String): Flow<List<TournamentParticipant>> {
        val flow = MutableStateFlow<List<TournamentParticipant>>(emptyList())
        coroutineScope.launch {
            flow.value = remoteDataSource.getTournamentLeaderboard(tournamentId)
        }
        return flow.asStateFlow()
    }

    override suspend fun refreshTournaments() {
        val userId = getActiveUserId()
        _tournaments.value = remoteDataSource.getTournaments(userId)
    }

    override suspend fun registerForTournament(
        tournamentId: String,
        idempotencyKey: String
    ): Result<TournamentEvent> {
        val userId = getActiveUserId()
        if (remoteDataSource is FirebaseTaskRemoteDataSource) {
            val registration = remoteDataSource.registerForTournament(userId, tournamentId, idempotencyKey)
            if (registration.isSuccess) refreshTournaments()
            return registration
        }
        val userLevel = getActiveUserLevel()

        val event = remoteDataSource.getTournamentDetails(tournamentId)
            ?: return Result.failure(IllegalArgumentException("Tournament '$tournamentId' not found."))

        if (event.isRegistered) {
            return Result.success(event)
        }

        if (event.status == TournamentStatus.COMPLETED) {
            return Result.failure(IllegalStateException("Tournament has already concluded."))
        }

        // 1. Verify Minimum Level Eligibility
        if (userLevel < event.minLevelRequired) {
            return Result.failure(
                IllegalStateException("Level $userLevel player does not meet minimum requirement (Level ${event.minLevelRequired}).")
            )
        }

        // 2. Atomic Entry Fee Deduction (if fee > 0)
        if (event.entryFeePoints > 0) {
            val balance = walletRepository.getBalance().first()
            if (balance < event.entryFeePoints) {
                return Result.failure(
                    IllegalStateException("Insufficient balance ($balance Pts). Entry fee requires ${event.entryFeePoints} Pts.")
                )
            }

            val debitResult = walletRepository.debit(
                userId = userId,
                amount = event.entryFeePoints,
                reason = TransactionReason.TOURNAMENT_ENTRY_FEE,
                description = "Tournament Entry: ${event.title}",
                referenceId = tournamentId,
                idempotencyKey = idempotencyKey
            )
            if (debitResult.isFailure) {
                return Result.failure(debitResult.exceptionOrNull() ?: Exception("Failed to debit tournament entry fee."))
            }
        }

        // 3. Register participant on remote service
        val regResult = remoteDataSource.registerForTournament(userId, tournamentId, idempotencyKey)
        if (regResult.isSuccess) {
            refreshTournaments()
        }
        return regResult
    }

    override suspend fun submitTournamentScore(
        tournamentId: String,
        score: Int,
        accuracyRate: Double,
        timeElapsedSeconds: Int,
        idempotencyKey: String
    ): Result<TournamentParticipant> {
        if (remoteDataSource is FirebaseTaskRemoteDataSource) {
            return Result.failure(
                SecurityException("Submit a verified quiz result instead of client-provided tournament scores.")
            )
        }
        val userId = getActiveUserId()
        val submitResult = remoteDataSource.submitTournamentScore(
            userId = userId,
            tournamentId = tournamentId,
            score = score,
            accuracyRate = accuracyRate,
            timeElapsedSeconds = timeElapsedSeconds,
            idempotencyKey = idempotencyKey
        )
        if (submitResult.isSuccess) {
            refreshTournaments()
        }
        return submitResult
    }

    override suspend fun submitTournamentResult(
        tournamentId: String,
        sessionId: String,
        idempotencyKey: String
    ): Result<TournamentParticipant> {
        val result = remoteDataSource.submitTournamentResult(
            userId = getActiveUserId(),
            tournamentId = tournamentId,
            sessionId = sessionId,
            idempotencyKey = idempotencyKey
        )
        if (result.isSuccess) refreshTournaments()
        return result
    }

    override suspend fun finalizeTournament(tournamentId: String): Result<List<TournamentParticipant>> {
        if (remoteDataSource is FirebaseTaskRemoteDataSource) {
            val result = remoteDataSource.finalizeTournament(tournamentId)
            if (result.isSuccess) refreshTournaments()
            return result
        }
        val event = remoteDataSource.getTournamentDetails(tournamentId)
            ?: return Result.failure(IllegalArgumentException("Tournament '$tournamentId' not found."))

        val finalResult = remoteDataSource.finalizeTournament(tournamentId)
        if (finalResult.isFailure) return finalResult

        val rankedWinners = finalResult.getOrThrow()
        val totalPrizePool = event.prizePoolPoints

        // Distribute prizes to top-3 podium via immutable wallet ledger
        rankedWinners.take(3).forEach { winner ->
            val prizePoints = when (winner.rank) {
                1 -> (totalPrizePool * 0.50).toLong()
                2 -> (totalPrizePool * 0.30).toLong()
                3 -> (totalPrizePool * 0.20).toLong()
                else -> 0L
            }

            if (prizePoints > 0 && winner.userId == getActiveUserId()) {
                walletRepository.credit(
                    userId = winner.userId,
                    amount = prizePoints,
                    reason = TransactionReason.TOURNAMENT_PRIZE,
                    description = "Tournament Prize: ${event.title} (Rank #${winner.rank})",
                    referenceId = tournamentId,
                    idempotencyKey = UUID.randomUUID().toString()
                )
            }
        }

        refreshTournaments()
        return Result.success(rankedWinners)
    }

    companion object {
        @Volatile
        private var instance: TournamentRepositoryImpl? = null

        fun getInstance(
            remoteDataSource: TaskRemoteDataSource = MockTaskRemoteDataSource(),
            walletRepository: WalletRepository = WalletRepositoryImpl.getInstance(),
            sessionManager: AuthSessionManager = AuthSessionManager.getInstance()
        ): TournamentRepositoryImpl {
            return instance ?: synchronized(this) {
                instance ?: TournamentRepositoryImpl(
                    remoteDataSource = remoteDataSource,
                    walletRepository = walletRepository,
                    sessionManager = sessionManager
                ).also { instance = it }
            }
        }
    }
}
