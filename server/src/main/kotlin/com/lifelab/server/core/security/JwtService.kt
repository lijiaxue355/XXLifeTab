package com.lifelab.server.core.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.lifelab.server.core.config.AppConfig
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

class JwtService(config: AppConfig) {
    private val issuer = config.jwtIssuer
    private val audience = config.jwtAudience
    private val algorithm = Algorithm.HMAC256(config.jwtSecret)

    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    fun createAccessToken(userId: String): String = JWT.create()
        .withIssuer(issuer)
        .withAudience(audience)
        .withClaim(USER_ID_CLAIM, userId)
        .withExpiresAt(Date.from(Instant.now().plus(7, ChronoUnit.DAYS)))
        .sign(algorithm)

    companion object {
        const val USER_ID_CLAIM = "userId"
    }
}
