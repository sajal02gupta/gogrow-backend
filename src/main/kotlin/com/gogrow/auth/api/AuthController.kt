package com.gogrow.auth.api

import com.gogrow.auth.security.AuthenticatedPrincipal
import com.gogrow.auth.service.AuthService
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.HttpHeaders
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus

@RestController
@RequestMapping("/api")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/auth/request-otp")
    fun requestOtp(@RequestBody request: RequestOtpRequest): RequestOtpResponse =
        authService.requestOtp(request.phoneNumber)

    @PostMapping("/auth/verify-otp")
    fun verifyOtp(@RequestBody request: VerifyOtpRequest): VerifyOtpResponse =
        authService.verifyOtp(request.phoneNumber, request.otp)

    @PostMapping("/auth/logout")
    @SecurityRequirement(name = "bearerAuth")
    fun logout(@RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?): Map<String, String> {
        val parts = authorization?.trim()?.split(Regex("\\s+"), limit = 2)
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing bearer token.")
        if (parts.size != 2 || !parts[0].equals("Bearer", ignoreCase = true)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing bearer token.")
        }
        val rawToken = parts[1].trim()
        if (rawToken.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing bearer token.")
        }
        authService.logout(rawToken)
        return mapOf("message" to "Logged out successfully.")
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    fun me(authentication: Authentication): UserResponse {
        val principal = authentication.principal as? AuthenticatedPrincipal
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized")
        return authService.getUserById(principal.userId)
    }

    @PatchMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    fun updateMe(authentication: Authentication, @RequestBody request: UpdateUserRequest): UserResponse {
        val principal = authentication.principal as? AuthenticatedPrincipal
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized")
        return authService.updateUser(principal.userId, request)
    }

    @PostMapping("/me/delete")
    @SecurityRequirement(name = "bearerAuth")
    fun softDeleteMe(authentication: Authentication): Map<String, String> {
        val principal = authentication.principal as? AuthenticatedPrincipal
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized")
        return authService.softDeleteUser(principal.userId)
    }
}
