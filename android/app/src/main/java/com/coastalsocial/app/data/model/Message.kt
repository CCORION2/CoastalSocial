package com.coastalsocial.app.data.model

import com.google.gson.annotations.SerializedName

data class Message(
    val id: Int,
    val uuid: String,
    @SerializedName("sender_id")
    val senderId: Int,
    @SerializedName("receiver_id")
    val receiverId: Int,
    val content: String,
    @SerializedName("is_read")
    val isRead: Boolean = false,
    @SerializedName("created_at")
    val createdAt: String,
    val username: String? = null,
    @SerializedName("full_name")
    val fullName: String? = null,
    @SerializedName("profile_picture")
    val profilePicture: String? = null
)

data class Conversation(
    val id: Int,
    val uuid: String,
    val username: String,
    @SerializedName("full_name")
    val fullName: String,
    @SerializedName("profile_picture")
    val profilePicture: String?,
    @SerializedName("is_verified")
    val isVerified: Boolean = false,
    @SerializedName("last_message")
    val lastMessage: String,
    @SerializedName("last_message_time")
    val lastMessageTime: String,
    @SerializedName("is_own_message")
    val isOwnMessage: Boolean = false,
    @SerializedName("unread_count")
    val unreadCount: Int = 0
)
