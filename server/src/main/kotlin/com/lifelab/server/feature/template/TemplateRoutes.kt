package com.lifelab.server.feature.template

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.templateRoutes(repository: TemplateRepository) {
    route("/templates") {
        get {
            call.respond(repository.getTemplates())
        }
    }
}
