package com.quizearn.app.data.remote

/**
 * Global backend environment selection.
 * Enables zero-dependency offline local execution by default,
 * with clean transition to Firebase Emulator or Production when credentials exist.
 */
enum class BackendEnvironment {
    MOCK_LOCAL,
    FIREBASE_EMULATOR,
    FIREBASE_PRODUCTION
}

/**
 * QuizEarn backend configuration specifications.
 * ZERO SECRETS POLICY: No API keys, credentials, or service account tokens are hardcoded.
 */
object BackendConfig {
    /**
     * Active backend target. Defaults to MOCK_LOCAL for safe, sandboxed development.
     */
    var activeEnvironment: BackendEnvironment = BackendEnvironment.MOCK_LOCAL

    @Volatile
    var firebaseConfigured: Boolean = false
        private set

    fun configureFirebase(isConfigured: Boolean) {
        firebaseConfigured = isConfigured
        activeEnvironment = if (isConfigured) {
            BackendEnvironment.FIREBASE_PRODUCTION
        } else {
            BackendEnvironment.MOCK_LOCAL
        }
    }

    /**
     * Points economy constants:
     * 1,000 Points = $1.00 USD
     */
    const val POINTS_PER_USD: Int = 1000

    /**
     * Minimum points required for cashout / redemption ($5.00 USD).
     */
    const val MIN_REDEMPTION_POINTS: Int = 5000

    /**
     * Allowed grace period (in milliseconds) for network latency on quiz submission.
     */
    const val SUBMISSION_GRACE_PERIOD_MS: Long = 3000L

    /**
     * Local Firebase Emulator Host settings.
     */
    const val EMULATOR_HOST: String = "10.0.2.2" // Standard Android Emulator loopback
    const val EMULATOR_AUTH_PORT: Int = 9099
    const val EMULATOR_FIRESTORE_PORT: Int = 8080
    const val EMULATOR_FUNCTIONS_PORT: Int = 5001
}
