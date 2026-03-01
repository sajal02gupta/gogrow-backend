package com.gogrow.player.api

import com.gogrow.auth.security.AuthenticatedPrincipal
import com.gogrow.player.service.PlayerService
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/api/players")
@SecurityRequirement(name = "bearerAuth")
class PlayerController(
    private val playerService: PlayerService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createPlayer(authentication: Authentication, @RequestBody request: CreatePlayerRequest): PlayerResponse {
        val principal = authentication.principal as? AuthenticatedPrincipal
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized")
        return playerService.createPlayer(principal.userId, request)
    }

    @PatchMapping("/{playerId}")
    fun updatePlayer(
        authentication: Authentication,
        @PathVariable playerId: UUID,
        @RequestBody request: UpdatePlayerRequest,
    ): PlayerResponse {
        val principal = authentication.principal as? AuthenticatedPrincipal
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized")
        return playerService.updatePlayer(principal.userId, playerId, request)
    }

    @PostMapping("/{playerId}/delete")
    fun softDeletePlayer(authentication: Authentication, @PathVariable playerId: UUID): Map<String, String> {
        val principal = authentication.principal as? AuthenticatedPrincipal
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized")
        return playerService.softDeletePlayer(principal.userId, playerId)
    }
}
