package net.blophy.workspace

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.ktor.sse.*

fun Application.configureRouting() {
    /*install(RequestValidation) {
        validate<String> { bodyText ->
            if (!bodyText.startsWith("Hello"))
                ValidationResult.Invalid("Body text should start with 'Hello'")
            else ValidationResult.Valid
        }
    }*/
    install(SSE)
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        sse("/hello") {
            send(ServerSentEvent("world"))
        }
    }
}
