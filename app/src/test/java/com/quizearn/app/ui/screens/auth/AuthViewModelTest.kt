package com.quizearn.app.ui.screens.auth

import com.quizearn.app.core.result.Result
import com.quizearn.app.data.repository.AuthRepository
import com.quizearn.app.domain.model.User
import com.quizearn.app.domain.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests verifying AuthViewModel authentication state, credential validation,
 * password length enforcement, guest login mode, and guest-to-registered account upgrades.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeAuthRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeAuthRepository()
        viewModel = AuthViewModel(authRepository = fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun authValidator_validatesEmailAndEnforcesPasswordLength() {
        // Test Email Formatting
        assertFalse("Empty email must fail", AuthValidator.isValidEmail(""))
        assertFalse("Missing @ symbol must fail", AuthValidator.isValidEmail("plainaddress"))
        assertFalse("Missing domain must fail", AuthValidator.isValidEmail("user@"))
        assertFalse("Missing username must fail", AuthValidator.isValidEmail("@domain.com"))
        assertFalse("Single letter TLD must fail", AuthValidator.isValidEmail("user@domain.c"))
        assertTrue("Standard email should pass", AuthValidator.isValidEmail("alex@quizearn.com"))
        assertTrue("Subdomain and plus tag should pass", AuthValidator.isValidEmail("alex.smith+tag@sub.domain.co"))

        // Test Password Length Enforcement (minimum 6 characters)
        assertFalse("Empty password must fail", AuthValidator.isValidPassword(""))
        assertFalse("5 character password must fail", AuthValidator.isValidPassword("12345"))
        assertTrue("6 character password must pass", AuthValidator.isValidPassword("123456"))
        assertTrue("Strong password must pass", AuthValidator.isValidPassword("P@ssw0rd!2026"))

        // Test Login Form Validation Messages
        assertEquals("Please enter your email address.", AuthValidator.validateLoginForm("", "pass123"))
        assertEquals("Please enter a valid email format.", AuthValidator.validateLoginForm("invalid-email", "pass123"))
        assertEquals("Please enter your password.", AuthValidator.validateLoginForm("user@test.com", ""))
        assertEquals("Password must be at least 6 characters.", AuthValidator.validateLoginForm("user@test.com", "123"))
        assertNull("Valid credentials should return null error", AuthValidator.validateLoginForm("user@test.com", "pass123"))

        // Test Sign Up Form Validation
        val mismatchError = AuthValidator.validateSignUpForm(
            "AlexPlayer",
            "alex@test.com",
            "mockPass123",
            "mockPassXYZ",
            true
        )
        assertEquals("Passwords do not match.", mismatchError)

        val termsError = AuthValidator.validateSignUpForm(
            "AlexPlayer",
            "alex@test.com",
            "mockPass123",
            "mockPass123",
            false
        )
        assertEquals("You must accept the Terms of Service & Privacy Policy.", termsError)
    }

    @Test
    fun signIn_withInvalidCredentials_setsValidationErrorWithoutCallingRepository() = runTest(testDispatcher) {
        // Invalid email
        viewModel.signIn("invalid_email", "secret123")
        assertTrue(viewModel.uiState.value is AuthUiState.Error)
        assertEquals(
            "Please enter a valid email format.",
            (viewModel.uiState.value as AuthUiState.Error).message
        )
        assertEquals(0, fakeRepository.signInCallCount)

        // Invalid password (< 6 chars)
        viewModel.signIn("user@test.com", "123")
        assertTrue(viewModel.uiState.value is AuthUiState.Error)
        assertEquals(
            "Password must be at least 6 characters.",
            (viewModel.uiState.value as AuthUiState.Error).message
        )
        assertEquals(0, fakeRepository.signInCallCount)
    }

    @Test
    fun signIn_withValidCredentials_transitionsToSuccess() = runTest(testDispatcher) {
        viewModel.signIn("user@test.com", "password123")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fakeRepository.signInCallCount)
        val state = viewModel.uiState.value
        assertTrue("State must be Success", state is AuthUiState.Success)
        val user = (state as AuthUiState.Success).user
        assertEquals("user@test.com", user.email)
        assertEquals("TestUser", user.username)
    }

    @Test
    fun signUp_withValidForm_registersAndLogsInUser() = runTest(testDispatcher) {
        viewModel.signUp(
            "NewQuizzer",
            "new@quizearn.com",
            "mockPass123",
            "mockPass123",
            true
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fakeRepository.signUpCallCount)
        val state = viewModel.uiState.value
        assertTrue("State must be Success", state is AuthUiState.Success)
        val user = (state as AuthUiState.Success).user
        assertEquals("new@quizearn.com", user.email)
        assertEquals("NewQuizzer", user.username)
        assertEquals(100L, user.pointsBalance) // Welcome bonus credited
    }

    @Test
    fun signInAsGuest_createsGuestSession() = runTest(testDispatcher) {
        viewModel.signInAsGuest()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fakeRepository.guestSignInCallCount)
        val state = viewModel.uiState.value
        assertTrue("State must be Success for guest", state is AuthUiState.Success)
        val user = (state as AuthUiState.Success).user
        assertTrue("Guest username must start with Guest_", user.username.startsWith("Guest_"))
        assertEquals("", user.email)
    }

    @Test
    fun upgradeGuestAccount_convertsGuestToFullAccount() = runTest(testDispatcher) {
        // Initial guest login
        viewModel.signInAsGuest()
        testDispatcher.scheduler.advanceUntilIdle()

        // Upgrade guest to permanent registered account
        viewModel.upgradeGuestAccount(
            "converted@quizearn.com",
            "mockPass123",
            "UpgradedPlayer"
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fakeRepository.upgradeCallCount)
        val state = viewModel.uiState.value
        assertTrue("State must be Success", state is AuthUiState.Success)
        val user = (state as AuthUiState.Success).user
        assertEquals("converted@quizearn.com", user.email)
        assertEquals("UpgradedPlayer", user.username)
    }

    @Test
    fun sendPasswordReset_dispatchesResetEmailSuccessfully() = runTest(testDispatcher) {
        viewModel.sendPasswordReset("user@test.com")
        testDispatcher.scheduler.advanceUntilIdle()

        val resetState = viewModel.passwordResetState.value
        assertTrue("Reset state must be Success", resetState is PasswordResetUiState.Success)
        assertEquals("user@test.com", (resetState as PasswordResetUiState.Success).email)
    }

    /**
     * Fake AuthRepository simulating email/password auth, guest session creation,
     * and guest-to-registered account conversions.
     */
    private class FakeAuthRepository : AuthRepository {

        private val _currentUser = MutableStateFlow<User?>(null)
        override val currentUser: Flow<User?> = _currentUser.asStateFlow()

        var signInCallCount = 0
        var signUpCallCount = 0
        var guestSignInCallCount = 0
        var upgradeCallCount = 0

        override fun restoreActiveSession(): Flow<User?> = currentUser

        override suspend fun signIn(email: String, password: String): Result<User> {
            signInCallCount++
            val user = User(
                id = "usr_001",
                username = "TestUser",
                email = email,
                pointsBalance = 5000L,
                level = 3,
                totalQuizzesPlayed = 15,
                accuracyRate = 85.0,
                role = UserRole.USER
            )
            _currentUser.value = user
            return Result.Success(user)
        }

        override suspend fun signUp(email: String, password: String, username: String): Result<User> {
            signUpCallCount++
            val user = User(
                id = "usr_002",
                username = username,
                email = email,
                pointsBalance = 100L,
                level = 1,
                totalQuizzesPlayed = 0,
                accuracyRate = 0.0,
                role = UserRole.USER
            )
            _currentUser.value = user
            return Result.Success(user)
        }

        override suspend fun signInAsGuest(): Result<User> {
            guestSignInCallCount++
            val guest = User(
                id = "usr_guest_999",
                username = "Guest_999",
                email = "",
                pointsBalance = 50L,
                level = 1,
                totalQuizzesPlayed = 0,
                accuracyRate = 0.0,
                role = UserRole.USER
            )
            _currentUser.value = guest
            return Result.Success(guest)
        }

        override suspend fun convertGuestToAccount(
            email: String,
            password: String,
            username: String
        ): Result<User> {
            upgradeCallCount++
            val upgraded = User(
                id = "usr_guest_999",
                username = username,
                email = email,
                pointsBalance = 250L, // Retains earned points
                level = 1,
                totalQuizzesPlayed = 2,
                accuracyRate = 100.0,
                role = UserRole.USER
            )
            _currentUser.value = upgraded
            return Result.Success(upgraded)
        }

        override suspend fun updateProfile(username: String, avatarColorHex: Long): Result<User> {
            val curr = _currentUser.value ?: return Result.Error(IllegalStateException("No user logged in"))
            val updated = curr.copy(username = username)
            _currentUser.value = updated
            return Result.Success(updated)
        }

        override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
            return Result.Success(Unit)
        }

        override suspend fun signOut(): Result<Unit> {
            _currentUser.value = null
            return Result.Success(Unit)
        }
    }
}
