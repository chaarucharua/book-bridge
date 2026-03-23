package com.chaaru.bookbridge.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Screen 4: My Reservations ─────────────────────────────────
@Composable
fun ReservationsScreen(onBack: () -> Unit = {}) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Active", "History", "Cancelled")

    val filtered = when (selectedTab) {
        0    -> reservationSamples.filter { it.status == "ACCEPTED" || it.status == "PENDING" }
        1    -> reservationSamples.filter { it.status == "DONE" }
        else -> reservationSamples.filter { it.status == "CANCELLED" }
    }

    Scaffold(
        topBar = {
            Column(Modifier.background(White)) {
                BBTopBar(title = "My Reservations", onBack = onBack)
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor   = White,
                    contentColor     = Burgundy,
                    indicator        = { positions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(positions[selectedTab]),
                            color    = Burgundy
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected             = selectedTab == index,
                            onClick              = { selectedTab = index },
                            text                 = { Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
                            selectedContentColor = Burgundy,
                            unselectedContentColor = Slate400
                        )
                    }
                }
            }
        },
        containerColor = Parchment
    ) { padding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().background(Parchment).padding(padding),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filtered.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(top = 80.dp), Alignment.Center) {
                        Text("No reservations here", color = Slate400, fontSize = 14.sp)
                    }
                }
            }
            items(filtered) { res -> ReservationCard(res) }
        }
    }
}

// ── Reservation Card ──────────────────────────────────────────
@Composable
fun ReservationCard(res: ReservationUiModel) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier          = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Book thumbnail
            BookThumb(emoji = res.emoji, width = 50, height = 64)

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(res.title,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Slate800,
                    maxLines   = 2)
                Text("📖 ${res.storeName}",
                    fontSize = 11.sp,
                    color    = Slate400,
                    modifier = Modifier.padding(top = 2.dp))
                Text("📅 ${res.date}",
                    fontSize = 10.sp,
                    color    = Slate400,
                    modifier = Modifier.padding(top = 4.dp))
            }

            Column(horizontalAlignment = Alignment.End) {
                StatusPill(label = res.statusLabel, status = res.status)
                Spacer(Modifier.height(6.dp))
                Text("₹${res.price}",
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Burgundy
                )
            }
        }
    }
}

// ── UI Model + Sample Data ─────────────────────────────────────
data class ReservationUiModel(
    val title: String, val emoji: String, val storeName: String,
    val date: String, val price: Int, val status: String, val statusLabel: String
)

val reservationSamples = listOf(
    ReservationUiModel("Data Structures & Algorithms","📘","Campus Reads",
        "Reserved: Mar 11, 2026",220,"ACCEPTED","Ready"),
    ReservationUiModel("Physics Vol II","📙","Scholar's Corner",
        "Reserved: Mar 10, 2026",150,"PENDING","Pending"),
    ReservationUiModel("Organic Chemistry","📗","Campus Reads",
        "Collected: Mar 5, 2026",185,"DONE","Done"),
    ReservationUiModel("Engineering Maths II","📕","Scholar's Corner",
        "Collected: Feb 28, 2026",130,"DONE","Done"),
)
