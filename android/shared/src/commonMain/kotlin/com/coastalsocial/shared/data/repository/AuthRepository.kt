package com.coastalsocial.shared.data.repository

import com.coastalsocial.shared.data.api.ApiClient
import com.coastalsocial.shared.data.api.ApiConfig
import com.coastalsocial.shared.data.model.*
import io.ktor.client.call.*
import io.ktor.client.statement.*

class AuthRepository {
    
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response: HttpResponse = ApiClient.post(
                ApiConfig.Endpoints.LOGIN,
                LoginRequest(email, password)
            )
            val authResponse: AuthResponse = response.body()
            if (authResponse.success && authResponse.token != null) {
                ApiClient.setAuthToken(authResponse.token)
            }
            Result.success(authResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun register(username: String, email: String, password: String): Result<AuthResponse> {
        return try {
            val response: HttpResponse = ApiClient.post(
                ApiConfig.Endpoints.REGISTER,
                RegisterRequest(username, email, password)
            )
            val authResponse: AuthResponse = response.body()
            if (authResponse.success && authResponse.token != null) {
                ApiClient.setAuthToken(authResponse.token)
            }
            Result.success(authResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun verifyToken(): Result<AuthResponse> {
        return try {
            val response: HttpResponse = ApiClient.get(ApiConfig.Endpoints.VERIFY)
            val authResponse: AuthResponse = response.body()
            Result.success(authResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun logout() {
        ApiClient.setAuthToken(null)
    }
    
    fun setToken(token: String) {
        ApiClient.setAuthToken(token)
    }
}
