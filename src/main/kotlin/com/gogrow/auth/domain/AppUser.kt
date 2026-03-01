package com.gogrow.auth.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "users", schema = "core")
class AppUser(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(name = "name", length = 255)
    var name: String? = null,

    @Column(name = "email", length = 255)
    var email: String? = null,

    @Column(name = "phone", nullable = false, unique = true, length = 20)
    var phone: String,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "modified_at", nullable = false)
    var modifiedAt: Instant = Instant.now(),

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null,
)

{
    @PrePersist
    fun onCreate() {
        val now = Instant.now()
        createdAt = now
    }

    @PreUpdate
    fun onUpdate() {
        modifiedAt = Instant.now()
    }
}
