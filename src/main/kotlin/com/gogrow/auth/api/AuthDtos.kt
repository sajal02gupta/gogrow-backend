package com.gogrow.auth.api

import java.time.Instant
import java.util.UUID

data class RequestOtpRequest(
    val phoneNumber: String,
)

data class RequestOtpResponse(
    val message: String,
    val expiresAt: Instant,
    val otp: String? = null,
)

data class VerifyOtpRequest(
    val phoneNumber: String,
    val otp: String,
)

data class VerifyOtpResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val sessionInactivityDays: Long,
    val user: UserResponse,
)

data class UserResponse(
    val id: UUID,
    val name: String?,
    val email: String?,
    val phone: String,
    val createdAt: Instant,
    val modifiedAt: Instant,
    val deletedAt: Instant?,
)

data class UpdateUserRequest(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
)
