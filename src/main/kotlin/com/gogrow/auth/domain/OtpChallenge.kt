package com.gogrow.auth.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(
    name = "otp_challenge",
    schema = "auth",
    indexes = [
        Index(name = "idx_otp_challenge_phone_number", columnList = "phone_number"),
    ],
)
class OtpChallenge(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "phone_number", nullable = false, length = 20)
    var phoneNumber: String,

    @Column(name = "otp_hash", nullable = false, length = 64)
    var otpHash: String,

    @Column(name = "expires_at", nullable = false)
    var expiresAt: Instant,

    @Column(name = "consumed_at")
    var consumedAt: Instant? = null,

    @Column(name = "attempts", nullable = false)
    var attempts: Int = 0,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),
)
