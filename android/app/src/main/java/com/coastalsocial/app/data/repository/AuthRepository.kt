package com.coastalsocial.app.data.repository

import com.coastalsocial.app.data.api.ApiService
import com.coastalsocial.app.data.local.TokenManager
import com.coastalsocial.app.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    suspend fun login(email: String, password: String): Result<AuthData> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data!!
                tokenManager.saveToken(data.token)
                tokenManager.saveUserInfo(data.user.id.toString(), data.user.username)
                Result.success(data)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Login fehlgeschlagen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(
        username: String,
        email: String,
        password: String,
        fullName: String
    ): Result<AuthData> {
        return try {
            val response = apiService.register(
                RegisterRequest(username, email, password, fullName)
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data!!
                tokenManager.saveToken(data.token)
                tokenManager.saveUserInfo(data.user.id.toString(), data.user.username)
                Result.success(data)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Registrierung fehlgeschlagen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyToken(): Result<User> {
        return try {
            val response = apiService.verifyToken()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data?.user!!)
            } else {
                Result.failure(Exception("Token ungültig"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        tokenManager.clearAll()
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            val response = apiService.changePassword(
                mapOf(
                    "currentPassword" to currentPassword,
                    "newPassword" to newPassword
                )
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Passwortänderung fehlgeschlagen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isLoggedIn() = tokenManager.isLoggedIn
}
