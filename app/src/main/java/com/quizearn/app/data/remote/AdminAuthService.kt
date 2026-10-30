package com.quizearn.app.data.remote

import com.quizearn.app.domain.model.AdminAuditLog
import com.quizearn.app.domain.model.AdminPermission
import com.quizearn.app.domain.model.AdminRole
import com.quizearn.app.domain.model.AdminUser
import com.quizearn.app.domain.model.hasPermission
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Service contract for administrative authentication, role verification,
 * and immutable audit logging.
 * ZERO SECRETS POLICY: No admin passwords or service account credentials exist in code.
 */
interface AdminAuthService {
    /**
     * Authenticates an admin operator, verifying valid administrative custom claims.
     */
    suspend fun authenticateAdmin(email: String, token: String): Result<AdminUser>

    /**
     * Asserts that the currently authenticated operator possesses [permission].
     * Returns true if authorized, false otherwise.
     */
    suspend fun checkPermission(adminUid: String, permission: AdminPermission): Boolean

    /**
     * Records a privileged administrative action into the tamper-resistant audit log.
     */
    suspend fun recordAuditLog(
        adminUid: String,
        action: String,
        targetType: String,
        targetId: String,
        details: String,
        ipAddress: String? = null
    ): Result<AdminAuditLog>

    /**
     * Retrieves the audit log stream (strictly restricted to SUPER_ADMIN).
     */
    fun getAuditLogs(): Flow<List<AdminAuditLog>>
}

/**
 * Mock / Local development implementation of [AdminAuthService].
 * Provides deterministic simulation of Firebase Custom Claims verification and audit trail creation.
 */
class MockAdminAuthService : AdminAuthService {

    private val adminDirectory = mutableMapOf<String, AdminUser>(
        "admin-super-01" to AdminUser(
            uid = "admin-super-01",
            email = "admin@quizearn.com",
            displayName = "Lead System Admin",
            role = AdminRole.SUPER_ADMIN,
            assignedAtIso = "2026-01-01T00:00:00Z"
        ),
        "admin-content-01" to AdminUser(
            uid = "admin-content-01",
            email = "content@quizearn.com",
            displayName = "Trivia Content Editor",
            role = AdminRole.CONTENT_MANAGER,
            assignedAtIso = "2026-01-15T00:00:00Z"
        ),
        "admin-support-01" to AdminUser(
            uid = "admin-support-01",
            email = "support@quizearn.com",
            displayName = "Tier 2 Support Specialist",
            role = AdminRole.SUPPORT_AGENT,
            assignedAtIso = "2026-02-01T00:00:00Z"
        )
    )

    private val auditLogs = MutableStateFlow<List<AdminAuditLog>>(
        listOf(
            AdminAuditLog(
                id = "LOG-INIT-001",
                adminUid = "admin-super-01",
                adminEmail = "admin@quizearn.com",
                adminRole = AdminRole.SUPER_ADMIN,
                action = "SYSTEM_INITIALIZATION",
                targetType = "PLATFORM_CORE",
                targetId = "quizearn-main",
                details = "Admin RBAC policy initialized with SuperAdmin, ContentManager, SupportAgent roles.",
                timestampIso = "2026-10-29T10:00:00Z",
                ipAddress = "127.0.0.1"
            )
        )
    )

    override suspend fun authenticateAdmin(email: String, token: String): Result<AdminUser> {
        val user = adminDirectory.values.firstOrNull { it.email.equals(email, ignoreCase = true) }
            ?: return Result.failure(SecurityException("Unauthorized: Account does not have administrative custom claims."))

        if (!user.isActive) {
            return Result.failure(SecurityException("Forbidden: Administrative account is suspended."))
        }

        return Result.success(user)
    }

    override suspend fun checkPermission(adminUid: String, permission: AdminPermission): Boolean {
        val user = adminDirectory[adminUid] ?: return false
        if (!user.isActive) return false
        return user.role.hasPermission(permission)
    }

    override suspend fun recordAuditLog(
        adminUid: String,
        action: String,
        targetType: String,
        targetId: String,
        details: String,
        ipAddress: String?
    ): Result<AdminAuditLog> {
        val user = adminDirectory[adminUid]
            ?: return Result.failure(IllegalArgumentException("Unknown administrative operator UID: $adminUid"))

        val newLog = AdminAuditLog(
            id = "LOG-${UUID.randomUUID().toString().take(8).uppercase()}",
            adminUid = user.uid,
            adminEmail = user.email,
            adminRole = user.role,
            action = action,
            targetType = targetType,
            targetId = targetId,
            details = details,
            timestampIso = "2026-10-29T14:00:00Z",
            ipAddress = ipAddress ?: "127.0.0.1"
        )

        auditLogs.value = listOf(newLog) + auditLogs.value
        return Result.success(newLog)
    }

    override fun getAuditLogs(): Flow<List<AdminAuditLog>> {
        return auditLogs.asStateFlow()
    }
}
