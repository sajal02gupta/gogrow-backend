package com.gogrow.auth.repository

import com.gogrow.auth.domain.OtpChallenge
import org.springframework.data.jpa.repository.JpaRepository

interface OtpChallengeRepository : JpaRepository<OtpChallenge, Long> {
    fun findFirstByPhoneNumberAndConsumedAtIsNullOrderByCreatedAtDesc(phoneNumber: String): OtpChallenge?
}
