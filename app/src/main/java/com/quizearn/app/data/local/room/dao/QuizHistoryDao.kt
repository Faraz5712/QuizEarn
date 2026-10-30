package com.quizearn.app.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quizearn.app.data.local.room.entity.QuizHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for historical quiz session records.
 */
@Dao
interface QuizHistoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertQuizHistory(item: QuizHistoryEntity): Long

    @Query("SELECT * FROM quiz_history WHERE userId = :userId ORDER BY timestampIso DESC")
    fun getQuizHistoryFlow(userId: String): Flow<List<QuizHistoryEntity>>

    @Query("SELECT * FROM quiz_history WHERE userId = :userId ORDER BY timestampIso DESC")
    suspend fun getQuizHistory(userId: String): List<QuizHistoryEntity>

    @Query("SELECT COUNT(*) FROM quiz_history WHERE userId = :userId")
    suspend fun getQuizCount(userId: String): Int
}
