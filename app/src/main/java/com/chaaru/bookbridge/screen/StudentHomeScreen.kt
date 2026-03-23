package com.chaaru.bookbridge.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Screen 2: Student Home ────────────────────────────────────
@Composable
fun StudentHomeScreen(
    onBookClick: (String) -> Unit = {},
    onNotifClick: () -> Unit = {},
    onReservationsClick: () -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf("All") }

    Scaffold(
        bottomBar = {
            StudentBottomBar(
                currentRoute    = "home",
                onHome          = {},
                onSearch        = {},
                onReservations  = onReservationsClick,
                onNotifications = onNotifClick
            )
        },
        containerColor = Parchment
    ) { padding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {

            // ── Burgundy Header ──────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Burgundy, BurgundyDark)))
                        .padding(horizontal = 18.dp)
                        .padding(top = 48.dp, bottom = 52.dp)
                ) {
                    Column {
                        // Top row
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Good morning 👋",
                                    fontSize = 12.sp,
                                    color    = White.copy(0.7f))
                                Text("Find your next book",
                                    fontSize   = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = White
                                )
                            }
                            // Notification bell
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(White.copy(0.15f))
                                    .clickable(onClick = onNotifClick),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Notifications, null,
                                    tint = White, modifier = Modifier.size(18.dp))
                                // Orange dot
                                Box(
                                    Modifier
                                        .size(7.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Orange400)
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-6).dp, y = 6.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Search bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(White.copy(0.15f))
                                .padding(horizontal = 14.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Search, null,
                                tint = White.copy(0.7f), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Search books, authors, stores...",
                                fontSize = 13.sp, color = White.copy(0.6f))
                        }
                    }
                }
            }

            // ── White rounded card body ──────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-24).dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(Parchment)
                        .padding(top = 18.dp)
                ) {
                    // Categories header
                    Row(
                        modifier              = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text("Categories", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate800)
                        Text("See all", fontSize = 11.sp, color = Burgundy, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }

            // Categories chips
            item {
                LazyRow(
                    modifier       = Modifier.background(Parchment),
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(bookCategories) { cat ->
                        CategoryChip(
                            emoji      = cat.second,
                            label      = cat.first,
                            isSelected = selectedCategory == cat.first,
                            onClick    = { selectedCategory = cat.first }
                        )
                    }
                }
            }

            // Recommended header
            item {
                Column(
                    Modifier.fillMaxWidth().background(Parchment)
                        .padding(horizontal = 14.dp)
                        .padding(top = 16.dp)
                ) {
                    SectionHeader("Recommended for You", onAction = {})
                }
            }

            // Recommended books row
            item {
                LazyRow(
                    modifier       = Modifier.background(Parchment),
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(sampleBooks) { book ->
                        HomeBookCard(book = book, onClick = { onBookClick(book.id) })
                    }
                }
            }

            // Nearby Stores header
            item {
                Column(
                    Modifier.fillMaxWidth().background(Parchment)
                        .padding(horizontal = 14.dp)
                        .padding(top = 16.dp)
                ) {
                    SectionHeader("Nearby Bookstores", onAction = {})
                }
            }

            // Store cards
            items(sampleStores) { store ->
                Box(
                    Modifier.fillMaxWidth().background(Parchment)
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    StoreListCard(store = store)
                }
            }
            item { Spacer(Modifier.height(20.dp).background(Parchment)) }
        }
    }
}

// ── Category Chip ──────────────────────────────────────────────
@Composable
fun CategoryChip(emoji: String, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) BurgundyFaint else White)
            .border(
                width = 1.5.dp,
                color = if (isSelected) Burgundy.copy(0.3f) else White,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) BurgundyFaint else Parchment),
            contentAlignment = Alignment.Center
        ) { Text(emoji, fontSize = 16.sp) }
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            fontSize   = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color      = if (isSelected) Burgundy else Slate600
        )
    }
}

