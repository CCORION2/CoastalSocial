package com.coastalsocial.app.ui.screens.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.coastalsocial.app.ui.navigation.Screen
import com.coastalsocial.app.ui.screens.feed.FeedScreen
import com.coastalsocial.app.ui.screens.messages.MessagesScreen
import com.coastalsocial.app.ui.screens.notifications.NotificationsScreen
import com.coastalsocial.app.ui.screens.profile.ProfileScreen
import com.coastalsocial.app.ui.screens.search.SearchScreen
import com.coastalsocial.app.ui.theme.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.coastalsocial.app.ui.viewmodel.AuthViewModel

data class BottomNavItem(
    val title: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    
    val authState by authViewModel.uiState.collectAsState()
    val username = authState.user?.username ?: ""
    
    LaunchedEffect(Unit) {
        authViewModel.verifyToken(
            onSuccess = { },
            onError = { }
        )
    }

    val navItems = listOf(
        BottomNavItem("Feed", Icons.Filled.Home, Icons.Outlined.Home, "feed"),
        BottomNavItem("Suchen", Icons.Filled.Search, Icons.Outlined.Search, "search"),
        BottomNavItem("Nachrichten", Icons.Filled.Chat, Icons.Outlined.Chat, "messages"),
        BottomNavItem("Benachrichtigungen", Icons.Filled.Notifications, Icons.Outlined.Notifications, "notifications"),
        BottomNavItem("Profil", Icons.Filled.Person, Icons.Outlined.Person, "profile")
    )

    Scaffold(
        bottomBar = {
            Surface(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = CoastalBlue.copy(alpha = 0.2f),
                        spotColor = CoastalBlue.copy(alpha = 0.2f)
                    ),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    navItems.forEachIndexed { index, item ->
                        val isSelected = selectedIndex == index
                        val scale by animateFloatAsState(
                            targetValue = if (isSelected) 1.1f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "navScale"
                        )
                        val iconColor by animateColorAsState(
                            targetValue = if (isSelected) CoastalBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                            label = "iconColor"
                        )
                        
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedIndex = index },
                            icon = {
                                Box(
                                    modifier = Modifier.scale(scale),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .background(
                                                    color = CoastalBlue.copy(alpha = 0.1f),
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.title,
                                        tint = iconColor,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            },
                            label = null,
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (selectedIndex == 0) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.CreatePost.route) },
                    containerColor = CoastalBlue,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .size(60.dp)
                        .offset(y = 40.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = CircleShape,
                            ambientColor = CoastalBlue.copy(alpha = 0.4f),
                            spotColor = CoastalBlue.copy(alpha = 0.4f)
                        ),
                    shape = CircleShape
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Neuer Post",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        when (selectedIndex) {
            0 -> FeedScreen(
                navController = navController,
                modifier = Modifier.padding(paddingValues)
            )
            1 -> SearchScreen(
                navController = navController,
                modifier = Modifier.padding(paddingValues)
            )
            2 -> MessagesScreen(
                navController = navController,
                modifier = Modifier.padding(paddingValues)
            )
            3 -> NotificationsScreen(
                navController = navController,
                modifier = Modifier.padding(paddingValues)
            )
            4 -> {
                if (username.isNotBlank()) {
                    ProfileScreen(
                        username = username,
                        navController = navController,
                        modifier = Modifier.padding(paddingValues)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = CoastalBlue)
                    }
                }
            }
        }
    }
}
