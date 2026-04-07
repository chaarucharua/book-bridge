package com.chaaru.bookbridge.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.chaaru.bookbridge.viewmodel.AuthViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

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
    var passwordVisible by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Dark Academia Palette
    val creamBackground = Color(0xFFF5F5DC)
    val burgundy = Color(0xFF800020)
    val charcoal = Color(0xFF333333)
    val gold = Color(0xFFD4AF37)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = creamBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Burgundy Header Block (30% secondary color)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                color = burgundy,
                shape = MaterialTheme.shapes.extraLarge.copy(
                    topStart = androidx.compose.foundation.shape.CornerSize(0.dp),
                    topEnd = androidx.compose.foundation.shape.CornerSize(0.dp)
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "BOOK BRIDGE",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp
                        ),
                        color = gold
                    )
                    Text(
                        if (isRegistering) "Create Your Ledger" else "Welcome Back, Scholar",
                        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Serif),
                        color = creamBackground.copy(alpha = 0.8f)
                    )
                }
            }

            // Input Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                if (isRegistering) {
                    VintageTextField(name, { name = it }, "Full Name", charcoal, burgundy)
                    Spacer(modifier = Modifier.height(8.dp))
                    VintageTextField(phone, { phone = it }, "Phone Number", charcoal, burgundy)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        FilterChip(
                            selected = role == "student",
                            onClick = { role = "student" },
                            label = { Text("Scholar", fontFamily = FontFamily.Serif) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = burgundy,
                                selectedLabelColor = gold,
                                containerColor = Color.Transparent,
                                labelColor = charcoal
                            )
                        )
                        FilterChip(
                            selected = role == "owner",
                            onClick = { role = "owner" },
                            label = { Text("Curator", fontFamily = FontFamily.Serif) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = burgundy,
                                selectedLabelColor = gold,
                                containerColor = Color.Transparent,
                                labelColor = charcoal
                            )
                        )
                    }

                    if (role == "owner") {
                        Spacer(modifier = Modifier.height(16.dp))
                        VintageTextField(storeName, { storeName = it }, "Boutique Name", charcoal, burgundy)
                        Spacer(modifier = Modifier.height(8.dp))
                        VintageTextField(location, { location = it }, "Location", charcoal, burgundy)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                VintageTextField(email, { email = it }, "Email Address", charcoal, burgundy)
                Spacer(modifier = Modifier.height(8.dp))
                
                VintageTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Secret Key",
                    primaryColor = charcoal,
                    secondaryColor = burgundy,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = null, tint = burgundy)
                        }
                    }
                )

                if (!isRegistering) {
                    TextButton(
                        onClick = {
                            if (email.isNotEmpty()) {
                                viewModel.resetPassword(email) {
                                    // Handle success (e.g., show a toast)
                                }
                            } else {
                                viewModel.error.value = "Enter email to reset password"
                            }
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            "Forgot Secret Key?",
                            color = burgundy,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Serif)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (viewModel.isLoading.value) {
                    CircularProgressIndicator(color = burgundy)
                } else {
                    // Gold Accent CTA (10% accent color)
                    Button(
                        onClick = {
                            if (isRegistering) {
                                viewModel.register(
                                    name = name,
                                    email = email,
                                    password = password,
                                    role = role,
                                    phone = phone,
                                    storeName = if (role == "owner") storeName else null,
                                    location = if (role == "owner") location else null
                                ) { roleStr ->
                                    onNavigate(roleStr)
                                }
                            } else {
                                viewModel.login(email, password) { roleStr ->
                                    onNavigate(roleStr)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = burgundy),
                        shape = MaterialTheme.shapes.medium,
                        border = androidx.compose.foundation.BorderStroke(1.dp, gold)
                    ) {
                        Text(
                            if (isRegistering) "Register Ledger" else "Authenticate", 
                            color = gold,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    TextButton(onClick = { isRegistering = !isRegistering }) {
                        Text(
                            if (isRegistering) "Already a member? Return to Archive" else "New Scholar? Create Account",
                            color = charcoal.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Serif)
                        )
                    }
                }

                viewModel.error.value?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

