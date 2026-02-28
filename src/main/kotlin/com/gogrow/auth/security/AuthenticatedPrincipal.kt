package com.gogrow.auth.security

data class AuthenticatedPrincipal(
    val userId: Long,
    val phoneNumber: String,
)
