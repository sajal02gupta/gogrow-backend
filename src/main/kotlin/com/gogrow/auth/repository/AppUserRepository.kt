package com.gogrow.auth.repository

import com.gogrow.auth.domain.AppUser
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface AppUserRepository : JpaRepository<AppUser, Long> {
    fun findByPhoneNumber(phoneNumber: String): Optional<AppUser>
}
