package com.chaaru.bookbridge.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Screen 1: Student Login ───────────────────────────────────
@Composable
fun StudentLoginScreen(
    onLoginClick: () -> Unit = {},
    onSwitchToOwner: () -> Unit = {}
) {
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Burgundy)   // solid #722F37 background
    ) {
        // Subtle decorative circles
        Box(
            Modifier.size(220.dp).offset(x = 160.dp, y = (-60).dp)
                .clip(RoundedCornerShape(110.dp))
                .background(White.copy(alpha = 0.06f))
        )
        Box(
            Modifier.size(150.dp).offset(x = (-40).dp, y = 400.dp)
                .clip(RoundedCornerShape(75.dp))
                .background(White.copy(alpha = 0.04f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(72.dp))

            // ── Logo Icon ─────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(White.copy(alpha = 0.15f))
                    .border(1.dp, White.copy(alpha = 0.2f), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) { Text("📚", fontSize = 28.sp) }

            Spacer(Modifier.height(12.dp))

            Text(
                text       = "BookBridge",
                fontSize   = 26.sp,
                fontWeight = FontWeight.Bold,
                color      = White,
                letterSpacing = (-0.5).sp
            )
            Text(
                text     = "Find affordable books near you",
                fontSize = 13.sp,
                color    = White.copy(alpha = 0.70f),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(28.dp))

            // ── Login Card ─────────────────────────────────────
            Card(
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape     = RoundedCornerShape(24.dp),
                colors    = CardDefaults.cardColors(containerColor = ParchmentLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(Modifier.padding(24.dp)) {

                    Text(
                        "Welcome back 👋",
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Slate800
                    )
                    Text(
                        "Sign in to your student account",
                        fontSize = 12.sp,
                        color    = Slate400,
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                    )

                    // Email
                    BBTextField(
                        value         = email,
                        onValueChange = { email = it },
                        label         = "Email Address",
                        leadingIcon   = Icons.Default.Email
                    )
                    Spacer(Modifier.height(12.dp))

                    // Password
                    BBTextField(
                        value         = password,
                        onValueChange = { password = it },
                        label         = "Password",
                        leadingIcon   = Icons.Default.Lock,
                        isPassword    = true
                    )

                    // Forgot password
                    Text(
                        text       = "Forgot password?",
                        fontSize   = 12.sp,
                        color      = Burgundy,
                        fontWeight = FontWeight.SemiBold,
                        textAlign  = TextAlign.End,
                        modifier   = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 16.dp)
                            .clickable { }
                    )

                    // Sign In button
                    BurgundyButton(
                        text     = "Sign In",
                        onClick  = onLoginClick,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Divider
                    Row(
                        Modifier.padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(Modifier.weight(1f), color = Slate200)
                        Text(
                            "  or continue with  ",
                            fontSize = 11.sp,
                            color    = Slate400
                        )
                        HorizontalDivider(Modifier.weight(1f), color = Slate200)
                    }

                    // Google button
                    OutlinedButton(
                        onClick   = { },
                        modifier  = Modifier.fillMaxWidth().height(46.dp),
                        shape     = RoundedCornerShape(12.dp),
                        border    = ButtonDefaults.outlinedButtonBorder,
                        colors    = ButtonDefaults.outlinedButtonColors(contentColor = Slate700)
                    ) {
                        Text(
                            "G",
                            fontSize   = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color      = Color(0xFF4285F4)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Continue with Google",
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = Slate700
                        )
                    }

                    // Sign up link
                    Row(
                        modifier              = Modifier.fillMaxWidth().padding(top = 14.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Don't have an account? ", fontSize = 12.sp, color = Slate500)
                        Text(
                            "Sign up",
                            fontSize   = 12.sp,
                            color      = Burgundy,
                            fontWeight = FontWeight.Bold,
                            modifier   = Modifier.clickable { }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Are you a shop owner?", fontSize = 12.sp, color = White.copy(0.7f))
            Text(
                "Switch to Owner Portal →",
                fontSize   = 13.sp,
                color      = White,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier
                    .padding(top = 4.dp, bottom = 50.dp)
                    .clickable(onClick = onSwitchToOwner)
            )
        }
    }
}