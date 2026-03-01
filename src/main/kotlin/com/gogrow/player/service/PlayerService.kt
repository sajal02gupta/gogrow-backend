package com.gogrow.player.service

import com.gogrow.auth.repository.AppUserRepository
import com.gogrow.player.api.CreatePlayerRequest
import com.gogrow.player.api.PlayerResponse
import com.gogrow.player.api.UpdatePlayerRequest
import com.gogrow.player.domain.Player
import com.gogrow.player.repository.PlayerRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Service
class PlayerService(
    private val playerRepository: PlayerRepository,
    private val appUserRepository: AppUserRepository,
) {
    @Transactional
    fun createPlayer(userId: UUID, request: CreatePlayerRequest): PlayerResponse {
        validateDateOfBirth(request.dateOfBirth)
        val user = appUserRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found.") }
        val player = Player(
            dateOfBirth = request.dateOfBirth,
            user = user,
            name = request.name?.trim()?.ifBlank { null },
            gender = request.gender,
            profileImageUrl = request.profileImageUrl?.trim()?.ifBlank { null },
        )
        return playerRepository.save(player).toResponse()
    }

    @Transactional
    fun updatePlayer(userId: UUID, playerId: UUID, request: UpdatePlayerRequest): PlayerResponse {
        val player = playerRepository.findByPlayerIdAndUser_IdAndDeletedAtIsNull(playerId, userId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found.")

        request.dateOfBirth?.let {
            validateDateOfBirth(it)
            player.dateOfBirth = it
        }
        request.name?.let {
            player.name = it.trim().ifBlank { null }
        }
        request.gender?.let {
            player.gender = it
        }
        request.profileImageUrl?.let {
            player.profileImageUrl = it.trim().ifBlank { null }
        }

        return playerRepository.save(player).toResponse()
    }

    @Transactional
    fun softDeletePlayer(userId: UUID, playerId: UUID): Map<String, String> {
        val player = playerRepository.findByPlayerIdAndUser_IdAndDeletedAtIsNull(playerId, userId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found.")
        player.deletedAt = Instant.now()
        playerRepository.save(player)
        return mapOf("message" to "Player soft deleted successfully.")
    }

    private fun validateDateOfBirth(dateOfBirth: LocalDate) {
        if (dateOfBirth.isAfter(LocalDate.now())) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "dateOfBirth cannot be in the future.")
        }
    }

    private fun Player.toResponse(): PlayerResponse =
        PlayerResponse(
            playerId = playerId!!,
            userId = user.id!!,
            dateOfBirth = dateOfBirth,
            name = name,
            gender = gender,
            profileImageUrl = profileImageUrl,
            createdAt = createdAt,
            modifiedAt = modifiedAt,
            deletedAt = deletedAt,
        )
}
