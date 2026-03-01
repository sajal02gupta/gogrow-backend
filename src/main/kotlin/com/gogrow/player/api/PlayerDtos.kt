package com.gogrow.player.api

import com.gogrow.player.domain.PlayerGender
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class CreatePlayerRequest(
    val dateOfBirth: LocalDate,
    val name: String? = null,
    val gender: PlayerGender? = null,
    val profileImageUrl: String? = null,
)

data class UpdatePlayerRequest(
    val dateOfBirth: LocalDate? = null,
    val name: String? = null,
    val gender: PlayerGender? = null,
    val profileImageUrl: String? = null,
)

data class PlayerResponse(
    val playerId: UUID,
    val userId: UUID,
    val dateOfBirth: LocalDate,
    val name: String?,
    val gender: PlayerGender?,
    val profileImageUrl: String?,
    val createdAt: Instant,
    val modifiedAt: Instant?,
    val deletedAt: Instant?,
)
