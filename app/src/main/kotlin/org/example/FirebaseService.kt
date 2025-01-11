package org.example

import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import org.slf4j.LoggerFactory
import java.io.FileInputStream

object FirebaseService {
    private val log = LoggerFactory.getLogger(javaClass)

    init {
        // Initialize Firebase
        val serviceAccount = FileInputStream("app/serviceAccountKey.json")

        val options = FirebaseOptions.builder()
            .setCredentials(com.google.auth.oauth2.GoogleCredentials.fromStream(serviceAccount))
            .build()

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options)
        }
    }

    fun sendNotification(userIdToSendTo: String) {
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

    fun sendNotificationToTopic(topic: String, title: String, body: String) {
        try {
            DatabaseFactory.init()

            val tokens = FcmTokenDao.findBySubscribedTopic(topic)
            if (tokens.isNotEmpty()) {
                tokens.forEach { token ->
                    val message = Message.builder()
                        .setNotification(
                            Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build()
                        )
                        .setToken(token.fcmToken) // Send to individual tokens
                        .build()

                    val response = FirebaseMessaging.getInstance().send(message)
                    log.info("Successfully sent message to ${token.userId} with token ${token.fcmToken} subscribed to $topic: $response")
                }
            } else {
                log.info("No active tokens found for topic $topic")
            }
        } catch (e: Exception) {
            log.error("Error sending notification to topic: ${e.message}")
            e.printStackTrace()
        }
    }
}