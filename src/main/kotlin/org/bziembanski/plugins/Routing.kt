package org.bziembanski.plugins

import org.bziembanski.toJPG.ToJpgService
import org.bziembanski.toJPG.toJpg
import io.ktor.server.routing.*
import io.ktor.server.http.content.*
import io.ktor.server.application.*

fun Application.configureRouting() {


    routing {
        // Static plugin. Try to access `/static/index.html`
        resource("/", "index.html", "static")
        toJpg(ToJpgService())
    }
}
