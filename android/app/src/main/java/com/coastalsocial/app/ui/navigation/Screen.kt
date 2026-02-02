package com.coastalsocial.app.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Feed : Screen("feed")
    object Search : Screen("search")
    object CreatePost : Screen("create_post")
    object Notifications : Screen("notifications")
    object Profile : Screen("profile/{username}") {
        fun createRoute(username: String) = "profile/$username"
    }
    object EditProfile : Screen("edit_profile")
    object PostDetail : Screen("post/{postId}") {
        fun createRoute(postId: String) = "post/$postId"
    }
    object Messages : Screen("messages")
    object Chat : Screen("chat/{userId}/{username}") {
        fun createRoute(userId: Int, username: String) = "chat/$userId/$username"
    }
    object Friends : Screen("friends")
    object FriendRequests : Screen("friend_requests")
    object Settings : Screen("settings")
}
