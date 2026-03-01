package com.gogrow.auth.repository

import com.gogrow.auth.domain.AppUser
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface AppUserRepository : JpaRepository<AppUser, UUID> {
    fun findByPhone(phone: String): Optional<AppUser>
    fun findByIdAndDeletedAtIsNull(id: UUID): Optional<AppUser>
}
