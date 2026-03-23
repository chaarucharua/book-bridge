package com.chaaru.bookbridge.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Screen 10: Reservation Requests ──────────────────────────
@Composable
fun ReservationRequestsScreen(onBack: () -> Unit = {}) {
    var selectedTab  by remember { mutableIntStateOf(0) }
    val requestState by remember { mutableStateOf(ownerRequestSamples.toMutableList()) }
    var requests     by remember { mutableStateOf(ownerRequestSamples) }

    val pendingCount = requests.count { it.status == "PENDING" }

    val filtered = when (selectedTab) {
        0    -> requests.filter { it.status == "PENDING" }
        1    -> requests.filter { it.status == "ACCEPTED" }
        else -> requests.filter { it.status == "REJECTED" }
    }

    Scaffold(
        topBar         = { BBTopBar("Reservation Requests", onBack = onBack) },
        containerColor = Parchment
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Parchment)
                .padding(padding)
        ) {

            // ── Tab row ───────────────────────────────────────
            Row(
                modifier = Modifier
                    .background(White)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Pending"  to pendingCount,
                    "Accepted" to 0,
                    "Rejected" to 0
                ).forEachIndexed { i, (label, count) ->
                    val isSelected = selectedTab == i
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Burgundy else Slate100)
                            .clickable { selectedTab = i }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                label,
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = if (isSelected) White else Slate600
                            )
                            if (count > 0) {
                                Spacer(Modifier.width(5.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Orange400)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("$count",
                                        fontSize   = 9.sp,
                                        color      = White,
                                        fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // ── Request cards list ─────────────────────────────
            LazyColumn(
                modifier       = Modifier.fillMaxSize().background(Parchment),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filtered.isEmpty()) item {
                    Box(Modifier.fillMaxWidth().padding(top = 60.dp), Alignment.Center) {
                        Text("No requests here", color = Slate400, fontSize = 14.sp)
                    }
                }
                items(filtered, key = { it.id }) { req ->
                    RequestCard(
                        request  = req,
                        onAccept = {
                            requests = requests.map {
                                if (it.id == req.id) it.copy(status = "ACCEPTED") else it
                            }
                        },
                        onReject = {
                            requests = requests.map {
                                if (it.id == req.id) it.copy(status = "REJECTED") else it
                            }
                        }
                    )
                }
            }
        }
    }
}

// ── Request Card ──────────────────────────────────────────────
@Composable
fun RequestCard(
    request: OwnerRequestUiModel,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(12.dp)) {

            // Top info row
            Row(verticalAlignment = Alignment.CenterVertically) {
                BookThumb(emoji = request.bookEmoji, width = 46, height = 58)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(request.bookTitle,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Slate800,
                        maxLines   = 2)
                    // Student avatar + name
                    Row(
                        modifier          = Modifier.padding(top = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(BurgundyFaint),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                request.studentName.take(2).uppercase(),
                                fontSize   = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color      = Burgundy
                            )
                        }
                        Spacer(Modifier.width(5.dp))
                        Text(request.studentName,
                            fontSize = 11.sp,
                            color    = Slate500
                        )
                    }
                    Text("📅 ${request.requestedDate}",
                        fontSize = 10.sp,
                        color    = Slate400,
                        modifier = Modifier.padding(top = 3.dp))
                }
                // Price tag
                Text("₹${request.price}",
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Burgundy
                )
            }

            // Action buttons — only for PENDING
            if (request.status == "PENDING") {
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Accept
                    Button(
                        onClick  = onAccept,
                        modifier = Modifier.weight(1f).height(38.dp),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = GreenText)
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Accept", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    // Reject
                    OutlinedButton(
                        onClick  = onReject,
                        modifier = Modifier.weight(1f).height(38.dp),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = Red400)
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Reject", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // Status badge if already actioned
                Row(Modifier.padding(top = 8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (request.status == "ACCEPTED") GreenBg else RedBg)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            request.status.lowercase().replaceFirstChar { it.uppercase() },
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color      = if (request.status == "ACCEPTED") GreenText else Red400
                        )
                    }
                }
            }
        }
    }
}

// ── UI Model + Sample Data ─────────────────────────────────────
data class OwnerRequestUiModel(
    val id: String,
    val bookTitle: String,
    val bookEmoji: String,
    val studentName: String,
    val requestedDate: String,
    val price: Int,
    val status: String
)

val ownerRequestSamples = listOf(
    OwnerRequestUiModel("1","Data Structures & Algorithms","📘","Student A","Mar 11, 10:24 AM",220,"PENDING"),
    OwnerRequestUiModel("2","Physics Vol II","📙","Student B","Mar 11, 9:15 AM",150,"PENDING"),
    OwnerRequestUiModel("3","Organic Chemistry","📗","Student C","Mar 10, 4:30 PM",185,"PENDING"),
)
