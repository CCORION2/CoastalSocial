package com.coastalsocial.app.data.model

import com.google.gson.annotations.SerializedName

data class Post(
    val id: Int,
    val uuid: String,
    @SerializedName("user_id")
    val userId: Int,
    val content: String?,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("video_url")
    val videoUrl: String?,
    val privacy: String = "public",
    @SerializedName("likes_count")
    val likesCount: Int = 0,
    @SerializedName("comments_count")
    val commentsCount: Int = 0,
    @SerializedName("shares_count")
    val sharesCount: Int = 0,
    @SerializedName("created_at")
    val createdAt: String,
    val username: String,
    @SerializedName("full_name")
    val fullName: String,
    @SerializedName("profile_picture")
    val profilePicture: String?,
    @SerializedName("is_verified")
    val isVerified: Boolean = false,
    @SerializedName("is_liked")
    val isLiked: Boolean = false,
    @SerializedName("is_saved")
    val isSaved: Boolean = false
)

data class Comment(
    val id: Int,
    val uuid: String,
    @SerializedName("post_id")
    val postId: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("parent_comment_id")
    val parentCommentId: Int?,
    val content: String,
    @SerializedName("likes_count")
    val likesCount: Int = 0,
    @SerializedName("created_at")
    val createdAt: String,
    val username: String,
    @SerializedName("full_name")
    val fullName: String,
    @SerializedName("profile_picture")
    val profilePicture: String?,
    @SerializedName("is_verified")
    val isVerified: Boolean = false,
    @SerializedName("is_liked")
    val isLiked: Boolean = false
)
