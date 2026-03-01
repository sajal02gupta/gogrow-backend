package com.gogrow.auth.service

import com.gogrow.auth.AuthProperties
import com.gogrow.auth.api.RequestOtpResponse
import com.gogrow.auth.api.UpdateUserRequest
import com.gogrow.auth.api.UserResponse
import com.gogrow.auth.api.VerifyOtpResponse
import com.gogrow.auth.domain.AppUser
import com.gogrow.auth.domain.AuthSession
import com.gogrow.auth.domain.OtpChallenge
import com.gogrow.auth.repository.AppUserRepository
import com.gogrow.auth.repository.AuthSessionRepository
import com.gogrow.auth.repository.OtpChallengeRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class AuthService(
    private val authProperties: AuthProperties,
    private val userRepository: AppUserRepository,
    private val otpChallengeRepository: OtpChallengeRepository,
    private val authSessionRepository: AuthSessionRepository,
    private val authTokenCodec: AuthTokenCodec,
) {
    private val secureRandom = SecureRandom()
    private val phoneRegex = Regex("^\\+?[1-9]\\d{7,14}$")
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

    @Transactional
    fun requestOtp(rawPhoneNumber: String): RequestOtpResponse {
        val phoneNumber = normalizeAndValidatePhone(rawPhoneNumber)
        val otp = generateOtp()
        val now = Instant.now()
        val challenge = OtpChallenge(
            phoneNumber = phoneNumber,
            otpHash = authTokenCodec.hash("$phoneNumber:$otp"),
            expiresAt = now.plus(authProperties.otpExpiryMinutes, ChronoUnit.MINUTES),
            createdAt = now,
        )
        otpChallengeRepository.save(challenge)

        return RequestOtpResponse(
            message = "OTP generated successfully.",
            expiresAt = challenge.expiresAt,
            otp = if (authProperties.debugReturnOtp) otp else null,
        )
    }

    @Transactional
    fun verifyOtp(rawPhoneNumber: String, otp: String): VerifyOtpResponse {
        val phoneNumber = normalizeAndValidatePhone(rawPhoneNumber)
        val normalizedOtp = otp.trim()
        if (!normalizedOtp.matches(Regex("^\\d{6}$"))) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP must be 6 digits.")
        }

        val now = Instant.now()
        val challenge = otpChallengeRepository.findFirstByPhoneNumberAndConsumedAtIsNullOrderByCreatedAtDesc(phoneNumber)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "OTP not found. Please request a new OTP.")

        if (challenge.expiresAt.isBefore(now)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "OTP expired. Please request a new OTP.")
        }

        if (challenge.attempts >= authProperties.otpMaxAttempts) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Maximum OTP attempts reached. Request a new OTP.")
        }

        val otpHash = authTokenCodec.hash("$phoneNumber:$normalizedOtp")
        if (challenge.otpHash != otpHash) {
            challenge.attempts += 1
            otpChallengeRepository.save(challenge)
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid OTP.")
        }

        challenge.consumedAt = now
        otpChallengeRepository.save(challenge)

        val user = userRepository.findByPhone(phoneNumber).orElseGet {
            userRepository.save(AppUser(phone = phoneNumber, createdAt = now, modifiedAt = now))
        }
        if (user.deletedAt != null) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "User account is deleted.")
        }

        val token = authTokenCodec.generateToken()
        val session = AuthSession(
            user = user,
            tokenHash = authTokenCodec.hash(token),
            createdAt = now,
            lastActiveAt = now,
        )
        authSessionRepository.save(session)

        return VerifyOtpResponse(
            accessToken = token,
            sessionInactivityDays = authProperties.sessionInactivityDays,
            user = user.toResponse(),
        )
    }

    @Transactional
    fun logout(token: String) {
        val tokenHash = authTokenCodec.hash(token)
        val session = authSessionRepository.findByTokenHash(tokenHash) ?: return
        session.revokedAt = Instant.now()
        authSessionRepository.save(session)
    }

    @Transactional(readOnly = true)
    fun getUserById(userId: UUID): UserResponse {
        val user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found.") }
        return user.toResponse()
    }

    @Transactional
    fun updateUser(userId: UUID, request: UpdateUserRequest): UserResponse {
        val user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found.") }

        request.name?.let {
            user.name = it.trim().ifBlank { null }
        }

        request.email?.let {
            val normalizedEmail = it.trim().lowercase()
            if (normalizedEmail.isBlank()) {
                user.email = null
            } else {
                if (!emailRegex.matches(normalizedEmail)) {
                    throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email format.")
                }
                user.email = normalizedEmail
            }
        }

        request.phone?.let {
            val normalizedPhone = normalizeAndValidatePhone(it)
            val existingUser = userRepository.findByPhone(normalizedPhone).orElse(null)
            if (existingUser != null && existingUser.id != user.id) {
                throw ResponseStatusException(HttpStatus.CONFLICT, "Phone number is already in use.")
            }
            user.phone = normalizedPhone
        }

        val savedUser = userRepository.save(user)
        return savedUser.toResponse()
    }

    @Transactional
    fun softDeleteUser(userId: UUID): Map<String, String> {
        val user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found.") }

        val now = Instant.now()
        user.deletedAt = now
        userRepository.save(user)

        val activeSessions = authSessionRepository.findAllByUser_IdAndRevokedAtIsNull(userId)
        activeSessions.forEach { it.revokedAt = now }
        authSessionRepository.saveAll(activeSessions)

        return mapOf("message" to "User soft deleted successfully.")
    }

    private fun normalizeAndValidatePhone(rawPhoneNumber: String): String {
        val phone = rawPhoneNumber.trim().replace(" ", "")
        if (!phoneRegex.matches(phone)) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Phone number must be in international format, e.g. +14155552671.",
            )
        }
        return phone
    }

    private fun generateOtp(): String = (100000 + secureRandom.nextInt(900000)).toString()

    private fun AppUser.toResponse(): UserResponse =
        UserResponse(
            id = id!!,
            name = name,
            email = email,
            phone = phone,
            createdAt = createdAt,
            modifiedAt = modifiedAt,
            deletedAt = deletedAt,
        )
}
