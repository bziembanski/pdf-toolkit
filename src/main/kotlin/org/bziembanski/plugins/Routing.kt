package org.bziembanski.plugins

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import org.bziembanski.toJPG.ToJpgService
import org.bziembanski.toJPG.toJpg

fun Application.configureRouting() {


    routing {
        resource("/", "index.html", "static")
        toJpg(ToJpgService())
    }
}
