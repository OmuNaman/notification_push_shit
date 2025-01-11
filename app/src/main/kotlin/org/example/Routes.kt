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

    configureRouting()
}

data class TokenRegistrationRequest(
    val userId: String,
    val fcmToken: String,
    val subscribedTopic: String? = null
)

data class UpdateTopicRequest(
    val userId: String,
    val fcmToken: String,
    val newTopic: String?
)

fun Application.configureRouting() {
    val log = LoggerFactory.getLogger("Routes")
    routing {
        post("/register") {
            val registration = call.receive<TokenRegistrationRequest>()
            log.info("Received registration request: $registration")

            val existingToken = FcmTokenDao.findByUserIdAndFcmToken(registration.userId, registration.fcmToken)
            if (existingToken == null) {
                FcmTokenDao.create(registration.userId, registration.fcmToken, registration.subscribedTopic)
                call.respond(HttpStatusCode.Created)
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
        post("/updateTopic") {
            val updateTopicRequest = call.receive<UpdateTopicRequest>()
            log.info("Received update topic request: $updateTopicRequest")

            val existingToken = FcmTokenDao.findByUserIdAndFcmToken(updateTopicRequest.userId, updateTopicRequest.fcmToken)
            if (existingToken != null) {
                FcmTokenDao.updateSubscribedTopic(updateTopicRequest.userId, updateTopicRequest.fcmToken, updateTopicRequest.newTopic)
                call.respond(HttpStatusCode.OK, "Topic updated")
            } else {
                call.respond(HttpStatusCode.NotFound, "Token not found")
            }
        }
        
    }
}