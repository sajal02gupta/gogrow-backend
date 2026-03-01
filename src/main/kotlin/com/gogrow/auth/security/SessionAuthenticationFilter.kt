package com.gogrow.auth.security
import com.gogrow.auth.AuthProperties
import com.gogrow.auth.repository.AuthSessionRepository
import com.gogrow.auth.service.AuthTokenCodec
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class SessionAuthenticationFilter(
    private val authProperties: AuthProperties,
    private val authSessionRepository: AuthSessionRepository,
    private val authTokenCodec: AuthTokenCodec,
) : OncePerRequestFilter() {

    private val exemptPaths = setOf(
        "/api/auth/request-otp",
        "/api/auth/verify-otp",
        "/v3/api-docs",
        "/swagger-ui.html",
    )

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI ?: return false
        return exemptPaths.contains(path) ||
            path.startsWith("/swagger-ui/") ||
            path.startsWith("/v3/api-docs/")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (header.isNullOrBlank()) {
            writeUnauthorized(response, "Missing bearer token.")
            return
        }

        val parts = header.trim().split(Regex("\\s+"), limit = 2)
        if (parts.size != 2 || !parts[0].equals("Bearer", ignoreCase = true)) {
            writeUnauthorized(response, "Missing bearer token.")
            return
        }
        val rawToken = parts[1].trim()
        if (rawToken.isEmpty()) {
            writeUnauthorized(response, "Invalid bearer token.")
            return
        }

        val tokenHash = authTokenCodec.hash(rawToken)
        val session = authSessionRepository.findByTokenHashWithUser(tokenHash)
        if (session == null || session.revokedAt != null) {
            writeUnauthorized(response, "Session not found.")
            return
        }
        if (session.user.deletedAt != null) {
            session.revokedAt = Instant.now()
            authSessionRepository.save(session)
            writeUnauthorized(response, "User account is deleted.")
            return
        }

        val now = Instant.now()
        val inactivityThreshold = now.minus(authProperties.sessionInactivityDays, ChronoUnit.DAYS)
        if (session.lastActiveAt.isBefore(inactivityThreshold)) {
            session.revokedAt = now
            authSessionRepository.save(session)
            writeUnauthorized(
                response,
                "Session expired after ${"%d".format(authProperties.sessionInactivityDays)} days of inactivity.",
            )
            return
        }

        session.lastActiveAt = now
        authSessionRepository.save(session)

        val principal = AuthenticatedPrincipal(
            userId = session.user.id!!,
        )
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(
            principal,
            null,
            emptyList(),
        )

        filterChain.doFilter(request, response)
    }

    private fun writeUnauthorized(response: HttpServletResponse, message: String) {
        if (response.isCommitted) return
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.writer.write("{\"error\":\"${escapeJson(message)}\"}")
    }

    private fun escapeJson(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
    }
}
