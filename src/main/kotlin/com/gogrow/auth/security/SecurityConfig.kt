package com.gogrow.auth.security

import com.gogrow.auth.AuthProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(AuthProperties::class)
class SecurityConfig(
    private val sessionAuthenticationFilter: SessionAuthenticationFilter,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { it.disable() }
            .headers { headers -> headers.frameOptions { it.sameOrigin() } }
            .cors(Customizer.withDefaults())
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .logout { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers(
                    "/api/auth/request-otp",
                    "/api/auth/verify-otp",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                ).permitAll()
                it.anyRequest().permitAll()
            }
            .addFilterBefore(sessionAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
