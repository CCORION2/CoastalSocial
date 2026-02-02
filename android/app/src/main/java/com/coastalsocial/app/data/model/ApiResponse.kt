package com.coastalsocial.app.data.model

import com.google.gson.annotations.SerializedName

// Generische API Response
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)

// Auth Responses
data class AuthData(
    val token: String,
    val user: User
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val fullName: String
)

// Post Responses
data class PostsData(
    val posts: List<Post>,
    val page: Int,
    val hasMore: Boolean
)

data class PostData(
    val post: Post
)

data class PostCreateData(
    val postId: Int,
    val uuid: String
)

// Comments
data class CommentsData(
    val comments: List<Comment>
)

// Like Response
data class LikeData(
    val liked: Boolean
)

// Save Response  
data class SaveData(
    val saved: Boolean
)

// Users
data class UsersData(
    val users: List<User>
)

// Friends
data class FriendsData(
    val friends: List<Friend>
)

data class FriendRequestsData(
    val requests: List<Friend>
)

// Messages
data class ConversationsData(
    val conversations: List<Conversation>
)

data class MessagesData(
    val messages: List<Message>,
    val page: Int,
    val hasMore: Boolean
)

// Notifications
data class NotificationsData(
    val notifications: List<Notification>,
    val unreadCount: Int,
    val page: Int,
    val hasMore: Boolean
)

// Stories
data class StoriesData(
    val stories: List<StoryGroup>
)