// ── Home Book Card ─────────────────────────────────────────────
@Composable
fun HomeBookCard(book: BookUiModel, onClick: () -> Unit) {
    Card(
        modifier  = Modifier.width(120.dp).clickable(onClick = onClick),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(10.dp)) {
            BookThumb(emoji = book.emoji, width = 100, height = 118)
            Spacer(Modifier.height(8.dp))
            Text(book.title, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                maxLines = 2, overflow = TextOverflow.Ellipsis,
                color = Slate800, lineHeight = 15.sp)
            Text(book.author, fontSize = 10.sp, color = Slate400,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp))
            Spacer(Modifier.height(5.dp))
            Row(Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically) {
                Text("₹${book.price}", fontSize = 13.sp,
                    fontWeight = FontWeight.Bold, color = Burgundy
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⭐", fontSize = 9.sp)
                    Text(book.rating, fontSize = 10.sp, color = Slate500,
                        modifier = Modifier.padding(start = 2.dp))
                }
            }
        }
    }
}

// ── Store Card ─────────────────────────────────────────────────
@Composable
fun StoreListCard(store: StoreUiModel) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            // Store icon
            Box(
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(10.dp))
                    .background(BurgundyFaint),
                contentAlignment = Alignment.Center
            ) { Text("📖", fontSize = 20.sp) }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(store.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate800)
                Text("📍 ${store.distance} · ${store.bookCount} books",
                    fontSize = 11.sp, color = Slate400, modifier = Modifier.padding(top = 2.dp))
            }
            // Status badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (store.isOpen) GreenBg else AmberBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    if (store.isOpen) "Open" else "Closed",
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color      = if (store.isOpen) GreenText else AmberText
                )
            }
        }
    }
}

// ── Bottom Navigation ──────────────────────────────────────────
@Composable
fun StudentBottomBar(
    currentRoute: String,
    onHome: () -> Unit,
    onSearch: () -> Unit,
    onReservations: () -> Unit,
    onNotifications: () -> Unit
) {
    NavigationBar(containerColor = White, tonalElevation = 4.dp) {
        listOf(
            Triple("home",          "Home",     Icons.Default.Home),
            Triple("search",        "Search",   Icons.Default.Search),
            Triple("reservations",  "Reserves", Icons.Default.Bookmark),
            Triple("notifications", "Alerts",   Icons.Default.Notifications),
        ).forEach { (route, label, icon) ->
            NavigationBarItem(
                selected  = currentRoute == route,
                onClick   = when (route) {
                    "home"         -> onHome
                    "search"       -> onSearch
                    "reservations" -> onReservations
                    else           -> onNotifications
                },
                icon      = { Icon(icon, label, modifier = Modifier.size(22.dp)) },
                label     = { Text(label, fontSize = 10.sp) },
                colors    = NavigationBarItemDefaults.colors(
                    indicatorColor      = BurgundyFaint,
                    selectedIconColor   = Burgundy,
                    selectedTextColor   = Burgundy,
                    unselectedIconColor = Slate400,
                    unselectedTextColor = Slate400
                )
            )
        }
    }
}

// ── UI Models ─────────────────────────────────────────────────
data class BookUiModel(
    val id: String, val title: String, val author: String,
    val emoji: String, val price: Int, val rating: String,
    val condition: String = "Good", val originalPrice: Int = 0,
    val category: String = "", val storeName: String = "", val storeDistance: String = ""
)
data class StoreUiModel(val name: String, val distance: String, val bookCount: Int, val isOpen: Boolean)

val bookCategories = listOf(
    "All" to "📚", "Science" to "🔬", "Math" to "📐",
    "CS" to "🖥️", "History" to "🏛️", "Arts" to "🎨"
)

val sampleBooks = listOf(
    BookUiModel("1","Data Structures & Algorithms","T. Cormen","📘",220,"4.7","Good",650,"CS","Campus Reads","0.3 km"),
    BookUiModel("2","Organic Chemistry","P. Morrison","📗",185,"4.2","Like New",500,"Science","Campus Reads","0.3 km"),
    BookUiModel("3","Physics Vol II","H.C. Verma","📙",150,"4.5","Fair",450,"Science","Scholar's","0.7 km"),
)
val sampleStores = listOf(
    StoreUiModel("Campus Reads","0.3 km",43,true),
    StoreUiModel("Scholar's Corner","0.7 km",28,true),
)