package com.gogrow.auth.security

import java.util.UUID

data class AuthenticatedPrincipal(
    val userId: UUID,
)
