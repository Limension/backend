package net.blophy.workspace

import configureDatabases
import io.ktor.server.application.*
import net.blophy.workspace.models.MissionsService
import net.blophy.workspace.models.UserService
import net.blophy.workspace.plugins.configureAdministration
import net.blophy.workspace.plugins.configureMonitoring
import net.blophy.workspace.plugins.configureSecurity
import net.blophy.workspace.routes.configureHTTP
import net.blophy.workspace.routes.configureSockets

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    Config.environment = this.environment
    configureSecurity()
    configureHTTP()
    configureMonitoring()
    //configureDatabases()
    configureSockets()
    configureAdministration()
    configureRouting()
    val userService = UserService()
    val missionService = MissionsService()
}
