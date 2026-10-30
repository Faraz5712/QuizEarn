package com.quizearn.app.data.repository

import com.quizearn.app.domain.model.TournamentEvent
import com.quizearn.app.domain.model.TournamentParticipant
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repository interface for managing tournaments, entry registrations,
 * authoritative tie-breaking scoring, and prize disbursements.
 */
interface TournamentRepository {
    fun getTournaments(): Flow<List<TournamentEvent>>
    fun getTournamentDetails(tournamentId: String): Flow<TournamentEvent?>
    fun getTournamentLeaderboard(tournamentId: String): Flow<List<TournamentParticipant>>

    suspend fun registerForTournament(
        tournamentId: String,
        idempotencyKey: String = UUID.randomUUID().toString()
    ): Result<TournamentEvent>

    suspend fun submitTournamentScore(
        tournamentId: String,
        score: Int,
        accuracyRate: Double,
        timeElapsedSeconds: Int,
        idempotencyKey: String = UUID.randomUUID().toString()
    ): Result<TournamentParticipant>

    suspend fun submitTournamentResult(
        tournamentId: String,
        sessionId: String,
        idempotencyKey: String = UUID.randomUUID().toString()
    ): Result<TournamentParticipant>

    suspend fun finalizeTournament(tournamentId: String): Result<List<TournamentParticipant>>
    suspend fun refreshTournaments()
}
