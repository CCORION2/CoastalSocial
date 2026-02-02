package com.coastalsocial.app.data.api

import com.coastalsocial.app.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ===== AUTH =====
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthData>>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthData>>

    @GET("auth/verify")
    suspend fun verifyToken(): Response<ApiResponse<UserProfile>>

    @PUT("auth/change-password")
    suspend fun changePassword(
        @Body body: Map<String, String>
    ): Response<ApiResponse<Unit>>

    // ===== USERS =====
    @GET("users/{username}")
    suspend fun getUserProfile(@Path("username") username: String): Response<ApiResponse<UserProfile>>

    @PUT("users/profile")
    suspend fun updateProfile(@Body body: Map<String, Any?>): Response<ApiResponse<Unit>>

    @Multipart
    @POST("users/profile-picture")
    suspend fun uploadProfilePicture(
        @Part profilePicture: MultipartBody.Part
    ): Response<ApiResponse<Map<String, String>>>

    @Multipart
    @POST("users/cover-picture")
    suspend fun uploadCoverPicture(
        @Part coverPicture: MultipartBody.Part
    ): Response<ApiResponse<Map<String, String>>>

    @GET("users/search/{query}")
    suspend fun searchUsers(@Path("query") query: String): Response<ApiResponse<UsersData>>

    // ===== POSTS =====
    @Multipart
    @POST("posts")
    suspend fun createPost(
        @Part("content") content: RequestBody?,
        @Part("privacy") privacy: RequestBody?,
        @Part postMedia: MultipartBody.Part?
    ): Response<ApiResponse<PostCreateData>>

    @GET("posts/feed")
    suspend fun getFeed(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<PostsData>>

    @GET("posts/{postId}")
    suspend fun getPost(@Path("postId") postId: String): Response<ApiResponse<PostData>>

    @GET("posts/user/{username}")
    suspend fun getUserPosts(
        @Path("username") username: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<PostsData>>

    @POST("posts/{postId}/like")
    suspend fun toggleLike(@Path("postId") postId: Int): Response<ApiResponse<LikeData>>

    @POST("posts/{postId}/comments")
    suspend fun addComment(
        @Path("postId") postId: Int,
        @Body body: Map<String, String>
    ): Response<ApiResponse<Map<String, Any>>>

    @GET("posts/{postId}/comments")
    suspend fun getComments(@Path("postId") postId: Int): Response<ApiResponse<CommentsData>>

    @DELETE("posts/{postId}")
    suspend fun deletePost(@Path("postId") postId: String): Response<ApiResponse<Unit>>

    @POST("posts/{postId}/save")
    suspend fun toggleSavePost(@Path("postId") postId: Int): Response<ApiResponse<SaveData>>

    // ===== FRIENDS =====
    @POST("friends/request/{userId}")
    suspend fun sendFriendRequest(@Path("userId") userId: Int): Response<ApiResponse<Unit>>

    @PUT("friends/accept/{userId}")
    suspend fun acceptFriendRequest(@Path("userId") userId: Int): Response<ApiResponse<Unit>>

    @PUT("friends/decline/{userId}")
    suspend fun declineFriendRequest(@Path("userId") userId: Int): Response<ApiResponse<Unit>>

    @DELETE("friends/{userId}")
    suspend fun removeFriend(@Path("userId") userId: Int): Response<ApiResponse<Unit>>

    @GET("friends")
    suspend fun getFriends(): Response<ApiResponse<FriendsData>>

    @GET("friends/requests")
    suspend fun getFriendRequests(): Response<ApiResponse<FriendRequestsData>>

    @POST("friends/block/{userId}")
    suspend fun blockUser(@Path("userId") userId: Int): Response<ApiResponse<Unit>>

    // ===== MESSAGES =====
    @POST("messages/{receiverId}")
    suspend fun sendMessage(
        @Path("receiverId") receiverId: Int,
        @Body body: Map<String, String>
    ): Response<ApiResponse<Map<String, String>>>

    @GET("messages/conversations")
    suspend fun getConversations(): Response<ApiResponse<ConversationsData>>

    @GET("messages/{userId}")
    suspend fun getMessages(
        @Path("userId") userId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<ApiResponse<MessagesData>>

    @DELETE("messages/{messageId}")
    suspend fun deleteMessage(@Path("messageId") messageId: String): Response<ApiResponse<Unit>>

    // ===== NOTIFICATIONS =====
    @GET("notifications")
    suspend fun getNotifications(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<NotificationsData>>

    @PUT("notifications/{notificationId}/read")
    suspend fun markNotificationRead(
        @Path("notificationId") notificationId: String
    ): Response<ApiResponse<Unit>>

    @PUT("notifications/read-all")
    suspend fun markAllNotificationsRead(): Response<ApiResponse<Unit>>

    @DELETE("notifications/{notificationId}")
    suspend fun deleteNotification(
        @Path("notificationId") notificationId: String
    ): Response<ApiResponse<Unit>>

    // ===== STORIES =====
    @Multipart
    @POST("stories")
    suspend fun createStory(
        @Part("caption") caption: RequestBody?,
        @Part storyMedia: MultipartBody.Part
    ): Response<ApiResponse<Map<String, String>>>

    @GET("stories/feed")
    suspend fun getStories(): Response<ApiResponse<StoriesData>>

    @POST("stories/{storyId}/view")
    suspend fun viewStory(@Path("storyId") storyId: Int): Response<ApiResponse<Unit>>

    @DELETE("stories/{storyId}")
    suspend fun deleteStory(@Path("storyId") storyId: String): Response<ApiResponse<Unit>>
}
