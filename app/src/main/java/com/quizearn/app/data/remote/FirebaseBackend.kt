package com.quizearn.app.data.remote

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.quizearn.app.BuildConfig
import kotlinx.coroutines.tasks.await

object FirebaseBackend {
    @Volatile
    private var initialized = false

    lateinit var auth: FirebaseAuth
        private set
    lateinit var firestore: FirebaseFirestore
        private set
    lateinit var functions: FirebaseFunctions
        private set

    val isConfigured: Boolean
        get() = initialized

    @Synchronized
    fun initialize(context: Context) {
        if (initialized) return
        val app = FirebaseApp.initializeApp(context.applicationContext)
        if (app == null) {
            BackendConfig.configureFirebase(false)
            return
        }

        auth = FirebaseAuth.getInstance(app)
        firestore = FirebaseFirestore.getInstance(app)
        functions = FirebaseFunctions.getInstance(app)
        initialized = true
        BackendConfig.configureFirebase(true)
        if (BuildConfig.USE_FIREBASE_EMULATOR) {
            auth.useEmulator(BackendConfig.EMULATOR_HOST, BackendConfig.EMULATOR_AUTH_PORT)
            firestore.useEmulator(BackendConfig.EMULATOR_HOST, BackendConfig.EMULATOR_FIRESTORE_PORT)
            functions.useEmulator(BackendConfig.EMULATOR_HOST, BackendConfig.EMULATOR_FUNCTIONS_PORT)
            BackendConfig.activeEnvironment = BackendEnvironment.FIREBASE_EMULATOR
        }
    }

    suspend fun call(name: String, data: Map<String, Any?> = emptyMap()): Map<String, Any?> {
        check(initialized) { "Firebase is not configured. Add app/google-services.json and enable Firebase Authentication." }
        val raw = functions.getHttpsCallable(name).call(data).await().getData()
        val map = raw as? Map<*, *> ?: error("Firebase callable '$name' returned an invalid response.")
        return map.entries.associate { (key, value) ->
            (key as? String ?: error("Firebase callable '$name' returned a non-string key.")) to value
        }
    }
}
