package com.coastalsocial.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val id: Int = 0,
    val userId: Int = 0,
    val username: String = "",
    val displayName: String? = null,
    val profileImageUrl: String? = null,
    val content: String = "",
    val imageUrl: String? = null,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isLiked: Boolean = false,
    val createdAt: String = ""
)

@Serializable
data class CreatePostRequest(
    val content: String
)

@Serializable
data class PostsResponse(
    val success: Boolean,
    val posts: List<Post> = emptyList(),
    val message: String? = null
)

@Serializable
data class Comment(
    val id: Int = 0,
    val postId: Int = 0,
    val userId: Int = 0,
    val username: String = "",
    val displayName: String? = null,
    val profileImageUrl: String? = null,
    val content: String = "",
    val createdAt: String = ""
)

@Serializable
data class CommentsResponse(
    val success: Boolean,
    val comments: List<Comment> = emptyList()
)
