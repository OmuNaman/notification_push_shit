package org.example

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

object FcmTokenDao {
    private val log = LoggerFactory.getLogger(javaClass)

    fun create(userId: String, fcmToken: String, subscribedTopic: String? = null) {
        transaction {
            FcmTokens.insert {
                it[FcmTokens.userId] = userId
                it[FcmTokens.fcmToken] = fcmToken
                it[isActive] = true
                it[userSubscribedTopic] = subscribedTopic // Set the topic
                it[createdAt] = LocalDateTime.now()
                it[updatedAt] = LocalDateTime.now()
            }
            log.info("FCM token created for user $userId")
        }
    }

    fun findByUserId(userId: String): List<FcmToken> {
        return transaction {
            FcmTokens.select { FcmTokens.userId eq userId and (FcmTokens.isActive eq true) }
                .map { it.toFcmToken() }
        }
    }

    fun findByUserIdAndFcmToken(userId: String, fcmToken: String): FcmToken? {
        return transaction {
            FcmTokens.select { FcmTokens.userId eq userId and (FcmTokens.fcmToken eq fcmToken) }
                .map { it.toFcmToken() }
                .firstOrNull()
        }
    }

    fun updateToken(userId: String, oldToken: String, newToken: String) {
        transaction {
            FcmTokens.update({ FcmTokens.userId eq userId and (FcmTokens.fcmToken eq oldToken) }) {
                it[fcmToken] = newToken
                it[isActive] = true
                it[updatedAt] = LocalDateTime.now()
            }
            log.info("FCM token updated for user $userId")
        }
    }

    fun updateSubscribedTopic(userId: String, fcmToken: String, subscribedTopic: String?) {
        transaction {
            FcmTokens.update({ FcmTokens.userId eq userId and (FcmTokens.fcmToken eq fcmToken) }) {
                it[userSubscribedTopic] = subscribedTopic
                it[updatedAt] = LocalDateTime.now()
            }
            log.info("Subscribed topic updated for user $userId")
        }
    }

    fun deactivateToken(userId: String, fcmToken: String) {
        transaction {
            FcmTokens.update({ FcmTokens.userId eq userId and (FcmTokens.fcmToken eq fcmToken) }) {
                it[isActive] = false
                it[updatedAt] = LocalDateTime.now()
            }
            log.info("FCM token deactivated for user $userId")
        }
    }
    
    fun deactivateUserTokens(userId: String) {
        transaction {
            FcmTokens.update({ FcmTokens.userId eq userId }) {
                it[isActive] = false
                it[updatedAt] = LocalDateTime.now()
            }
            log.info("All FCM tokens deactivated for user $userId")
        }
    }

    fun deleteToken(userId: String, fcmToken: String) {
        transaction {
            FcmTokens.deleteWhere { FcmTokens.userId eq userId and (FcmTokens.fcmToken eq fcmToken) }
            log.info("FCM token deleted for user $userId")
        }
    }

    fun findBySubscribedTopic(topic: String): List<FcmToken> {
        return transaction {
            FcmTokens.select { FcmTokens.userSubscribedTopic eq topic and (FcmTokens.isActive eq true) }
                .map { it.toFcmToken() }
        }
    }

    private fun ResultRow.toFcmToken(): FcmToken =
        FcmToken(
            userId = this[FcmTokens.userId],
            fcmToken = this[FcmTokens.fcmToken],
            isActive = this[FcmTokens.isActive],
            subscribedTopic = this[FcmTokens.userSubscribedTopic], // Include topic
            createdAt = this[FcmTokens.createdAt],
            updatedAt = this[FcmTokens.updatedAt]
        )
}

data class FcmToken(
    val userId: String,
    val fcmToken: String,
    val isActive: Boolean,
    val subscribedTopic: String?, 
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)