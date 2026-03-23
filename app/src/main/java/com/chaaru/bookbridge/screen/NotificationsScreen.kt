package com.chaaru.bookbridge.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Screen 5: Notifications ───────────────────────────────────
@Composable
fun NotificationsScreen(onBack: () -> Unit = {}) {
    Scaffold(
        topBar = {
            BBTopBar(
                title  = "Notifications",
                onBack = onBack,
                actions = {
                    TextButton(onClick = {}) {
                        Text("Mark all read",
                            fontSize   = 12.sp,
                            color      = Burgundy,
                            fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        },
        containerColor = Parchment
    ) { padding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().background(Parchment).padding(padding),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(notifSamples) { n -> NotifCard(n) }
        }
    }
}

// ── Notification Card ──────────────────────────────────────────
@Composable
fun NotifCard(n: NotifUiModel) {
    val cardMod = if (!n.isRead)
        Modifier.fillMaxWidth().border(2.dp, Burgundy, RoundedCornerShape(14.dp))
    else
        Modifier.fillMaxWidth()

    Card(
        modifier  = cardMod,
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier          = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(n.iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(n.icon, null, tint = n.iconTint, modifier = Modifier.size(18.dp))
            }

            Spacer(Modifier.width(10.dp))

            Column(Modifier.weight(1f)) {
                Text(n.title,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Slate800
                )
                Text(n.message,
                    fontSize = 11.sp,
                    color    = Slate500,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 3.dp))
                Text(n.time,
                    fontSize = 10.sp,
                    color    = Slate400,
                    modifier = Modifier.padding(top = 4.dp))
            }

            // Unread dot
            if (!n.isRead) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Burgundy)
                )
            }
        }
    }
}

// ── UI Model + Sample Data ─────────────────────────────────────
data class NotifUiModel(
    val title: String, val message: String, val time: String,
    val icon: ImageVector, val iconBg: Color, val iconTint: Color,
    val isRead: Boolean
)

val notifSamples = listOf(
    NotifUiModel(
        "Book Now Available! 📗",
        "Organic Chemistry by P. Morrison is now available at Campus Reads.",
        "Just now",
        Icons.AutoMirrored.Filled.MenuBook, GreenBg, GreenText, false
    ),
    NotifUiModel(
        "Reservation Confirmed ✅",
        "Your reservation for Data Structures is confirmed. Pickup by Mar 14.",
        "2 hours ago",
        Icons.Default.Bookmark, BurgundyFaint, Burgundy, false
    ),
    NotifUiModel(
        "Flash Sale — 30% Off 🎉",
        "Scholar's Corner has a flash sale on all Science books today only!",
        "Yesterday",
        Icons.Default.LocalOffer, AmberBg, AmberText, true
    ),
    NotifUiModel(
        "Wishlist Alert 📘",
        "Physics Vol II from your wishlist is now listed at ₹150.",
        "Mar 10",
        Icons.AutoMirrored.Filled.MenuBook, GreenBg, GreenText, true
    ),
    NotifUiModel(
        "Pickup Reminder ⏰",
        "Don't forget to collect Engineering Maths II by tomorrow.",
        "Mar 9",
        Icons.Default.Notifications, Slate100, Slate600, true
    ),
)
