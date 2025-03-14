package net.blophy.workspace

import io.ktor.server.application.*
import net.blophy.workspace.plugins.configureAdministration
import net.blophy.workspace.plugins.configureMonitoring
import net.blophy.workspace.plugins.configureSecurity
import net.blophy.workspace.plugins.configureSerialization
import net.blophy.workspace.routes.configureDatabases
import net.blophy.workspace.routes.configureHTTP
import net.blophy.workspace.routes.configureSockets

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSecurity()
    configureSerialization()
    configureHTTP()
    configureMonitoring()
    configureDatabases()
    configureSockets()
    configureAdministration()
    configureRouting()
}
