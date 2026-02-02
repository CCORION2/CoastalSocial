package com.coastalsocial.shared.data.api

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

expect fun createHttpClient(): HttpClient

object ApiClient {
    private var authToken: String? = null
    
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }
    
    val client by lazy {
        createHttpClient()
    }
    
    fun setAuthToken(token: String?) {
        authToken = token
    }
    
    fun getAuthToken(): String? = authToken
    
    suspend fun get(endpoint: String): HttpResponse {
        return client.get("${ApiConfig.BASE_URL}$endpoint") {
            authToken?.let {
                header("Authorization", "Bearer $it")
            }
        }
    }
    
    suspend fun post(endpoint: String, body: Any? = null): HttpResponse {
        return client.post("${ApiConfig.BASE_URL}$endpoint") {
            contentType(ContentType.Application.Json)
            authToken?.let {
                header("Authorization", "Bearer $it")
            }
            if (body != null) {
                setBody(body)
            }
        }
    }
    
    suspend fun put(endpoint: String, body: Any? = null): HttpResponse {
        return client.put("${ApiConfig.BASE_URL}$endpoint") {
            contentType(ContentType.Application.Json)
            authToken?.let {
                header("Authorization", "Bearer $it")
            }
            if (body != null) {
                setBody(body)
            }
        }
    }
    
    suspend fun delete(endpoint: String): HttpResponse {
        return client.delete("${ApiConfig.BASE_URL}$endpoint") {
            authToken?.let {
                header("Authorization", "Bearer $it")
            }
        }
    }
}
