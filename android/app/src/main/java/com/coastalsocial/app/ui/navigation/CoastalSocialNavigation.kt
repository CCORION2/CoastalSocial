package com.coastalsocial.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.coastalsocial.app.ui.screens.auth.LoginScreen
import com.coastalsocial.app.ui.screens.auth.RegisterScreen
import com.coastalsocial.app.ui.screens.chat.ChatScreen
import com.coastalsocial.app.ui.screens.createpost.CreatePostScreen
import com.coastalsocial.app.ui.screens.feed.FeedScreen
import com.coastalsocial.app.ui.screens.friends.FriendRequestsScreen
import com.coastalsocial.app.ui.screens.friends.FriendsScreen
import com.coastalsocial.app.ui.screens.home.HomeScreen
import com.coastalsocial.app.ui.screens.messages.MessagesScreen
import com.coastalsocial.app.ui.screens.notifications.NotificationsScreen
import com.coastalsocial.app.ui.screens.post.PostDetailScreen
import com.coastalsocial.app.ui.screens.profile.EditProfileScreen
import com.coastalsocial.app.ui.screens.profile.ProfileScreen
import com.coastalsocial.app.ui.screens.search.SearchScreen
import com.coastalsocial.app.ui.screens.settings.SettingsScreen
import com.coastalsocial.app.ui.screens.splash.SplashScreen
import com.coastalsocial.app.ui.viewmodel.AuthViewModel

@Composable
fun CoastalSocialNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState(initial = null)

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(Screen.Feed.route) {
            FeedScreen(navController = navController)
        }

        composable(Screen.Search.route) {
            SearchScreen(navController = navController)
        }

        composable(Screen.CreatePost.route) {
            CreatePostScreen(
                onPostCreated = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(navController = navController)
        }

        composable(
            route = Screen.Profile.route,
            arguments = listOf(navArgument("username") { type = NavType.StringType })
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            ProfileScreen(
                username = username,
                navController = navController
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.PostDetail.route,
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            PostDetailScreen(
                postId = postId,
                navController = navController
            )
        }

        composable(Screen.Messages.route) {
            MessagesScreen(navController = navController)
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.IntType },
                navArgument("username") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            val username = backStackEntry.arguments?.getString("username") ?: ""
            ChatScreen(
                userId = userId,
                username = username,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Friends.route) {
            FriendsScreen(navController = navController)
        }

        composable(Screen.FriendRequests.route) {
            FriendRequestsScreen(navController = navController)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
