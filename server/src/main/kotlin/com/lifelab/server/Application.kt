package com.lifelab.server

import com.lifelab.server.core.config.AppConfig
import com.lifelab.server.core.database.DatabaseFactory
import com.lifelab.server.core.error.ApiException
import com.lifelab.server.core.error.ErrorResponse
import com.lifelab.server.core.security.JwtService
import com.lifelab.server.core.security.configureAuthentication
import com.lifelab.server.feature.auth.AuthRepository
import com.lifelab.server.feature.auth.authRoutes
import com.lifelab.server.feature.experiment.ExperimentRepository
import com.lifelab.server.feature.experiment.experimentRoutes
import com.lifelab.server.feature.record.RecordRepository
import com.lifelab.server.feature.record.recordRoutes
import com.lifelab.server.feature.template.TemplateRepository
import com.lifelab.server.feature.template.templateRoutes
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

fun Application.module() {
    module(AppConfig.fromEnvironment())
}

fun Application.module(config: AppConfig) {
    DatabaseFactory.initialize(config.databaseUrl)
    val jwtService = JwtService(config)

    install(CallLogging)
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
                explicitNulls = false
            },
        )
    }
    install(StatusPages) {
        exception<ApiException> { call, cause ->
            call.respond(cause.status, ErrorResponse(cause.code, cause.message))
        }
        exception<BadRequestException> { call, _ ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("INVALID_REQUEST", "请求参数格式错误"),
            )
        }
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled request error", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("INTERNAL_ERROR", "服务器暂时无法处理请求"),
            )
        }
    }
    configureAuthentication(config, jwtService)

    val authRepository = AuthRepository()
    val templateRepository = TemplateRepository()
    val experimentRepository = ExperimentRepository()
    val recordRepository = RecordRepository()

    routing {
        get("/health") {
            call.respond(HealthResponse())
        }
        route("/api/v1") {
            authRoutes(authRepository, jwtService)
            templateRoutes(templateRepository)
            authenticate("auth-jwt") {
                experimentRoutes(experimentRepository)
                recordRoutes(recordRepository)
            }
        }
    }
}

@Serializable
private data class HealthResponse(
    val status: String = "ok",
    val service: String = "lifelab-server",
)
