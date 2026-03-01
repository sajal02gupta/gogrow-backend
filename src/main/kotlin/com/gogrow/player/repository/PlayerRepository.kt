package com.gogrow.player.repository

import com.gogrow.player.domain.Player
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PlayerRepository : JpaRepository<Player, UUID> {
    fun findByPlayerIdAndUser_IdAndDeletedAtIsNull(playerId: UUID, userId: UUID): Player?
}
