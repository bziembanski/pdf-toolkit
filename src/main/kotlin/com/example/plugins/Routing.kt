package com.example.plugins

import com.example.toJPG.ToJpgService
import com.example.toJPG.toJpg
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
