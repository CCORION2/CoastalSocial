package com.coastalsocial.app.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    val uuid: String,
    val username: String,
    val email: String? = null,
    @SerializedName("full_name")
    val fullName: String,
    val bio: String? = null,
    @SerializedName("profile_picture")
    val profilePicture: String? = null,
    @SerializedName("cover_picture")
    val coverPicture: String? = null,
    val location: String? = null,
    val website: String? = null,
    @SerializedName("is_verified")
    val isVerified: Boolean = false,
    @SerializedName("is_private")
    val isPrivate: Boolean = false,
    @SerializedName("posts_count")
    val postsCount: Int = 0,
    @SerializedName("friends_count")
    val friendsCount: Int = 0,
    @SerializedName("created_at")
    val createdAt: String? = null,
    val isOwnProfile: Boolean = false,
    val friendshipStatus: String? = null
)

data class UserProfile(
    val user: User
)
