package net.blophy.workspace

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import net.blophy.workspace.routes.user.userRoutes

/*import io.ktor.server.sse.*
import io.ktor.sse.**/

fun Application.configureRouting() {
    /*install(RequestValidation) {
        validate<String> { bodyText ->
            if (!bodyText.startsWith("Hello"))
                ValidationResult.Invalid("Body text should start with 'Hello'")
            else ValidationResult.Valid
        }
    }*/
    //install(SSE)
    install(ContentNegotiation) {
        json(Json { prettyPrint = true })
    }
    routing {
        get("/") {
            call.respond(mapOf("content" to "Hello World!"))
        }
        /*        sse("/hello") {
                    send(ServerSentEvent("world"))
                }*/
        staticResources("/static", "static")
        userRoutes()
    }
}
