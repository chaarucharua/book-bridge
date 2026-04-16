package com.chaaru.bookbridge.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
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
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val userProfile = authViewModel.profile.value ?: return
    var isEditing by remember { mutableStateOf(false) }
    
    var name by remember(userProfile) { mutableStateOf(userProfile.name) }
    var phone by remember(userProfile) { mutableStateOf(userProfile.phone) }
    val isLoading by authViewModel.isLoading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Serif)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Burgundy)
                    }
                },
                actions = {
                    if (isEditing) {
                        TextButton(onClick = { 
                            authViewModel.updateProfile(userProfile.copy(name = name, phone = phone))
                            isEditing = false 
                        }) {
                            Text("Save", color = Burgundy, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, "Edit", tint = Burgundy)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Parchment)
            )
        },
        containerColor = Parchment
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(50.dp),
                color = Burgundy,
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        userProfile.name.take(1).uppercase(),
                        style = MaterialTheme.typography.displayMedium.copy(fontFamily = FontFamily.Serif, color = White)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isEditing) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Burgundy, focusedLabelColor = Burgundy)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Burgundy, focusedLabelColor = Burgundy)
                )
            } else {
                Text(
                    userProfile.name,
                    style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = Slate900)
                )
                Text(
                    userProfile.role.uppercase(),
                    style = MaterialTheme.typography.labelLarge.copy(color = Burgundy, letterSpacing = 2.sp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ProfileInfoRow("Email", userProfile.email)
                    if (!isEditing) {
                        ProfileInfoRow("Phone", userProfile.phone)
                    }
                    if (userProfile.role == "owner") {
                        ProfileInfoRow("Store ID", userProfile.storeId ?: "N/A")
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = { authViewModel.logout { onLogout() } },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Burgundy)
            ) {
                Icon(Icons.Default.Logout, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout")
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = { authViewModel.deleteAccount { onLogout() } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Delete Account", color = Color.Red.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = Slate500))
        Text(value, style = MaterialTheme.typography.bodyLarge.copy(color = Slate900, fontWeight = FontWeight.Medium))
    }
}
