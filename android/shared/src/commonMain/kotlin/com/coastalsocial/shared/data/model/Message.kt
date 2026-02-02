package com.coastalsocial.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: Int = 0,
    val senderId: Int = 0,
    val receiverId: Int = 0,
    val content: String = "",
    val isRead: Boolean = false,
    val createdAt: String = ""
)

@Serializable
data class Conversation(
    val id: Int = 0,
    val otherUserId: Int = 0,
    val otherUsername: String = "",
    val otherDisplayName: String? = null,
    val otherProfileImageUrl: String? = null,
    val lastMessage: String? = null,
    val lastMessageTime: String? = null,
    val unreadCount: Int = 0
)

@Serializable
data class MessagesResponse(
    val success: Boolean,
    val messages: List<Message> = emptyList()
)

@Serializable
data class ConversationsResponse(
    val success: Boolean,
    val conversations: List<Conversation> = emptyList()
)
