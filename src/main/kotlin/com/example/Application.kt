package com.example

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.example.plugins.*
import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.partialcontent.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(PartialContent)
        install(AutoHeadResponse)
        configureRouting()
        configureHTTP()
        configureSerialization()
    }.start(wait = true)
}
