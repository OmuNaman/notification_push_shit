package org.example

import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.slf4j.LoggerFactory
import java.io.FileInputStream

fun main() {
    // Initialize Firebase
    val serviceAccount = FileInputStream("app/serviceAccountKey.json")

    val options = FirebaseOptions.builder()
        .setCredentials(com.google.auth.oauth2.GoogleCredentials.fromStream(serviceAccount))
        .build()

    if (FirebaseApp.getApps().isEmpty()) {
        FirebaseApp.initializeApp(options)
    }

    // Start Ktor server
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

// This function is now separate and can be called when you want to send a notification
fun sendNotification(userIdToSendTo: String) {
    val log = LoggerFactory.getLogger("App")

    try {
        DatabaseFactory.init()

        val tokens = FcmTokenDao.findByUserId(userIdToSendTo)
        if (tokens.isNotEmpty()) {
            tokens.forEach { token ->
                val message = Message.builder()
                    .setNotification(
                        Notification.builder()
                            .setTitle("Hello from Server")
                            .setBody("This is a test notification sent to ${token.userId}!")
                            .build()
                    )
                    .setToken(token.fcmToken)
                    .build()

                val response = FirebaseMessaging.getInstance().send(message)
                log.info("Successfully sent message to ${token.userId}: $response")
            }
        } else {
            log.info("No active tokens found for user $userIdToSendTo")
        }
    } catch (e: Exception) {
        log.error("Error sending notification: ${e.message}")
        e.printStackTrace()
    }
}