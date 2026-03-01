package com.gogrow.player.domain

import com.gogrow.auth.domain.AppUser
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "player", schema = "core")
class Player(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "player_id", nullable = false)
    var playerId: UUID? = null,

    @Column(name = "date_of_birth", nullable = false)
    var dateOfBirth: LocalDate,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: AppUser,

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null,

    @Column(name = "name", length = 255)
    var name: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 16)
    var gender: PlayerGender? = null,

    @Column(name = "profile_image_url", length = 1024)
    var profileImageUrl: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "modified_at")
    var modifiedAt: Instant? = null,
) {
    @PrePersist
    fun onCreate() {
        createdAt = Instant.now()
    }

    @PreUpdate
    fun onUpdate() {
        modifiedAt = Instant.now()
    }
}
