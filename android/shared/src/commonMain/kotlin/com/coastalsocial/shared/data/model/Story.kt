package com.coastalsocial.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Story(
    val id: Int = 0,
    val userId: Int = 0,
    val username: String = "",
    val displayName: String? = null,
    val profileImageUrl: String? = null,
    val mediaUrl: String = "",
    val mediaType: String = "image",
    val expiresAt: String = "",
    val viewsCount: Int = 0,
    val hasViewed: Boolean = false,
    val createdAt: String = ""
)

@Serializable
data class StoriesResponse(
    val success: Boolean,
    val stories: List<Story> = emptyList()
)

@Serializable
data class StoryGroup(
    val userId: Int,
    val username: String,
    val displayName: String?,
    val profileImageUrl: String?,
    val stories: List<Story>,
    val hasUnviewed: Boolean
)
