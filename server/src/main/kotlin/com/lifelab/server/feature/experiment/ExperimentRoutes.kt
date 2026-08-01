package com.lifelab.server.feature.experiment

import com.lifelab.server.core.security.requireUserId
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.experimentRoutes(repository: ExperimentRepository) {
    route("/experiments") {
        get {
            call.respond(repository.getExperiments(call.requireUserId()))
        }
        post {
            call.respond(
                HttpStatusCode.Created,
                repository.createOrReplace(
                    userId = call.requireUserId(),
                    request = call.receive<CreateExperimentRequest>(),
                ),
            )
        }
    }
}
