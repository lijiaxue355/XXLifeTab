package com.lifelab.server.feature.auth

import com.lifelab.server.core.security.JwtService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.authRoutes(repository: AuthRepository, jwtService: JwtService) {
    route("/auth") {
        post("/register") {
            val user = repository.register(call.receive<RegisterRequest>())
            call.respond(
                HttpStatusCode.Created,
                AuthResponse(jwtService.createAccessToken(user.id), user.toResponse()),
            )
        }
        post("/login") {
            val user = repository.authenticate(call.receive<LoginRequest>())
            call.respond(
                AuthResponse(jwtService.createAccessToken(user.id), user.toResponse()),
            )
        }
    }
}
