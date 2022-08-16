package org.bziembanski

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.partialcontent.*
import org.bziembanski.plugins.configureHTTP
import org.bziembanski.plugins.configureRouting
import org.bziembanski.plugins.configureSerialization

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(PartialContent)
        install(AutoHeadResponse)
        configureRouting()
        configureHTTP()
        configureSerialization()
    }.start(wait = true)
}
