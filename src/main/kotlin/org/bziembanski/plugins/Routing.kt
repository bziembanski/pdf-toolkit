package org.bziembanski.plugins

import org.bziembanski.toJPG.ToJpgService
import org.bziembanski.toJPG.toJpg
import io.ktor.server.routing.*
import io.ktor.server.http.content.*
import io.ktor.server.application.*
import io.ktor.server.response.*

fun Application.configureRouting() {


    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        // Static plugin. Try to access `/static/index.html`
        static("/static") {
            resources("static")
        }
        toJpg(ToJpgService())
    }
}
