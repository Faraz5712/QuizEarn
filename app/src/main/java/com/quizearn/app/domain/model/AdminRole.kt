package com.quizearn.app.domain.model

/**
 * Administrative roles in QuizEarn, establishing a strict Role-Based Access Control (RBAC) hierarchy.
 * Server-authoritative enforcement prevents client-side tampering via Firebase Custom Claims.
 */
enum class AdminRole(val displayName: String, val level: Int) {
    /**
     * Root system authority.
     * Full access: manages administrator roles, inspects full financial ledgers,
     * reads immutable audit logs, adjusts global platform parameters, overrides disputes.
     */
    SUPER_ADMIN("Super Administrator", 100),

    /**
     * Trivia & gameplay content authority.
     * Manages question banks (CRUD, bulk CSV/JSON import), categories, difficulty tags,
     * daily/weekly tasks, and tournament scheduling.
     * Strictly barred from wallet ledger mutations, user bans, and redemption approvals.
     */
    CONTENT_MANAGER("Content Manager", 50),

    /**
     * Customer operations & support authority.
     * Handles customer support tickets, inspects player profiles and redemption requests,
     * approves redemptions with digital voucher codes or rejects them with mandatory reasons.
     * Strictly barred from modifying questions, tournaments, or system configurations.
     */
    SUPPORT_AGENT("Support Agent", 30)
}

/**
 * Granular permissions mapped across administrative roles.
 */
enum class AdminPermission {
    // SuperAdmin Exclusive Permissions
    MANAGE_ADMIN_ROLES,
    VIEW_FINANCIAL_LEDGER,
    VIEW_AUDIT_LOGS,
    CONFIGURE_SYSTEM_SETTINGS,
    OVERRIDE_DISPUTES,

    // Content Manager Permissions
    MANAGE_CATEGORIES,
    MANAGE_QUESTIONS,
    BULK_IMPORT_QUESTIONS,
    MANAGE_TASKS,
    MANAGE_TOURNAMENTS,

    // Support Agent Permissions
    VIEW_SUPPORT_TICKETS,
    REPLY_SUPPORT_TICKETS,
    RESOLVE_SUPPORT_TICKETS,
    VIEW_USER_PROFILES,
    REVIEW_REDEMPTIONS,
    APPROVE_REDEMPTION,
    REJECT_REDEMPTION
}

/**
 * Evaluates whether an [AdminRole] possesses a specific [AdminPermission].
 * [AdminRole.SUPER_ADMIN] inherits all administrative permissions.
 */
fun AdminRole.hasPermission(permission: AdminPermission): Boolean {
    if (this == AdminRole.SUPER_ADMIN) return true

    return when (this) {
        AdminRole.CONTENT_MANAGER -> when (permission) {
            AdminPermission.MANAGE_CATEGORIES,
            AdminPermission.MANAGE_QUESTIONS,
            AdminPermission.BULK_IMPORT_QUESTIONS,
            AdminPermission.MANAGE_TASKS,
            AdminPermission.MANAGE_TOURNAMENTS -> true
            else -> false
        }
        AdminRole.SUPPORT_AGENT -> when (permission) {
            AdminPermission.VIEW_SUPPORT_TICKETS,
            AdminPermission.REPLY_SUPPORT_TICKETS,
            AdminPermission.RESOLVE_SUPPORT_TICKETS,
            AdminPermission.VIEW_USER_PROFILES,
            AdminPermission.REVIEW_REDEMPTIONS,
            AdminPermission.APPROVE_REDEMPTION,
            AdminPermission.REJECT_REDEMPTION -> true
            else -> false
        }
        AdminRole.SUPER_ADMIN -> true
    }
}

/**
 * Domain entity representing an administrative operator account.
 */
data class AdminUser(
    val uid: String,
    val email: String,
    val displayName: String,
    val role: AdminRole,
    val assignedAtIso: String,
    val lastLoginIso: String? = null,
    val isActive: Boolean = true
)

/**
 * Domain entity representing an immutable administrative audit log record.
 * Every privileged administrative action produces a tamper-resistant entry.
 */
data class AdminAuditLog(
    val id: String,
    val adminUid: String,
    val adminEmail: String,
    val adminRole: AdminRole,
    val action: String,
    val targetType: String,
    val targetId: String,
    val details: String,
    val timestampIso: String,
    val ipAddress: String? = null
)
