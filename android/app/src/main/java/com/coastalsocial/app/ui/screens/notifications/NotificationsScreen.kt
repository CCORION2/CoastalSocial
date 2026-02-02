package com.coastalsocial.app.ui.screens.notifications

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.coastalsocial.app.data.model.Notification
import com.coastalsocial.app.ui.components.formatTimeAgo
import com.coastalsocial.app.ui.navigation.Screen
import com.coastalsocial.app.ui.theme.*
import com.coastalsocial.app.ui.viewmodel.NotificationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Modern Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(CoastalBlue, CoastalTeal)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Benachrichtigungen",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.unreadCount > 0) {
                            Text(
                                text = "${uiState.unreadCount} ungelesen",
                                fontSize = 12.sp,
                                color = CoastalBlue
                            )
                        }
                    }
                }
                
                if (uiState.unreadCount > 0) {
                    TextButton(
                        onClick = { viewModel.markAllAsRead() },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = CoastalBlue
                        )
                    ) {
                        Text("Alle lesen", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = CoastalBlue)
                }
            }
            
            uiState.notifications.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            CoastalBlue.copy(alpha = 0.1f),
                                            CoastalTeal.copy(alpha = 0.1f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.NotificationsNone,
                                contentDescription = null,
                                modifier = Modifier.size(50.dp),
                                tint = CoastalBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Keine Benachrichtigungen",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Wenn jemand mit dir interagiert,\nsiehst du es hier",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
            
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    itemsIndexed(uiState.notifications) { index, notification ->
                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(index * 50L)
                            visible = true
                        }
                        
                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn() + slideInVertically { it / 2 }
                        ) {
                            NotificationItem(
                                notification = notification,
                                onClick = {
                                    viewModel.markAsRead(notification.uuid)
                                    when (notification.type) {
                                        "like", "comment" -> {
                                            notification.referenceId?.let {
                                                navController.navigate(Screen.PostDetail.createRoute(it.toString()))
                                            }
                                        }
                                        "friend_request", "friend_accept" -> {
                                            notification.username?.let {
                                                navController.navigate(Screen.Profile.createRoute(it))
                                            }
                                        }
                                        "message" -> {
                                            notification.fromUserId?.let { userId ->
                                                notification.username?.let { username ->
                                                    navController.navigate(Screen.Chat.createRoute(userId, username))
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    val isUnread = !notification.isRead
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnread) {
                CoastalBlue.copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isUnread) 2.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                // Profile picture with gradient border for unread
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .then(
                            if (isUnread) {
                                Modifier
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(CoastalBlue, CoastalTeal)
                                        ),
                                        CircleShape
                                    )
                                    .padding(2.dp)
                            } else Modifier
                        )
                ) {
                    AsyncImage(
                        model = notification.profilePicture ?: "",
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Crop
                    )
                }
                
                // Notification type icon
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(22.dp)
                        .shadow(2.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            when (notification.type) {
                                "like" -> CoralAccent
                                "comment" -> CoastalBlue
                                "friend_request", "friend_accept" -> CoastalTeal
                                "message" -> SunsetOrange
                                else -> CoastalBlue
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (notification.type) {
                            "like" -> Icons.Default.Favorite
                            "comment" -> Icons.Default.ChatBubble
                            "friend_request" -> Icons.Default.PersonAdd
                            "friend_accept" -> Icons.Default.People
                            "message" -> Icons.Default.Message
                            else -> Icons.Default.Notifications
                        },
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(notification.fullName ?: notification.username ?: "Jemand")
                        }
                        append(" ")
                        append(
                            when (notification.type) {
                                "like" -> "gefällt dein Post"
                                "comment" -> "hat kommentiert"
                                "friend_request" -> "möchte dein Freund sein"
                                "friend_accept" -> "hat deine Anfrage akzeptiert"
                                "message" -> "hat dir geschrieben"
                                "mention" -> "hat dich erwähnt"
                                else -> "hat interagiert"
                            }
                        )
                    },
                    fontSize = 14.sp,
                    fontWeight = if (isUnread) FontWeight.Medium else FontWeight.Normal
                )
                
                if (!notification.content.isNullOrBlank() && notification.type in listOf("comment", "message")) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\"${notification.content}\"",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimeAgo(notification.createdAt),
                    fontSize = 12.sp,
                    color = if (isUnread) CoastalBlue else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Unread indicator
            if (isUnread) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(CoastalBlue)
                )
            }
        }
    }
}
