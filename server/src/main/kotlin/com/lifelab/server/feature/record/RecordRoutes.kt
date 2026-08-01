package com.lifelab.server.feature.record

import com.lifelab.server.core.error.ApiException
import com.lifelab.server.core.security.requireUserId
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.recordRoutes(repository: RecordRepository) {
    route("/records") {
        get {
            call.respond(
                repository.getRecords(
                    userId = call.requireUserId(),
                    experimentId = call.request.queryParameters["experimentId"],
                    fromMillis = call.request.queryParameters["fromMillis"].toOptionalLong("fromMillis"),
                    toMillis = call.request.queryParameters["toMillis"].toOptionalLong("toMillis"),
                ),
            )
        }
        post {
            call.respond(
                HttpStatusCode.Created,
                repository.createOrReplace(
                    userId = call.requireUserId(),
                    request = call.receive<CreateRecordRequest>(),
                ),
            )
        }
    }
}

private fun String?.toOptionalLong(parameterName: String): Long? {
    if (this == null) return null
    return toLongOrNull() ?: throw ApiException(
        HttpStatusCode.BadRequest,
        "INVALID_QUERY_PARAMETER",
        "$parameterName 必须是整数",
    )
}
