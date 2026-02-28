package com.gogrow.auth

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.auth")
data class AuthProperties(
    var otpExpiryMinutes: Long = 5,
    var otpMaxAttempts: Int = 5,
    var sessionInactivityDays: Long = 30,
    var debugReturnOtp: Boolean = false,
)
