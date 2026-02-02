package com.coastalsocial.app.ui.screens.createpost

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.coastalsocial.app.ui.theme.*
import com.coastalsocial.app.ui.viewmodel.CreatePostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    onPostCreated: () -> Unit,
    onBack: () -> Unit,
    viewModel: CreatePostViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var content by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedPrivacy by remember { mutableStateOf("public") }
    var showPrivacyMenu by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onPostCreated()
        }
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Schließen",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Text(
                        text = "Neuer Post",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    
                    Button(
                        onClick = { viewModel.createPost(content, selectedImageUri, selectedPrivacy) },
                        enabled = !uiState.isLoading && (content.isNotBlank() || selectedImageUri != null),
                        modifier = Modifier
                            .height(44.dp)
                            .shadow(
                                elevation = if (!uiState.isLoading && (content.isNotBlank() || selectedImageUri != null)) 8.dp else 0.dp,
                                shape = RoundedCornerShape(22.dp),
                                ambientColor = CoastalBlue.copy(alpha = 0.3f)
                            ),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CoastalBlue,
                            disabledContainerColor = CoastalBlue.copy(alpha = 0.4f)
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Posten", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
        ) {
            // Privacy Selector - Modern chip style
            Box {
                Surface(
                    onClick = { showPrivacyMenu = true },
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (selectedPrivacy) {
                                "public" -> Icons.Outlined.Public
                                "friends" -> Icons.Outlined.People
                                else -> Icons.Outlined.Lock
                            },
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = CoastalBlue
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (selectedPrivacy) {
                                "public" -> "Öffentlich"
                                "friends" -> "Freunde"
                                else -> "Privat"
                            },
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                DropdownMenu(
                    expanded = showPrivacyMenu,
                    onDismissRequest = { showPrivacyMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Öffentlich") },
                        onClick = { 
                            selectedPrivacy = "public"
                            showPrivacyMenu = false
                        },
                        leadingIcon = { Icon(Icons.Outlined.Public, null, tint = CoastalBlue) }
                    )
                    DropdownMenuItem(
                        text = { Text("Freunde") },
                        onClick = { 
                            selectedPrivacy = "friends"
                            showPrivacyMenu = false
                        },
                        leadingIcon = { Icon(Icons.Outlined.People, null, tint = CoastalBlue) }
                    )
                    DropdownMenuItem(
                        text = { Text("Privat") },
                        onClick = { 
                            selectedPrivacy = "private"
                            showPrivacyMenu = false
                        },
                        leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = CoastalBlue) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Content Input - Modern card style
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = CoastalBlue.copy(alpha = 0.05f)
                    ),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                TextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { 
                        Text(
                            "Was möchtest du teilen?",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ) 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        cursorColor = CoastalBlue
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Image Preview or Add Image Button
            AnimatedVisibility(
                visible = selectedImageUri != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (selectedImageUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(20.dp)
                            )
                    ) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Ausgewähltes Bild",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.2f),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { selectedImageUri = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .size(36.dp)
                                .shadow(4.dp, CircleShape)
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Entfernen",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            
            AnimatedVisibility(
                visible = selectedImageUri == null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    onClick = { imagePicker.launch("image/*") }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                width = 2.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(CoastalBlue.copy(alpha = 0.5f), CoastalTeal.copy(alpha = 0.5f))
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(CoastalBlue, CoastalTeal)
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AddPhotoAlternate,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Bild hinzufügen",
                                color = CoastalBlue,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
            
            AnimatedVisibility(visible = uiState.error != null) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = LikeRed.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = uiState.error ?: "",
                            color = LikeRed,
                            modifier = Modifier.padding(16.dp),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
