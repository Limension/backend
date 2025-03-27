package net.blophy.workspace.routes.user

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.blophy.workspace.manager.VerificationCodeManager
import net.blophy.workspace.models.UserService

fun Routing.codeRoutes() {
    route("/code") {
        post("/generate") {
            val param = call.receiveParameters()
            val email = param["email"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            if (!email.matches(Regex("^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$"))) return@post call.respond(HttpStatusCode.BadRequest)
            val username = UserService.findUserByEmail(email)?.name ?: "player"
            return@post when (VerificationCodeManager.genCode(username, email)) {
                0 -> call.respond(HttpStatusCode.OK)
                1 -> call.respond(HttpStatusCode.InternalServerError)
                else -> call.respond(HttpStatusCode.TooManyRequests)
            }
        }
        post("/verify") {
            val param = call.receiveParameters()
            val email = param["email"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            val code = param["code"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            val type = param["type"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            return@post if (VerificationCodeManager.verifyCode(email, code, type)) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }
}
