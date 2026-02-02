package com.coastalsocial.app.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.coastalsocial.app.ui.navigation.Screen
import com.coastalsocial.app.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    onLogout: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Abmelden") },
            text = { Text("Möchtest du dich wirklich abmelden?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                        onLogout()
                    }
                ) {
                    Text("Abmelden", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Einstellungen", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Account Section
            SettingsSectionHeader(title = "Konto")
            
            SettingsItem(
                icon = Icons.Default.Person,
                title = "Profil bearbeiten",
                subtitle = "Name, Bio, Profilbild",
                onClick = { navController.navigate(Screen.EditProfile.route) }
            )
            
            SettingsItem(
                icon = Icons.Default.Lock,
                title = "Passwort ändern",
                subtitle = "Sicherheitseinstellungen",
                onClick = { /* TODO */ }
            )
            
            SettingsItem(
                icon = Icons.Default.Security,
                title = "Privatsphäre",
                subtitle = "Wer kann dein Profil sehen",
                onClick = { /* TODO */ }
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Social Section
            SettingsSectionHeader(title = "Soziales")
            
            SettingsItem(
                icon = Icons.Default.People,
                title = "Freunde",
                subtitle = "Freundesliste verwalten",
                onClick = { navController.navigate(Screen.Friends.route) }
            )
            
            SettingsItem(
                icon = Icons.Default.Block,
                title = "Blockierte Benutzer",
                subtitle = "Blockierte Personen verwalten",
                onClick = { /* TODO */ }
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Notifications Section
            SettingsSectionHeader(title = "Benachrichtigungen")
            
            SettingsItem(
                icon = Icons.Default.Notifications,
                title = "Push-Benachrichtigungen",
                subtitle = "Benachrichtigungseinstellungen",
                onClick = { /* TODO */ }
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // About Section
            SettingsSectionHeader(title = "Über")
            
            SettingsItem(
                icon = Icons.Default.Info,
                title = "Über CoastalSocial",
                subtitle = "Version 1.0.0",
                onClick = { /* TODO */ }
            )
            
            SettingsItem(
                icon = Icons.Default.Description,
                title = "Nutzungsbedingungen",
                onClick = { /* TODO */ }
            )
            
            SettingsItem(
                icon = Icons.Default.PrivacyTip,
                title = "Datenschutz",
                onClick = { /* TODO */ }
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Logout
            SettingsItem(
                icon = Icons.Default.Logout,
                title = "Abmelden",
                titleColor = MaterialTheme.colorScheme.error,
                onClick = { showLogoutDialog = true }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = titleColor,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                color = titleColor
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
