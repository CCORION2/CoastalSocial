package com.coastalsocial.app.data.model

import com.google.gson.annotations.SerializedName

data class Notification(
    val id: Int,
    val uuid: String,
    @SerializedName("user_id")
    val userId: Int,
    val type: String,
    @SerializedName("reference_id")
    val referenceId: Int?,
    @SerializedName("from_user_id")
    val fromUserId: Int?,
    val content: String?,
    @SerializedName("is_read")
    val isRead: Boolean = false,
    @SerializedName("created_at")
    val createdAt: String,
    val username: String?,
    @SerializedName("full_name")
    val fullName: String?,
    @SerializedName("profile_picture")
    val profilePicture: String?,
    @SerializedName("is_verified")
    val isVerified: Boolean = false
)

data class Story(
    val id: Int,
    val uuid: String,
    @SerializedName("media_url")
    val mediaUrl: String,
    @SerializedName("media_type")
    val mediaType: String,
    val caption: String?,
    @SerializedName("views_count")
    val viewsCount: Int = 0,
    @SerializedName("is_viewed")
    val isViewed: Boolean = false,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("expires_at")
    val expiresAt: String
)

data class StoryGroup(
    val user: StoryUser,
    val stories: List<Story>
)

data class StoryUser(
    val id: Int,
    val username: String,
    val fullName: String,
    val profilePicture: String?,
    val isVerified: Boolean = false
)

data class Friend(
    val id: Int,
    val uuid: String,
    val username: String,
    @SerializedName("full_name")
    val fullName: String,
    @SerializedName("profile_picture")
    val profilePicture: String?,
    @SerializedName("is_verified")
    val isVerified: Boolean = false,
    @SerializedName("friends_since")
    val friendsSince: String? = null,
    @SerializedName("requested_at")
    val requestedAt: String? = null
)
