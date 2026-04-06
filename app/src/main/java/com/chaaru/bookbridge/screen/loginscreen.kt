package com.chaaru.bookbridge.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.chaaru.bookbridge.viewmodel.AuthViewModel

@Composable
fun LoginScreen(viewModel: AuthViewModel, onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var storeName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }
    var role by remember { mutableStateOf("student") }

    // Dark Theme Colors matching Burgundy theme
    val backgroundColor = Color(0xFF121212) // Dark surface
    val burgundy = Color(0xFF800020)
    val onSurface = Color(0xFFF5F5DC) // Cream for text

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                if (isRegistering) "Create Account" else "Welcome Back",
                style = MaterialTheme.typography.headlineLarge,
                color = burgundy
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (isRegistering) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = onSurface,
                        unfocusedTextColor = onSurface,
                        focusedLabelColor = burgundy,
                        unfocusedLabelColor = onSurface.copy(alpha = 0.6f),
                        focusedBorderColor = burgundy,
                        unfocusedBorderColor = onSurface.copy(alpha = 0.3f)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = onSurface,
                        unfocusedTextColor = onSurface,
                        focusedLabelColor = burgundy,
                        unfocusedLabelColor = onSurface.copy(alpha = 0.6f),
                        focusedBorderColor = burgundy,
                        unfocusedBorderColor = onSurface.copy(alpha = 0.3f)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    FilterChip(
                        selected = role == "student",
                        onClick = { role = "student" },
                        label = { Text("Student") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = burgundy,
                            selectedLabelColor = Color.White,
                            containerColor = Color.Transparent,
                            labelColor = onSurface
                        )
                    )
                    FilterChip(
                        selected = role == "owner",
                        onClick = { role = "owner" },
                        label = { Text("Store Owner") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = burgundy,
                            selectedLabelColor = Color.White,
                            containerColor = Color.Transparent,
                            labelColor = onSurface
                        )
                    )
                }

                if (role == "owner") {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = storeName,
                        onValueChange = { storeName = it },
                        label = { Text("Store Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = onSurface,
                            unfocusedTextColor = onSurface,
                            focusedLabelColor = burgundy,
                            unfocusedLabelColor = onSurface.copy(alpha = 0.6f),
                            focusedBorderColor = burgundy,
                            unfocusedBorderColor = onSurface.copy(alpha = 0.3f)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Store Location") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = onSurface,
                            unfocusedTextColor = onSurface,
                            focusedLabelColor = burgundy,
                            unfocusedLabelColor = onSurface.copy(alpha = 0.6f),
                            focusedBorderColor = burgundy,
                            unfocusedBorderColor = onSurface.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = onSurface,
                    unfocusedTextColor = onSurface,
                    focusedLabelColor = burgundy,
                    unfocusedLabelColor = onSurface.copy(alpha = 0.6f),
                    focusedBorderColor = burgundy,
                    unfocusedBorderColor = onSurface.copy(alpha = 0.3f)
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = onSurface,
                    unfocusedTextColor = onSurface,
                    focusedLabelColor = burgundy,
                    unfocusedLabelColor = onSurface.copy(alpha = 0.6f),
                    focusedBorderColor = burgundy,
                    unfocusedBorderColor = onSurface.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (viewModel.isLoading.value) {
                CircularProgressIndicator(color = burgundy)
            } else {
                Button(
                    onClick = {
                        if (isRegistering) {
                            viewModel.register(name, email, password, role, if (role == "owner") phone else "", if (role == "owner") storeName else null, if (role == "owner") location else null) { roleStr ->
                                val destination = if (roleStr.equals("owner", ignoreCase = true)) "owner_dashboard" else "student_home"
                                onNavigate(destination)
                            }
                        } else {
                            viewModel.login(email, password) { roleStr ->
                                val destination = if (roleStr.equals("owner", ignoreCase = true)) "owner_dashboard" else "student_home"
                                onNavigate(destination)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = burgundy)
                ) {
                    Text(if (isRegistering) "Register" else "Login", color = Color.White)
                }

                TextButton(onClick = { isRegistering = !isRegistering }) {
                    Text(
                        if (isRegistering) "Already have an account? Login" else "Don't have an account? Register",
                        color = onSurface
                    )
                }
            }

            viewModel.error.value?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
