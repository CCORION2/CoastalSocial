package com.coastalsocial.shared.data.repository

import com.coastalsocial.shared.data.api.ApiClient
import com.coastalsocial.shared.data.api.ApiConfig
import com.coastalsocial.shared.data.model.*
import io.ktor.client.call.*
import io.ktor.client.statement.*
import kotlinx.serialization.Serializable

class PostRepository {
    
    suspend fun getFeed(): Result<List<Post>> {
        return try {
            val response: HttpResponse = ApiClient.get(ApiConfig.Endpoints.POSTS)
            val postsResponse: PostsResponse = response.body()
            if (postsResponse.success) {
                Result.success(postsResponse.posts)
            } else {
                Result.failure(Exception(postsResponse.message ?: "Failed to load posts"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserPosts(username: String): Result<List<Post>> {
        return try {
            val response: HttpResponse = ApiClient.get("${ApiConfig.Endpoints.POSTS}/user/$username")
            val postsResponse: PostsResponse = response.body()
            if (postsResponse.success) {
                Result.success(postsResponse.posts)
            } else {
                Result.failure(Exception(postsResponse.message ?: "Failed to load posts"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createPost(content: String): Result<Post> {
        return try {
            val response: HttpResponse = ApiClient.post(
                ApiConfig.Endpoints.POSTS,
                CreatePostRequest(content)
            )
            @Serializable
            data class SinglePostResponse(val success: Boolean, val post: Post?, val message: String? = null)
            val postResponse: SinglePostResponse = response.body()
            if (postResponse.success && postResponse.post != null) {
                Result.success(postResponse.post)
            } else {
                Result.failure(Exception(postResponse.message ?: "Failed to create post"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun likePost(postId: Int): Result<Boolean> {
        return try {
            val response: HttpResponse = ApiClient.post("${ApiConfig.Endpoints.POSTS}/$postId/like")
            @Serializable
            data class LikeResponse(val success: Boolean, val isLiked: Boolean)
            val likeResponse: LikeResponse = response.body()
            Result.success(likeResponse.isLiked)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getComments(postId: Int): Result<List<Comment>> {
        return try {
            val response: HttpResponse = ApiClient.get("${ApiConfig.Endpoints.POSTS}/$postId/comments")
            val commentsResponse: CommentsResponse = response.body()
            if (commentsResponse.success) {
                Result.success(commentsResponse.comments)
            } else {
                Result.failure(Exception("Failed to load comments"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun addComment(postId: Int, content: String): Result<Comment> {
        return try {
            @Serializable
            data class CommentRequest(val content: String)
            val response: HttpResponse = ApiClient.post(
                "${ApiConfig.Endpoints.POSTS}/$postId/comments",
                CommentRequest(content)
            )
            @Serializable
            data class SingleCommentResponse(val success: Boolean, val comment: Comment?, val message: String? = null)
            val commentResponse: SingleCommentResponse = response.body()
            if (commentResponse.success && commentResponse.comment != null) {
                Result.success(commentResponse.comment)
            } else {
                Result.failure(Exception(commentResponse.message ?: "Failed to add comment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
