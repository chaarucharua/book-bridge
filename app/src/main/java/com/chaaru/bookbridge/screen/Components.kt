package com.chaaru.bookbridge.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaaru.bookbridge.ui.theme.*

// ── Burgundy Primary Button ───────────────────────────────────
@Composable
fun BurgundyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    Button(
        onClick   = onClick,
        modifier  = modifier.height(48.dp),
        enabled   = enabled,
        shape     = RoundedCornerShape(14.dp),
        colors    = ButtonDefaults.buttonColors(
            containerColor         = Burgundy,
            contentColor           = White,
            disabledContainerColor = Burgundy.copy(alpha = 0.5f),
            disabledContentColor   = White.copy(alpha = 0.7f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 2.dp
        )
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 0.3.sp)
    }
}

// ── Google Button ─────────────────────────────────────────────
@Composable
fun GoogleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick   = onClick,
        modifier  = modifier.height(46.dp),
        shape     = RoundedCornerShape(12.dp),
        border    = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(Slate200)),
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
}

// ── Outlined Text Field matching UI design ────────────────────
@Composable
fun BBTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    isPassword: Boolean = false,
    singleLine: Boolean = true,
    placeholder: String = ""
) {
    var showPass by remember { mutableStateOf(false) }

    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label, fontSize = 12.sp) },
        modifier      = modifier.fillMaxWidth(),
        singleLine    = singleLine,
        textStyle     = LocalTextStyle.current.copy(fontSize = 14.sp, color = Slate800),
        placeholder   = if (placeholder.isNotEmpty()) {
            { Text(placeholder, fontSize = 14.sp, color = Slate400) }
        } else null,
        leadingIcon   = leadingIcon?.let {
            { Icon(it, null, tint = Slate400, modifier = Modifier.size(18.dp)) }
        },
        trailingIcon  = if (isPassword) {
            {
                IconButton(onClick = { showPass = !showPass }) {
                    Icon(
                        if (showPass) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        null, tint = Slate400, modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !showPass)
            PasswordVisualTransformation() else VisualTransformation.None,
        shape  = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor      = Burgundy,
            unfocusedBorderColor    = Slate200,
            focusedLabelColor       = Burgundy,
            unfocusedLabelColor     = Slate400,
            unfocusedContainerColor = ParchmentMid,
            focusedContainerColor   = ParchmentLight,
            cursorColor             = Burgundy
        )
    )
}

// ── White Card with rounded corners & shadow ──────────────────
@Composable
fun BBCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val base = modifier
        .shadow(elevation = 2.dp, shape = RoundedCornerShape(14.dp), clip = false)
        .clip(RoundedCornerShape(14.dp))
        .background(White)
    Card(
        modifier  = if (onClick != null) base.clickable(onClick = onClick) else base,
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        content   = content
    )
}

// ── Status Pill matching design ───────────────────────────────
@Composable
fun StatusPill(label: String, status: String) {
    val (bg, fg) = when (status.uppercase()) {
        "PENDING"   -> AmberBg to AmberText
        "ACCEPTED", "READY"   -> GreenBg to GreenText
        "DONE", "COLLECTED"   -> Slate100 to Slate500
        "REJECTED", "CANCELLED" -> RedBg to Red400
        else        -> Slate100 to Slate500
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = fg)
    }
}

// ── Mini Tag (condition / availability) ───────────────────────
@Composable
fun MiniTag(text: String, bg: Color, fg: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) { Text(text, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = fg) }
}

// ── Section Header ────────────────────────────────────────────
@Composable
fun SectionHeader(
    title: String,
    actionText: String = "See all",
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate800)
        if (onAction != null)
            Text(actionText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                color = Burgundy, modifier = Modifier.clickable(onClick = onAction))
    }
}

// ── Top App Bar ───────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BBTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Box(
                        modifier = Modifier.size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Slate100),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back",
                            tint = Slate700, modifier = Modifier.size(18.dp))
                    }
                }
            }
        },
        actions = actions,
        colors  = TopAppBarDefaults.topAppBarColors(containerColor = White)
    )
}

// ── Book Cover Thumbnail ──────────────────────────────────────
@Composable
fun BookThumb(emoji: String, width: Int = 46, height: Int = 58) {
    Box(
        modifier = Modifier
            .size(width = width.dp, height = height.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Brush.verticalGradient(listOf(ParchmentLight, ParchmentDark))),
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = (width * 0.5).sp)
        // Book spine shadow
        Box(
            Modifier.align(Alignment.CenterStart)
                .fillMaxHeight().width(4.dp)
                .background(Black12)
        )
    }
}

// ── Notification Badge dot ────────────────────────────────────
@Composable
fun UnreadDot(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(Burgundy)
    )
}

// ── Loading ───────────────────────────────────────────────────
@Composable
fun BBLoading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Burgundy, strokeWidth = 3.dp)
    }
}

// ── Quick Action Card (Dashboard) ────────────────────────────
@Composable
fun QuickAction(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BBCard(modifier = modifier, onClick = onClick) {
        Column(Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(iconBg),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp)) }
            Spacer(Modifier.height(8.dp))
            Text(title,    fontSize = 13.sp, fontWeight = FontWeight.Bold,  color = Slate800)
            Text(subtitle, fontSize = 10.sp, color = Slate400, modifier = Modifier.padding(top = 2.dp))
        }
    }
}
