package com.quizearn.app.data.local.room

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room schema migrations for QuizEarn SQLite database.
 * Every schema modification must follow an explicit, non-destructive migration path.
 */
object DatabaseMigrations {

    /** Adds task period scoping and makes task IDs unique per user. */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE daily_tasks_new (
                    id TEXT NOT NULL,
                    userId TEXT NOT NULL,
                    title TEXT NOT NULL,
                    description TEXT NOT NULL,
                    rewardPoints INTEGER NOT NULL,
                    currentProgress INTEGER NOT NULL,
                    targetGoal INTEGER NOT NULL,
                    isClaimed INTEGER NOT NULL,
                    type TEXT NOT NULL,
                    categoryKey TEXT,
                    claimedAtTimestamp INTEGER,
                    periodKey TEXT NOT NULL,
                    PRIMARY KEY(id, userId)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO daily_tasks_new (
                    id, userId, title, description, rewardPoints, currentProgress,
                    targetGoal, isClaimed, type, categoryKey, claimedAtTimestamp, periodKey
                )
                SELECT
                    id, userId, title, description, rewardPoints, currentProgress,
                    targetGoal, isClaimed, type, categoryKey, claimedAtTimestamp,
                    CASE WHEN type = 'DAILY' THEN strftime('%Y-%m-%d', 'now') ELSE 'WEEKLY' END
                FROM daily_tasks
                """.trimIndent()
            )
            db.execSQL("DROP TABLE daily_tasks")
            db.execSQL("ALTER TABLE daily_tasks_new RENAME TO daily_tasks")
            db.execSQL("CREATE INDEX index_daily_tasks_userId ON daily_tasks(userId)")
            db.execSQL(
                "CREATE UNIQUE INDEX index_daily_tasks_id_userId ON daily_tasks(id, userId)"
            )
        }
    }

    val ALL_MIGRATIONS: Array<Migration> = arrayOf(
        MIGRATION_1_2
    )
}
