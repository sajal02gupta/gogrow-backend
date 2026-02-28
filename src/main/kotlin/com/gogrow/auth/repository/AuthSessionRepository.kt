package com.gogrow.auth.repository

import com.gogrow.auth.domain.AuthSession
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface AuthSessionRepository : JpaRepository<AuthSession, Long> {
    fun findByTokenHash(tokenHash: String): AuthSession?

    @Query("select s from AuthSession s join fetch s.user where s.tokenHash = :tokenHash")
    fun findByTokenHashWithUser(@Param("tokenHash") tokenHash: String): AuthSession?
}
