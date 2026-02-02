package com.coastalsocial.app.data.repository

import com.coastalsocial.app.data.api.ApiService
import com.coastalsocial.app.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getConversations(): Result<List<Conversation>> {
        return try {
            val response = apiService.getConversations()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data?.conversations ?: emptyList())
            } else {
                Result.failure(Exception("Konversationen laden fehlgeschlagen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMessages(userId: Int, page: Int = 1): Result<MessagesData> {
        return try {
            val response = apiService.getMessages(userId, page)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data!!)
            } else {
                Result.failure(Exception("Nachrichten laden fehlgeschlagen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendMessage(receiverId: Int, content: String): Result<String> {
        return try {
            val response = apiService.sendMessage(receiverId, mapOf("content" to content))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data?.get("uuid") ?: "")
            } else {
                Result.failure(Exception("Senden fehlgeschlagen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMessage(messageId: String): Result<Unit> {
        return try {
            val response = apiService.deleteMessage(messageId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Löschen fehlgeschlagen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@Singleton
class NotificationRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getNotifications(page: Int = 1): Result<NotificationsData> {
        return try {
            val response = apiService.getNotifications(page)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data!!)
            } else {
                Result.failure(Exception("Benachrichtigungen laden fehlgeschlagen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            val response = apiService.markNotificationRead(notificationId)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Fehler"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAllAsRead(): Result<Unit> {
        return try {
            val response = apiService.markAllNotificationsRead()
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Fehler"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
