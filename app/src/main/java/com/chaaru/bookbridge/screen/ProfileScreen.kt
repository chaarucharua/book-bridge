package com.chaaru.bookbridge.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chaaru.bookbridge.viewmodel.AuthViewModel

/**
 * ROOT CAUSE FOR NAVIGATION ISSUES:
 * 1. Missing Scaffold for standard UI structure.
 * 2. Missing TopAppBar for back navigation.
 * 3. Missing NavigationBar for consistent app-wide traversal.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(authViewModel: AuthViewModel, onNavigate: (String) -> Unit, onBack: () -> Unit, onLogout: () -> Unit) {
    val userProfile = authViewModel.profile.value ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            val homeRoute = if (userProfile.role.equals("owner", ignoreCase = true)) "owner_dashboard" else "student_home"
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Home") },
                    selected = false,
                    onClick = { 
                        onNavigate(homeRoute) 
                    }
                )
                if (userProfile.role.equals("student", ignoreCase = true)) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.List, contentDescription = null) },
                        label = { Text("Reservations") },
                        selected = false,
                        onClick = { onNavigate("reservations") }
                    )
                }
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Profile") },
                    selected = true,
                    onClick = {}
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Name: ${userProfile.name}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    Text("Email: ${userProfile.email}", color = MaterialTheme.colorScheme.onSurface)
                    Text("Role: ${userProfile.role}", color = MaterialTheme.colorScheme.onSurface)
                    Text("Phone: ${userProfile.phone}", color = MaterialTheme.colorScheme.onSurface)
                    
                    if (userProfile.role == "owner") {
                        Text("Store ID: ${userProfile.storeId ?: "None"}", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = { authViewModel.logout { onLogout() } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = { authViewModel.deleteAccount { onLogout() } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Delete Account")
            }
        }
    }
}
