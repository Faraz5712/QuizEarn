package com.quizearn.app.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.quizearn.app.data.local.room.dao.QuizHistoryDao
import com.quizearn.app.data.local.room.dao.RedemptionDao
import com.quizearn.app.data.local.room.dao.TaskDao
import com.quizearn.app.data.local.room.dao.TransactionDao
import com.quizearn.app.data.local.room.entity.QuizHistoryEntity
import com.quizearn.app.data.local.room.entity.RedemptionEntity
import com.quizearn.app.data.local.room.entity.TaskEntity
import com.quizearn.app.data.local.room.entity.TransactionEntity

/**
 * Authoritative SQLite Room database for QuizEarn.
 * Persists append-only wallet transactions, redemption records, quiz history, and challenges.
 */
@Database(
    entities = [
        TransactionEntity::class,
        QuizHistoryEntity::class,
        TaskEntity::class,
        RedemptionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class QuizEarnDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun quizHistoryDao(): QuizHistoryDao
    abstract fun taskDao(): TaskDao
    abstract fun redemptionDao(): RedemptionDao

    companion object {
        const val DATABASE_NAME = "quizearn.db"

        @Volatile
        private var instance: QuizEarnDatabase? = null

        fun getInstance(context: Context): QuizEarnDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    QuizEarnDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                    .also { instance = it }
            }
        }

        /**
         * Creates an isolated in-memory database instance for testing.
         */
        fun createInMemory(context: Context): QuizEarnDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                QuizEarnDatabase::class.java
            )
                .allowMainThreadQueries()
                .build()
        }
    }
}
