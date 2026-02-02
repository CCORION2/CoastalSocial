package com.coastalsocial.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    val id: Int = 0,
    val type: String = "",
    val actorId: Int = 0,
    val actorUsername: String = "",
    val actorDisplayName: String? = null,
    val actorProfileImageUrl: String? = null,
    val postId: Int? = null,
    val commentId: Int? = null,
    val isRead: Boolean = false,
    val createdAt: String = ""
)

@Serializable
data class NotificationsResponse(
    val success: Boolean,
    val notifications: List<Notification> = emptyList()
)

@Serializable
data class FriendRequest(
    val id: Int = 0,
    val senderId: Int = 0,
    val senderUsername: String = "",
    val senderDisplayName: String? = null,
    val senderProfileImageUrl: String? = null,
    val status: String = "pending",
    val createdAt: String = ""
)

@Serializable
data class FriendRequestsResponse(
    val success: Boolean,
    val requests: List<FriendRequest> = emptyList()
)
