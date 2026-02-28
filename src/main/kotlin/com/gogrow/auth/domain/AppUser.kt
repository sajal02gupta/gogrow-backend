package com.gogrow.auth.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "app_user")
class AppUser(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    var phoneNumber: String,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),
)
