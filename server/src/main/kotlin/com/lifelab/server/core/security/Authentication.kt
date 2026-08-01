package com.lifelab.server.core.security

import com.lifelab.server.core.config.AppConfig
import com.lifelab.server.core.error.ApiException
import com.lifelab.server.core.error.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.principal
import io.ktor.server.response.respond

fun Application.configureAuthentication(config: AppConfig, jwtService: JwtService) {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = config.jwtRealm
            verifier(jwtService.verifier)
            validate { credential ->
                credential.payload
                    .getClaim(JwtService.USER_ID_CLAIM)
                    .asString()
                    ?.takeIf(String::isNotBlank)
                    ?.let { JWTPrincipal(credential.payload) }
            }
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse("UNAUTHORIZED", "登录状态无效或已过期"),
                )
            }
        }
    }
}

fun ApplicationCall.requireUserId(): String =
    principal<JWTPrincipal>()
        ?.payload
        ?.getClaim(JwtService.USER_ID_CLAIM)
        ?.asString()
        ?.takeIf(String::isNotBlank)
        ?: throw ApiException(
            HttpStatusCode.Unauthorized,
            "UNAUTHORIZED",
            "请先登录",
        )
