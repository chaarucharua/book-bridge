package com.chaaru.bookbridge.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaaru.bookbridge.data.model.Book
import com.chaaru.bookbridge.data.model.Booking
import com.chaaru.bookbridge.viewmodel.AuthViewModel
import com.chaaru.bookbridge.viewmodel.BooksViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentHomeScreen(
    booksViewModel: BooksViewModel,
    authViewModel: AuthViewModel,
    onNavigate: (String) -> Unit
) {
    val books by booksViewModel.filteredBooks.collectAsState()
    val recommended by booksViewModel.recommendedBooks.collectAsState()
    val bookings by booksViewModel.bookings.collectAsState()
    val searchQuery by booksViewModel.searchQuery.collectAsState()
    val user = authViewModel.profile.value

    LaunchedEffect(user?.uid) {
        booksViewModel.observeBooks()
        user?.uid?.let { booksViewModel.observeUserBookings(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            "Hi, ${user?.name ?: "Reader"}", 
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = FontFamily.Serif, 
                                fontWeight = FontWeight.Bold, 
                                color = Burgundy
                            )
                        )
                        Text(
                            "Find your next great read 📚", 
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate600,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        )
                    }
                },
                actions = {
                    Surface(
                        onClick = { onNavigate("chat") },
                        shape = CircleShape,
                        color = Burgundy.copy(alpha = 0.05f),
                        modifier = Modifier.padding(end = 8.dp).size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Chat, "AI Chat", tint = Burgundy, modifier = Modifier.size(20.dp))
                        }
                    }
                    Surface(
                        onClick = { onNavigate("profile") },
                        shape = CircleShape,
                        color = Burgundy.copy(alpha = 0.05f),
                        modifier = Modifier.padding(end = 16.dp).size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, "Profile", tint = Burgundy, modifier = Modifier.size(20.dp))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Parchment)
            )
        },
        containerColor = Parchment
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { booksViewModel.updateSearchQuery(it) },
                modifier = Modifier.padding(16.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                if (searchQuery.isEmpty()) {
                    // Bookings Section
                    if (bookings.isNotEmpty()) {
                        item {
                            SectionHeader("Your Active Bookings", onViewAll = { onNavigate("my_bookings") })
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(bookings.filter { it.status == "advance_paid" }.take(5)) { booking ->
                                    BookingPreviewCard(booking) { onNavigate("booking_detail/${booking.id}") }
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }

                    // Recommended Section
                    item {
                        SectionHeader("Curated for You")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(recommended) { book ->
                                RecommendedBookCard(book) { onNavigate("book_detail/${book.id}") }
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    item {
                        SectionHeader("Explore All Books")
                    }
                } else {
                    item {
                        SectionHeader("Search Results for \"$searchQuery\"")
                    }
                }

                // 2-Column Grid using items
                val columns = 2
                val rows = books.chunked(columns)
                items(rows) { rowBooks ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowBooks.forEach { book ->
                            Box(modifier = Modifier.weight(1f)) {
                                VerticalBookCard(book) { onNavigate("book_detail/${book.id}") }
                            }
                        }
                        if (rowBooks.size < columns) {
                            repeat(columns - rowBooks.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingPreviewCard(booking: Booking, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(70.dp).clip(RoundedCornerShape(12.dp))) {
                BookImage(imageUrl = booking.bookImageUrl, modifier = Modifier.fillMaxSize())
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    booking.bookTitle, 
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), 
                    maxLines = 1, 
                    overflow = TextOverflow.Ellipsis,
                    color = Slate900
                )
                Text(
                    "₹${booking.advancePaid.toInt()} Paid", 
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), 
                    color = GreenText
                )
                Spacer(modifier = Modifier.height(4.dp))
                StatusBadge(booking.status)
            }
        }
    }
}

@Composable
fun RecommendedBookCard(book: Book, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(170.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column {
            Box(modifier = Modifier.height(200.dp).fillMaxWidth()) {
                BookImage(book.imageUrl, Modifier.fillMaxSize())
                Surface(
                    color = Burgundy,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "₹${book.price.toInt()}", 
                        color = White, 
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), 
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    book.title, 
                    maxLines = 1, 
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), 
                    overflow = TextOverflow.Ellipsis,
                    color = Slate900
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = AmberText, modifier = Modifier.size(16.dp))
                    Text(
                        "${book.rating} (${book.reviewCount})", 
                        style = MaterialTheme.typography.bodySmall, 
                        modifier = Modifier.padding(start = 4.dp),
                        color = Slate600
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        book.condition, 
                        style = MaterialTheme.typography.labelSmall,
                        color = Burgundy.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun VerticalBookCard(book: Book, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Box(modifier = Modifier.height(180.dp).fillMaxWidth()) {
                BookImage(book.imageUrl, Modifier.fillMaxSize())
                BookStatusBadge(
                    book.status, 
                    Modifier.align(Alignment.BottomStart).padding(8.dp)
                )
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    book.title, 
                    maxLines = 1, 
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), 
                    overflow = TextOverflow.Ellipsis,
                    color = Slate900
                )
                Text(
                    book.author, 
                    maxLines = 1, 
                    style = MaterialTheme.typography.bodySmall, 
                    color = Slate600, 
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth(), 
                    horizontalArrangement = Arrangement.SpaceBetween, 
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "₹${book.price.toInt()}", 
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), 
                        color = Burgundy
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = AmberText, modifier = Modifier.size(14.dp))
                        Text(
                            "${book.rating} (${book.reviewCount})", 
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 4.dp),
                            color = Slate600
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun SectionHeader(title: String, onViewAll: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = Burgundy)
        )
        if (onViewAll != null) {
            TextButton(
                onClick = onViewAll,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    "View All",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = Burgundy,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = White,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search by title, author, or category...", color = Slate400) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Burgundy) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Slate500)
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Burgundy,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = White,
                unfocusedContainerColor = White
            ),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge
        )
    }
}
