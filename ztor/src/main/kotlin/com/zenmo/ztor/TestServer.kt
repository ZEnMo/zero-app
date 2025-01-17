package com.zenmo.ztor

import com.zenmo.ztor.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun interface StopZtor {
    fun stop(): Unit
}

@JvmOverloads
fun startTestServer(port: Int = 8082): StopZtor {
    val server = embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::vallumMinimal)
        .start(wait = false)

    return StopZtor { server.stop(1000, 1000) }
}

fun Application.vallumMinimal() {
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureAuthentication()
    val db = configureDatabases()
    configureRouting()
    configureStatusPages()
}
