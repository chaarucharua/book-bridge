package com.chaaru.bookbridge.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Screen 6: Owner Login ─────────────────────────────────────
@Composable
fun OwnerLoginScreen(
    onLoginClick: () -> Unit = {},
    onSwitchToStudent: () -> Unit = {}
) {
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Burgundy)  // solid #722F37
    ) {
        // Decorative circle
        Box(
            Modifier.size(180.dp).offset(x = (-40).dp, y = (-40).dp)
                .clip(RoundedCornerShape(90.dp))
                .background(White.copy(alpha = 0.05f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp))

            // ── Owner Badge ───────────────────────────────────
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(Burgundy, BurgundyDark)))
                    .border(1.dp, White.copy(0.15f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) { Text("🏪", fontSize = 30.sp) }

            Spacer(Modifier.height(12.dp))

            Text(
                "BookBridge",
                fontSize      = 24.sp,
                fontWeight    = FontWeight.Bold,
                color         = White,
                letterSpacing = (-0.5).sp
            )

            // Role tag
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(White.copy(0.18f))
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            ) {
                Text(
                    "STORE OWNER PORTAL",
                    fontSize      = 10.sp,
                    fontWeight    = FontWeight.Bold,
                    color         = White,
                    letterSpacing = 1.sp
                )
            }

            Spacer(Modifier.height(28.dp))

            // ── Login Card ─────────────────────────────────────
            Card(
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape     = RoundedCornerShape(24.dp),
                colors    = CardDefaults.cardColors(containerColor = ParchmentLight),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Column(Modifier.padding(24.dp)) {
                    Text(
                        "Store Owner Sign In",
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Slate800
                    )
                    Text(
                        "Manage your bookstore with ease",
                        fontSize = 12.sp,
                        color    = Slate400,
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                    )

                    BBTextField(
                        value         = email,
                        onValueChange = { email = it },
                        label         = "Store Email",
                        leadingIcon   = Icons.Default.Store
                    )
                    Spacer(Modifier.height(12.dp))
                    BBTextField(
                        value         = password,
                        onValueChange = { password = it },
                        label         = "Password",
                        leadingIcon   = Icons.Default.Lock,
                        isPassword    = true
                    )

                    Text(
                        "Forgot password?",
                        fontSize   = 12.sp,
                        color      = Burgundy,
                        fontWeight = FontWeight.SemiBold,
                        textAlign  = TextAlign.End,
                        modifier   = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 16.dp)
                            .clickable { }
                    )

                    // Access Dashboard button — dark Slate background as per design
                    Button(
                        onClick   = onLoginClick,
                        modifier  = Modifier.fillMaxWidth().height(48.dp),
                        shape     = RoundedCornerShape(14.dp),
                        colors    = ButtonDefaults.buttonColors(
                            containerColor = Slate800,
                            contentColor   = White
                        ),
                        elevation = ButtonDefaults.buttonElevation(4.dp)
                    ) {
                        Text("Access Dashboard",
                            fontWeight = FontWeight.Bold,
                            fontSize   = 14.sp)
                    }

                    Row(
                        modifier              = Modifier.fillMaxWidth().padding(top = 14.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("New to BookBridge? ", fontSize = 12.sp, color = Slate500)
                        Text(
                            "Register Store",
                            fontSize   = 12.sp,
                            color      = Burgundy,
                            fontWeight = FontWeight.Bold,
                            modifier   = Modifier.clickable { }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Are you a student?", fontSize = 12.sp, color = White.copy(0.7f))
            Text(
                "Switch to Student Portal →",
                fontSize   = 13.sp,
                color      = White,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier
                    .padding(top = 4.dp, bottom = 50.dp)
                    .clickable(onClick = onSwitchToStudent)
            )
        }
    }
}