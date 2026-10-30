package com.quizearn.app

import android.app.Application
import com.quizearn.app.data.local.AuthSessionManager
import com.quizearn.app.data.local.room.QuizEarnDatabase
import com.quizearn.app.data.remote.FirebaseBackend
import com.quizearn.app.data.remote.FirebaseTaskRemoteDataSource
import com.quizearn.app.data.remote.MockTaskRemoteDataSource
import com.quizearn.app.data.repository.QuizRepositoryImpl
import com.quizearn.app.data.repository.TaskRepositoryImpl
import com.quizearn.app.data.repository.TournamentRepositoryImpl
import com.quizearn.app.data.repository.WalletRepositoryImpl

class QuizEarnApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseBackend.initialize(this)

        // Initialize authoritative SQLite Room database
        val db = QuizEarnDatabase.getInstance(this)

        // Initialize session manager with persistent DataStore
        AuthSessionManager.getInstance(this)

        // Initialize repositories with persistent Room database
        WalletRepositoryImpl.getInstance(database = db)
        QuizRepositoryImpl.getInstance(database = db)
        val taskRemoteDataSource = if (FirebaseBackend.isConfigured) {
            FirebaseTaskRemoteDataSource()
        } else {
            MockTaskRemoteDataSource()
        }
        TaskRepositoryImpl.getInstance(remoteDataSource = taskRemoteDataSource, database = db)
        TournamentRepositoryImpl.getInstance(remoteDataSource = taskRemoteDataSource)
    }
}
