package com.chaaru.bookbridge.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaaru.bookbridge.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(authViewModel: AuthViewModel, onNavigate: (String) -> Unit, onBack: () -> Unit, onLogout: () -> Unit) {
    val userProfile = authViewModel.profile.value ?: return
    var isEditing by remember { mutableStateOf(false) }
    
    var name by remember(userProfile) { mutableStateOf(userProfile.name) }
    var phone by remember(userProfile) { mutableStateOf(userProfile.phone) }

    // Dark Academia Palette
    val creamBackground = Color(0xFFF5F5DC)
    val burgundy = Color(0xFF800020)
    val gold = Color(0xFFD4AF37)
    val charcoal = Color(0xFF333333)

    val isCurator = userProfile.role.trim().equals("owner", ignoreCase = true)
    val roleTitle = if (isCurator) "STORE OWNER" else "STUDENT"
    val homeRoute = if (isCurator) "owner_dashboard" else "student_home"

    Scaffold(
        containerColor = creamBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "MY PROFILE", 
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = gold)
                    }
                },
                actions = {
                    if (!isEditing) {
                        TextButton(onClick = { isEditing = true }) {
                            Text("EDIT", color = gold, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                        }
                    } else {
                        TextButton(onClick = { 
                            authViewModel.updateProfile(userProfile.copy(name = name, phone = phone))
                            isEditing = false 
                        }) {
                            Text("SAVE", color = gold, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = burgundy,
                    titleContentColor = gold
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = burgundy,
                contentColor = gold
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text(if (isCurator) "Home" else "Books", fontFamily = FontFamily.Serif) },
                    selected = false,
                    onClick = { onNavigate(homeRoute) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = gold,
                        selectedTextColor = gold,
                        unselectedIconColor = gold.copy(alpha = 0.5f),
                        unselectedTextColor = gold.copy(alpha = 0.5f),
                        indicatorColor = burgundy.copy(alpha = 0.1f)
                    )
                )
                if (!isCurator) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.List, contentDescription = null) },
                        label = { Text("Reservations", fontFamily = FontFamily.Serif) },
                        selected = false,
                        onClick = { onNavigate("reservations") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = gold,
                            selectedTextColor = gold,
                            unselectedIconColor = gold.copy(alpha = 0.5f),
                            unselectedTextColor = gold.copy(alpha = 0.5f),
                            indicatorColor = burgundy.copy(alpha = 0.1f)
                        )
                    )
                }
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Profile", fontFamily = FontFamily.Serif) },
                    selected = true,
                    onClick = {},
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = gold,
                        selectedTextColor = gold,
                        unselectedIconColor = gold.copy(alpha = 0.5f),
                        unselectedTextColor = gold.copy(alpha = 0.5f),
                        indicatorColor = burgundy.copy(alpha = 0.1f)
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Profile Seal/Avatar placeholder
            Surface(
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(50.dp),
                color = burgundy,
                border = androidx.compose.foundation.BorderStroke(2.dp, gold)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        userProfile.name.take(1).uppercase(),
                        style = MaterialTheme.typography.displayMedium.copy(fontFamily = FontFamily.Serif, color = gold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                roleTitle,
                style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Serif, letterSpacing = 3.sp),
                color = gold
            )
            
            if (isEditing) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("NAME", fontFamily = FontFamily.Serif) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = burgundy, focusedLabelColor = burgundy)
                )
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Text(
                    userProfile.name.uppercase(),
                    style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
                    color = burgundy
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = burgundy.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, burgundy.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ProfileDetailRow("EMAIL", userProfile.email, charcoal)
                    if (isEditing) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("PHONE", fontFamily = FontFamily.Serif) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = burgundy, focusedLabelColor = burgundy)
                        )
                    } else {
                        ProfileDetailRow("PHONE", userProfile.phone, charcoal)
                    }
                    if (isCurator) {
                        ProfileDetailRow("STORE ID", userProfile.storeId ?: "N/A", charcoal)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { authViewModel.logout { onLogout() } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = burgundy),
                border = androidx.compose.foundation.BorderStroke(1.dp, gold)
            ) {
                Text("LOGOUT", color = gold, style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold))
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = { authViewModel.deleteAccount { onLogout() } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("DELETE ACCOUNT", color = charcoal.copy(alpha = 0.5f), style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Serif))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProfileDetailRow(label: String, value: String, color: Color) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Serif, letterSpacing = 1.sp),
            color = color.copy(alpha = 0.5f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
            color = color
        )
    }
}
