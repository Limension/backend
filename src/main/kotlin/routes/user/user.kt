package net.blophy.workspace.routes.user

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.blophy.workspace.Config
import net.blophy.workspace.models.NewUser
import net.blophy.workspace.models.User
import net.blophy.workspace.models.UserService
import net.blophy.workspace.models.useCache

fun Route.userRoutes() {
    post("/register") {
        if (!Config.enableRegistration) {
            return@post call.respond(HttpStatusCode.Forbidden)
        }
        val info = call.receive<NewUser>()
        if (!info.username.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            return@post call.respond(HttpStatusCode.BadRequest)
        }
        if (UserService.findUserByUsername(info.username) != null) {
            return@post call.respond(HttpStatusCode.Conflict)
        }
        return@post useCache {
            if (it.exists("register:${info.email}")) {
                it.del("register:${info.email}")
                UserService.new(info)
                return@useCache call.respond(HttpStatusCode.Created)
            } else {
                return@useCache call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }
    get("/test") {
        return@get call.respond(
            User(
                id = 1,
                username = "mja",
                password = "mja",
                email = "mja@blophy.nova",
                verified = true,
                extraEmail = emptyList(),
                group = listOf(0),
                totp = null,
                webauthn = null
            )
        )
    }
}
