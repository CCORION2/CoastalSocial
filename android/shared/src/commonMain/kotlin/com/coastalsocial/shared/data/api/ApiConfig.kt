package com.coastalsocial.shared.data.api

object ApiConfig {
    // Ändern Sie diese URL zu Ihrer Server-IP
    const val BASE_URL = "http://192.168.0.118:3000/api/"
    
    object Endpoints {
        const val LOGIN = "auth/login"
        const val REGISTER = "auth/register"
        const val VERIFY = "auth/verify"
        
        const val POSTS = "posts"
        const val STORIES = "stories"
        const val USERS = "users"
        const val MESSAGES = "messages"
        const val NOTIFICATIONS = "notifications"
        const val FRIENDS = "friends"
    }
}
