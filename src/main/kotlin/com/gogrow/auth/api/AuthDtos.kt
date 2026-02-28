package com.gogrow.auth.api

import java.time.Instant

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
)

data class MeResponse(
    val userId: Long,
    val phoneNumber: String,
)
