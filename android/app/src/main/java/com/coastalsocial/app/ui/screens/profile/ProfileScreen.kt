package com.coastalsocial.app.ui.screens.profile

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.coastalsocial.app.ui.components.PostCard
import com.coastalsocial.app.ui.navigation.Screen
import com.coastalsocial.app.ui.theme.*
import com.coastalsocial.app.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    username: String,
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(username) {
        if (username.isNotBlank()) {
            viewModel.loadProfile(username)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = CoastalBlue)
                }
            }
            
            uiState.user != null -> {
                val user = uiState.user!!
                
                LazyColumn {
                    item {
                        // Modern Header with Cover & Profile
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                        ) {
                            // Cover Image with gradient overlay
                            Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                                AsyncImage(
                                    model = user.coverPicture ?: "",
                                    contentDescription = "Titelbild",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(CoastalBlue, CoastalBlueLight)
                                            )
                                        ),
                                    contentScale = ContentScale.Crop
                                )
                                // Gradient overlay
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color.Black.copy(alpha = 0.3f)
                                                )
                                            )
                                        )
                                )
                            }
                            
                            // Top Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (user.isOwnProfile) "Mein Profil" else "@$username",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                                if (user.isOwnProfile) {
                                    IconButton(
                                        onClick = { navController.navigate(Screen.Settings.route) },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                color = Color.Black.copy(alpha = 0.3f),
                                                shape = CircleShape
                                            )
                                    ) {
                                        Icon(
                                            Icons.Outlined.Settings,
                                            contentDescription = "Einstellungen",
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                            
                            // Profile Picture with gradient border
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .align(Alignment.BottomCenter)
                                    .offset(y = 10.dp)
                                    .shadow(
                                        elevation = 12.dp,
                                        shape = CircleShape,
                                        ambientColor = CoastalBlue.copy(alpha = 0.3f)
                                    )
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(CoastalBlue, CoastalTeal)
                                        ),
                                        shape = CircleShape
                                    )
                                    .padding(4.dp)
                            ) {
                                AsyncImage(
                                    model = user.profilePicture ?: "",
                                    contentDescription = "Profilbild",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // User Info - Centered
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = user.fullName,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (user.isVerified) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        Icons.Rounded.Verified,
                                        contentDescription = "Verifiziert",
                                        modifier = Modifier.size(24.dp),
                                        tint = CoastalBlue
                                    )
                                }
                            }
                            
                            Text(
                                text = "@${user.username}",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            if (!user.bio.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = user.bio,
                                    fontSize = 15.sp,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 22.sp
                                )
                            }
                            
                            if (!user.location.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = CoastalBlue
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = user.location,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Stats in modern cards
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatCard(
                                    count = user.postsCount,
                                    label = "Posts"
                                )
                                StatCard(
                                    count = user.friendsCount,
                                    label = "Freunde"
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // Action Buttons - Modern style
                            if (user.isOwnProfile) {
                                Button(
                                    onClick = { navController.navigate(Screen.EditProfile.route) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Profil bearbeiten", fontWeight = FontWeight.SemiBold)
                                }
                            } else {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    when (user.friendshipStatus) {
                                        "accepted" -> {
                                            OutlinedButton(
                                                onClick = { viewModel.removeFriend() },
                                                modifier = Modifier.weight(1f).height(52.dp),
                                                shape = RoundedCornerShape(14.dp),
                                                border = ButtonDefaults.outlinedButtonBorder
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = SuccessGreen)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Befreundet", fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                        "pending" -> {
                                            OutlinedButton(
                                                onClick = { },
                                                modifier = Modifier.weight(1f).height(52.dp),
                                                shape = RoundedCornerShape(14.dp),
                                                enabled = false
                                            ) {
                                                Icon(Icons.Default.Schedule, contentDescription = null)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Angefragt", fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                        else -> {
                                            Button(
                                                onClick = { viewModel.sendFriendRequest() },
                                                modifier = Modifier.weight(1f).height(52.dp),
                                                shape = RoundedCornerShape(14.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = CoastalBlue)
                                            ) {
                                                Icon(Icons.Default.PersonAdd, contentDescription = null)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Hinzufügen", fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.width(12.dp))
                                    
                                    Button(
                                        onClick = { 
                                            navController.navigate(Screen.Chat.createRoute(user.id, user.username))
                                        },
                                        modifier = Modifier.weight(1f).height(52.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    ) {
                                        Icon(Icons.Default.Message, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Nachricht", fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                        
                        // Posts Section Header
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.GridView,
                                    contentDescription = null,
                                    tint = CoastalBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Posts",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                    
                    if (uiState.posts.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.PhotoCamera,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Noch keine Posts",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    } else {
                        items(uiState.posts) { post ->
                            PostCard(
                                post = post,
                                onLikeClick = { viewModel.toggleLike(post) },
                                onCommentClick = { 
                                    navController.navigate(Screen.PostDetail.createRoute(post.uuid))
                                },
                                onShareClick = { },
                                onSaveClick = { },
                                onProfileClick = { },
                                onPostClick = {
                                    navController.navigate(Screen.PostDetail.createRoute(post.uuid))
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
fun StatCard(count: Int, label: String) {
    Surface(
        modifier = Modifier
            .width(140.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = CoastalBlue.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = CoastalBlue
            )
            Text(
                text = label,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
