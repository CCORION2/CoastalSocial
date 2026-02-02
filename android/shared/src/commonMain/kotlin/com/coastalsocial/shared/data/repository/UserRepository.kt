package com.coastalsocial.shared.data.repository

import com.coastalsocial.shared.data.api.ApiClient
import com.coastalsocial.shared.data.api.ApiConfig
import com.coastalsocial.shared.data.model.*
import io.ktor.client.call.*
import io.ktor.client.statement.*
import kotlinx.serialization.Serializable

class UserRepository {
    
    suspend fun getProfile(username: String): Result<User> {
        return try {
            val response: HttpResponse = ApiClient.get("${ApiConfig.Endpoints.USERS}/profile/$username")
            @Serializable
            data class ProfileResponse(val success: Boolean, val user: User?, val message: String? = null)
            val profileResponse: ProfileResponse = response.body()
            if (profileResponse.success && profileResponse.user != null) {
                Result.success(profileResponse.user)
            } else {
                Result.failure(Exception(profileResponse.message ?: "User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun followUser(userId: Int): Result<Boolean> {
        return try {
            val response: HttpResponse = ApiClient.post("${ApiConfig.Endpoints.USERS}/$userId/follow")
            @Serializable
            data class FollowResponse(val success: Boolean, val isFollowing: Boolean)
            val followResponse: FollowResponse = response.body()
            Result.success(followResponse.isFollowing)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun searchUsers(query: String): Result<List<User>> {
        return try {
            val response: HttpResponse = ApiClient.get("${ApiConfig.Endpoints.USERS}/search?q=$query")
            @Serializable
            data class SearchResponse(val success: Boolean, val users: List<User>)
            val searchResponse: SearchResponse = response.body()
            if (searchResponse.success) {
                Result.success(searchResponse.users)
            } else {
                Result.failure(Exception("Search failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getFollowers(username: String): Result<List<User>> {
        return try {
            val response: HttpResponse = ApiClient.get("${ApiConfig.Endpoints.USERS}/$username/followers")
            @Serializable
            data class FollowersResponse(val success: Boolean, val followers: List<User>)
            val followersResponse: FollowersResponse = response.body()
            if (followersResponse.success) {
                Result.success(followersResponse.followers)
            } else {
                Result.failure(Exception("Failed to load followers"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getFollowing(username: String): Result<List<User>> {
        return try {
            val response: HttpResponse = ApiClient.get("${ApiConfig.Endpoints.USERS}/$username/following")
            @Serializable
            data class FollowingResponse(val success: Boolean, val following: List<User>)
            val followingResponse: FollowingResponse = response.body()
            if (followingResponse.success) {
                Result.success(followingResponse.following)
            } else {
                Result.failure(Exception("Failed to load following"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
