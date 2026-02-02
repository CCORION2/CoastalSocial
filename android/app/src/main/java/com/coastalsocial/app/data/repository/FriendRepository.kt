package com.coastalsocial.app.data.repository

import com.coastalsocial.app.data.api.ApiService
import com.coastalsocial.app.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FriendRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getFriends(): Result<List<Friend>> {
        return try {
            val response = apiService.getFriends()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data?.friends ?: emptyList())
            } else {
                Result.failure(Exception("Freunde laden fehlgeschlagen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFriendRequests(): Result<List<Friend>> {
        return try {
            val response = apiService.getFriendRequests()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data?.requests ?: emptyList())
            } else {
                Result.failure(Exception("Anfragen laden fehlgeschlagen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendFriendRequest(userId: Int): Result<Unit> {
        return try {
            val response = apiService.sendFriendRequest(userId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Anfrage fehlgeschlagen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptFriendRequest(userId: Int): Result<Unit> {
        return try {
            val response = apiService.acceptFriendRequest(userId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Akzeptieren fehlgeschlagen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun declineFriendRequest(userId: Int): Result<Unit> {
        return try {
            val response = apiService.declineFriendRequest(userId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Ablehnen fehlgeschlagen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFriend(userId: Int): Result<Unit> {
        return try {
            val response = apiService.removeFriend(userId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Entfernen fehlgeschlagen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun blockUser(userId: Int): Result<Unit> {
        return try {
            val response = apiService.blockUser(userId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Blockieren fehlgeschlagen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
