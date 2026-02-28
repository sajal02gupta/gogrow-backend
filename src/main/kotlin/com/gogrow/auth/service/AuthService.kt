package com.gogrow.auth.service

import com.gogrow.auth.AuthProperties
import com.gogrow.auth.api.RequestOtpResponse
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
            otp = if (authProperties.debugReturnOtp == false) otp else null,
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

        val user = userRepository.findByPhoneNumber(phoneNumber).orElseGet {
            userRepository.save(AppUser(phoneNumber = phoneNumber, createdAt = now))
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
        )
    }

    @Transactional
    fun logout(token: String) {
        val tokenHash = authTokenCodec.hash(token)
        val session = authSessionRepository.findByTokenHash(tokenHash) ?: return
        session.revokedAt = Instant.now()
        authSessionRepository.save(session)
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
}
