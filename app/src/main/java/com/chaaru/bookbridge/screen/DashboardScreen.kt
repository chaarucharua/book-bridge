package com.chaaru.bookbridge.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Screen 7: Owner Dashboard ─────────────────────────────────
@Composable
fun DashboardScreen(
    onAddBook: () -> Unit = {},
    onManageBooks: () -> Unit = {},
    onReservations: () -> Unit = {},
    onProfile: () -> Unit = {}
) {
    LazyColumn(
        modifier       = Modifier.fillMaxSize().background(Burgundy),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {

        // ── Burgundy Header ──────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Burgundy)
                    .padding(horizontal = 16.dp)
                    .padding(top = 52.dp, bottom = 24.dp)
            ) {
                Column {
                    // Top row
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Welcome back,", fontSize = 12.sp, color = White.copy(0.75f))
                            Text("Campus Reads 📖",
                                fontSize   = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color      = White
                            )
                        }
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(21.dp))
                                .background(BurgundyLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("CR",
                                fontSize   = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color      = White
                            )
                        }
                    }

                    // Stats row
                    Row(
                        modifier              = Modifier.fillMaxWidth().padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DashStatCard("Books Listed", "43", "↑ +3 today", Modifier.weight(1f))
                        DashStatCard("Reservations","12", "↑ +2 new",  Modifier.weight(1f))
                        DashStatCard("Revenue", "₹8.4k","↑ this week", Modifier.weight(1f))
                    }
                }
            }
        }

        // ── Parchment rounded body ────────────────────────────
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Parchment)
                    .padding(14.dp)
            ) {
                Text("Quick Actions",
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Slate800,
                    modifier   = Modifier.padding(bottom = 10.dp))

                // 2×2 Quick actions
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickAction(
                        title    = "Add Book",
                        subtitle = "List a new book",
                        icon     = Icons.Default.Add,
                        iconBg   = BurgundyFaint,
                        iconTint = Burgundy,
                        onClick  = onAddBook,
                        modifier = Modifier.weight(1f)
                    )
                    QuickAction(
                        title    = "Manage Books",
                        subtitle = "Edit inventory",
                        icon     = Icons.AutoMirrored.Filled.LibraryBooks,
                        iconBg   = GreenBg,
                        iconTint = GreenText,
                        onClick  = onManageBooks,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickAction(
                        title    = "Reservations",
                        subtitle = "12 pending",
                        icon     = Icons.Default.Bookmark,
                        iconBg   = AmberBg,
                        iconTint = AmberText,
                        onClick  = onReservations,
                        modifier = Modifier.weight(1f)
                    )
                    QuickAction(
                        title    = "Profile",
                        subtitle = "Store settings",
                        icon     = Icons.Default.Person,
                        iconBg   = Color(0xFFF5F3FF),
                        iconTint = Color(0xFF7C3AED),
                        onClick  = onProfile,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(20.dp))
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text("Recently Added", fontSize = 14.sp,
                        fontWeight = FontWeight.Bold, color = Slate800
                    )
                    Text("View all", fontSize = 11.sp, color = Burgundy,
                        fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Recent books list
        items(sampleBooks) { book ->
            Box(
                Modifier.fillMaxWidth().background(Parchment)
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                RecentBookRow(book)
            }
        }
        item { Spacer(Modifier.height(20.dp).background(Parchment)) }
    }
}

// ── Dashboard Stat Card ────────────────────────────────────────
@Composable
fun DashStatCard(label: String, value: String, delta: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(White.copy(alpha = 0.18f))
            .padding(10.dp)
    ) {
        Column {
            Text(label, fontSize = 9.sp, color = White.copy(0.75f))
            Text(value,
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = White,
                modifier   = Modifier.padding(top = 2.dp))
            Text(delta, fontSize = 10.sp, color = Color(0xFF86EFAC))
        }
    }
}

// ── Recent Book Row ────────────────────────────────────────────
@Composable
fun RecentBookRow(book: BookUiModel) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier          = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BookThumb(emoji = book.emoji, width = 38, height = 48)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(book.title, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    color = Slate800, maxLines = 1)
                Text("${book.author} · ${book.condition}",
                    fontSize = 10.sp, color = Slate400
                )
            }
            Text("₹${book.price}",
                fontSize   = 13.sp,
                fontWeight = FontWeight.Bold,
                color      = Burgundy
            )
        }
    }
}