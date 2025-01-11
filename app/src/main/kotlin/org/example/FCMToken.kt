package org.example

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object FcmTokens : Table() {
    val userId = varchar("user_id", 255).index()
    val fcmToken = text("fcm_token")
    val isActive = bool("is_active").default(true)
    val userSubscribedTopic = varchar("subscribed_topic", 255).nullable() // New column
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(userId, fcmToken)
}