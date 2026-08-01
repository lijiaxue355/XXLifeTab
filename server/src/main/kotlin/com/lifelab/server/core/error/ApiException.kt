package com.lifelab.server.core.error

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable

class ApiException(
    val status: HttpStatusCode,
    val code: String,
    override val message: String,
) : RuntimeException(message)

@Serializable
data class ErrorResponse(
    val code: String,
    val message: String,
)
