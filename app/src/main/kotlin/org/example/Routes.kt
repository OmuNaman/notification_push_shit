package org.example

import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

fun Application.module() {
    install(ContentNegotiation) {
        jackson {
            findAndRegisterModules()
        }
    }
    DatabaseFactory.init()
    configureRouting()
}

data class TokenRegistrationRequest(
    val userId: String,
    val fcmToken: String
)

fun Application.configureRouting() {
    val log = LoggerFactory.getLogger("Routes")
    routing {
        post("/register") {
            val registration = call.receive<TokenRegistrationRequest>()
            log.info("Received registration request: $registration")
            val existingToken = FcmTokenDao.findByUserIdAndFcmToken(registration.userId, registration.fcmToken)
            if (existingToken == null) {
                FcmTokenDao.create(registration.userId, registration.fcmToken)
                call.respond(HttpStatusCode.Created)

                // Send a notification after registration (for testing)
                FirebaseService.sendNotification(registration.userId)
            } else {
                call.respond(HttpStatusCode.OK, "Token already registered")
            }
        }

        post("/logout") {
            val logoutRequest = call.receive<TokenRegistrationRequest>()
            log.info("Received logout request: $logoutRequest")
            FcmTokenDao.deactivateToken(logoutRequest.userId, logoutRequest.fcmToken)
            call.respond(HttpStatusCode.OK)
        }
    }
}